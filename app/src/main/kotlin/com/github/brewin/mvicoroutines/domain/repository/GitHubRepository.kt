package com.github.brewin.mvicoroutines.domain.repository

import com.github.brewin.mvicoroutines.domain.entity.RepoEntity

interface GitHubRepository {

    suspend fun searchRepos(query: String): List<RepoEntity>
}