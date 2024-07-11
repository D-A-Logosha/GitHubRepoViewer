package com.example.githubrepoviewer.ui.detail

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.githubrepoviewer.data.AppRepository
import com.example.githubrepoviewer.data.model.RepoDetails
import com.example.githubrepoviewer.ui.providers.ResourcesProvider
import com.example.githubrepoviewer.ui.repositories.RepositoriesListViewModel.State
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RepositoryInfoViewModel @Inject constructor(
    private val resources: ResourcesProvider,
    private val appRepository: AppRepository,
) : ViewModel() {

    private val _state = MutableLiveData<State>(State.Loading)
    val state: LiveData<State> = _state

    private var loadRepositoryJob: Job? = null

    fun loadRepository(repoId: String) {
        if (loadRepositoryJob != null) return
        _state.value = State.Loading
        loadRepositoryJob = viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                val repoDetails = appRepository.getRepository(repoId)
                Log.d("RepositoryInfoViewModel", "$repoDetails")
            }.onFailure { e ->
                Log.d("RepositoryInfoViewModel", "${e.message}")
            }
            loadRepositoryJob = null
        }
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
