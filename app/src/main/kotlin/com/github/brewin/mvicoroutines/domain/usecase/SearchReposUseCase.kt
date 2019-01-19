package com.github.brewin.mvicoroutines.domain.usecase

import com.github.brewin.mvi.MviUpdate
import com.github.brewin.mvicoroutines.domain.entity.RepoEntity
import com.github.brewin.mvicoroutines.domain.repository.GitHubRepository
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.produce

class SearchReposUseCase(private val gitHubRepository: GitHubRepository) {

    sealed class Update : MviUpdate {
        object Started : Update()
        data class Success(val query: String, val searchResults: List<RepoEntity>) : Update()
        data class Failure(val query: String, val errorMessage: String) : Update()
        object Finally : Update()
    }

    // FIXME: GlobalScope okay here? Could pass in a scope.
    operator fun invoke(query: String) = GlobalScope.produce {
        send(Update.Started)
        try {
            val searchResults = gitHubRepository.searchRepos(query)
            send(Update.Success(query, searchResults))
        } catch (e: Exception) {
            send(Update.Failure(query, e.message ?: "Unknown error"))
        } finally {
            send(Update.Finally)
        }
    }
}