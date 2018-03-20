package com.github.brewin.mvicoroutines.ui.base

import android.os.Bundle
import android.support.annotation.LayoutRes
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

abstract class UiFragment<A : UiAction, R : UiResult, S : UiState> : Fragment(),
    UiRenderer<A, R, S> {

    @LayoutRes
    protected abstract fun getLayoutId(): Int

    protected abstract fun setupUi()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(getLayoutId(), container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUi()
        ui.startRendering(this)
    }

    override fun onDestroy() {
        ui.stopRendering()
        super.onDestroy()
    }
}