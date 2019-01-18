package com.github.brewin.mvicoroutines.data.repository

import com.github.brewin.mvicoroutines.data.remote.GitHubApi
import com.github.brewin.mvicoroutines.domain.entity.RepoEntity
import com.github.brewin.mvicoroutines.domain.repository.GitHubRepository

class GitHubRepositoryImpl(private val gitHubApi: GitHubApi) : GitHubRepository {

    override suspend fun searchRepos(query: String): List<RepoEntity> =
        gitHubApi.searchRepos(query).await().asRepoEntityList
}