package com.github.brewin.mvi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

abstract class Machine<E : Machine.Event, U : Machine.Update, S : Machine.State>(
    initialState: S
) : ViewModel() {

    interface Event
    interface Update
    interface State

    private val _events = Channel<E>(Channel.CONFLATED)
    val events: SendChannel<E> get() = _events

    private val updateFlows = Channel<Flow<U>>(Channel.UNLIMITED)

    private val _states = ConflatedBroadcastChannel(initialState)
    val states: ReceiveChannel<S> get() = _states.openSubscription()

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

    protected abstract fun handleEvent(event: E): Flow<U>

    protected abstract fun updateState(update: U): S
}