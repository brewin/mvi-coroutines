package com.github.brewin.mvicoroutines.data

import com.github.brewin.mvicoroutines.data.remote.GitHubApi
import com.github.brewin.mvicoroutines.model.RepoItem

class Repository(private val gitHubApi: GitHubApi) {

    suspend fun searchRepos(query: String): List<RepoItem> =
        gitHubApi.searchRepos(query).await().asRepoItemList

}