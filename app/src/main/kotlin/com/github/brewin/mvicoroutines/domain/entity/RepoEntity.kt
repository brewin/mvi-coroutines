package com.github.brewin.mvicoroutines.domain.entity

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class RepoEntity(val name: String, val url: String) : Parcelable
