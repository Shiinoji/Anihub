package com.watchlist.anihub.data

import com.watchlist.anihub.BuildConfig
import com.watchlist.anihub.data.local.AnimeDao
import com.watchlist.anihub.data.local.NotificationEntity
import com.watchlist.anihub.data.remote.AniListService
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages application update checks by fetching version information from a remote source.
 * Notifies the user via system and in-app notifications if a newer version is available.
 */
@Singleton
class UpdateManager @Inject constructor(
    private val aniListService: AniListService,
    private val animeDao: AnimeDao,
    private val notificationHelper: NotificationHelper
) {
    // URL pointing to the raw JSON file containing the latest version metadata
    private val updateUrl = "https://raw.githubusercontent.com/username/repo/main/version.json"

    /**
     * Checks if a newer version of the app is available on GitHub.
     * Compares the remote version code with the local [BuildConfig.VERSION_CODE].
     */
    suspend fun checkForUpdates() {
        try {
            val latestUpdate = aniListService.checkForUpdate(updateUrl)
            
            if (latestUpdate.versionCode > BuildConfig.VERSION_CODE) {
                // Prevent duplicate notifications for the same version
                val existingNotifications = animeDao.getNotifications().first()
                val alreadyNotified = existingNotifications.any { 
                    it.type == "APP_UPDATE" && it.title.contains(latestUpdate.versionName) 
                }

                if (!alreadyNotified) {
                    val message = latestUpdate.changelog
                    
                    // Trigger system-level update notification
                    notificationHelper.showUpdateNotification(
                        "App Update Available",
                        "AniHub ${latestUpdate.versionName} is here! check the changelog."
                    )

                    // Persist notification in the local database for the in-app inbox
                    animeDao.insertNotification(
                        NotificationEntity(
                            type = "APP_UPDATE",
                            title = "AniHub ${latestUpdate.versionName} is here!",
                            message = message
                        )
                    )
                }
            }
        } catch (e: Exception) {
            // Silently fail if update check fails (e.g., no internet)
            e.printStackTrace()
        }
    }
}
