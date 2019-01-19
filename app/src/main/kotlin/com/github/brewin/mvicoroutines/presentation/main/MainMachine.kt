package com.github.brewin.mvicoroutines.presentation.main

import com.github.brewin.mvi.MviMachine
import com.github.brewin.mvi.MviUpdate
import com.github.brewin.mvicoroutines.domain.usecase.SearchReposUseCase

class MainMachine(
    initialState: MainState,
    private val searchReposUseCase: SearchReposUseCase
) : MviMachine<MainIntent, MainState>(initialState, MainState::Default) {

    override fun handle(intent: MainIntent) = when (intent) {
        is MainIntent.Search -> searchReposUseCase(intent.query)
        MainIntent.Refresh -> searchReposUseCase(state.query)
    }

    override fun reduce(update: MviUpdate) = when (update) {
        is SearchReposUseCase.Update -> when (update) {
            is SearchReposUseCase.Update.Started ->
                MainState.Progressing(state, true)
            is SearchReposUseCase.Update.Success ->
                MainState.ReposReceived(state, update.query, update.searchResults)
            is SearchReposUseCase.Update.Failure ->
                MainState.ErrorReceived(state, update.query, update.errorMessage)
            SearchReposUseCase.Update.Finally ->
                MainState.Progressing(state, false)
        }
        else -> throw IllegalStateException("Illegal Update: $update")
    }
}