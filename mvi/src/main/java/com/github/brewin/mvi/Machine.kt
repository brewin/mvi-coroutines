package com.github.brewin.mvi

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*
import kotlin.coroutines.CoroutineContext

abstract class Machine<E : Machine.Event, U : Machine.Update, S : Machine.State>(
    initialState: S
) : ViewModel(), CoroutineScope {

    interface Event
    interface Update
    interface State

    override val coroutineContext: CoroutineContext = SupervisorJob() + Dispatchers.IO

    private val _events = Channel<E>(Channel.CONFLATED)
    val events: SendChannel<E> get() = _events

    private val updateChannels = Channel<ReceiveChannel<U>>(Channel.UNLIMITED)

    private val _states = ConflatedBroadcastChannel(initialState)
    val states: ReceiveChannel<S> get() = _states.openSubscription()

    val state get() = _states.value

    init {
        launch {
            _events.consumeEach {
                updateChannels.send(handleEvent(it))
            }
        }
        launch {
            updateChannels.consumeEach { updates ->
                launch {
                    updates.consumeEach {
                        _states.send(updateState(it))
                    }
                }
            }
        }
    }

    protected abstract fun handleEvent(event: E): ReceiveChannel<U>

    protected abstract fun updateState(update: U): S

    override fun onCleared() {
        super.onCleared()
        coroutineContext.cancel()
    }
}