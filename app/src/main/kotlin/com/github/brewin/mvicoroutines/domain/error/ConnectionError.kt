package com.github.brewin.mvicoroutines.domain.error

data class ConnectionError(
    override val message: String,
    override val exception: Exception
) : DomainError