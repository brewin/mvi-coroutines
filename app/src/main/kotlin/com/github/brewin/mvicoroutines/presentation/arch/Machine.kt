package com.github.brewin.mvicoroutines.presentation.arch

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import timber.log.Timber


abstract class Machine<I : Machine.Input, S : Machine.State, E : Machine.Effect>(
    inputs: Flow<I>,
    initialState: S
) {

    interface Input

    interface Output
    interface State : Output
    interface Effect : Output

    private var _state = initialState
    val state get() = _state

    val outputs: Flow<Output> = inputs
        .flowOn(Dispatchers.Main)
        .flatMapConcat { it.process() }
        .onStart { emit(initialState) }
        .onEach { if (it is State) _state = it as S }
        .flowOn(Dispatchers.Default)

    protected abstract fun I.process(): Flow<Output>
}