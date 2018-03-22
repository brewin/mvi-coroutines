package com.github.brewin.mvicoroutines.view.main

import android.support.v7.widget.SearchView
import android.view.Menu
import android.view.MenuInflater
import android.widget.Button
import androidx.view.forEach
import androidx.view.isVisible
import com.github.brewin.mvicoroutines.NavigatorTarget
import com.github.brewin.mvicoroutines.R
import com.github.brewin.mvicoroutines.data.GitHubApi
import com.github.brewin.mvicoroutines.data.Repository
import com.github.brewin.mvicoroutines.navigateTo
import com.github.brewin.mvicoroutines.view.base.GenericListAdapter
import com.github.brewin.mvicoroutines.view.base.Fragment
import com.github.brewin.mvicoroutines.view.base.machineProvider
import kotlinx.android.synthetic.main.fragment_main.*
import org.jetbrains.anko.appcompat.v7.coroutines.onQueryTextListener
import org.jetbrains.anko.sdk23.coroutines.onClick
import timber.log.Timber

class MainFragment : Fragment<MainIntent, MainTask, MainState>() {

    override val machine by machineProvider { MainMachine(Repository(GitHubApi.service)) }

    private val listAdapter by lazy {
        GenericListAdapter<Button, ReposItem>(R.layout.item_repo) { button, (name, url) ->
            button.text = name
            button.onClick { navigateTo(NavigatorTarget.OpenUrl(requireContext(), url)) }
        }
    }

    override fun getLayoutId() = R.layout.fragment_main

    override fun setupUi() {
        setHasOptionsMenu(true)
        swipeRefreshLayout.setOnRefreshListener { machine.offerActionWithProgress(MainIntent.Refresh) }
        reposRecyclerView.adapter = listAdapter
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_main, menu)
        menu.forEach {
            when (it.itemId) {
                R.id.action_search -> {
                    (it.actionView as SearchView).onQueryTextListener {
                        onQueryTextSubmit { query ->
                            query?.let { machine.offerActionWithProgress(MainIntent.Search(it)) }
                            true
                        }
                    }
                }
                R.id.action_refresh -> {
                    it.setOnMenuItemClickListener {
                        machine.offerActionWithProgress(MainIntent.Refresh)
                        true
                    }
                }
            }
        }
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun render(state: MainState) {
        Timber.d("State:\n$state")
        emptyView.text = state.error?.message ?: "Search for GitHub repos"
        emptyView.isVisible = state.repoList.isEmpty()
        listAdapter.items = state.repoList
        swipeRefreshLayout.isRefreshing = state.isLoading
    }
}
