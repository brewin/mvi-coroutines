package com.github.brewin.mvicoroutines.presentation.main

import com.github.brewin.mvi.Machine
import com.github.brewin.mvi.UseCaseUpdate
import com.github.brewin.mvicoroutines.domain.usecase.SearchReposUseCase

class MainMachine(
    initialState: MainState,
    private val searchReposUseCase: SearchReposUseCase
) : Machine<MainEvent, MainState>(initialState, MainState::Default) {

    override fun handleEvent(event: MainEvent) = when (event) {
        is MainEvent.SearchSubmitted -> searchReposUseCase(event.query)
        MainEvent.RefreshClicked -> searchReposUseCase(state.query)
    }

    override fun updateState(update: UseCaseUpdate) = when (update) {
        is SearchReposUseCase.Update -> when (update) {
            is SearchReposUseCase.Update.Started ->
                MainState.InProgress(state)
            is SearchReposUseCase.Update.Success ->
                MainState.ReposReceived(state, update.query, update.searchResults)
            is SearchReposUseCase.Update.Failure ->
                MainState.ErrorReceived(state, update.query, update.errorMessage)
        }
        else -> throw IllegalStateException("Illegal Update: $update")
    }
}