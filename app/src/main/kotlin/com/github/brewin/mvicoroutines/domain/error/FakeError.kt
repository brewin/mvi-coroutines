package com.github.brewin.mvicoroutines.domain.error

class FakeError : DomainError {
    override val message: String = "This is a fake error"
    override val exception: Exception = Exception(message)
}