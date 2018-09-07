package com.github.brewin.mvicoroutines.model

import android.net.Uri
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class RepoItem(val name: String, val url: Uri) : Parcelable
