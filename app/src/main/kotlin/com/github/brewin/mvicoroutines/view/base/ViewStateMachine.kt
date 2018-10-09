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
import timber.log.Timber
import java.util.*
import kotlin.coroutines.CoroutineContext

@Parcelize
open class ViewState : Parcelable

interface ViewStateSubscriber<S : ViewState> {
    @MainThread
    fun onNewState(old: S?, new: S)
}

class ViewStateTask<S : ViewState, T>(
    private val machine: ViewStateMachine<S>,
    private val block: suspend CoroutineScope.() -> T
) {

    private var onStarted: (S.() -> S)? = null
    private var onFailure: (S.(error: Exception) -> S)? = null
    private var onSuccess: (S.(value: T) -> S)? = null
    private var onCompleted: (S.() -> S)? = null

    fun onStarted(reducer: S.() -> S): ViewStateTask<S, T> {
        onStarted = reducer
        return this
    }

    fun onFailure(reducer: S.(error: Exception) -> S): ViewStateTask<S, T> {
        onFailure = reducer
        return this
    }

    fun onSuccess(reducer: S.(value: T) -> S): ViewStateTask<S, T> {
        onSuccess = reducer
        return this
    }

    fun onCompleted(reducer: S.() -> S): ViewStateTask<S, T> {
        onCompleted = reducer
        return this
    }

    fun start(): Job = launch(machine.coroutineContext + Dispatchers.IO) {
        onStarted?.let(machine::sendState)
        try {
            val value = block()
            onSuccess?.let { machine.sendState { it(value) } }
        } catch (error: Exception) {
            onFailure?.let { machine.sendState { it(error) } }
            Timber.w(error)
        }
        onCompleted?.let(machine::sendState)
    }
}

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

    fun <T> task(block: suspend CoroutineScope.() -> T): ViewStateTask<S, T> =
        ViewStateTask(this, block)

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