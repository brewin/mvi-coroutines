package com.github.brewin.mvicoroutines.domain

sealed class Either<out L, out R>
data class Left<out L>(val value: L) : Either<L, Nothing>()
data class Right<out R>(val value: R) : Either<Nothing, R>()

fun <L, R, T> Either<L, R>.fold(onLeft: (L) -> T, onRight: (R) -> T): T =
    when (this) {
        is Left -> onLeft(value)
        is Right -> onRight(value)
    }

suspend fun <L, R, T> Either<L, R>.foldSuspend(
    onLeft: suspend (L) -> T,
    onRight: suspend (R) -> T
): T =
    when (this) {
        is Left -> onLeft(value)
        is Right -> onRight(value)
    }

fun <L, R, T> Either<L, R>.flatMap(f: (R) -> Either<L, T>): Either<L, T> =
    fold({ this as Left }, f)

suspend fun <L, R, T> Either<L, R>.flatMapSuspend(f: suspend (R) -> Either<L, T>): Either<L, T> =
    foldSuspend({ this as Left }, f)

fun <L, R, T> Either<L, R>.map(f: (R) -> T): Either<L, T> =
    flatMap { Right(f(it)) }

suspend fun <L, R, T> Either<L, R>.mapSuspend(f: suspend (R) -> T): Either<L, T> =
    flatMapSuspend { Right(f(it)) }

val <T> T.asLeft: Left<T>
    get() =
        Left(this)

val <T> T.asRight: Right<T>
    get() =
        Right(this)