package com.watchlist.anihub.data.remote

import retrofit2.http.GET
import kotlinx.serialization.Serializable

@Serializable
data class GitHubRelease(
    val tag_name: String,
    val html_url: String
)

interface GitHubReleaseApi {
    @GET("repos/Shiinoji/Anihub/releases/latest")
    suspend fun getLatestRelease(): GitHubRelease
}