package com.github.brewin.mvicoroutines.presentation.arch

import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.*

// TODO: Don't use ViewModel. Make it multiplatform ready.
abstract class Machine<I : Machine.Input, S : Machine.State, E : Machine.Effect>(
    initialState: S
) : ViewModel() {

    interface Input
    interface Output

    interface State : Output
    interface Effect : Output

    private val _states = ConflatedBroadcastChannel(initialState)
    val states = _states.asFlow()
    val state get() = _states.value

    private val _effects = BroadcastChannel<E>(Channel.BUFFERED)
    val effects = _effects.asFlow()

    @Suppress("UNCHECKED_CAST")
    fun start(inputs: Flow<I>): Job =
        inputs
            .flowOn(Dispatchers.Main)
            .flatMapConcat { it.process() }
            .onEach {
                when (it) {
                    is State -> _states.send(it as S)
                    is Effect -> _effects.send(it as E)
                }
            }
            .flowOn(Dispatchers.Default)
            .launchIn(viewModelScope)

    protected abstract fun I.process(): Flow<Output>
}

@Suppress("UNCHECKED_CAST")
inline fun <reified VM : ViewModel> Fragment.provideMachine(
    crossinline provider: () -> VM
) = ViewModelProvider(this, object : ViewModelProvider.Factory {
    override fun <T1 : ViewModel> create(aClass: Class<T1>) = provider() as T1
}).get(VM::class.java)