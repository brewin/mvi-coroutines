package com.github.brewin.mvicoroutines.view.main

import com.github.brewin.mvicoroutines.data.Repository
import com.github.brewin.mvicoroutines.model.RepoItem
import com.github.brewin.mvicoroutines.view.base.*
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
        // Start some random tasks as a demonstration
        (1..4).forEach {
            task { delay((1000..5000L).random()) }.start {
                copy(
                    time = Calendar.getInstance().timeInMillis,
                    count = count + 1
                )
            }
        }

        task { repository.searchRepos(query) }.start {
            when (it) {
                is Started -> copy(
                    isLoading = true,
                    time = Calendar.getInstance().timeInMillis,
                    query = query,
                    count = count + 1,
                    error = null
                )
                is Success -> copy(
                    isLoading = false,
                    query = query,
                    time = Calendar.getInstance().timeInMillis,
                    repoList = it.value,
                    count = count + 1,
                    error = null
                )
                is Failure -> copy(
                    isLoading = false,
                    query = query,
                    time = Calendar.getInstance().timeInMillis,
                    repoList = emptyList(),
                    count = count + 1,
                    error = it.error
                )
            }
        }
    }

    fun refresh() {
        withState {
            search(query)
        }
    }
}