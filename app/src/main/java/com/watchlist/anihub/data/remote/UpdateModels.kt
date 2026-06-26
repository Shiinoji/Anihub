package com.watchlist.anihub.data.remote

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UpdateInfo(
    val versionCode: Int,
    val versionName: String,
    val changelog: String,
    val downloadUrl: String? = null
)
