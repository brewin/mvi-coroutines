package com.github.brewin.mvicoroutines.presentation.main

import com.github.brewin.mvi.MviMachine
import com.github.brewin.mvi.MviUseCase
import com.github.brewin.mvicoroutines.domain.usecase.SearchReposUseCase

class MainMachine(
    initialState: MainState,
    private val searchReposUseCase: SearchReposUseCase
) : MviMachine<MainIntent, MainState>(initialState, MainState::Default) {

    override fun process(intent: MainIntent) = when (intent) {
        is MainIntent.Search -> searchReposUseCase(intent.query)
        MainIntent.Refresh -> searchReposUseCase(state.query)
    }

    override fun reduce(result: MviUseCase.Result) = when (result) {
        is SearchReposUseCase.Result -> when (result) {
            is SearchReposUseCase.Result.Waiting ->
                MainState.Progressing(state, true)
            is SearchReposUseCase.Result.Success ->
                MainState.ReposReceived(state, result.query, result.searchResults)
            is SearchReposUseCase.Result.Failure ->
                MainState.ErrorReceived(state, result.query, result.errorMessage)
            SearchReposUseCase.Result.Finally ->
                MainState.Progressing(state, false)
        }
        else -> throw IllegalStateException("Illegal Result: $result")
    }
}