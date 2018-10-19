package com.github.brewin.mvicoroutines.view.base

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class ViewStateTask<S : ViewState, T>(
    private val machine: ViewStateMachine<S>,
    private val block: suspend CoroutineScope.() -> T
) {

    private var startedReducer: (S.() -> S)? = null
    private var failureReducer: (S.(Exception) -> S)? = null
    private var successReducer: (S.(T) -> S)? = null
    private var finallyReducer: (S.() -> S)? = null

    fun started(reducer: S.() -> S): ViewStateTask<S, T> {
        startedReducer = reducer
        return this
    }

    fun failure(reducer: S.(Exception) -> S): ViewStateTask<S, T> {
        failureReducer = reducer
        return this
    }

    fun success(reducer: S.(T) -> S): ViewStateTask<S, T> {
        successReducer = reducer
        return this
    }

    fun finally(reducer: S.() -> S): ViewStateTask<S, T> {
        finallyReducer = reducer
        return this
    }

    fun start(): Job = machine.launch(Dispatchers.IO) {
        startedReducer?.let(machine::sendState)
        try {
            val value = block()
            successReducer?.let { machine.sendState { it(value) } }
        } catch (e: Exception) {
            failureReducer?.let { machine.sendState { it(e) } }
        }
        finallyReducer?.let(machine::sendState)
    }
}