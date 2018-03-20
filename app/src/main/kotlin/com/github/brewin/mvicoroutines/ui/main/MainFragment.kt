package com.github.brewin.mvicoroutines.ui.main

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
import com.github.brewin.mvicoroutines.ui.base.GenericListAdapter
import com.github.brewin.mvicoroutines.ui.base.UiFragment
import com.github.brewin.mvicoroutines.ui.base.uiProvider
import kotlinx.android.synthetic.main.fragment_main.*
import org.jetbrains.anko.appcompat.v7.coroutines.onQueryTextListener
import org.jetbrains.anko.sdk23.coroutines.onClick
import timber.log.Timber

class MainFragment : UiFragment<MainUiAction, MainUiResult, MainUiState>() {

    override val ui by uiProvider { MainUi(Repository(GitHubApi.service)) }

    private val listAdapter by lazy {
        GenericListAdapter<Button, ReposItem>(R.layout.item_repo) { button, (name, url) ->
            button.text = name
            button.onClick { navigateTo(NavigatorTarget.OpenUrl(requireContext(), url)) }
        }
    }

    override fun getLayoutId() = R.layout.fragment_main

    override fun setupUi() {
        setHasOptionsMenu(true)
        swipeRefreshLayout.setOnRefreshListener { ui.offerActionWithProgress(MainUiAction.Refresh) }
        reposRecyclerView.adapter = listAdapter
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_main, menu)
        menu.forEach {
            when (it.itemId) {
                R.id.action_search -> {
                    (it.actionView as SearchView).onQueryTextListener {
                        onQueryTextSubmit { query ->
                            query?.let { ui.offerActionWithProgress(MainUiAction.Search(it)) }
                            true
                        }
                    }
                }
                R.id.action_refresh -> {
                    it.setOnMenuItemClickListener {
                        ui.offerActionWithProgress(MainUiAction.Refresh)
                        true
                    }
                }
            }
        }
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun render(state: MainUiState) {
        Timber.d("State:\n$state")
        emptyView.text = state.error?.message ?: "Search for GitHub repos"
        emptyView.isVisible = state.repoList.isEmpty()
        listAdapter.items = state.repoList
        swipeRefreshLayout.isRefreshing = state.isLoading
    }
}
