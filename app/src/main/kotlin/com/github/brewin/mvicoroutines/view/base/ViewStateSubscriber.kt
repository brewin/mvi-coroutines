package com.github.brewin.mvicoroutines.view.base

import androidx.annotation.MainThread

interface ViewStateSubscriber<S : ViewState> {
    @MainThread
    fun onNewState(old: S?, new: S)
}