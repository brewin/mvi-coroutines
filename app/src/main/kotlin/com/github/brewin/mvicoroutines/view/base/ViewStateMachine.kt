package com.github.brewin.mvicoroutines.view.base

import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*
import java.util.*
import kotlin.coroutines.CoroutineContext

@Suppress("UNCHECKED_CAST")
inline fun <reified M : ViewStateMachine<*>> Fragment.machineProvider(
    crossinline provider: () -> M
) = ViewModelProviders.of(this, object : ViewModelProvider.Factory {
    override fun <T1 : ViewModel> create(aClass: Class<T1>) = provider() as T1
}).get(M::class.java)

abstract class ViewStateMachine<S : ViewState>(
    initialState: S,
    override val coroutineContext: CoroutineContext = Job()
) : ViewModel(), CoroutineScope {

    sealed class Msg<S> {
        class SendState<S>(val reducer: S.() -> S) : Msg<S>()
        class WithState<S>(val block: S.() -> Unit) : Msg<S>()
    }

    private val broadcast = ConflatedBroadcastChannel(initialState)

    private val actor = actor<Msg<S>>(Dispatchers.IO, Channel.UNLIMITED) {
        val withBlocks = ArrayDeque<S.() -> Unit>()
        consumeEach {
            when (it) {
                is Msg.SendState -> broadcast.offer(it.reducer(broadcast.value))
                is Msg.WithState -> withBlocks.offer(it.block)
            }
            while (isEmpty) withBlocks.poll()?.invoke(broadcast.value) ?: break
        }
    }

    private val subscriptions = mutableMapOf<ViewStateSubscriber<S>, ReceiveChannel<S>>()

    //val state: S
    //    get() = broadcast.value

    fun addSubscriber(subscriber: ViewStateSubscriber<S>) {
        subscriptions[subscriber] = broadcast.openSubscription().apply {
            launch(Dispatchers.Main) {
                var old: S? = null
                consumeEach {
                    subscriber.onNewState(old, it)
                    old = it
                }
            }
        }
    }

    fun removeSubscriber(subscriber: ViewStateSubscriber<S>) {
        subscriptions[subscriber]?.cancel()
        subscriptions.remove(subscriber)
    }

    fun withState(block: S.() -> Unit) {
        actor.offer(Msg.WithState(block))
    }

    fun sendState(reducer: S.() -> S) {
        actor.offer(Msg.SendState(reducer))
    }

    fun <T> task(block: suspend CoroutineScope.() -> T) = ViewStateTask(this, block)

    override fun onCleared() {
        coroutineContext.cancelChildren()
        broadcast.close()
        actor.close()
        super.onCleared()
    }
}