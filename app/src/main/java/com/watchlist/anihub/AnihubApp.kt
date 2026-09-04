package com.watchlist.anihub

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.*
import com.watchlist.anihub.data.NotificationWorker
import com.watchlist.anihub.data.UpdateManager
import com.watchlist.anihub.workers.UpdateCheckWorker
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidApp
class AnihubApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var updateManager: UpdateManager

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        setupNotificationWork()
        
        // Initial update check
        MainScope().launch {
            updateManager.checkForUpdates()
        }
    }

    private fun setupNotificationWork() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val repeatingRequest = PeriodicWorkRequestBuilder<NotificationWorker>(
            15, TimeUnit.MINUTES // High frequency (Minimum allowed by Android)
        )
            .setConstraints(constraints)
            .build()
        
        val updateCheckRequest = PeriodicWorkRequestBuilder<UpdateCheckWorker>(
            24, TimeUnit.HOURS
        )
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "AnimeUpdateWork",
            ExistingPeriodicWorkPolicy.KEEP,
            repeatingRequest
        )
        
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "AppUpdateCheckWork",
            ExistingPeriodicWorkPolicy.KEEP,
            updateCheckRequest
        )
    }
}
