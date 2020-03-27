package com.github.brewin.mvicoroutines.presentation.main

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.widget.SearchView
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

    private val repoListAdapter = RepoListAdapter {
        Toast.makeText(requireContext(), it.url, Toast.LENGTH_LONG).show()
    }

    private val errorSnackbar by lazy {
        Snackbar.make(requireView(), "", Snackbar.LENGTH_LONG)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)

        binding.setup()

        if (!::machine.isInitialized) {
            machine = MainMachine(
                binding.events(),
                savedInstanceState?.getParcelable(SAVED_STATE_KEY) ?: MainState.DEFAULT,
                GitHubRepositoryImpl(GitHubDataSource())
            )
        }

        machine.states
            .onEach {
                Timber.d("state = $it")
                it.render()
            }.launchIn(lifecycleScope)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(SAVED_STATE_KEY, machine.state)
    }

    private fun MainFragmentBinding.setup() {
        toolbar.inflateMenu(R.menu.menu_main)
        repoListView.adapter = repoListAdapter
    }

    private fun MainFragmentBinding.events() = flowOf(
        errorSnackbar.dismissEvents()
            .map { MainEvent.ErrorMessageDismiss },
        (toolbar.menu.findItem(R.id.action_search).actionView as SearchView).queryTextEvents()
            .filterIsInstance<QueryTextEvent.QuerySubmitted>()
            .map { MainEvent.QuerySubmit(it.queryText.toString()) }
            .onEach { hideKeyboard() },
        toolbar.menu.findItem(R.id.action_refresh).clicks()
            .map { MainEvent.RefreshClick },
        toolbar.menu.findItem(R.id.action_test).clicks()
            .map { MainEvent.TestClick },
        swipeRefreshLayout.refreshes()
            .map { MainEvent.RefreshSwipe }
    ).flattenMerge()

    private fun MainState.render() {
        if (shouldShowError && !errorSnackbar.isShownOrQueued) {
            errorSnackbar.setText(errorMessage).show()
        }

        binding.swipeRefreshLayout.isRefreshing = isInProgress

        repoListAdapter.submitList(searchResults)
    }

    companion object {
        const val SAVED_STATE_KEY = "main_fragment_saved_state"
    }
}