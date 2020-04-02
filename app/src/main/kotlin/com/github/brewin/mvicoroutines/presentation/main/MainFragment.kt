package com.github.brewin.mvicoroutines.presentation.main

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.widget.SearchView
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.github.brewin.mvicoroutines.R
import com.github.brewin.mvicoroutines.data.remote.GitHubDataSource
import com.github.brewin.mvicoroutines.data.repository.GitHubRepositoryImpl
import com.github.brewin.mvicoroutines.databinding.MainFragmentBinding
import com.github.brewin.mvicoroutines.presentation.common.hideKeyboard
import com.github.brewin.mvicoroutines.presentation.common.viewBinding
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.flow.*
import reactivecircus.flowbinding.android.view.clicks
import reactivecircus.flowbinding.appcompat.QueryTextEvent
import reactivecircus.flowbinding.appcompat.queryTextEvents
import reactivecircus.flowbinding.material.dismissEvents
import reactivecircus.flowbinding.swiperefreshlayout.refreshes
import timber.log.Timber

class MainFragment : Fragment(R.layout.main_fragment) {

    private val binding by viewBinding(MainFragmentBinding::bind)
    private lateinit var machine: MainMachine
    private val repoListAdapter = RepoListAdapter()
    private val errorSnackbar by lazy {
        Snackbar.make(requireView(), "", Snackbar.LENGTH_LONG)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)

        binding.setup()

        if (!::machine.isInitialized) {
            machine = MainMachine(
                binding.inputs(),
                savedInstanceState?.getParcelable(SAVED_STATE_KEY) ?: MainState.DEFAULT,
                GitHubRepositoryImpl(GitHubDataSource())
            )
        }

        flowOf(
            machine.states
                .onEach { it.render();Timber.d("state = $it") },
            machine.effects
                .onEach { it.react() }
        )
            .flattenMerge()
            .launchIn(lifecycleScope)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(SAVED_STATE_KEY, machine.state)
    }

    private fun MainFragmentBinding.setup() {
        toolbar.inflateMenu(R.menu.menu_main)
        repoListView.adapter = repoListAdapter
    }

    private fun MainFragmentBinding.inputs() = flowOf(
        errorSnackbar.dismissEvents()
            .map { MainInput.ErrorMessageDismiss },
        (toolbar.menu.findItem(R.id.action_search).actionView as SearchView).queryTextEvents()
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
        if (shouldShowError && !errorSnackbar.isShownOrQueued) {
            errorSnackbar.setText(errorMessage).show()
        }

        binding.swipeRefreshLayout.isRefreshing = isInProgress

        repoListAdapter.submitList(searchResults)

        if (shouldOpenUrl) {
            startActivity(Intent(Intent.ACTION_VIEW, urlToShow.toUri()))
        }
    }

    private fun MainEffect.react() = when (this) {
        is MainEffect.RepoUrlOpen -> {
            startActivity(Intent(Intent.ACTION_VIEW, url.toUri()))
        }
    }

    companion object {
        const val SAVED_STATE_KEY = "main_fragment_saved_state"
    }
}