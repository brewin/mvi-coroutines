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
import com.github.brewin.mvicoroutines.model.RepoItem
import com.github.brewin.mvicoroutines.view.base.GenericListAdapter
import com.github.brewin.mvicoroutines.view.base.StateStatusSubscriberFragment
import com.google.android.material.snackbar.Snackbar
import hideKeyboard
import kotlinx.android.synthetic.main.fragment_main.*
import kotlinx.android.synthetic.main.item_state.view.*

class MainFragment : StateStatusSubscriberFragment<MainMachine, MainStateStatus, MainState>() {

    override val layoutRes = R.layout.fragment_main

    private val repoListAdapter by lazy {
        GenericListAdapter<ConstraintLayout, RepoItem>(R.layout.item_state) { layout, repoItem ->
            layout.repoName.text = repoItem.name
        }
    }

    override fun createMachine(savedInstanceState: Bundle?): MainMachine {
        val initialStateStatus = MainStateStatus.Initial(
            savedInstanceState?.getParcelable(SAVED_VIEW_STATE) ?: MainState()
        )
        return machineProvider { MainMachine(initialStateStatus, Repository(GitHubApi.api)) }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        swipeRefreshLayout.setOnRefreshListener {
            machine.refresh()
        }
        repoListView.adapter = repoListAdapter
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
                    it.setOnMenuItemClickListener {
                        machine.refresh()
                        true
                    }
                }
            }
        }
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onStateStatus(stateStatus: MainStateStatus) {
        when (stateStatus) {
            is MainStateStatus.Initial, is MainStateStatus.Results -> {
                repoListAdapter.items = stateStatus.state.repoList
            }
            is MainStateStatus.Loading -> {
                swipeRefreshLayout.isRefreshing = stateStatus.isLoading
            }
            is MainStateStatus.Error -> {
                view?.let {
                    Snackbar.make(it, stateStatus.exception.localizedMessage, Snackbar.LENGTH_LONG)
                        .show()
                }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        machine.withState {
            outState.putParcelable(SAVED_VIEW_STATE, this@withState)
            super.onSaveInstanceState(outState)
        }
    }

    companion object {
        const val SAVED_VIEW_STATE = "main_fragment_view_state"
    }
}