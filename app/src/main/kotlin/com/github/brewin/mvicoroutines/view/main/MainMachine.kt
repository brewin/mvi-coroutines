package com.github.brewin.mvicoroutines.view.main

import com.github.brewin.mvicoroutines.common.Failure
import com.github.brewin.mvicoroutines.common.Success
import com.github.brewin.mvicoroutines.data.Repository
import com.github.brewin.mvicoroutines.model.RepoItem
import com.github.brewin.mvicoroutines.view.base.State
import com.github.brewin.mvicoroutines.view.base.StateMachine
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import java.util.*

data class MainState(
    val isLoading: Boolean = false,
    val error: Throwable? = null,
    val time: Long = Calendar.getInstance().timeInMillis,
    val query: String = "",
    val repoList: List<RepoItem> = emptyList(),
    val countTest: List<Int> = emptyList()
) : State()

class MainMachine(
    private val repository: Repository,
    initialState: MainState = MainState()
) : StateMachine<MainState>(initialState) {

    fun search(query: String) {
        sendState {
            copy(
                isLoading = true,
                error = null,
                time = Calendar.getInstance().timeInMillis,
                query = query,
                countTest = countTest + 0
            )
        }

        // Test to make sure all sendState run and run in correct order
        setStateFourTimes()

        sendState({ repository.searchRepos(query) }) {
            when (it) {
                is Success -> copy(
                    isLoading = false,
                    error = null,
                    query = query,
                    time = Calendar.getInstance().timeInMillis,
                    repoList = it.value,
                    countTest = countTest + 5
                )
                is Failure -> copy(
                    isLoading = false,
                    error = it.error,
                    query = query,
                    time = Calendar.getInstance().timeInMillis,
                    repoList = emptyList(),
                    countTest = countTest + 5
                )
            }
        }
    }

    fun refresh() {
        withState {
            search(query)
        }
    }

    private fun setStateFourTimes() {
        (1..4).forEach { n ->
            val deferred = async { delay((500..5000).random()) }
            sendState(deferred) {
                copy(countTest = countTest + n)
            }
        }
    }
}