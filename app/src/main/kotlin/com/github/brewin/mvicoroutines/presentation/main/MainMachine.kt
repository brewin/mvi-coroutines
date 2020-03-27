package com.github.brewin.mvicoroutines.presentation.main

import android.os.Parcelable
import com.github.brewin.mvicoroutines.domain.entity.RepoEntity
import com.github.brewin.mvicoroutines.domain.foldSuspend
import com.github.brewin.mvicoroutines.domain.repository.GitHubRepository
import com.github.brewin.mvicoroutines.presentation.arch.Machine
import kotlinx.android.parcel.Parcelize
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.*
import kotlin.time.ExperimentalTime

sealed class MainEvent {
    data class QuerySubmit(val query: String) : MainEvent()
    object RefreshClick : MainEvent()
    object RefreshSwipe : MainEvent()
    object TestClick : MainEvent()
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
                timestamp = Calendar.getInstance().timeInMillis
            )
    }
}

class MainMachine(
    events: Flow<MainEvent>,
    initialState: MainState,
    internal val gitHubRepository: GitHubRepository
) : Machine<MainEvent, MainState>(events, initialState) {

    override fun MainEvent.toState() = when (this) {
        is MainEvent.QuerySubmit -> searchRepos(query)
        MainEvent.RefreshClick,
        MainEvent.RefreshSwipe -> searchRepos(state.query)
        MainEvent.TestClick -> saveTimestamp()
        MainEvent.ErrorMessageDismiss -> hideErrorMessage()
    }
}

/* State Mutations (ie. use cases) */

fun MainMachine.searchRepos(query: String) = flow {
    emit(state.copy(isInProgress = true))
    gitHubRepository.searchRepos(query)
        .foldSuspend(
            onLeft = { error ->
                emit(state.copy(query = query, errorMessage = error.message, isInProgress = false))
            },
            onRight = { searchResults ->
                emit(state.copy(query = query, searchResults = searchResults, isInProgress = false))
            }
        )
}

fun MainMachine.saveTimestamp() = flow {
    emit(state.copy(timestamp = Calendar.getInstance().timeInMillis))
}

fun MainMachine.hideErrorMessage() = flow {
    emit(state.copy(shouldShowError = false))
}