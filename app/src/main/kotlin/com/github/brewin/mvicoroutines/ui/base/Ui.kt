package com.github.brewin.mvicoroutines.ui.base

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

interface UiRenderer<A : UiAction, R : UiResult, S : UiState> {
    val ui: Ui<A, R, S>
    @MainThread
    fun render(state: S)
}

interface UiAction
interface UiResult
interface UiState

abstract class Ui<A : UiAction, R : UiResult, S : UiState>(initialState: S) : ViewModel() {

    // Handles actions (aka intents/events) (eg. button clicks)
    private val actions = actor<A>(CommonPool, Channel.CONFLATED) {
        consumeEach { results.send(resultFromAction(it)) }
    }

    // Handles results of actions

    private val results = actor<R>(CommonPool, Channel.CONFLATED) {
        consumeEach { state.send(stateFromResult(it)) }
    }

    // Broadcasts state changes to subscribers
    private val state = ConflatedBroadcastChannel(initialState)
    private var stateSub: ReceiveChannel<S>? = null

    private var renderJob: Job? = null

    // Reduce a UiAction to a UiResult
    protected abstract suspend fun resultFromAction(action: A): R

    // Reduce a UiResult to a UiState
    protected abstract suspend fun stateFromResult(result: R): S

    fun startRendering(renderer: UiRenderer<A, R, S>) {
        stopRendering()
        stateSub = state.openSubscription().also {
            renderJob = launch(UI) { it.consumeEach(renderer::render) }
        }
    }

    fun stopRendering() {
        renderJob?.cancel()
        stateSub?.cancel()
    }

    fun lastState() = state.valueOrNull

    fun offerAction(action: A) = actions.offer(action)

    override fun onCleared() {
        super.onCleared()
        stopRendering()
        Timber.d("ViewModel::onCleared")
    }
}

@Suppress("UNCHECKED_CAST")
inline fun <reified VM : ViewModel> Fragment.uiProvider(
    mode: LazyThreadSafetyMode = LazyThreadSafetyMode.NONE,
    crossinline provider: () -> VM
) = lazy(mode) {
    ViewModelProviders.of(this, object : ViewModelProvider.Factory {
        override fun <T1 : ViewModel> create(aClass: Class<T1>) = provider() as T1
    }).get(VM::class.java)
}