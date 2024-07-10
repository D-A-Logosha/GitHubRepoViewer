package com.example.githubrepoviewer.ui.repositories

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.githubrepoviewer.data.AppRepository
import com.example.githubrepoviewer.data.model.Repo
import com.example.githubrepoviewer.ui.providers.ResourcesProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.net.UnknownHostException
import javax.inject.Inject

@HiltViewModel
class RepositoriesListViewModel @Inject constructor(
    private val resources: ResourcesProvider,
    private val appRepository: AppRepository,
) : ViewModel() {
    private val _state = MutableLiveData<State>(State.Loading)
    val state: LiveData<State> = _state

    private var loadRepositoriesJob: Job? = null

    fun loadRepositories() {
        if (loadRepositoriesJob != null) return
        _state.value = State.Loading
        loadRepositoriesJob = viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                val repos = appRepository.getRepositories()
                if (repos.isEmpty()) State.Empty
                else State.Loaded(repos)
            }.onSuccess {
                _state.postValue(it)
            }.onFailure { e ->
                val newState = when (e) {
                    is UnknownHostException -> {
                        State.Error("UnknownHostException")
                    }

                    else -> State.Error("${e.message}")
                }
                _state.postValue(newState)
            }
            loadRepositoriesJob = null
        }
    }

    sealed interface State {
        object Loading : State
        data class Loaded(val repos: List<Repo>) : State
        data class Error(val error: String) : State
        object Empty : State
    }
}
