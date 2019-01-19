package com.github.brewin.mvi

import android.os.Parcelable
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*
import kotlin.coroutines.CoroutineContext

// or UiEvent, or Action
interface MviIntent

// or UiState
interface MviState : Parcelable

// or Change, or Result
interface MviUpdate

// or StateScope, or StateStore
abstract class MviMachine<I : MviIntent, S : MviState>(
    initialState: S,
    private val defaultStateConstructor: (S) -> S
) : ViewModel(), CoroutineScope {

    override val coroutineContext: CoroutineContext = Job() + Dispatchers.IO

    private var _state = initialState
    val state get() = _state
    val stateAsDefault get() = defaultStateConstructor(_state)

    private val _intents = Channel<I>(Channel.CONFLATED)
    val intents: SendChannel<I> get() = _intents

    private val updateProducers = Channel<ReceiveChannel<MviUpdate>>(Channel.UNLIMITED)

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
            _intents.consumeEach {
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

    protected abstract fun handle(intent: I): ReceiveChannel<MviUpdate>

    protected abstract fun reduce(update: MviUpdate): S

    override fun onCleared() {
        super.onCleared()
        coroutineContext.cancel()
    }
}