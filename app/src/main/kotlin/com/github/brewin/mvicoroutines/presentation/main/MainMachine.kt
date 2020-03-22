package com.github.brewin.mvicoroutines.presentation.main

import android.os.Parcelable
import com.github.brewin.mvi.Machine
import com.github.brewin.mvicoroutines.domain.entity.RepoEntity
import com.github.brewin.mvicoroutines.domain.foldSuspend
import com.github.brewin.mvicoroutines.domain.repository.GitHubRepository
import kotlinx.android.parcel.Parcelize
import kotlinx.coroutines.flow.flow

sealed class MainEvent {
    data class QuerySubmit(val query: String) : MainEvent()
    object RefreshClick : MainEvent()
    object RefreshSwipe : MainEvent()
    object ErrorMessageDismiss : MainEvent()
}

sealed class MainUpdate {
    data class Progress(val isInProgress: Boolean) : MainUpdate()
    data class Results(val query: String, val searchResults: List<RepoEntity>) : MainUpdate()
    data class Error(val query: String, val errorMessage: String) : MainUpdate()
    object HideError : MainUpdate()
}

@Parcelize
data class MainState(
    val query: String,
    val searchResults: List<RepoEntity>,
    val isInProgress: Boolean,
    val errorMessage: String,
    val shouldShowError: Boolean
) : Parcelable {

    companion object {
        fun default() = MainState(
            query = "",
            searchResults = emptyList(),
            isInProgress = false,
            errorMessage = "",
            shouldShowError = false
        )
    }
}

class MainMachine(
    initialState: MainState,
    internal val gitHubRepository: GitHubRepository
) : Machine<MainEvent, MainUpdate, MainState>(initialState) {

    override fun MainEvent.toUpdateFlow() = when (this) {
        is MainEvent.QuerySubmit -> searchRepos(query)
        MainEvent.RefreshClick,
        MainEvent.RefreshSwipe -> searchRepos(state.query)
        MainEvent.ErrorMessageDismiss -> hideErrorMessage()
    }

    override fun MainUpdate.toState() = when (this) {
        is MainUpdate.Progress -> state.copy(
            isInProgress = isInProgress
        )
        is MainUpdate.Results -> state.copy(
            query = query,
            searchResults = searchResults
        )
        is MainUpdate.Error -> state.copy(
            query = query,
            errorMessage = errorMessage,
            shouldShowError = true
        )
        MainUpdate.HideError -> state.copy(
            shouldShowError = false
        )
    }
}

/* Update Flows (ie. use cases) */

fun MainMachine.searchRepos(query: String) = flow {
    emit(MainUpdate.Progress(true))
    gitHubRepository.searchRepos(query)
        .foldSuspend(
            onLeft = { error ->
                emit(MainUpdate.Error(query, error.message))
            },
            onRight = { searchResults ->
                emit(MainUpdate.Results(query, searchResults))
            }
        )
    emit(MainUpdate.Progress(false))
}

fun MainMachine.hideErrorMessage() = flow {
    emit(MainUpdate.HideError)
}