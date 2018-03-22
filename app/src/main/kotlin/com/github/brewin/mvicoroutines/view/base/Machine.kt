package com.github.brewin.mvicoroutines.view.base

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.support.annotation.MainThread
import android.support.v4.app.Fragment
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.channels.*
import kotlinx.coroutines.experimental.launch
import timber.log.Timber

interface Renderer<I : Intent, T : Task, S : State> {
    val machine: Machine<I, T, S>
    @MainThread
    fun render(state: S)
}

interface Intent
interface Task
interface State

abstract class Machine<I : Intent, T : Task, S : State>(initialState: S) : ViewModel() {

    // Handles intents (eg. button clicks)
    private val intents = actor<I>(CommonPool, Channel.CONFLATED) {
        consumeEach { tasks.send(taskFromIntent(it)) }
    }

    // Handles tasks of intents
    private val tasks = actor<T>(CommonPool, Channel.CONFLATED) {
        consumeEach { state.send(stateFromTask(it)) }
    }

    // Broadcasts state changes to subscribers
    private val state = ConflatedBroadcastChannel(initialState)
    private var stateSub: ReceiveChannel<S>? = null

    private var renderJob: Job? = null

    protected abstract suspend fun taskFromIntent(action: I): T

    protected abstract suspend fun stateFromTask(result: T): S

    fun startRendering(renderer: Renderer<I, T, S>) {
        stopRendering()
        stateSub = state.openSubscription().also {
            renderJob = launch(UI) { it.consumeEach(renderer::render) }
        }
    }

    fun stopRendering() {
        renderJob?.cancel()
        stateSub?.cancel()
    }

    fun lastState() = state.value

    fun offerIntent(intent: I) = intents.offer(intent)

    override fun onCleared() {
        super.onCleared()
        stopRendering()
        Timber.d("onCleared() called")
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