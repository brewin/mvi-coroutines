package com.github.brewin.mvicoroutines.data.repository

import com.github.brewin.mvicoroutines.data.asRepoEntityList
import com.github.brewin.mvicoroutines.data.remote.GitHubDataSource
import com.github.brewin.mvicoroutines.domain.Either
import com.github.brewin.mvicoroutines.domain.asLeft
import com.github.brewin.mvicoroutines.domain.asRight
import com.github.brewin.mvicoroutines.domain.entity.RepoEntity
import com.github.brewin.mvicoroutines.domain.error.GithubSearchError
import com.github.brewin.mvicoroutines.domain.repository.GitHubRepository
import io.ktor.client.features.ResponseException
import timber.log.Timber

class GitHubRepositoryImpl(private val gitHubDataSource: GitHubDataSource) : GitHubRepository {

    override suspend fun searchRepos(query: String): Either<GithubSearchError, List<RepoEntity>> =
        try {
            gitHubDataSource.searchRepos(query).asRepoEntityList.asRight
        } catch (e: ResponseException) {
            Timber.e(e)
            GithubSearchError("Error getting search results from GitHub", e).asLeft
        }
}