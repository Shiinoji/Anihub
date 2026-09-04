package com.watchlist.anihub.data

import com.watchlist.anihub.BuildConfig
import com.watchlist.anihub.data.remote.GitHubReleaseApi
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UpdateChecker @Inject constructor(
    private val gitHubReleaseApi: GitHubReleaseApi,
    private val themeManager: ThemeManager
) {
    /**
     * Checks if a newer version exists on GitHub. Returns the release info if an update is available.
     */
    suspend fun getLatestReleaseIfAvailable(): com.watchlist.anihub.data.remote.GitHubRelease? {
        return try {
            val release = gitHubReleaseApi.getLatestRelease()
            
            // Ignore prereleases
            if (release.isPrerelease) return null
            
            val latestVersion = release.tagName.removePrefix("v").removePrefix("V")
            val currentVersion = BuildConfig.VERSION_NAME.removePrefix("v").removePrefix("V")
            
            if (isNewerVersion(latestVersion, currentVersion)) {
                val lastNotified = themeManager.lastNotifiedUpdateVersion.first()
                if (lastNotified != release.tagName) {
                    return release
                }
            }
            null
        } catch (e: Exception) {
            null
        }
    }

    private fun isNewerVersion(latest: String, current: String): Boolean {
        val latestParts = latest.split(".").map { it.toIntOrNull() ?: 0 }
        val currentParts = current.split(".").map { it.toIntOrNull() ?: 0 }
        
        for (i in 0 until maxOf(latestParts.size, currentParts.size)) {
            val l = latestParts.getOrElse(i) { 0 }
            val c = currentParts.getOrElse(i) { 0 }
            if (l > c) return true
            if (l < c) return false
        }
        return false
    }
}
