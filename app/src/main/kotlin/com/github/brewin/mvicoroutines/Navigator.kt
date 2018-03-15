package com.github.brewin.mvicoroutines

import android.content.Context
import android.content.Intent
import android.net.Uri
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.channels.actor
import java.lang.ref.WeakReference

sealed class NavigatorAction {
    data class OpenUri(
        val context: Context,
        val uri: android.net.Uri
    ) : NavigatorAction()
}

private val navigatorJob = Job()
val navigator = actor<NavigatorAction>(navigatorJob, Channel.CONFLATED) {
    for (action in this) when (action) {
        is NavigatorAction.OpenUri -> navigateToUri(
            WeakReference<Context>(action.context).get(),
            action.uri
        )
    }
}

fun closeNavigator() = navigatorJob.cancel()

private fun navigateToUri(context: Context?, uri: Uri) {
    context?.startActivity(Intent(Intent.ACTION_VIEW, uri))
}