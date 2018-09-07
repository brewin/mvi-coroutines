package com.github.brewin.mvicoroutines.data

import com.jakewharton.retrofit2.adapter.kotlin.coroutines.experimental.CoroutineCallAdapterFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.experimental.Deferred
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface GitHubApi {

    @GET("/search/repositories?sort=updated")
    fun searchRepos(@Query("q") query: String): Deferred<GitHubRepos>

    companion object {
        val api: GitHubApi = Retrofit.Builder()
            .baseUrl("https://api.github.com")
            .addConverterFactory(
                MoshiConverterFactory.create(
                    Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
                )
            )
            .addCallAdapterFactory(CoroutineCallAdapterFactory())
            .build().create(GitHubApi::class.java)
    }
}