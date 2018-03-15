package com.github.brewin.mvicoroutines

import android.content.Context
import android.content.Intent
import android.net.Uri
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.channels.actor
import java.lang.ref.WeakReference

sealed class NavigatorTarget {
    data class OpenUri(
        val context: Context,
        val uri: android.net.Uri
    ) : NavigatorTarget()
}

private val navigator = actor<NavigatorTarget>(CommonPool, Channel.CONFLATED) {
    for (target in this) when (target) {
        is NavigatorTarget.OpenUri -> navigateToUri(
            WeakReference<Context>(target.context).get(),
            target.uri
        )
    }
}

fun navigateTo(target: NavigatorTarget) = navigator.offer(target)

private fun navigateToUri(context: Context?, uri: Uri) {
    context?.startActivity(Intent(Intent.ACTION_VIEW, uri))
}