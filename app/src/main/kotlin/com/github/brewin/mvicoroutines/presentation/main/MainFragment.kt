package com.github.brewin.mvicoroutines.presentation.main

import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import androidx.appcompat.widget.SearchView
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.github.brewin.mvicoroutines.R
import com.github.brewin.mvicoroutines.data.remote.GitHubDataSource
import com.github.brewin.mvicoroutines.data.repository.GitHubRepositoryImpl
import com.github.brewin.mvicoroutines.databinding.MainFragmentBinding
import com.github.brewin.mvicoroutines.presentation.arch.provideMachine
import com.github.brewin.mvicoroutines.presentation.common.hideKeyboard
import com.github.brewin.mvicoroutines.presentation.common.viewBinding
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.flow.*
import reactivecircus.flowbinding.android.view.clicks
import reactivecircus.flowbinding.appcompat.QueryTextEvent
import reactivecircus.flowbinding.appcompat.queryTextEvents
import reactivecircus.flowbinding.swiperefreshlayout.refreshes

class MainFragment : Fragment(R.layout.main_fragment) {

    private val binding by viewBinding(MainFragmentBinding::bind)
    private lateinit var machine: MainMachine
    private val repoListAdapter = RepoListAdapter()

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)

        binding.setup()

        machine = provideMachine {
            MainMachine(
                savedInstanceState?.getParcelable(SAVED_STATE_KEY) ?: MainState.DEFAULT,
                GitHubRepositoryImpl(GitHubDataSource())
            )
        }

        machine.states
            .onEach { it.render() }
            .launchIn(lifecycleScope)

        machine.effects
            .onEach { it.react() }
            .launchIn(lifecycleScope)

        machine.start(binding.inputs())
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(SAVED_STATE_KEY, machine.state)
    }

    private fun MainFragmentBinding.setup() {
        toolbar.inflateMenu(R.menu.menu_main)
        (toolbar.menu.findItem(R.id.action_search).actionView as SearchView).apply {
            imeOptions = imeOptions or EditorInfo.IME_FLAG_NO_EXTRACT_UI
        }
        repoListView.adapter = repoListAdapter
    }

    private fun MainFragmentBinding.inputs() = flowOf(
        (toolbar.menu.findItem(R.id.action_search).actionView as SearchView).queryTextEvents()
            .debounce(500)
            .filterIsInstance<QueryTextEvent.QuerySubmitted>()
            .map { MainInput.QuerySubmit(it.queryText.toString()) }
            .onEach { hideKeyboard() },
        toolbar.menu.findItem(R.id.action_refresh).clicks()
            .debounce(500)
            .map { MainInput.RefreshClick },
        swipeRefreshLayout.refreshes()
            .debounce(500)
            .map { MainInput.RefreshSwipe },
        repoListAdapter.itemClicks()
            .debounce(500)
            .map { MainInput.RepoClick(it.item.url) }
    ).flattenMerge()

    private fun MainState.render() {
        binding.swipeRefreshLayout.isRefreshing = isInProgress
        repoListAdapter.submitList(searchResults) {
            binding.repoListView.scrollToPosition(0)
        }
    }

    private fun MainEffect.react() = when (this) {
        is MainEffect.ShowError ->
            Snackbar.make(requireView(), message, Snackbar.LENGTH_SHORT).show()
        is MainEffect.OpenRepoUrl ->
            startActivity(Intent(Intent.ACTION_VIEW, url.toUri()))
    }

    companion object {
        const val SAVED_STATE_KEY = "main_fragment_saved_state"
    }
}