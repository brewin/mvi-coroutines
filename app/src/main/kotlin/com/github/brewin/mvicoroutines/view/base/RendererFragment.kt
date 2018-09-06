package com.github.brewin.mvicoroutines.view.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment

abstract class RendererFragment<I : Intent, T : Task, S : State> : Fragment(), Renderer<I, T, S> {

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
        machine.startRendering(this)
    }

    override fun onDestroy() {
        machine.stopRendering()
        super.onDestroy()
    }
}