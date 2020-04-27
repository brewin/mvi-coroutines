package com.github.brewin.mvicoroutines.presentation.main

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.flow.*
import reactivecircus.flowbinding.android.view.clicks
import reactivecircus.flowbinding.appcompat.QueryTextEvent
import reactivecircus.flowbinding.appcompat.queryTextEvents
import reactivecircus.flowbinding.swiperefreshlayout.refreshes
import timber.log.Timber

class MainFragment : Fragment() {

    private var machine: MainMachine? = null
    private val repoListAdapter = RepoListAdapter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = MainFragmentBinding.inflate(inflater, container, false)
        binding.setup()

        machine = provideMachine {
            MainMachine(
                savedInstanceState?.getParcelable(SAVED_STATE_KEY) ?: MainState.DEFAULT,
                GitHubRepositoryImpl(GitHubDataSource())
            )
        }

        machine!!.outputs(binding.inputs())
            .onEach { output ->
                Timber.d("output = $output")
                when (output) {
                    is MainState -> binding.render(output)
                    is MainEffect -> binding.react(output)
                }
            }
            .launchIn(lifecycleScope)


        return binding.root
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(SAVED_STATE_KEY, machine?.state)
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
            .map { QuerySubmit(it.queryText.toString()) }
            .onEach { hideKeyboard() },
        toolbar.menu.findItem(R.id.action_refresh).clicks()
            .debounce(500)
            .map { RefreshClick },
        swipeRefreshLayout.refreshes()
            .debounce(500)
            .map { RefreshSwipe },
        repoListAdapter.itemClicks()
            .debounce(500)
            .map { RepoClick(it.item.url) }
    ).flattenMerge()

    private fun MainFragmentBinding.render(state: MainState) {
        swipeRefreshLayout.isRefreshing = state.isInProgress
        repoListAdapter.submitList(state.searchResults) {
            repoListView.scrollToPosition(0)
        }
    }

    private fun MainFragmentBinding.react(effect: MainEffect) = when (effect) {
        is MainEffect.ShowError ->
            Snackbar.make(requireView(), effect.message, Snackbar.LENGTH_SHORT).show()
        is MainEffect.OpenRepoUrl ->
            startActivity(Intent(Intent.ACTION_VIEW, effect.url.toUri()))
    }

    companion object {
        const val SAVED_STATE_KEY = "MAIN_FRAGMENT_SAVED_STATE"
    }
}