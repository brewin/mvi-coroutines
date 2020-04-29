package com.github.brewin.mvicoroutines.domain

sealed class Either<out L, out R>
data class Left<out L>(val value: L) : Either<L, Nothing>()
data class Right<out R>(val value: R) : Either<Nothing, R>()

inline fun <L, R, T> Either<L, R>.fold(onLeft: (L) -> T, onRight: (R) -> T) =
    when (this) {
        is Left -> onLeft(value)
        is Right -> onRight(value)
    }

val <T> T.asLeft: Left<T>
    get() = Left(this)

val <T> T.asRight: Right<T>
    get() = Right(this)