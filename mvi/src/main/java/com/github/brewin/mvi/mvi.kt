package com.github.brewin.mvi

import android.os.Parcelable
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*
import kotlin.coroutines.CoroutineContext

interface UiEvent
interface UseCaseUpdate
interface UiState : Parcelable

abstract class Machine<E : UiEvent, S : UiState>(
    initialState: S,
    private val defaultStateConstructor: (S) -> S
) : ViewModel(), CoroutineScope {

    override val coroutineContext: CoroutineContext = Job() + Dispatchers.IO

    private var _state = initialState
    val state get() = _state
    val stateAsDefault get() = defaultStateConstructor(_state)

    private val _events = Channel<E>(Channel.CONFLATED)
    val events: SendChannel<E> get() = _events

    private val useCases = Channel<ReceiveChannel<UseCaseUpdate>>(Channel.UNLIMITED)

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
                useCases.send(handleEvent(it))
            }
        }
        launch {
            useCases.consumeEach { updates ->
                launch {
                    updates.consumeEach {
                        _state = updateState(it)
                        _states.send(_state)
                    }
                }
            }
        }
    }

    protected abstract fun handleEvent(event: E): ReceiveChannel<UseCaseUpdate>

    protected abstract fun updateState(update: UseCaseUpdate): S

    override fun onCleared() {
        super.onCleared()
        coroutineContext.cancel()
    }
}