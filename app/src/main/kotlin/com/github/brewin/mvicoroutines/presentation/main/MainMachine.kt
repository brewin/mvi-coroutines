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
    val query: String = "",
    val searchResults: List<RepoEntity> = emptyList(),
    val isInProgress: Boolean = false,
    val errorMessage: String = "",
    val shouldShowError: Boolean = false
) : Parcelable

class MainMachine(
    initialState: MainState,
    internal val gitHubRepository: GitHubRepository
) : Machine<MainEvent, MainUpdate, MainState>(initialState) {

    override fun handleEvent(event: MainEvent) = when (event) {
        is MainEvent.QuerySubmit -> searchRepos(event.query)
        MainEvent.RefreshClick,
        MainEvent.RefreshSwipe -> searchRepos(state.query)
        MainEvent.ErrorMessageDismiss -> hideErrorMessage()
    }

    override fun updateState(update: MainUpdate) = when (update) {
        is MainUpdate.Progress -> state.copy(
            isInProgress = update.isInProgress
        )
        is MainUpdate.Results -> state.copy(
            query = update.query,
            searchResults = update.searchResults
        )
        is MainUpdate.Error -> state.copy(
            query = update.query,
            errorMessage = update.errorMessage,
            shouldShowError = true
        )
        MainUpdate.HideError -> state.copy(
            shouldShowError = false
        )
    }
}

/* State updaters (ie. use cases) */

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