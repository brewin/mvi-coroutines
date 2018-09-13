package com.github.brewin.mvicoroutines.view.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

abstract class StateSubscriberFragment<S : State> : Fragment(), StateSubscriber<S> {

    abstract val layoutRes: Int
    abstract val machine: StateMachine<S>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(layoutRes, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        machine.subscribe(this)
    }

    override fun onDestroy() {
        machine.unsubscribe(this)
        super.onDestroy()
    }
}