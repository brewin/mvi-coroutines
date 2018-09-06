package com.github.brewin.mvicoroutines.view.base

import androidx.annotation.MainThread
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
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

@Suppress("UNCHECKED_CAST")
inline fun <reified VM : ViewModel> Fragment.machineProvider(
    mode: LazyThreadSafetyMode = LazyThreadSafetyMode.NONE,
    crossinline provider: () -> VM
) = lazy(mode) {
    ViewModelProviders.of(this, object : ViewModelProvider.Factory {
        override fun <T1 : ViewModel> create(aClass: Class<T1>) = provider() as T1
    }).get(VM::class.java)
}

abstract class Machine<I : Intent, T : Task, S : State>(initialState: S) : ViewModel() {

    // Handles intents (eg. button clicks)
    private val intents = actor<I>(CommonPool, Channel.CONFLATED) {
        consumeEach { tasks.send(taskFromIntent(lastState(), it)) }
    }

    // Handles tasks of intents
    private val tasks = actor<T>(CommonPool, Channel.CONFLATED) {
        consumeEach { state.send(stateFromTask(lastState(), it)) }
    }

    // Broadcasts state changes to subscribers
    private val state = ConflatedBroadcastChannel(initialState)
    private var stateSub: ReceiveChannel<S>? = null

    private var renderJob: Job? = null

    protected abstract suspend fun taskFromIntent(lastState: S, intent: I): T

    protected abstract suspend fun stateFromTask(lastState: S, task: T): S

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