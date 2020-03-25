package com.github.brewin.mvicoroutines.presentation.arch

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*

abstract class Machine<EVENT, UPDATE, STATE>(events: Flow<EVENT>, initialState: STATE) {

    private var _state = initialState
    val state get() = _state

    val states = events
        .flowOn(Dispatchers.Main)
        .flatMapMerge { it.toUpdates() }
        .flowOn(Dispatchers.IO)
        .map { it.toState() }
        .onEach { _state = it }
        .onStart { emit(initialState) }
        .flowOn(Dispatchers.Default)

    protected abstract fun EVENT.toUpdates(): Flow<UPDATE>

    protected abstract fun UPDATE.toState(): STATE
}