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

sealed class MainInput : Machine.Input {
    data class QuerySubmit(val query: String) : MainInput()
    object RefreshClick : MainInput()
    object RefreshSwipe : MainInput()
    data class RepoClick(val url: String) : MainInput()
    object ErrorMessageDismiss : MainInput()
}

@Parcelize
data class MainState(
    val query: String,
    val searchResults: List<RepoEntity>,
    val isInProgress: Boolean,
    val urlToShow: String,
    val shouldOpenUrl: Boolean,
    val errorMessage: String,
    val shouldShowError: Boolean,
    val timestamp: Long
) : Machine.State, Parcelable {

    companion object {
        val DEFAULT = MainState(
            query = "",
            searchResults = emptyList(),
            isInProgress = false,
            urlToShow = "",
            shouldOpenUrl = false,
            errorMessage = "",
            shouldShowError = false,
            timestamp = 0
        )
    }
}

sealed class MainEffect : Machine.Effect {
    data class RepoUrlOpen(val url: String) : MainEffect()
}

class MainMachine(
    events: Flow<MainInput>,
    initialState: MainState,
    private val gitHubRepository: GitHubRepository
) : Machine<MainInput, MainState, MainEffect>(events, initialState) {

    override fun MainInput.process() = when (this) {
        is MainInput.QuerySubmit -> searchRepos(query)
        MainInput.RefreshClick, MainInput.RefreshSwipe -> searchRepos(state.query)
        is MainInput.RepoClick -> showRepoUrl(url)
        MainInput.ErrorMessageDismiss -> hideErrorMessage()
    }

    /* Output Flows (ie. use cases) */

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

    private fun showRepoUrl(url: String) = flow {
        emit(MainEffect.RepoUrlOpen(url))
    }

    private fun hideErrorMessage() = flow {
        emit(state.copy(shouldShowError = false))
    }
}