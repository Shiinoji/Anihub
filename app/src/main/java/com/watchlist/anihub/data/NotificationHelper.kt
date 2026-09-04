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

/**
 * Utility class for creating and displaying system notifications for anime-related events.
 * Handles notification channels and pending intents for deep-linking into the app.
 */
@Singleton
class NotificationHelper @Inject constructor(
    @param:ApplicationContext private val context: Context,
) {
    private val channelId = "anime_updates"

    init {
        createNotificationChannel()
    }

    /**
     * Creates the required notification channel for Android 8.0+.
     */
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

    /**
     * Displays a notification when a new episode of a watched anime airs.
     * Tapping the notification opens the AnimeDetailScreen for the specific anime.
     */
    fun showEpisodeNotification(animeTitle: String, episodeNumber: Int, animeId: Int) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("animeId", animeId)
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            context, animeId, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.anihub)
            .setContentTitle("New Episode: $animeTitle")
            .setContentText("Episode $episodeNumber is now available!")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(context)) {
            try {
                notify(animeId, builder.build())
            } catch (e: SecurityException) {
                // Occurs if the app lacks post-notification permissions
                e.printStackTrace()
            }
        }
    }

    /**
     * Displays a notification for a newly trending anime recommendation.
     */
    fun showNewAnimeNotification(animeTitle: String, animeId: Int) {
        val intent = createDetailIntent(animeId)
        val pendingIntent = createPendingIntent(animeId + 1000000, intent)

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.anihub)
            .setContentTitle("New Trending Anime")
            .setContentText("$animeTitle is trending now. Check it out!")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        notify(animeId + 1000000, builder.build())
    }

    /**
     * Displays a notification for an application update.
     */
    fun showUpdateNotification(title: String, message: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = createPendingIntent(999999, intent)

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.anihub)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        notify(999999, builder.build())
    }

    private fun createDetailIntent(animeId: Int): Intent {
        return Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("animeId", animeId)
        }
    }

    private fun createPendingIntent(id: Int, intent: Intent): PendingIntent {
        return PendingIntent.getActivity(
            context, id, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    private fun notify(id: Int, notification: android.app.Notification) {
        with(NotificationManagerCompat.from(context)) {
            try {
                notify(id, notification)
            } catch (e: SecurityException) {
                e.printStackTrace()
            }
        }
    }
}
