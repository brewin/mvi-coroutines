package com.github.brewin.mvicoroutines.view.main

import android.net.Uri
import android.os.Parcelable
import androidx.net.toUri
import com.github.brewin.mvicoroutines.common.*
import com.github.brewin.mvicoroutines.data.GitHubRepos
import com.github.brewin.mvicoroutines.data.Repository
import com.github.brewin.mvicoroutines.view.base.Intent
import com.github.brewin.mvicoroutines.view.base.Machine
import com.github.brewin.mvicoroutines.view.base.State
import com.github.brewin.mvicoroutines.view.base.Task
import kotlinx.android.parcel.Parcelize
import timber.log.Timber

sealed class MainIntent : Intent {
    object InProgress : MainIntent()
    class Search(val query: String) : MainIntent()
    object Refresh : MainIntent()
}

sealed class MainTask : Task {
    object InProgress : MainTask()
    class Search(
        val query: String,
        val result: Result<MainError.Search, GitHubRepos>
    ) : MainTask()

    class Refresh(
        val result: Result<MainError.Search, GitHubRepos>
    ) : MainTask()
}

sealed class MainError : Exception() {
    sealed class Search : MainError() {
        class NoQuery(override val message: String = "Enter a search term") : Search()
        class NoResults(override val message: String = "No results found") : Search()
    }
}

@Parcelize
data class MainState(
    val query: String = "",
    val isLoading: Boolean = false,
    val repoList: List<ReposItem> = emptyList(),
    val error: Throwable? = null
) : State, Parcelable

@Parcelize
data class ReposItem(val name: String, val url: Uri) : Parcelable

class MainMachine(
    private val repository: Repository,
    initialState: MainState = MainState()
) : Machine<MainIntent, MainTask, MainState>(initialState) {

    override suspend fun taskFromIntent(lastState: MainState, intent: MainIntent): MainTask =
        when (intent) {
            is MainIntent.InProgress -> MainTask.InProgress
            is MainIntent.Search -> MainTask.Search(intent.query, search(intent.query))
            is MainIntent.Refresh -> MainTask.Refresh(search(lastState.query))
        }.also { Timber.d("Intent:\n$intent") }

    override suspend fun stateFromTask(lastState: MainState, task: MainTask): MainState =
        when (task) {
            is MainTask.InProgress -> lastState.copy(
                isLoading = true
            )
            is MainTask.Search -> when (task.result) {
                is Success -> lastState.copy(
                    query = task.query,
                    isLoading = false,
                    repoList = task.result.value.toReposItemList(),
                    error = null
                )
                is Failure.Known -> lastState.copy(
                    query = task.query,
                    isLoading = false,
                    repoList = emptyList(),
                    error = task.result.error
                )
                is Failure.Unknown -> lastState.copy(
                    query = task.query,
                    isLoading = false,
                    repoList = emptyList(),
                    error = task.result.exception
                )
            }
            is MainTask.Refresh -> when (task.result) {
                is Success -> lastState.copy(
                    query = lastState.query,
                    isLoading = false,
                    repoList = task.result.value.toReposItemList(),
                    error = null
                )
                is Failure.Known -> lastState.copy(
                    query = lastState.query,
                    isLoading = false,
                    repoList = emptyList(),
                    error = task.result.error
                )
                is Failure.Unknown -> lastState.copy(
                    query = lastState.query,
                    isLoading = false,
                    repoList = emptyList(),
                    error = task.result.exception
                )
            }
        }.also { Timber.d("Task:\n$task") }

    fun offerIntentWithProgress(intent: MainIntent) {
        offerIntent(MainIntent.InProgress)
        offerIntent(intent)
    }

    private suspend fun search(query: String): Result<MainError.Search, GitHubRepos> = resultOf {
        if (query.isBlank()) {
            throw MainError.Search.NoQuery()
        }
        retry(3) { repository.searchRepos(query) }.run {
            if (items == null || items.isEmpty()) {
                throw MainError.Search.NoResults()
            }
            this
        }
    }

    private fun GitHubRepos.toReposItemList(): List<ReposItem> = items.orEmpty()
        .filterNot { it.name.isNullOrBlank() || it.htmlUrl.isNullOrBlank() }
        .map { ReposItem(it.name!!, it.htmlUrl!!.toUri()) }
}