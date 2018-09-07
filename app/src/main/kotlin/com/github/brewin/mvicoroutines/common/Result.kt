package com.github.brewin.mvicoroutines.common

@Suppress("unused")
sealed class Result<out T>

data class Success<out T>(val value: T) : Result<T>()
data class Failure(val exception: Exception) : Result<Nothing>()

inline fun <T> resultOf(block: () -> T): Result<T> = try {
    Success(block())
} catch (e: Exception) {
    Failure(e)
}