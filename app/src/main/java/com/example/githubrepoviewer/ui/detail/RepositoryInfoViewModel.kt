package com.example.githubrepoviewer.ui.detail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.githubrepoviewer.data.AppRepository
import com.example.githubrepoviewer.data.model.RepoDetails
import com.example.githubrepoviewer.ui.providers.ResourcesProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.net.UnknownHostException
import javax.inject.Inject

@HiltViewModel
class RepositoryInfoViewModel @Inject constructor(
    private val resources: ResourcesProvider,
    private val appRepository: AppRepository,
) : ViewModel() {

    private val _state = MutableLiveData<State>(State.Loading)
    val state: LiveData<State> = _state

    private var loadRepositoryJob: Job? = null
    private var loadRepositoryReadmeJob: Job? = null

    private lateinit var repoDetails: RepoDetails

    fun loadRepository(repoId: String) {
        if (loadRepositoryJob != null) return
        loadRepositoryJob = viewModelScope.launch(Dispatchers.IO) {
            loadRepositoryReadmeJob?.join()
            _state.postValue(State.Loading)
            runCatching {
                appRepository.getRepository(repoId)
            }.onSuccess {
                _state.postValue(State.Loaded(it, ReadmeState.Loading))
                repoDetails = it
                loadRepositoryReadme()
            }.onFailure { e ->
                val newState = when (e) {
                    is UnknownHostException -> {
                        State.Error("UnknownHostException")
                    }

                    else -> State.Error("${e.message}")
                }
                _state.postValue(newState)
            }
            loadRepositoryJob = null
        }
    }

    fun loadRepositoryReadme() {
        if (loadRepositoryReadmeJob != null) return
        loadRepositoryReadmeJob = viewModelScope.launch(Dispatchers.IO) {
            loadRepositoryJob?.join()
            while (_state.value !is State.Loaded) delay(0)
            val state = _state.value as State.Loaded
            if (state.readmeState !is ReadmeState.Loading)
                _state.postValue(State.Loaded(state.githubRepo, ReadmeState.Loading))
            runCatching {
                appRepository.getRepositoryReadme(repoDetails.owner.login, repoDetails.name)
            }.onSuccess {
                val readmeState = if (it.isEmpty()) ReadmeState.Empty
                else ReadmeState.Loaded(it)
                _state.postValue(State.Loaded(state.githubRepo, readmeState))
            }.onFailure { e ->
                val readmeState = when (e) {
                    is UnknownHostException -> {
                        ReadmeState.Error("UnknownHostException")
                    }

                    else -> ReadmeState.Error("${e.message}")

                }
                _state.postValue(State.Loaded(state.githubRepo, readmeState))
            }
            loadRepositoryReadmeJob = null
        }
    }

    fun cancelJob() {
        loadRepositoryJob?.cancel()
        loadRepositoryReadmeJob?.cancel()
    }

    sealed interface State {
        object Loading : State
        data class Error(val error: String) : State

        data class Loaded(
            val githubRepo: RepoDetails,
            val readmeState: ReadmeState
        ) : State
    }

    sealed interface ReadmeState {
        object Loading : ReadmeState
        object Empty : ReadmeState
        data class Error(val error: String) : ReadmeState
        data class Loaded(val markdown: String) : ReadmeState
    }
}
