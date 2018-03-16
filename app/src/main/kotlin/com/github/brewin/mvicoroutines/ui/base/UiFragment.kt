package com.github.brewin.mvicoroutines.ui.base

import android.os.Bundle
import android.support.annotation.LayoutRes
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.github.brewin.mvicoroutines.ui.main.MainUiResult
import com.github.brewin.mvicoroutines.ui.main.MainUiState

abstract class UiFragment<A : UiAction, R : MainUiResult, S : MainUiState> : Fragment(),
    UiRenderer<A, R, S> {

    @LayoutRes
    protected abstract fun getLayoutId(): Int

    protected abstract fun setupUi()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(getLayoutId(), container, false)
    }

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