package com.github.brewin.mvicoroutines.common

@Suppress("unused")
sealed class Result<out T> {

    fun <T : Any> Result<T>.getOrNull(): T? = (this as? Success)?.value

    fun <T : Any> Result<T>.getOrDefault(default: T): T = getOrNull() ?: default

    fun <T : Any> Result<T>.getOrElse(block: () -> T): T = getOrNull() ?: block()

    fun <T : Any> Result<T>.getOrThrow(throwable: Throwable? = null): T = when (this) {
        is Loading -> TODO()
        is Success -> value
        is Failure -> throw throwable ?: error
    }
}

data class Loading(val shouldLoad: Boolean) : Result<Nothing>()
data class Success<out T>(val value: T) : Result<T>()
data class Failure(val error: Exception) : Result<Nothing>()

inline fun <T> resultOf(block: () -> T): Result<T> = try {
    Success(block())
} catch (e: Exception) {
    Failure(e)
}