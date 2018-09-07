package com.github.brewin.mvicoroutines.view.main

import android.os.Parcelable
import com.github.brewin.mvicoroutines.common.Failure
import com.github.brewin.mvicoroutines.common.Success
import com.github.brewin.mvicoroutines.common.resultOf
import com.github.brewin.mvicoroutines.data.Repository
import com.github.brewin.mvicoroutines.model.RepoItem
import com.github.brewin.mvicoroutines.view.base.ViewState
import com.github.brewin.mvicoroutines.view.base.ViewStateMachine
import kotlinx.android.parcel.Parcelize

@Parcelize
data class MainState(
    val error: Throwable? = null,
    val query: String = "",
    val isLoading: Boolean = false,
    val repoList: List<RepoItem> = emptyList()
) : ViewState, Parcelable

class MainMachine(
    private val repository: Repository,
    initialState: MainState = MainState()
) : ViewStateMachine<MainState>(initialState) {

    fun search(query: String) {
        newState { copy(isLoading = true) }
        newState {
            val result = resultOf {
                repository.searchRepos(query.trim())
            }

            when (result) {
                is Success -> copy(
                    error = null,
                    query = query,
                    isLoading = false,
                    repoList = result.value
                )
                is Failure -> copy(
                    error = result.exception,
                    query = query,
                    isLoading = false,
                    repoList = emptyList()
                )
            }
        }
    }

    fun refresh() = withState {
        search(query)
    }
}