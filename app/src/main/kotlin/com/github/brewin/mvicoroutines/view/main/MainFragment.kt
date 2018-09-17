package com.github.brewin.mvicoroutines.view.main

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.forEach
import com.github.brewin.mvicoroutines.R
import com.github.brewin.mvicoroutines.data.Repository
import com.github.brewin.mvicoroutines.data.remote.GitHubApi
import com.github.brewin.mvicoroutines.view.base.GenericListAdapter
import com.github.brewin.mvicoroutines.view.base.StateSubscriberFragment
import com.github.brewin.mvicoroutines.view.base.machineProvider
import hideKeyboard
import kotlinx.android.synthetic.main.fragment_main.*
import kotlinx.android.synthetic.main.item_state.view.*
import java.text.SimpleDateFormat

class MainFragment : StateSubscriberFragment<MainState>() {

    override val layoutRes = R.layout.fragment_main

    override val machine by machineProvider { MainMachine(Repository(GitHubApi.api)) }

    private val listAdapter by lazy {
        GenericListAdapter<ConstraintLayout, MainState>(R.layout.item_state) { layout, state ->
            layout.apply {
                timeView.text = SimpleDateFormat("HH:mm:ss").format(state.time)
                isLoadingView.text = state.isLoading.toString()
                errorView.text = state.error.toString()
                queryView.text = state.query
                repoListView.text = state.repoList.asSequence()
                    .take(5)
                    .joinToString { it.name }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        swipeRefreshLayout.setOnRefreshListener {
            machine.refresh()
        }
        stateListView.adapter = listAdapter
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_main, menu)
        menu.forEach {
            when (it.itemId) {
                R.id.action_search -> {
                    (it.actionView as SearchView).setOnQueryTextListener(
                        object : SearchView.OnQueryTextListener {
                            override fun onQueryTextSubmit(query: String?): Boolean {
                                if (query != null && query.isNotBlank()) {
                                    machine.search(query.trim())
                                }
                                hideKeyboard()
                                return true
                            }

                            override fun onQueryTextChange(newText: String?): Boolean {
                                return false
                            }
                        })
                }
                R.id.action_refresh -> {
                    it.setOnMenuItemClickListener { _ ->
                        machine.refresh()
                        true
                    }
                }
            }
        }
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onNewState(old: MainState?, new: MainState) {
        listAdapter.items = listAdapter.items.toMutableList()
            .apply {
                add(0, new)
            }
        swipeRefreshLayout.isRefreshing = new.isLoading
    }
}