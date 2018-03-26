package com.github.brewin.mvicoroutines.common

import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.async

@Suppress("unused")
sealed class Result<out E, out T>

data class Success<out T>(val value: T) : Result<Nothing, T>()

sealed class Failure<out E> : Result<E, Nothing>() {
    data class Known<out E>(val error: E) : Failure<E>()
    data class Unknown(val exception: Throwable) : Failure<Nothing>()
}

inline fun <reified E, T> resultOf(block: () -> T): Result<E, T> = try {
    Success(block())
} catch (e: Throwable) {
    if (e is E)
        Failure.Known(e)
    else
        Failure.Unknown(e)
}

inline fun <reified E, T> deferredResultOf(
    noinline block: suspend () -> T
): Deferred<Result<E, T>> = async {
    resultOf<E, T> { block() }
}