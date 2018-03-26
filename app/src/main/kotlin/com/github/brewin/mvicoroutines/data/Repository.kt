package com.github.brewin.mvicoroutines.data

class Repository(private val service: GitHubApi) {

    suspend fun searchRepos(query: String): GitHubRepos =
        service.searchRepos(query).await()
}

