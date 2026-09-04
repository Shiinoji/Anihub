package com.watchlist.anihub.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.watchlist.anihub.data.NotificationHelper
import com.watchlist.anihub.data.ThemeManager
import com.watchlist.anihub.data.UpdateChecker
import kotlinx.coroutines.flow.first

class UpdateCheckWorker(
    context: Context,
    workerParams: WorkerParameters,
    private val updateChecker: UpdateChecker,
    private val themeManager: ThemeManager,
    private val notificationHelper: NotificationHelper
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val notificationsEnabled = themeManager.notificationsEnabled.first()
        if (!notificationsEnabled) return Result.success()

        val release = updateChecker.getLatestReleaseIfAvailable()
        
        if (release != null) {
            notificationHelper.showUpdateNotification(
                "AniHub Update Available",
                "AniHub ${release.tagName} is now available."
            )
            themeManager.setLastNotifiedUpdateVersion(release.tagName)
        }
        
        return Result.success()
    }
}
