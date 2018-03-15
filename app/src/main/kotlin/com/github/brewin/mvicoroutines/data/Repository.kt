package com.github.brewin.mvicoroutines.data

import com.github.brewin.mvicoroutines.util.retry

class Repository(private val service: GitHubApi) {

    suspend fun searchRepos(query: String) = retry(times = 3) {
        service.searchRepos(query).await()
    }
}