package com.github.brewin.mvicoroutines.view.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

abstract class ViewStateSubscriberFragment<M : ViewStateMachine<S>, S : ViewState> :
    Fragment(), ViewStateSubscriber<S> {

    abstract val layoutRes: Int

    lateinit var machine: M
        private set

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(layoutRes, container, false)

    abstract fun createMachine(savedInstanceState: Bundle?): M

    override fun onViewCreated(view:View,savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        machine = createMachine(savedInstanceState)
        machine.addSubscriber(this)
    }

    override fun onDestroy() {
        machine.removeSubscriber(this)
        super.onDestroy()
    }
}