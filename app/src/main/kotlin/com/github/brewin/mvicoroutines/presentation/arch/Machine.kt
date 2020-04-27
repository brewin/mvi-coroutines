package com.github.brewin.mvicoroutines.presentation.arch

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*

// TODO: Don't use ViewModel. Make it multiplatform ready.
abstract class Machine<INPUT : Any, OUTPUT : Any, STATE : OUTPUT, EFFECT : OUTPUT>(
    initialState: STATE
) : ViewModel() {

    private var mutableState = initialState
    val state get() = mutableState

    // Flow is cached in order to survive configuration changes.
    private var outputs: Flow<OUTPUT> = emptyFlow()

    @Suppress("UNCHECKED_CAST")
    fun outputs(inputs: Flow<INPUT>): Flow<OUTPUT> {
        outputs = flowOf(
            outputs,
            inputs
                .flowOn(Dispatchers.Main)
                .flatMapMerge { input -> input.process() }
                .map { transform -> transform() }
                .onStart { emit(mutableState) }
                .onEach { if (mutableState::class.isInstance(it)) mutableState = it as STATE }
                .flowOn(Dispatchers.Default)
                .broadcastIn(viewModelScope)
                .asFlow()
        ).flattenMerge()

        return outputs
    }

    protected fun update(
        block: suspend FlowCollector<() -> OUTPUT>.() -> Unit
    ): Flow<() -> OUTPUT> = flow(block)

    protected abstract fun INPUT.process(): Flow<() -> OUTPUT>
}

@Suppress("UNCHECKED_CAST")
inline fun <reified VM : ViewModel> Fragment.provideMachine(
    crossinline provider: () -> VM
) = ViewModelProvider(this, object : ViewModelProvider.Factory {
    override fun <T1 : ViewModel> create(aClass: Class<T1>) = provider() as T1
}).get(VM::class.java)

@Suppress("UNCHECKED_CAST")
inline fun <reified VM : ViewModel> FragmentActivity.provideMachine(
    crossinline provider: () -> VM
) = ViewModelProvider(this, object : ViewModelProvider.Factory {
    override fun <T1 : ViewModel> create(aClass: Class<T1>) = provider() as T1
}).get(VM::class.java)