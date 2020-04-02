package com.github.brewin.mvicoroutines.presentation.arch

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*


abstract class Machine<I : Machine.Input, S : Machine.State, E : Machine.Effect>(
    events: Flow<I>,
    initialState: S
) {

    interface Input

    interface Output
    interface State : Output
    interface Effect : Output

    private var _state = initialState
    val state get() = _state

    val states: Flow<S> = events
        .flowOn(Dispatchers.Main)
        .flatMapMerge { it.process() }
        .filterIsInstance<State>()
        .map { (it as? S) ?: throw IllegalStateException("Invalid State") }
        .onEach { _state = it }
        .onStart { emit(initialState) }
        .flowOn(Dispatchers.IO)

    val effects: Flow<E> = events
        .flowOn(Dispatchers.Main)
        .flatMapMerge { it.process() }
        .filterIsInstance<Effect>()
        .map { (it as? E) ?: throw IllegalStateException("Invalid Effect") }
        .flowOn(Dispatchers.IO)

    protected abstract fun I.process(): Flow<Output>
}