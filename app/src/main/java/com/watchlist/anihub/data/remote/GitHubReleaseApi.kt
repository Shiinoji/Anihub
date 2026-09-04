package com.watchlist.anihub.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.http.GET

@JsonClass(generateAdapter = true)
data class GitHubRelease(
    @Json(name = "tag_name") val tagName: String,
    val name: String,
    @Json(name = "html_url") val htmlUrl: String,
    val body: String,
    @Json(name = "prerelease") val isPrerelease: Boolean
)

interface GitHubReleaseApi {
    @GET("repos/Shiinoji/Anihub/releases/latest")
    suspend fun getLatestRelease(): GitHubRelease
}
