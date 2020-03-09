package com.github.brewin.mvicoroutines.data

import com.github.brewin.mvicoroutines.data.remote.response.GitHubRepoSearchResponse
import com.github.brewin.mvicoroutines.domain.entity.RepoEntity

val GitHubRepoSearchResponse.asRepoEntityList: List<RepoEntity>
    get() = items.orEmpty()
        .filterNotNull()
        .filterNot { it.name.isNullOrBlank() || it.htmlUrl.isNullOrBlank() }
        .map { RepoEntity(it.name!!, it.htmlUrl!!) }