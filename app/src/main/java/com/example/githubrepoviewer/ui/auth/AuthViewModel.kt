package com.example.githubrepoviewer.ui.auth

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.githubrepoviewer.R
import com.example.githubrepoviewer.data.AppRepository
import com.example.githubrepoviewer.data.KeyValueStorage
import com.example.githubrepoviewer.data.remote.getErrorMessage
import com.example.githubrepoviewer.di.AuthInterceptor
import com.example.githubrepoviewer.ui.providers.ResourcesProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import retrofit2.HttpException
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val keyValueStorage: KeyValueStorage,
    private val resources: ResourcesProvider,
    private val authInterceptor: AuthInterceptor,
    private val appRepository: AppRepository,
) : ViewModel() {

    val token: MutableLiveData<String> = MutableLiveData("")

    private val _state = MutableLiveData<State>(State.Idle)
    val state: LiveData<State> = _state

    private var _actions = MutableSharedFlow<Action>()
    val actions: Flow<Action> = _actions.asSharedFlow()

    private val tokenPattern = Regex("^[A-Za-z0-9_]{0,255}$")
    private var tokenChangeJob: Job? = null
    private var signInJob: Job? = null

    fun init() {
        token.value = keyValueStorage.authToken ?: ""
    }

    fun logout() {
        token.value = ""
        keyValueStorage.authToken = null
        authInterceptor.setAuthToken(null)
    }

    fun onSignButtonPressed() {
        if (signInJob != null) return
        val previousState = _state.value
        _state.postValue(State.Loading)
        val token = token.value ?: ""
        signInJob = viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                authInterceptor.setAuthToken("Bearer $token")
                appRepository.signIn()
            }.onSuccess {
                keyValueStorage.authToken = token
                _actions.emit(Action.RouteToMain)
            }.onFailure { e ->
                when (e) {
                    is HttpException -> {
                        if (e.code() == 401) {
                            val errorMessage = e.getErrorMessage()
                            Log.d("eh", "${e.message}: $errorMessage")
                            _state.postValue(State.InvalidInput("$errorMessage"))
                            _actions.emit(
                                Action.ShowError(
                                    "$errorMessage / ${e.message} \n" + resources.getString(R.string.information_for_developers)
                                )
                            )
                        } else {
                            _state.postValue(previousState)
                        }
                    }

                    else -> {
                        _state.postValue(previousState)
                    }
                }
            }
            signInJob = null
        }
    }

    sealed interface State {
        data object Idle : State
        data object Loading : State
        data class InvalidInput(val reason: String) : State
        data class TokenInput(val isTooShort: Boolean) : State
    }

    sealed interface Action {
        data class ShowError(val message: String) : Action
        object RouteToMain : Action
    }

    private data class CachedInput(
        var token: String = "",
        var hasFocus: Boolean = false,
        val mutex: Mutex = Mutex(),
    )

    private val cachedInput = CachedInput()

    fun onTokenChanged(token: String, hasFocus: Boolean = true) {
        this.token.value = token
        viewModelScope.launch {
            cachedInput.mutex.withLock {
                cachedInput.token = token
                cachedInput.hasFocus = hasFocus
            }
            if (tokenChangeJob == null)
                performValidation(token, hasFocus)
        }
    }

    private fun performValidation(token: String, hasFocus: Boolean = true) {
        tokenChangeJob = viewModelScope.launch {
            _state.value = when (validateToken(token)) {
                ValidationResult.VALID -> State.TokenInput(isTooShort = false)

                ValidationResult.INVALID_FORMAT -> State.InvalidInput(resources.getString(R.string.invalid_format))

                ValidationResult.EMPTY -> State.Idle

                ValidationResult.TOO_SHORT -> if (hasFocus) State.TokenInput(isTooShort = true)
                else State.InvalidInput(resources.getString(R.string.too_short))

                ValidationResult.TOO_LONG -> State.InvalidInput(resources.getString(R.string.too_long))
            }
            delay(VALIDATION_DELAY_MILLIS)
        }
        viewModelScope.launch {
            val tokenChangeJobLocal = tokenChangeJob
            tokenChangeJobLocal?.join()
            cachedInput.mutex.withLock {
                if (cachedInput.token != token || cachedInput.hasFocus != hasFocus) {
                    performValidation(cachedInput.token, cachedInput.hasFocus)
                } else tokenChangeJob = null
            }
        }
    }

    private fun validateToken(token: String): ValidationResult {
        return when {
            token.isEmpty() -> ValidationResult.EMPTY
            token.length > 255 -> ValidationResult.TOO_LONG
            !tokenPattern.matches(token) -> ValidationResult.INVALID_FORMAT
            token.length < 40 -> ValidationResult.TOO_SHORT
            else -> ValidationResult.VALID
        }
    }

    enum class ValidationResult {
        VALID,
        INVALID_FORMAT,
        EMPTY,
        TOO_SHORT,
        TOO_LONG,
    }

    private companion object {
        const val VALIDATION_DELAY_MILLIS = 250L
    }
}
