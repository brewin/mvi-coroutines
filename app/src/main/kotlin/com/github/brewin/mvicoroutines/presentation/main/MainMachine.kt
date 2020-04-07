package com.github.brewin.mvicoroutines.presentation.main

import android.os.Parcelable
import com.github.brewin.mvicoroutines.domain.Left
import com.github.brewin.mvicoroutines.domain.Right
import com.github.brewin.mvicoroutines.domain.entity.RepoEntity
import com.github.brewin.mvicoroutines.domain.repository.GitHubRepository
import com.github.brewin.mvicoroutines.presentation.arch.Machine
import kotlinx.android.parcel.Parcelize
import kotlinx.coroutines.flow.flow
import java.util.*

sealed class MainInput
data class QuerySubmit(val query: String) : MainInput()
object RefreshClick : MainInput()
object RefreshSwipe : MainInput()
data class RepoClick(val url: String) : MainInput()

sealed class MainOutput

@Parcelize
data class MainState(
    val query: String,
    val searchResults: List<RepoEntity>,
    val isInProgress: Boolean,
    val timestamp: Long
) : MainOutput(), Parcelable {

    companion object {
        val DEFAULT = MainState(
            query = "",
            searchResults = emptyList(),
            isInProgress = false,
            timestamp = 0
        )
    }
}

sealed class MainEffect : MainOutput() {
    data class OpenRepoUrl(val url: String) : MainEffect()
    data class ShowError(val message: String) : MainEffect()
}

class MainMachine(
    initialState: MainState,
    private val gitHubRepository: GitHubRepository
) : Machine<MainInput, MainOutput, MainState, MainEffect>(initialState) {

    override fun MainInput.process() = when (this) {
        is QuerySubmit -> searchRepos(query)
        RefreshClick, RefreshSwipe -> searchRepos(state.query)
        is RepoClick -> showRepoUrl(url)
    }

    /* Output Flows (ie. use cases) */

    private fun searchRepos(query: String) = flow {
        emit(
            state.copy(
                query = query,
                isInProgress = true,
                timestamp = Calendar.getInstance().timeInMillis
            )
        )
        emit(
            when (val either = gitHubRepository.searchRepos(query)) {
                is Left -> MainEffect.ShowError(either.value.message)
                is Right -> state.copy(searchResults = either.value)
            }
        )
        emit(state.copy(isInProgress = false))
    }

    private fun showRepoUrl(url: String) = flow {
        emit(MainEffect.OpenRepoUrl(url))
    }
}