package com.github.brewin.mvicoroutines.data.repository

import com.github.brewin.mvicoroutines.data.asRepoEntityList
import com.github.brewin.mvicoroutines.data.remote.GitHubDataSource
import com.github.brewin.mvicoroutines.domain.Either
import com.github.brewin.mvicoroutines.domain.asLeft
import com.github.brewin.mvicoroutines.domain.asRight
import com.github.brewin.mvicoroutines.domain.entity.RepoEntity
import com.github.brewin.mvicoroutines.domain.error.ConnectionError
import com.github.brewin.mvicoroutines.domain.error.DomainError
import com.github.brewin.mvicoroutines.domain.error.GithubSearchError
import com.github.brewin.mvicoroutines.domain.repository.GitHubRepository
import io.ktor.client.features.ResponseException
import timber.log.Timber
import java.io.IOException

class GitHubRepositoryImpl(private val gitHubDataSource: GitHubDataSource) : GitHubRepository {

    override suspend fun searchRepos(query: String): Either<DomainError, List<RepoEntity>> =
        try {
            gitHubDataSource.searchRepos(query).asRepoEntityList.asRight
        } catch (e: Exception) {
            Timber.e(e)
            when (e) {
                is ResponseException ->
                    GithubSearchError("Error getting search results from GitHub", e)
                is IOException ->
                    ConnectionError("Error connecting to GitHub", e)
                else -> throw e // Unexpected exception
            }.asLeft
        }
}