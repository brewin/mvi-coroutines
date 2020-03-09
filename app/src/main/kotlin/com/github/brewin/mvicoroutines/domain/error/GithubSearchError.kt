package com.github.brewin.mvicoroutines.domain.error

data class GithubSearchError(
    override val message: String,
    override val exception: Exception
) : DomainError