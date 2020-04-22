package com.github.brewin.mvicoroutines.presentation.arch

import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*

// TODO: Don't use ViewModel. Make it multiplatform ready.
abstract class Machine<INPUT : Any, OUTPUT : Any, STATE : OUTPUT, EFFECT : OUTPUT>(
    initialState: STATE
) : ViewModel() {

    var state: STATE = initialState
        private set

    private var outputs: Flow<OUTPUT> = emptyFlow()

    @Suppress("UNCHECKED_CAST")
    fun outputs(inputs: Flow<INPUT>): Flow<OUTPUT> {
        outputs = flowOf(
            outputs,
            inputs
                .flowOn(Dispatchers.Main)
                .flatMapConcat { it.process() }
                .onStart { emit(state) }
                .onEach { if (state::class.isInstance(it)) state = it as STATE }
                .flowOn(Dispatchers.Default)
                .broadcastIn(viewModelScope)
                .asFlow()
        ).flattenMerge()

        return outputs
    }

    protected abstract fun INPUT.process(): Flow<OUTPUT>
}

@Suppress("UNCHECKED_CAST")
inline fun <reified VM : ViewModel> Fragment.provideMachine(
    crossinline provider: () -> VM
) = ViewModelProvider(this, object : ViewModelProvider.Factory {
    override fun <T1 : ViewModel> create(aClass: Class<T1>) = provider() as T1
}).get(VM::class.java)