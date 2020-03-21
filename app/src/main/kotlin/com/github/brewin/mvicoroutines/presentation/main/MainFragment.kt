package com.github.brewin.mvicoroutines.presentation.main

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.github.brewin.mvicoroutines.R
import com.github.brewin.mvicoroutines.data.remote.GitHubDataSource
import com.github.brewin.mvicoroutines.data.repository.GitHubRepositoryImpl
import com.github.brewin.mvicoroutines.databinding.MainFragmentBinding
import com.github.brewin.mvicoroutines.presentation.common.hideKeyboard
import com.github.brewin.mvicoroutines.presentation.common.provideMachine
import com.github.brewin.mvicoroutines.presentation.common.viewBinding
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import reactivecircus.flowbinding.android.view.clicks
import reactivecircus.flowbinding.appcompat.QueryTextEvent
import reactivecircus.flowbinding.appcompat.queryTextEvents
import reactivecircus.flowbinding.material.dismissEvents
import reactivecircus.flowbinding.swiperefreshlayout.refreshes

class MainFragment : Fragment(R.layout.main_fragment) {

    private val binding by viewBinding(MainFragmentBinding::bind)

    private lateinit var machine: MainMachine

    private val repoListAdapter = RepoListAdapter {
        Toast.makeText(requireContext(), it.url, Toast.LENGTH_LONG).show()
    }

    private val errorSnackbar by lazy {
        Snackbar.make(requireView(), "", Snackbar.LENGTH_LONG)
            .apply {
                dismissEvents()
                    .onEach { machine.events.send(MainEvent.ErrorMessageDismiss) }
                    .launchIn(lifecycleScope)
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        machine = provideMachine {
            val initial = savedInstanceState?.getParcelable(SAVED_STATE_KEY) ?: MainState()
            val gitHubRepository = GitHubRepositoryImpl(GitHubDataSource())
            MainMachine(initial, gitHubRepository)
        }

        machine.states
            .onEach { it.render() }
            .launchIn(lifecycleScope)

        binding.setup()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putParcelable(SAVED_STATE_KEY, machine.state)
        super.onSaveInstanceState(outState)
    }

    private fun MainFragmentBinding.setup() {
        toolbar.apply {
            inflateMenu(R.menu.menu_main)

            menu.findItem(R.id.action_search)
                .let { it.actionView as SearchView }
                .queryTextEvents()
                .filterIsInstance<QueryTextEvent.QuerySubmitted>()
                .onEach {
                    machine.events.send(MainEvent.QuerySubmit(it.queryText.toString()))
                    hideKeyboard()
                }.launchIn(lifecycleScope)

            menu.findItem(R.id.action_refresh)
                .clicks()
                .onEach { machine.events.send(MainEvent.RefreshClick) }
                .launchIn(lifecycleScope)
        }

        repoListView.adapter = repoListAdapter

        swipeRefreshLayout
            .refreshes()
            .onEach { machine.events.send(MainEvent.RefreshSwipe) }
            .launchIn(lifecycleScope)
    }

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
