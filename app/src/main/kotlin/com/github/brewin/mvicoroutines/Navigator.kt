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
        val url: Uri
    ) : NavigatorTarget()
}

private val navigator = actor<NavigatorTarget>(CommonPool, Channel.CONFLATED) {
    for (target in this) when (target) {
        is NavigatorTarget.OpenUri -> navigateToUrl(
            WeakReference<Context>(target.context).get(),
            target.url
        )
    }
}

fun navigateTo(target: NavigatorTarget) = navigator.offer(target)

private fun navigateToUrl(context: Context?, url: Uri) {
    context?.startActivity(Intent(Intent.ACTION_VIEW, url))
}