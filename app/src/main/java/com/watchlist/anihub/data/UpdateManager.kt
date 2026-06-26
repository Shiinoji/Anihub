package com.watchlist.anihub.data

import com.watchlist.anihub.BuildConfig
import com.watchlist.anihub.data.local.AnimeDao
import com.watchlist.anihub.data.local.NotificationEntity
import com.watchlist.anihub.data.remote.AniListService
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UpdateManager @Inject constructor(
    private val aniListService: AniListService,
    private val animeDao: AnimeDao,
    private val notificationHelper: NotificationHelper
) {
    // You should replace this with your actual raw JSON URL from GitHub
    private val updateUrl = "https://raw.githubusercontent.com/username/repo/main/version.json"

    suspend fun checkForUpdates() {
        try {
            val latestUpdate = aniListService.checkForUpdate(updateUrl)
            
            if (latestUpdate.versionCode > BuildConfig.VERSION_CODE) {
                // Check if we already notified about this specific version
                val existingNotifications = animeDao.getNotifications().first()
                val alreadyNotified = existingNotifications.any { 
                    it.type == "APP_UPDATE" && it.title.contains(latestUpdate.versionName) 
                }

                if (!alreadyNotified) {
                    val message = latestUpdate.changelog
                    
                    // Show system notification
                    notificationHelper.showEpisodeNotification(
                        "App Update Available: ${latestUpdate.versionName}",
                        0, // Not an episode
                        999999 // Fixed ID for app updates
                    )

                    // Add to in-app notifications
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
            e.printStackTrace()
        }
    }
}
