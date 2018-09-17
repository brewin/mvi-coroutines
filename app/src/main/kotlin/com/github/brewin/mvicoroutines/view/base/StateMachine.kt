package com.github.brewin.mvicoroutines.view.base

import androidx.annotation.MainThread
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import kotlinx.coroutines.*
import kotlinx.coroutines.android.Main
import kotlinx.coroutines.channels.*
import java.util.*
import kotlin.coroutines.CoroutineContext

interface State

interface StateSubscriber<S : State> {
    @MainThread
    fun onNewState(old: S?, new: S)
}

abstract class StateMachine<S : State>(
    initialState: S,
    final override val coroutineContext: CoroutineContext = Job()
) : ViewModel(), CoroutineScope {

    sealed class Msg<S> {
        class SetState<S>(val block: suspend S.() -> S) : Msg<S>()
        class GetState<S>(val block: S.() -> Unit) : Msg<S>()
    }

    private val broadcast = ConflatedBroadcastChannel(initialState)
    private val subscribers = mutableMapOf<StateSubscriber<S>, ReceiveChannel<S>>()

    private val actor = actor<Msg<S>>(Dispatchers.IO, Channel.UNLIMITED) {
        val getBlocks = ArrayDeque<S.() -> Unit>()
        consumeEach { msg ->
            when (msg) {
                is Msg.SetState -> broadcast.send(msg.block(broadcast.value))
                is Msg.GetState -> getBlocks.add(msg.block)
            }
            if (isEmpty) {
                getBlocks.forEach { it(broadcast.value) }
                getBlocks.clear()
            }
        }
    }

    fun subscribe(subscriber: StateSubscriber<S>) {
        subscribers[subscriber] = broadcast.openSubscription()
            .apply {
                launch(Dispatchers.Main) {
                    var oldState: S? = null
                    consumeEach { newState ->
                        subscriber.onNewState(oldState, newState)
                        oldState = newState
                    }
                }
            }
    }

    fun unsubscribe(subscriber: StateSubscriber<S>) {
        subscribers[subscriber]?.cancel()
        subscribers.remove(subscriber)
    }

    fun withState(block: S.() -> Unit) {
        actor.offer(Msg.GetState(block))
    }

    fun setState(block: suspend S.() -> S) {
        actor.offer(Msg.SetState(block))
    }

    override fun onCleared() {
        broadcast.close()
        coroutineContext.cancel()
        super.onCleared()
    }
}

@Suppress("UNCHECKED_CAST")
inline fun <reified VM : ViewModel> Fragment.machineProvider(
    mode: LazyThreadSafetyMode = LazyThreadSafetyMode.NONE,
    crossinline provider: () -> VM
) = lazy(mode) {
    ViewModelProviders.of(this, object : ViewModelProvider.Factory {
        override fun <T1 : ViewModel> create(aClass: Class<T1>) = provider() as T1
    }).get(VM::class.java)
}