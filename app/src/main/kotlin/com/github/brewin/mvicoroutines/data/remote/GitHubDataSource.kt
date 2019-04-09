package com.github.brewin.mvicoroutines.data.remote

import com.github.brewin.mvicoroutines.data.remote.response.GitHubRepoSearchResponse
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.http.URLProtocol

class GitHubDataSource {

    suspend fun searchRepos(query: String): GitHubRepoSearchResponse = httpClient.get {
        url {
            protocol = URLProtocol.HTTPS
            host = "api.github.com"
            encodedPath = "/search/repositories"
            parameter("sort", "updated")
            parameter("q", query)
        }
    }
}