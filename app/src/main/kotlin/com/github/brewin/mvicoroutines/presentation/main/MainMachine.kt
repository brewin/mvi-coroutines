package com.github.brewin.mvicoroutines.presentation.main

import android.os.Parcelable
import com.github.brewin.mvicoroutines.domain.Left
import com.github.brewin.mvicoroutines.domain.Right
import com.github.brewin.mvicoroutines.domain.entity.RepoEntity
import com.github.brewin.mvicoroutines.domain.repository.GitHubRepository
import com.github.brewin.mvicoroutines.presentation.arch.Machine
import kotlinx.android.parcel.Parcelize
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.*

sealed class MainEvent {
    data class QuerySubmit(val query: String) : MainEvent()
    object RefreshClick : MainEvent()
    object RefreshSwipe : MainEvent()
    object ErrorMessageDismiss : MainEvent()
}

@Parcelize
data class MainState(
    val query: String,
    val searchResults: List<RepoEntity>,
    val isInProgress: Boolean,
    val errorMessage: String,
    val shouldShowError: Boolean,
    val timestamp: Long
) : Parcelable {

    companion object {
        val DEFAULT = MainState(
            query = "",
            searchResults = emptyList(),
            isInProgress = false,
            errorMessage = "",
            shouldShowError = false,
            timestamp = 0
        )
    }
}

class MainMachine(
    events: Flow<MainEvent>,
    initialState: MainState,
    private val gitHubRepository: GitHubRepository
) : Machine<MainEvent, MainState>(events, initialState) {

    override fun MainEvent.toStates() = when (this) {
        is MainEvent.QuerySubmit -> searchRepos(query)
        MainEvent.RefreshClick, MainEvent.RefreshSwipe -> searchRepos(state.query)
        MainEvent.ErrorMessageDismiss -> hideErrorMessage()
    }

    /* State Mutation Flows (ie. use cases) */

    private fun searchRepos(query: String) = flow {
        emit(state.copy(isInProgress = true, timestamp = Calendar.getInstance().timeInMillis))
        emit(
            when (val either = gitHubRepository.searchRepos(query)) {
                is Left -> state.copy(
                    query = query,
                    errorMessage = either.value.message,
                    isInProgress = false,
                    shouldShowError = true
                )
                is Right -> state.copy(
                    query = query,
                    searchResults = either.value,
                    isInProgress = false
                )
            }
        )
    }

    private fun hideErrorMessage() = flow {
        emit(state.copy(shouldShowError = false))
    }
}