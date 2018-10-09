package com.github.brewin.mvicoroutines.view.main

import com.github.brewin.mvicoroutines.data.Repository
import com.github.brewin.mvicoroutines.model.RepoItem
import com.github.brewin.mvicoroutines.view.base.ViewState
import com.github.brewin.mvicoroutines.view.base.ViewStateMachine
import kotlinx.coroutines.delay
import java.util.*

data class MainState(
    val isLoading: Boolean = false,
    val time: Long = Calendar.getInstance().timeInMillis,
    val query: String = "",
    val repoList: List<RepoItem> = emptyList(),
    val count: Int = 0,
    val error: Throwable? = null
) : ViewState()

class MainMachine(
    private val repository: Repository,
    initialState: MainState = MainState()
) : ViewStateMachine<MainState>(initialState) {

    fun search(query: String) {
        task {
            delay((2000..6000L).random())
            repository.searchRepos(query)
        }.onStarted {
            copy(
                isLoading = true,
                time = Calendar.getInstance().timeInMillis,
                query = query,
                count = count + 1,
                error = null
            )
        }.onFailure {
            copy(
                isLoading = false,
                query = query,
                time = Calendar.getInstance().timeInMillis,
                repoList = emptyList(),
                count = count + 1,
                error = it
            )
        }.onSuccess {
            copy(
                isLoading = false,
                query = query,
                time = Calendar.getInstance().timeInMillis,
                repoList = it,
                count = count + 1,
                error = null
            )
        }.start()
    }

    fun refresh() {
        withState {
            search(query)
        }
    }
}