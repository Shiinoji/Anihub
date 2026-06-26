package com.watchlist.anihub

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.*
import com.watchlist.anihub.data.NotificationWorker
import com.watchlist.anihub.data.UpdateManager
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
            3, TimeUnit.HOURS // Check every 3 hours
        )
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "AnimeUpdateWork",
            ExistingPeriodicWorkPolicy.KEEP,
            repeatingRequest
        )
    }
}
