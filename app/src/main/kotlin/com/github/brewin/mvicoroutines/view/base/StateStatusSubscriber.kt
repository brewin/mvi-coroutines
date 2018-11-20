package com.github.brewin.mvicoroutines.view.base

import androidx.annotation.MainThread

interface StateStatusSubscriber<SS : StateStatus<S>, S : State> {
    @MainThread
    fun onStateStatus(stateStatus: SS)
}