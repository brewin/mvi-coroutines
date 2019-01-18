package com.github.brewin.mvicoroutines.presentation.main

import com.github.brewin.mvi.MviIntent

sealed class MainIntent : MviIntent {
    data class Search(val query: String) : MainIntent()
    object Refresh : MainIntent()
}