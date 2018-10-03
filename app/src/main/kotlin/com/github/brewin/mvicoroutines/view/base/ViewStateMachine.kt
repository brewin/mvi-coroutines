package com.github.brewin.mvicoroutines.view.base

import android.os.Parcelable
import androidx.annotation.MainThread
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import kotlinx.android.parcel.Parcelize
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*
import java.util.*
import kotlin.coroutines.CoroutineContext

inline class Task<T>(val value: Deferred<TaskState<T>>)

sealed class TaskState<T>
class Started<T> : TaskState<T>()
data class Success<T>(val value: T) : TaskState<T>()
data class Failure<T>(val error: Throwable) : TaskState<T>()

@Parcelize
open class ViewState : Parcelable

interface ViewStateSubscriber<S : ViewState> {
    @MainThread
    fun onNewState(old: S?, new: S)
}

/*
 * NOTE: Not all states are guaranteed to be sent to subscribers, only the most recent.
 */
abstract class ViewStateMachine<S : ViewState>(
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

    private val subscriptions = mutableMapOf<ViewStateSubscriber<S>, ReceiveChannel<S>>()

    //val state: S
    //    get() = broadcast.deferred

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

    fun <T> Task<T>.start(reducer: S.(TaskState<T>) -> S) = start({ it }, reducer)

    fun <T, V> Task<T>.start(
        mapper: (TaskState<T>) -> TaskState<V>,
        reducer: S.(TaskState<V>) -> S
    ) = launch(Dispatchers.IO) {
        this@ViewStateMachine.sendState { reducer(Started()) }
        val completed = mapper(value.await())
        this@ViewStateMachine.sendState { reducer(completed) }
    }

    fun <T> task(block: suspend CoroutineScope.() -> T): Task<T> = Task(
        async(Dispatchers.IO) {
            try {
                Success(block())
            } catch (e: Exception) {
                Failure<T>(e)
            }
        }
    )

    override fun onCleared() {
        coroutineContext.cancelChildren()
        broadcast.close()
        actor.close()
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