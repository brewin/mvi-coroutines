package com.github.brewin.mvi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.flow.*

// TODO: Use DataFlow when available.
// https://github.com/Kotlin/kotlinx.coroutines/pull/1354

abstract class Machine<EVENT, UPDATE, STATE>(initialState: STATE) : ViewModel() {

    private val _events = Channel<EVENT>(Channel.CONFLATED)
    val events: SendChannel<EVENT> = _events

    private val _states = ConflatedBroadcastChannel(initialState)
    val states = _states.asFlow()

    val state get() = _states.value

    init {
        _events.consumeAsFlow()
            .flatMapConcat { it.toUpdateFlow() }
            .map { it.toState() }
            .onEach { _states.send(it) }
            .flowOn(Dispatchers.IO)
            .launchIn(viewModelScope)
    }

    protected abstract fun EVENT.toUpdateFlow(): Flow<UPDATE>

    protected abstract fun UPDATE.toState(): STATE
}