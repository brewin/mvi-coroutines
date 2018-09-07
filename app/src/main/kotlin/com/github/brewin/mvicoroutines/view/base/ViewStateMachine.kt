package com.github.brewin.mvicoroutines.view.base

import androidx.annotation.MainThread
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import kotlinx.coroutines.experimental.IO
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.experimental.channels.ReceiveChannel
import kotlinx.coroutines.experimental.channels.consumeEach
import kotlinx.coroutines.experimental.channels.distinct
import kotlinx.coroutines.experimental.launch

interface ViewState

interface ViewStateSubscriber<VS : ViewState> {
    @MainThread
    fun onNewState(old: VS, new: VS)
}

abstract class ViewStateMachine<VS : ViewState>(initialState: VS) : ViewModel() {

    private val stateChannel = ConflatedBroadcastChannel(initialState)
    private val subscribers = mutableMapOf<ViewStateSubscriber<VS>, ReceiveChannel<VS>>()

    fun subscribe(subscriber: ViewStateSubscriber<VS>) {
        subscribers[subscriber] = stateChannel.openSubscription()
            .also {
                var old = stateChannel.value
                launch(UI) {
                    it.consumeEach { new ->
                        subscriber.onNewState(old, new)
                        old = new
                    }
                }
            }
    }

    fun unsubscribe(subscriber: ViewStateSubscriber<VS>) {
        subscribers[subscriber]?.cancel()
        subscribers.remove(subscriber)
    }

    override fun onCleared() {
        subscribers.forEach { it.value.cancel() }
        super.onCleared()
    }

    fun newState(block: suspend VS.() -> VS) = launch(IO) {
        stateChannel.send(block(stateChannel.value))
    }

    fun withState(block: VS.() -> Unit) {
        block(stateChannel.value)
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