package com.github.brewin.mvicoroutines.ui.main

import android.net.Uri
import android.os.Parcelable
import androidx.net.toUri
import com.github.brewin.mvicoroutines.data.GitHubRepos
import com.github.brewin.mvicoroutines.data.Repository
import com.github.brewin.mvicoroutines.ui.base.Ui
import com.github.brewin.mvicoroutines.ui.base.UiAction
import com.github.brewin.mvicoroutines.ui.base.UiResult
import com.github.brewin.mvicoroutines.ui.base.UiState
import kotlinx.android.parcel.Parcelize
import timber.log.Timber
import java.io.IOException

sealed class MainUiAction : UiAction {
    object UiEnter : MainUiAction()
    object UiExit : MainUiAction()
    object InProgress : MainUiAction()
    data class Search(val query: String) : MainUiAction()
    object Refresh : MainUiAction()
    data class ItemClick(val item: ReposItem) : MainUiAction()
}

sealed class MainUiResult : UiResult {
    object InProgress : MainUiResult()
    data class GotRepos(val query: String, val repos: GitHubRepos) : MainUiResult()
    data class GotError(val query: String, val error: Throwable) : MainUiResult()
    data class GotItemUrl(val url: Uri) : MainUiResult()
}

sealed class MainUiState : UiState {
    object Initial : MainUiState()
    object InProgress : MainUiState()

    @Parcelize
    data class Success(
        val query: String,
        val content: List<ReposItem>
    ) : MainUiState(), Parcelable

    data class Failure(
        val query: String,
        val error: Throwable
    ) : MainUiState()

    data class OpenUrl(
        val itemUrlToOpen: Uri
    ) : MainUiState()
}

@Parcelize
data class ReposItem(val name: String, val url: Uri) : Parcelable

class MainUi(
    private val repository: Repository,
    initialState: MainUiState = MainUiState.Initial
) : Ui<MainUiAction, MainUiResult, MainUiState>(initialState) {

    private var lastQuery = "" // FIXME: Better to propagate query through all Actions?

    override suspend fun resultFromAction(action: MainUiAction): MainUiResult = when (action) {
        is MainUiAction.UiEnter -> TODO()
        is MainUiAction.UiExit -> TODO()
        is MainUiAction.InProgress -> MainUiResult.InProgress
        is MainUiAction.Search -> search(action.query)
        is MainUiAction.Refresh -> refresh()
        is MainUiAction.ItemClick -> MainUiResult.GotItemUrl(action.item.url)
    }.also { Timber.d("Action:\n$action") }

    override suspend fun stateFromResult(result: MainUiResult): MainUiState = when (result) {
        is MainUiResult.InProgress -> MainUiState.InProgress
        is MainUiResult.GotRepos -> MainUiState.Success(result.query, mapToUi(result.repos))
        is MainUiResult.GotError -> MainUiState.Failure(result.query, result.error)
        is MainUiResult.GotItemUrl -> MainUiState.OpenUrl(result.url)
    }.also { Timber.d("Result:\n$result") }

    fun offerActionWithProgress(action: MainUiAction) {
        offerAction(MainUiAction.InProgress)
        offerAction(action)
    }

    private suspend fun search(query: String): MainUiResult = if (query.isNotBlank()) {
        lastQuery = query
        repository.searchRepos(query).run {
            val body = body()
            if (isSuccessful && body != null) {
                if (body.items != null && body.items.isNotEmpty()) {
                    MainUiResult.GotRepos(query, body)
                } else {
                    MainUiResult.GotError(query, IOException("No results found for: $query"))
                }
            } else {
                MainUiResult.GotError(query, IOException(message()))
            }
        }
    } else {
        MainUiResult.GotError(query, IllegalArgumentException("Enter a search term"))
    }

    private suspend fun refresh(): MainUiResult = search(lastQuery)

    private fun mapToUi(result: GitHubRepos): List<ReposItem> = result.items.orEmpty()
        .filterNot { it.name.isNullOrBlank() || it.htmlUrl.isNullOrBlank() }
        .map { ReposItem(it.name!!, it.htmlUrl!!.toUri()) }
}
