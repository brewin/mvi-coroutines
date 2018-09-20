package com.github.brewin.mvicoroutines.view.main

import android.os.Parcelable
import com.github.brewin.mvicoroutines.common.Failure
import com.github.brewin.mvicoroutines.common.Success
import com.github.brewin.mvicoroutines.data.Repository
import com.github.brewin.mvicoroutines.model.RepoItem
import com.github.brewin.mvicoroutines.view.base.State
import com.github.brewin.mvicoroutines.view.base.StateMachine
import kotlinx.android.parcel.Parcelize
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import java.util.*

@Parcelize
data class MainState(
    val isLoading: Boolean = false,
    val error: Throwable? = null,
    val time: Long = Calendar.getInstance().timeInMillis,
    val query: String = "",
    val repoList: List<RepoItem> = emptyList(),
    val countTest: List<Int> = emptyList()
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
                query = query,
                countTest = countTest + 0
            )
        }

        // Test to make sure all setState run and run in correct order
        setStateFourTimes()

        val task = async { repository.searchRepos(query) }
        setState {
            when (val result = task.await()) {
                is Success -> copy(
                    isLoading = false,
                    error = null,
                    query = query,
                    time = Calendar.getInstance().timeInMillis,
                    repoList = result.value,
                    countTest = countTest + 5
                )
                is Failure -> copy(
                    isLoading = false,
                    error = result.error,
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
            val task = async { delay((1000)) }
            setState {
                task.await()
                copy(countTest = countTest + n)
            }
        }
    }
}


