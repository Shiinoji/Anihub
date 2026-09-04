package com.watchlist.anihub.domain

import com.watchlist.anihub.BuildConfig
import com.watchlist.anihub.data.remote.GitHubReleaseApi
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UpdateManager @Inject constructor(
    private val gitHubReleaseApi: GitHubReleaseApi
) {
    suspend fun checkForUpdates(): String? {
        return try {
            val latestRelease = gitHubReleaseApi.getLatestRelease()
            val currentVersion = "v${BuildConfig.VERSION_NAME}"
            if (latestRelease.tagName > currentVersion) {
                latestRelease.htmlUrl
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}