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
    private val updateUrl = "https://raw.githubusercontent.com/Shiinoji/Anihub/main/version.json"

    suspend fun checkForUpdates() {
        try {
            val latestUpdate = aniListService.checkForUpdate(updateUrl)
            
            if (latestUpdate.versionCode > BuildConfig.VERSION_CODE) {
                val existingNotifications = animeDao.getNotifications().first()
                val alreadyNotified = existingNotifications.any { 
                    it.type == "APP_UPDATE" && it.title.contains(latestUpdate.versionName) 
                }

                if (!alreadyNotified) {
                    notificationHelper.showUpdateNotification(
                        "App Update Available",
                        "AniHub ${latestUpdate.versionName} is here! Check the changelog."
                    )

                    animeDao.insertNotification(
                        NotificationEntity(
                            type = "APP_UPDATE",
                            title = "AniHub ${latestUpdate.versionName} is here!",
                            message = latestUpdate.changelog
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
