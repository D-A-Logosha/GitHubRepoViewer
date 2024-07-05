package com.example.githubrepoviewer.ui.auth

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.githubrepoviewer.data.KeyValueStorage
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
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val keyValueStorage: KeyValueStorage
) : ViewModel() {

    val token: MutableLiveData<String> = MutableLiveData("")

    private var _state = MutableLiveData<State>(State.Idle)
    val state: LiveData<State> = _state

    private var _actions = MutableSharedFlow<Action>()
    val actions: Flow<Action> = _actions.asSharedFlow()

    private val tokenPattern = Regex("^[A-Za-z0-9_]{0,255}$")
    private var tokenChangeJob: Job? = null

    init {
        token.value = keyValueStorage.authToken
    }

    private var i = 0
    fun onSignButtonPressed() {
        viewModelScope.launch(Dispatchers.IO) {
            Log.d("d", "${i++}")
            keyValueStorage.authToken = token.value
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

                ValidationResult.INVALID_FORMAT -> State.InvalidInput("Invalid format")

                ValidationResult.EMPTY -> State.Idle

                ValidationResult.TOO_SHORT -> if (hasFocus) State.TokenInput(isTooShort = true)
                else State.InvalidInput("Too short")

                ValidationResult.TOO_LONG -> State.InvalidInput("Too long")
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
