package com.github.brewin.mvicoroutines.view.base

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*
import java.util.*
import kotlin.coroutines.CoroutineContext

abstract class StateMachine<SS : StateStatus<S>, S : State>(
    initialStateEvent: SS
) : ViewModel(), CoroutineScope {

    override val coroutineContext: CoroutineContext = Job()

    sealed class StateMsg<SS, S> {
        class SendState<SS, S>(val reducer: S.() -> SS) : StateMsg<SS, S>()
        class WithState<SS, S>(val block: S.() -> Unit) : StateMsg<SS, S>()
    }

    private val broadcast = ConflatedBroadcastChannel(initialStateEvent)

    private val stateMsgChannel = actor<StateMsg<SS, S>>(Dispatchers.IO, Channel.UNLIMITED) {
        val withBlocks = ArrayDeque<S.() -> Unit>()
        consumeEach {
            when (it) {
                is StateMsg.SendState -> broadcast.offer(it.reducer(broadcast.value.state))
                is StateMsg.WithState -> withBlocks.offer(it.block)
            }
            while (isEmpty) withBlocks.poll()?.invoke(broadcast.value.state) ?: break
        }
    }

    private val subscriptions = mutableMapOf<StateStatusSubscriber<SS, S>, ReceiveChannel<SS>>()

    //val state: S
    //    get() = broadcast.value.state

    fun addSubscriber(subscriber: StateStatusSubscriber<SS, S>) {
        subscriptions[subscriber] = broadcast.openSubscription().apply {
            launch(Dispatchers.Main) {
                consumeEach(subscriber::onStateStatus)
            }
        }
    }

    fun removeSubscriber(subscriber: StateStatusSubscriber<SS, S>) {
        subscriptions[subscriber]?.cancel()
        subscriptions.remove(subscriber)
    }

    fun withState(block: S.() -> Unit) {
        stateMsgChannel.offer(StateMsg.WithState(block))
    }

    fun sendStateReducer(reducer: S.() -> SS) {
        stateMsgChannel.offer(StateMsg.SendState(reducer))
    }

    override fun onCleared() {
        coroutineContext.cancelChildren()
        broadcast.close()
        stateMsgChannel.close()
        super.onCleared()
    }
}