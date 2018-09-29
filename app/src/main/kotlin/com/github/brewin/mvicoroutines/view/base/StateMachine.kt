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

sealed class Task<V>
class Loading<V> : Task<V>()
data class Success<V>(val value: V) : Task<V>()
data class Failure<V>(val error: Exception) : Task<V>()

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

    var state = initialState

    private val broadcast = ArrayBroadcastChannel<S>(256)
        .apply { offer(state) }

    private val actor = actor<Msg<S>>(Dispatchers.IO, Channel.UNLIMITED) {
        val withBlocks = ArrayDeque<S.() -> Unit>()
        consumeEach {
            when (it) {
                is Msg.SendState -> broadcast.send(it.reducer(state))
                is Msg.WithState -> withBlocks.offer(it.block)
            }
            while (isEmpty) withBlocks.poll()?.invoke(state) ?: break
        }
    }

    private val subscriptions = mutableMapOf<StateSubscriber<S>, ReceiveChannel<S>>()

    fun addSubscriber(subscriber: StateSubscriber<S>) {
        subscriptions[subscriber] = broadcast.openSubscription().apply {
            launch(Dispatchers.Main) {
                var old: S? = null
                consumeEach {
                    state = it
                    subscriber.onNewState(old, state)
                    old = state
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

    private suspend fun sendState(reducer: S.() -> S) {
        actor.send(Msg.SendState(reducer))
    }

    fun <T> Deferred<T>.sendState(reducer: S.(Task<T>) -> S) =
        sendState({ it }, reducer)

    fun <T, V> Deferred<T>.sendState(mapper: (T) -> V, reducer: S.(Task<V>) -> S) =
        launch(Dispatchers.IO) {
            this@StateMachine.sendState { reducer(Loading()) }
            val task: Task<V> = try {
                Success(mapper(await()))
            } catch (e: Exception) {
                Failure(e)
            }
            this@StateMachine.sendState { reducer(task) }
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