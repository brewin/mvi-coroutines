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
) : State()

class MainMachine(
    private val repository: Repository,
    initialState: MainState = MainState()
) : StateMachine<MainState>(initialState) {

    fun search(query: String) {
        // test
        (1..4).forEach {
            asyncTry {
                delay((1000..5000L).random())
            }.sendState {
                copy(
                    time = Calendar.getInstance().timeInMillis,
                    count = count + 1
                )
            }
        }

        asyncTry {
            repository.searchRepos(query)
        }.sendState {
            when (it) {
                is Loading -> copy(
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