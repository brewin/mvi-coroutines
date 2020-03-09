package com.github.brewin.mvicoroutines.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.json.serializer.KotlinxSerializer
import io.ktor.client.features.logging.LogLevel
import io.ktor.client.features.logging.Logger
import io.ktor.client.features.logging.Logging
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import timber.log.Timber

val httpClient by lazy {
    HttpClient(OkHttp) {
        install(JsonFeature) {
            serializer = KotlinxSerializer(Json(JsonConfiguration.Default))
        }
        install(Logging) {
            logger = object : Logger {
                override fun log(message: String) {
                    Timber.i(message)
                }
            }
            level = LogLevel.INFO
        }
    }
}