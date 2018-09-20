package com.github.brewin.mvicoroutines.common

import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async

@Suppress("unused")
sealed class Result<out T> {

    fun <T : Any> Result<T>.getOrNull(): T? = (this as? Success)?.value

    fun <T : Any> Result<T>.getOrDefault(default: T): T = getOrNull() ?: default

    fun <T : Any> Result<T>.getOrElse(block: () -> T): T = getOrNull() ?: block()

    fun <T : Any> Result<T>.getOrThrow(throwable: Throwable? = null): T = when (this) {
        is Success -> value
        is Failure -> throw throwable ?: error
    }
}

data class Success<out T>(val value: T) : Result<T>()
data class Failure(val error: Exception) : Result<Nothing>()

inline fun <T> resultOf(block: () -> T): Result<T> = try {
    Success(block())
} catch (e: Exception) {
    Failure(e)
}

fun <T> asyncResultOf(block: suspend () -> T): Deferred<Result<T>> = async {
    try {
        Success(block())
    } catch (e: Exception) {
        Failure(e)
    }
}