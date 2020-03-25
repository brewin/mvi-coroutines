package com.github.brewin.mvicoroutines.presentation.arch

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*

abstract class Machine<EVENT, STATE>(events: Flow<EVENT>, initialState: STATE) {

    private var _state = initialState
    val state get() = _state

    val states = events
        .flowOn(Dispatchers.Main)
        .flatMapMerge { it.toState() }
        .onEach { _state = it }
        .onStart { emit(initialState) }
        .flowOn(Dispatchers.IO)

    protected abstract fun EVENT.toState(): Flow<STATE>
}