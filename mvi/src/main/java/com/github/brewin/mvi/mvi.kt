package com.github.brewin.mvi

import android.os.Parcelable
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*
import kotlin.coroutines.CoroutineContext

// or UiEvent, or Action
interface UiEvent

// or UiState
interface UiState : Parcelable

// or Change, or Result
interface Update

// or StateScope, or StateStore
abstract class MviMachine<E : UiEvent, S : UiState>(
    initialState: S,
    private val defaultStateConstructor: (S) -> S
) : ViewModel(), CoroutineScope {

    override val coroutineContext: CoroutineContext = Job() + Dispatchers.IO

    private var _state = initialState
    val state get() = _state
    val stateAsDefault get() = defaultStateConstructor(_state)

    private val _events = Channel<E>(Channel.CONFLATED)
    val intents: SendChannel<E> get() = _events

    private val updateProducers = Channel<ReceiveChannel<Update>>(Channel.UNLIMITED)

    /*
     * Normally a ConflatedBroadcastChannel would be used for state changes, but since the renderer
     * needs to receive each and every state in order to update the view according to the state
     * type, an ArrayBroadcastChannel must be used instead.
     */
    private val _states = BroadcastChannel<S>(1)
    val states: ReceiveChannel<S>
        get() = _states.openSubscription().also { _states.offer(stateAsDefault) }

    init {
        launch {
            _events.consumeEach {
                updateProducers.send(handle(it))
            }
        }
        launch {
            updateProducers.consumeEach { updates ->
                launch {
                    updates.consumeEach {
                        _state = reduce(it)
                        _states.send(_state)
                    }
                }
            }
        }
    }

    protected abstract fun handle(event: E): ReceiveChannel<Update>

    protected abstract fun reduce(update: Update): S

    override fun onCleared() {
        super.onCleared()
        coroutineContext.cancel()
    }
}