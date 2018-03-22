package com.github.brewin.mvicoroutines.view.main

import android.net.Uri
import android.os.Parcelable
import androidx.net.toUri
import com.github.brewin.mvicoroutines.data.GitHubRepos
import com.github.brewin.mvicoroutines.data.Repository
import com.github.brewin.mvicoroutines.view.base.Machine
import com.github.brewin.mvicoroutines.view.base.Intent
import com.github.brewin.mvicoroutines.view.base.Task
import com.github.brewin.mvicoroutines.view.base.State
import kotlinx.android.parcel.Parcelize
import timber.log.Timber
import java.io.IOException

sealed class MainIntent : Intent {
    object UiEnter : MainIntent()
    object UiExit : MainIntent()
    object InProgress : MainIntent()
    data class Search(val query: String) : MainIntent()
    object Refresh : MainIntent()
}

sealed class MainTask : Task {
    object InProgress : MainTask()
    data class GotRepos(val query: String, val repos: GitHubRepos) : MainTask()
    data class GotError(val query: String, val error: Throwable) : MainTask()
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

    override suspend fun taskFromIntent(action: MainIntent): MainTask = when (action) {
        is MainIntent.UiEnter -> TODO()
        is MainIntent.UiExit -> TODO()
        is MainIntent.InProgress -> MainTask.InProgress
        is MainIntent.Search -> search(action.query)
        is MainIntent.Refresh -> refresh()
    }.also { Timber.d("Action:\n$action") }

    override suspend fun stateFromTask(result: MainTask): MainState = lastState().run {
        when (result) {
            is MainTask.InProgress -> copy(
                isLoading = true
            )
            is MainTask.GotRepos -> copy(
                query = result.query,
                isLoading = false,
                repoList = mapToUi(result.repos)
            )
            is MainTask.GotError -> copy(
                query = result.query,
                isLoading = false,
                repoList = emptyList(),
                error = result.error
            )
        }.also { Timber.d("Result:\n$result") }
    }

    fun offerActionWithProgress(action: MainIntent) {
        offerIntent(MainIntent.InProgress)
        offerIntent(action)
    }

    private suspend fun search(query: String): MainTask = if (query.isNotBlank()) {
        repository.searchRepos(query).run {
            val body = body()
            if (isSuccessful && body != null) {
                if (body.items != null && body.items.isNotEmpty()) {
                    MainTask.GotRepos(query, body)
                } else {
                    MainTask.GotError(query, IOException("No results found for: $query"))
                }
            } else {
                MainTask.GotError(query, IOException(message()))
            }
        }
    } else {
        MainTask.GotError(query, IllegalArgumentException("Enter a search term"))
    }

    private suspend fun refresh(): MainTask = search(lastState().query)

    private fun mapToUi(result: GitHubRepos): List<ReposItem> = result.items.orEmpty()
        .filterNot { it.name.isNullOrBlank() || it.htmlUrl.isNullOrBlank() }
        .map { ReposItem(it.name!!, it.htmlUrl!!.toUri()) }
}
