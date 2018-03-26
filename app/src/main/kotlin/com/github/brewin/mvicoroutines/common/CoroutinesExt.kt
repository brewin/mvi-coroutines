package com.github.brewin.mvicoroutines.common

import kotlinx.coroutines.experimental.delay
import java.io.IOException

/*
 * Source:
 * https://stackoverflow.com/questions/46872242/how-to-exponential-backoff-retry-on-kotlin-coroutines
 *
 * Usage:
 * val networkResult = retry(times = 3) { api.doSomething().await() }
 */
suspend fun <T> retry(
    times: Int = Int.MAX_VALUE,
    initialDelay: Long = 100, // 0.1 second
    maxDelay: Long = 1000,    // 1 second
    factor: Double = 2.0,
    block: suspend () -> T
): T {
    var currentDelay = initialDelay
    repeat(times - 1) {
        try {
            return block()
        } catch (e: IOException) {
            // you can log an error here and/or make a more finer-grained
            // analysis of the cause to see if retry is needed
        }
        delay(currentDelay)
        currentDelay = (currentDelay * factor).toLong().coerceAtMost(maxDelay)
    }
    return block() // last attempt
}