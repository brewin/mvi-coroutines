package com.github.brewin.mvicoroutines.view.main

import android.os.Parcelable
import com.github.brewin.mvicoroutines.common.Failure
import com.github.brewin.mvicoroutines.common.Success
import com.github.brewin.mvicoroutines.common.resultOf
import com.github.brewin.mvicoroutines.data.Repository
import com.github.brewin.mvicoroutines.model.RepoItem
import com.github.brewin.mvicoroutines.view.base.State
import com.github.brewin.mvicoroutines.view.base.StateMachine
import kotlinx.android.parcel.Parcelize
import java.util.*

@Parcelize
data class MainState(
    val isLoading: Boolean = false,
    val error: Throwable? = null,
    val time: Long = Calendar.getInstance().timeInMillis,
    val query: String = "",
    val repoList: List<RepoItem> = emptyList()
) : State, Parcelable

class MainMachine(
    private val repository: Repository,
    initialState: MainState = MainState()
) : StateMachine<MainState>(initialState) {

    fun search(query: String) {
        setState {
            copy(
                isLoading = true,
                error = null,
                time = Calendar.getInstance().timeInMillis,
                query = query
            )
        }
        setState {
            val result = resultOf { repository.searchRepos(query) }
            when (result) {
                is Success -> copy(
                    isLoading = false,
                    error = null,
                    query = query,
                    time = Calendar.getInstance().timeInMillis,
                    repoList = result.value
                )
                is Failure -> copy(
                    isLoading = false,
                    error = result.error,
                    query = query,
                    time = Calendar.getInstance().timeInMillis,
                    repoList = emptyList()
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