package com.github.brewin.mvicoroutines.view.base

import kotlinx.coroutines.launch

sealed class TaskStatus<T> {
    class Started<T> : TaskStatus<T>()
    data class Success<T>(val value: T) : TaskStatus<T>()
    data class Failure<T>(val exception: Exception) : TaskStatus<T>()
    class Finally<T> : TaskStatus<T>()
}

abstract class Task<S : State, T, SE : StateStatus<S>> {

    protected abstract suspend fun create(): T

    protected abstract fun onStatus(state: S, status: TaskStatus<T>): SE

    fun start(machine: StateMachine<SE, S>) = machine.launch {
        machine.sendStateReducer { onStatus(this, TaskStatus.Started()) }
        try {
            val value = create()
            machine.sendStateReducer { onStatus(this, TaskStatus.Success(value)) }
        } catch (e: Exception) {
            machine.sendStateReducer { onStatus(this, TaskStatus.Failure(e)) }
        }
        machine.sendStateReducer { onStatus(this, TaskStatus.Finally()) }
    }
}