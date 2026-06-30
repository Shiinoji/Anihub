package com.watchlist.anihub.data

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.watchlist.anihub.MainActivity
import com.watchlist.anihub.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationHelper @Inject constructor(
    @param:ApplicationContext private val context: Context,
) {
    private val channelId = "anime_updates"

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        val name = "Anime Updates"
        val descriptionText = "Notifications for new anime episodes"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(channelId, name, importance).apply {
            description = descriptionText
        }
        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    fun showEpisodeNotification(animeTitle: String, episodeNumber: Int, animeId: Int) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("animeId", animeId)
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            context, animeId, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.bell)
            .setContentTitle("New Episode: $animeTitle")
            .setContentText("Episode $episodeNumber is now available!")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(context)) {
            try {
                notify(animeId, builder.build())
            } catch (e: SecurityException) {
                e.printStackTrace()
            }
        }
    }
}
