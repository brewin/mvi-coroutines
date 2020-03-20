package com.github.brewin.mvi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

abstract class Machine<EVENT, UPDATE, STATE>(
    initialState: STATE
) : ViewModel() {

    private val _events = Channel<EVENT>(Channel.CONFLATED)
    val events: SendChannel<EVENT> get() = _events

    private val updateFlows = Channel<Flow<UPDATE>>(Channel.UNLIMITED)

    private val _states = ConflatedBroadcastChannel(initialState)
    val states get() = _states.asFlow()

    val state get() = _states.value

    init {
        viewModelScope.launch {
            _events.consumeEach { event ->
                updateFlows.send(handleEvent(event))
            }
        }
        viewModelScope.launch {
            updateFlows.consumeEach { updates ->
                updates.collect { update ->
                    _states.send(updateState(update))
                }
            }
        }
    }

    protected abstract fun handleEvent(event: EVENT): Flow<UPDATE>

    protected abstract fun updateState(update: UPDATE): STATE
}