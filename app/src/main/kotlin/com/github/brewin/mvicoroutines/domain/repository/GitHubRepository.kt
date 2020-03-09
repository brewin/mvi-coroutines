package com.github.brewin.mvicoroutines.domain.repository

import com.github.brewin.mvicoroutines.domain.Either
import com.github.brewin.mvicoroutines.domain.entity.RepoEntity
import com.github.brewin.mvicoroutines.domain.error.GithubSearchError

interface GitHubRepository {
    suspend fun searchRepos(query: String): Either<GithubSearchError, List<RepoEntity>>
}