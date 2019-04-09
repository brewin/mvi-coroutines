package com.github.brewin.mvicoroutines.data.repository

import com.github.brewin.mvicoroutines.data.asRepoEntityList
import com.github.brewin.mvicoroutines.data.remote.GitHubDataSource
import com.github.brewin.mvicoroutines.domain.entity.RepoEntity
import com.github.brewin.mvicoroutines.domain.repository.GitHubRepository

class GitHubRepositoryImpl(private val gitHubDataSource: GitHubDataSource) : GitHubRepository {

    override suspend fun searchRepos(query: String): List<RepoEntity> =
        gitHubDataSource.searchRepos(query).asRepoEntityList
}