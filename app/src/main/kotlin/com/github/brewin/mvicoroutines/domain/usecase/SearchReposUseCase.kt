package com.github.brewin.mvicoroutines.domain.usecase

import com.github.brewin.mvi.MviResult
import com.github.brewin.mvicoroutines.domain.entity.RepoEntity
import com.github.brewin.mvicoroutines.domain.repository.GitHubRepository
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.produce

class SearchReposUseCase(private val gitHubRepository: GitHubRepository) {

    sealed class Result : MviResult {
        object Waiting : Result()
        data class Success(val query: String, val searchResults: List<RepoEntity>) : Result()
        data class Failure(val query: String, val errorMessage: String) : Result()
        object Finally : Result()
    }

    // FIXME: GlobalScope okay here? Could pass in a scope.
    operator fun invoke(query: String) = GlobalScope.produce {
        send(Result.Waiting)
        try {
            val searchResults = gitHubRepository.searchRepos(query)
            send(Result.Success(query, searchResults))
        } catch (e: Exception) {
            send(Result.Failure(query, e.message ?: "Unknown error"))
        } finally {
            send(Result.Finally)
        }
    }
}