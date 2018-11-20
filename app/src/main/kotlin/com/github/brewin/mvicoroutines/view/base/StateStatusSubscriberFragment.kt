package com.github.brewin.mvicoroutines.view.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders

abstract class StateStatusSubscriberFragment<M : StateMachine<SS, S>, SS : StateStatus<S>, S : State> :
    Fragment(), StateStatusSubscriber<SS, S> {

    abstract val layoutRes: Int

    lateinit var machine: M
        private set

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(layoutRes, container, false)

    abstract fun createMachine(savedInstanceState: Bundle?): M

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        machine = createMachine(savedInstanceState)
        machine.addSubscriber(this)
    }

    override fun onDestroy() {
        machine.removeSubscriber(this)
        super.onDestroy()
    }

    @Suppress("UNCHECKED_CAST")
    inline fun <reified M : StateMachine<*, *>> machineProvider(
        crossinline provider: () -> M
    ) = ViewModelProviders.of(this, object : ViewModelProvider.Factory {
        override fun <T1 : ViewModel> create(aClass: Class<T1>) = provider() as T1
    }).get(M::class.java)
}