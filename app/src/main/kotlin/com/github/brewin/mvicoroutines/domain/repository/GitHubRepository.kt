package com.github.brewin.mvicoroutines.domain.repository

import com.github.brewin.mvicoroutines.domain.Either
import com.github.brewin.mvicoroutines.domain.entity.RepoEntity
import com.github.brewin.mvicoroutines.domain.error.DomainError

interface GitHubRepository {
    suspend fun searchRepos(query: String): Either<DomainError, List<RepoEntity>>
}