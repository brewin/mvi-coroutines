package com.github.brewin.mvicoroutines.presentation.main

import com.github.brewin.mvi.UiState
import com.github.brewin.mvicoroutines.domain.entity.RepoEntity
import kotlinx.android.parcel.Parcelize

/*
 * This sealed state structure is verbose, but has several benefits:
 *
 * 1. The public constructors act as reducers, restricting how the state can change.
 * 2. The renderer will know exactly which views need to be updated and which don't.
 * 3. The renderer will know when a value is not null, so fewer null checks.
 * 4. It solves the problem of transient information (ie. error message Snackbar) repeating on
 *    configuration changes. The MviMachine transforms the last state into its Default type,
 *    which has reducer constructor (S) -> S. The renderer will then receive Default and know not
 *    to show the error message.
 */
sealed class MainState : UiState {
    abstract val query: String
    abstract val repoList: List<RepoEntity>
    abstract val progress: Boolean
    abstract val errorMessage: String

    @Parcelize
    data class Default internal constructor(
        override val query: String = "",
        override val repoList: List<RepoEntity> = emptyList(),
        override val progress: Boolean = false,
        override val errorMessage: String = ""
    ) : MainState() {
        constructor(previousState: MainState) : this(
            query = previousState.query,
            repoList = previousState.repoList
        )
    }

    @Parcelize
    class Progressing private constructor(
        override val query: String,
        override val repoList: List<RepoEntity>,
        override val progress: Boolean,
        override val errorMessage: String
    ) : MainState() {
        constructor(previousState: MainState, isProgressing: Boolean) : this(
            query = previousState.query,
            repoList = previousState.repoList,
            progress = isProgressing,
            errorMessage = previousState.errorMessage
        )
    }

    @Parcelize
    class ReposReceived private constructor(
        override val query: String,
        override val repoList: List<RepoEntity>,
        override val progress: Boolean,
        override val errorMessage: String
    ) : MainState() {
        constructor(previousState: MainState, query: String, repoList: List<RepoEntity>) : this(
            query = query,
            repoList = repoList,
            progress = false,
            errorMessage = previousState.errorMessage
        )
    }

    @Parcelize
    class ErrorReceived private constructor(
        override val query: String,
        override val repoList: List<RepoEntity>,
        override val progress: Boolean,
        override val errorMessage: String
    ) : MainState() {
        constructor(previousState: MainState, query: String, errorMessage: String) : this(
            query = query,
            repoList = previousState.repoList,
            progress = false,
            errorMessage = errorMessage
        )
    }
}