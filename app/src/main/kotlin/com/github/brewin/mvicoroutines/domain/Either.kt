package com.github.brewin.mvicoroutines.domain

sealed class Either<out L, out R>
data class Left<out L>(val value: L) : Either<L, Nothing>()
data class Right<out R>(val value: R) : Either<Nothing, R>()

val <T> T.asLeft: Left<T>
    get() = Left(this)

val <T> T.asRight: Right<T>
    get() = Right(this)