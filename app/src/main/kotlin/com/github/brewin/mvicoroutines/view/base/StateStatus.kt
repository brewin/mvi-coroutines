package com.github.brewin.mvicoroutines.view.base

import android.os.Parcelable

interface State : Parcelable

abstract class StateStatus<S : State>(val state: S)