package com.github.brewin.mvicoroutines.view.main

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.widget.Button
import androidx.appcompat.widget.SearchView
import androidx.core.view.forEach
import androidx.core.view.isVisible
import com.github.brewin.mvicoroutines.R
import com.github.brewin.mvicoroutines.data.GitHubApi
import com.github.brewin.mvicoroutines.data.Repository
import com.github.brewin.mvicoroutines.model.RepoItem
import com.github.brewin.mvicoroutines.view.base.GenericListAdapter
import com.github.brewin.mvicoroutines.view.base.ViewStateFragment
import com.github.brewin.mvicoroutines.view.base.machineProvider
import hideKeyboard
import kotlinx.android.synthetic.main.fragment_main.*
import timber.log.Timber

class MainFragment : ViewStateFragment<MainState>() {

    override val layoutRes = R.layout.fragment_main

    override val machine by machineProvider { MainMachine(Repository(GitHubApi.api)) }

    private val listAdapter by lazy {
        GenericListAdapter<Button, RepoItem>(R.layout.item_repo) { button, (name, url) ->
            button.text = name
            button.setOnClickListener {
                context?.startActivity(Intent(Intent.ACTION_VIEW, url))
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setHasOptionsMenu(true)
        swipeRefreshLayout.setOnRefreshListener {
            machine.refresh()
        }
        reposRecyclerView.adapter = listAdapter
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_main, menu)
        menu.forEach {
            when (it.itemId) {
                R.id.action_search -> {
                    (it.actionView as SearchView).setOnQueryTextListener(
                        object : SearchView.OnQueryTextListener {
                            override fun onQueryTextSubmit(query: String?): Boolean {
                                if (!query.isNullOrBlank()) {
                                    machine.search(query!!)
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

    override fun onNewState(old: MainState, new: MainState) {
        Timber.d("Old ViewState: $old")
        Timber.d("New ViewState: $new")
        emptyView.text = new.error?.message ?: "Search for GitHub repos"
        emptyView.isVisible = new.repoList.isEmpty()
        listAdapter.items = new.repoList
        swipeRefreshLayout.isRefreshing = new.isLoading
    }
}