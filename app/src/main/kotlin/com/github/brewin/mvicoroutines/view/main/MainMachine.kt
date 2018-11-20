package com.github.brewin.mvicoroutines.view.main

import com.github.brewin.mvicoroutines.data.Repository
import com.github.brewin.mvicoroutines.model.RepoItem
import com.github.brewin.mvicoroutines.view.base.*
import kotlinx.android.parcel.Parcelize
import timber.log.Timber

@Parcelize
data class MainState(
    val query: String = "",
    val repoList: List<RepoItem> = emptyList()
) : State

sealed class MainStateStatus(state: MainState) : StateStatus<MainState>(state) {
    class Initial(state: MainState = MainState()) : MainStateStatus(state)
    class Loading(state: MainState, val isLoading: Boolean) : MainStateStatus(state)
    class Results(state: MainState) : MainStateStatus(state)
    class Error(state: MainState, val exception: Exception) : MainStateStatus(state)
}

class MainMachine(
    initialStateStatus: MainStateStatus,
    private val repository: Repository
) : StateMachine<MainStateStatus, MainState>(initialStateStatus) {

    fun search(query: String) {
        SearchReposTask(repository, query).start(this)
    }

    fun refresh() {
        withState {
            search(query)
        }
    }
}

class SearchReposTask(
    private val repository: Repository,
    private val query: String
) : Task<MainState, List<RepoItem>, MainStateStatus>() {

    override suspend fun create() = repository.searchRepos(query)

    override fun onStatus(state: MainState, status: TaskStatus<List<RepoItem>>) = when (status) {
        is TaskStatus.Started -> MainStateStatus.Loading(
            state = state.copy(query = query),
            isLoading = true
        )
        is TaskStatus.Success -> MainStateStatus.Results(
            state = state.copy(repoList = status.value)
        )
        is TaskStatus.Failure -> MainStateStatus.Error(
            state = state.copy(),
            exception = status.exception
        )
        is TaskStatus.Finally -> MainStateStatus.Loading(
            state = state.copy(),
            isLoading = false
        )
    }.also {
        Timber.d(status::class.simpleName)
    }
}