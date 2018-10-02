package com.github.brewin.mvicoroutines.view.base

import android.os.Parcelable
import androidx.annotation.MainThread
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import kotlinx.android.parcel.Parcelize
import kotlinx.coroutines.*
import kotlinx.coroutines.android.Main
import kotlinx.coroutines.channels.*
import java.util.*
import kotlin.coroutines.CoroutineContext

sealed class AsyncTry<T>
class Loading<T> : AsyncTry<T>()
data class Success<T>(val value: T) : AsyncTry<T>()
data class Failure<T>(val error: Throwable) : AsyncTry<T>()

@Parcelize
open class State : Parcelable

interface StateSubscriber<S : State> {
    @MainThread
    fun onNewState(old: S?, new: S)
}

/*
 * NOTE: Not all states are guaranteed to be sent to subscribers, only the most recent.
 */
abstract class StateMachine<S : State>(
    initialState: S,
    final override val coroutineContext: CoroutineContext = Job()
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

    private val subscriptions = mutableMapOf<StateSubscriber<S>, ReceiveChannel<S>>()

    //val state: S
    //    get() = broadcast.value

    fun addSubscriber(subscriber: StateSubscriber<S>) {
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

    fun removeSubscriber(subscriber: StateSubscriber<S>) {
        subscriptions[subscriber]?.cancel()
        subscriptions.remove(subscriber)
    }

    fun withState(block: S.() -> Unit) {
        actor.offer(Msg.WithState(block))
    }

    fun sendState(reducer: S.() -> S) {
        actor.offer(Msg.SendState(reducer))
    }

    fun <T> Deferred<AsyncTry<T>>.sendState(reducer: S.(AsyncTry<T>) -> S) =
        sendState({ it }, reducer)

    fun <T, V> Deferred<AsyncTry<T>>.sendState(
        mapper: (AsyncTry<T>) -> AsyncTry<V>,
        reducer: S.(AsyncTry<V>) -> S
    ) = launch(Dispatchers.IO) {
        this@StateMachine.sendState { reducer(Loading()) }
        val completed = mapper(await())
        this@StateMachine.sendState { reducer(completed) }
    }

    fun <T> asyncTry(block: suspend CoroutineScope.() -> T): Deferred<AsyncTry<T>> =
        async(Dispatchers.IO) {
            try {
                Success(block())
            } catch (e: Exception) {
                Failure<T>(e)
            }
        }

    override fun onCleared() {
        actor.close()
        broadcast.close()
        coroutineContext.cancelChildren()
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