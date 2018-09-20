package com.github.brewin.mvicoroutines.data

import com.github.brewin.mvicoroutines.common.Result
import com.github.brewin.mvicoroutines.common.resultOf
import com.github.brewin.mvicoroutines.data.remote.GitHubApi
import com.github.brewin.mvicoroutines.model.RepoItem

class Repository(private val gitHubApi: GitHubApi) {

    suspend fun searchRepos(query: String): Result<List<RepoItem>> = resultOf {
        gitHubApi.searchRepos(query).await().asRepoItemList
    }
}