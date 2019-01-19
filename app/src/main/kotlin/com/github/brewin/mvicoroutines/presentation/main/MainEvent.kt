package com.github.brewin.mvicoroutines.presentation.main

import com.github.brewin.mvi.UiEvent

sealed class MainEvent : UiEvent {
    data class SearchSubmitted(val query: String) : MainEvent()
    object RefreshClicked : MainEvent()
}