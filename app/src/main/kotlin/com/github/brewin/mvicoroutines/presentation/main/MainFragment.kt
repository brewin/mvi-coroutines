package com.github.brewin.mvicoroutines.presentation.main

import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.forEach
import androidx.fragment.app.Fragment
import com.github.brewin.mvicoroutines.R
import com.github.brewin.mvicoroutines.data.remote.GitHubApi
import com.github.brewin.mvicoroutines.data.repository.GitHubRepositoryImpl
import com.github.brewin.mvicoroutines.domain.entity.RepoEntity
import com.github.brewin.mvicoroutines.domain.usecase.SearchReposUseCase
import com.github.brewin.mvicoroutines.presentation.common.GenericListAdapter
import com.github.brewin.mvicoroutines.presentation.common.provideMachine
import com.google.android.material.snackbar.Snackbar
import hideKeyboard
import kotlinx.android.synthetic.main.repo_item.view.*
import kotlinx.android.synthetic.main.main_fragment.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.consumeEach
import kotlin.coroutines.CoroutineContext

class MainFragment : Fragment(), CoroutineScope {

    override val coroutineContext: CoroutineContext = Job() + Dispatchers.Main
    private lateinit var machine: MainMachine

    private val repoListAdapter by lazy {
        GenericListAdapter<ConstraintLayout, RepoEntity>(R.layout.repo_item) { layout, repoItem ->
            layout.repoName.text = repoItem.name
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        machine = provideMachine {
            val initial = savedInstanceState?.getParcelable(SAVED_STATE_KEY) ?: MainState.Default()
            val searchUseCase = SearchReposUseCase(GitHubRepositoryImpl(GitHubApi.api))
            MainMachine(initial, searchUseCase)
        }
        return inflater.inflate(R.layout.main_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        swipeRefreshLayout.setOnRefreshListener {
            machine.intents.offer(MainIntent.Refresh)
        }
        repoListView.adapter = repoListAdapter
        launch {
            machine.states.consumeEach(::render)
        }
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
                                    machine.intents.offer(MainIntent.Search(query.trim()))
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
                        machine.intents.offer(MainIntent.Refresh)
                        true
                    }
                }
            }
        }
        super.onCreateOptionsMenu(menu, inflater)
    }

    private fun render(state: MainState) = when (state) {
        is MainState.Default,
        is MainState.ReposReceived -> {
            repoListAdapter.items = state.repoList
        }
        is MainState.Progressing -> {
            swipeRefreshLayout.isRefreshing = state.progress
        }
        is MainState.ErrorReceived -> {
            Snackbar.make(view!!, state.errorMessage, Snackbar.LENGTH_LONG).show()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putParcelable(SAVED_STATE_KEY, machine.stateAsDefault)
        super.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        super.onDestroy()
        coroutineContext.cancel()
    }

    companion object {
        const val SAVED_STATE_KEY = "main_fragment_view_state"
    }
}