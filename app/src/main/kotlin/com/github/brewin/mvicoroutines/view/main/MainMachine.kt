package com.github.brewin.mvicoroutines.view.main

import com.github.brewin.mvicoroutines.data.Repository
import com.github.brewin.mvicoroutines.model.RepoItem
import com.github.brewin.mvicoroutines.view.base.ViewState
import com.github.brewin.mvicoroutines.view.base.ViewStateMachine
import kotlinx.android.parcel.Parcelize
import java.util.*

@Parcelize
data class MainState(
    val isLoading: Boolean = false,
    val time: Long = Calendar.getInstance().timeInMillis,
    val query: String = "",
    val repoList: List<RepoItem> = emptyList(),
    val count: Int = 0,
    val error: Throwable? = null
) : ViewState

class MainMachine(
    initialState: MainState,
    private val repository: Repository
) : ViewStateMachine<MainState>(initialState) {

    fun search(query: String) {
        task {
            repository.searchRepos(query)
        }.started {
            copy(
                isLoading = true,
                time = Calendar.getInstance().timeInMillis,
                query = query,
                count = count + 1,
                error = null
            )
        }.failure {
            copy(
                isLoading = false,
                query = query,
                time = Calendar.getInstance().timeInMillis,
                repoList = emptyList(),
                count = count + 1,
                error = it
            )
        }.success {
            copy(
                isLoading = false,
                query = query,
                time = Calendar.getInstance().timeInMillis,
                repoList = it,
                count = count + 1,
                error = null
            )
        }/*.finally {
            // Send some state after task completed
        }*/.start()
    }

    fun refresh() {
        withState {
            search(query)
        }
    }
}