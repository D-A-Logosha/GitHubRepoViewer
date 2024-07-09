package com.example.githubrepoviewer.ui.repositories

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.githubrepoviewer.data.AppRepository
import com.example.githubrepoviewer.data.model.Repo
import com.example.githubrepoviewer.ui.providers.ResourcesProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class RepositoriesListViewModel @Inject constructor(
    private val resources: ResourcesProvider,
    private val appRepository: AppRepository,
) : ViewModel() {
    private val _state: MutableLiveData<State> by lazy { MutableLiveData<State>(State.Loading) }
    val state: LiveData<State> = _state


    sealed interface State {
        object Loading : State
        data class Loaded(val repos: List<Repo>) : State
        data class Error(val error: String) : State
        object Empty : State
    }
}
