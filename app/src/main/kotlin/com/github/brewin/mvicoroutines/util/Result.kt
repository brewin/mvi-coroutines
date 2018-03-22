package com.github.brewin.mvicoroutines.util

@Suppress("unused")
sealed class Result<out E, out T>

sealed class Failure<out E> : Result<E, Nothing>() {
    data class Known<out E : Throwable>(val error: E) : Failure<E>()
    data class Unknown(val exception: Throwable) : Failure<Nothing>()
}

data class Success<out T>(val value: T) : Result<Nothing, T>()

inline fun <reified E : Throwable, T> resultOf(block: () -> T): Result<E, T> = try {
    Success(block())
} catch (e: Throwable) {
    if (e is E) Failure.Known(e) else Failure.Unknown(e)
}