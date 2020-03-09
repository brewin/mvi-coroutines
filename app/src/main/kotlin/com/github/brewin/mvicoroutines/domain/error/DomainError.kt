package com.github.brewin.mvicoroutines.domain.error

interface DomainError {
    val message: String
    val exception: Exception
}