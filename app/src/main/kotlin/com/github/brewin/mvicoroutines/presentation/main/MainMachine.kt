package com.github.brewin.mvicoroutines.presentation.main

import android.os.Parcelable
import com.github.brewin.mvi.Machine
import com.github.brewin.mvicoroutines.domain.entity.RepoEntity
import com.github.brewin.mvicoroutines.domain.foldSuspend
import com.github.brewin.mvicoroutines.domain.repository.GitHubRepository
import kotlinx.android.parcel.Parcelize
import kotlinx.coroutines.flow.flow

class MainMachine(
    initialState: State,
    internal val gitHubRepository: GitHubRepository
) : Machine<MainMachine.Event, MainMachine.Update, MainMachine.State>(initialState) {

    sealed class Event : Machine.Event {
        data class SearchSubmitted(val query: String) : Event()
        object RefreshClicked : Event()
        object ErrorMessageDismissed : Event()
    }

    sealed class Update : Machine.Update {
        data class Progress(val isInProgress: Boolean) : Update()
        data class Results(val query: String, val searchResults: List<RepoEntity>) : Update()
        data class Error(val query: String, val errorMessage: String) : Update()
        object HideError : Update()
    }

    @Parcelize
    data class State(
        val query: String = "",
        val searchResults: List<RepoEntity> = emptyList(),
        val isInProgress: Boolean = false,
        val errorMessage: String = "",
        val shouldShowError: Boolean = false
    ) : Machine.State, Parcelable

    override fun handleEvent(event: Event) = when (event) {
        is Event.SearchSubmitted -> searchRepos(event.query)
        Event.RefreshClicked -> searchRepos(state.query)
        Event.ErrorMessageDismissed -> hideErrorMessage()
    }

    override fun updateState(update: Update) = when (update) {
        is Update.Progress -> state.copy(
            isInProgress = update.isInProgress
        )
        is Update.Results -> state.copy(
            query = update.query,
            searchResults = update.searchResults
        )
        is Update.Error -> state.copy(
            query = update.query,
            errorMessage = update.errorMessage,
            shouldShowError = true
        )
        Update.HideError -> state.copy(
            shouldShowError = false
        )
    }
}

/* State updaters (ie. use cases) */

fun MainMachine.searchRepos(query: String) = flow {
    emit(MainMachine.Update.Progress(true))
    gitHubRepository.searchRepos(query)
        .foldSuspend(
            onLeft = { error ->
                emit(MainMachine.Update.Error(query, error.message))
            },
            onRight = { searchResults ->
                emit(MainMachine.Update.Results(query, searchResults))
            }
        )
    emit(MainMachine.Update.Progress(false))
}

fun MainMachine.hideErrorMessage() = flow {
    emit(MainMachine.Update.HideError)
}