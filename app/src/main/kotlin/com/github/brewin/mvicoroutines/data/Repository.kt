package com.github.brewin.mvicoroutines.data

import com.github.brewin.mvicoroutines.data.remote.GitHubApi
import com.github.brewin.mvicoroutines.model.RepoItem
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async

class Repository(private val gitHubApi: GitHubApi) {

    fun searchRepos(query: String): Deferred<List<RepoItem>> = async {
        gitHubApi.searchRepos(query).await().asRepoItemList
    }
}