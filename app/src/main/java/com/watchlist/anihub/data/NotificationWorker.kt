package com.watchlist.anihub.data

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.watchlist.anihub.data.local.AiringScheduleEntity
import com.watchlist.anihub.data.local.AnimeDao
import com.watchlist.anihub.data.local.NotificationEntity
import com.watchlist.anihub.data.remote.AniListQueries
import com.watchlist.anihub.data.remote.AniListService
import com.watchlist.anihub.data.remote.GraphQLRequest
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit

@HiltWorker
class NotificationWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val aniListService: AniListService,
    private val animeDao: AnimeDao,
    private val notificationHelper: NotificationHelper,
    private val updateManager: UpdateManager,
    private val themeManager: ThemeManager
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        try {
            // Respect notification settings
            if (!themeManager.notificationsEnabled.first()) return Result.success()

            // Check for App Updates via GitHub
            updateManager.checkForUpdates()

            // Fetch Airing Schedule (2 weeks ahead)
            fetchAiringSchedule()

            val watchlist = animeDao.getWatchlist().first()
            if (watchlist.isEmpty()) return Result.success()

            val ids = watchlist.map { it.id }
            val response = aniListService.getAnimeList(
                GraphQLRequest(AniListQueries.AIRING_CHECK, mapOf("ids" to ids))
            )

            response.data.page.media?.forEach { media ->
                val localAnime = watchlist.find { it.id == media.id } ?: return@forEach
                val currentEpisode = media.nextAiringEpisode?.episode?.minus(1) ?: 0
                
                if (currentEpisode > localAnime.lastNotifiedEpisode && currentEpisode > 0) {
                    notificationHelper.showEpisodeNotification(
                        animeTitle = media.title.displayTitle,
                        episodeNumber = currentEpisode,
                        animeId = media.id
                    )
                    // Add to in-app notifications
                    animeDao.insertNotification(
                        NotificationEntity(
                            type = "EPISODE",
                            title = "New Episode: ${media.title.displayTitle}",
                            message = "Episode $currentEpisode is now available!",
                            animeId = media.id,
                            imageUrl = media.coverImage.medium
                        )
                    )
                    // Update last notified episode
                    animeDao.insertAnime(localAnime.copy(lastNotifiedEpisode = currentEpisode))
                }
            }

            // Check for new anime release (e.g. check trending)
            checkNewReleases()

            return Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            return Result.retry()
        }
    }

    private suspend fun fetchAiringSchedule() {
        try {
            val start = (java.util.Calendar.getInstance().apply {
                set(java.util.Calendar.HOUR_OF_DAY, 0)
                set(java.util.Calendar.MINUTE, 0)
                set(java.util.Calendar.SECOND, 0)
                set(java.util.Calendar.MILLISECOND, 0)
            }.timeInMillis / 1000).toInt()
            val end = start + TimeUnit.DAYS.toSeconds(14).toInt()
            
            val allSchedules = mutableListOf<AiringScheduleEntity>()
            
            // Fetch up to 6 pages to get more airing data
            for (page in 1..6) {
                val response = aniListService.getAnimeList(
                    GraphQLRequest(
                        AniListQueries.AIRING_SCHEDULE,
                        mapOf("start" to start, "end" to end, "page" to page)
                    )
                )

                val schedules = response.data.page.airingSchedules?.map { 
                    AiringScheduleEntity(
                        id = it.id,
                        animeId = it.media.id,
                        episode = it.episode,
                        airingAt = it.airingAt,
                        title = it.media.title.displayTitle,
                        imageUrl = it.media.coverImage.large ?: ""
                    )
                } ?: emptyList()

                if (schedules.isEmpty()) break
                allSchedules.addAll(schedules)
                if (schedules.size < 50) break
            }

            if (allSchedules.isNotEmpty()) {
                animeDao.insertAiringSchedules(allSchedules)
                // Clean up old schedules (older than 2 days ago)
                animeDao.deleteOldSchedules(start - TimeUnit.DAYS.toSeconds(2))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private suspend fun checkNewReleases() {
        try {
            val response = aniListService.getAnimeList(
                GraphQLRequest(AniListQueries.TRENDING_NOW, mapOf("page" to 1, "perPage" to 5, "isAdult" to false))
            )
            val watchlistIds = animeDao.getWatchlist().first().map { it.id }
            
            response.data.page.media?.forEach { media ->
                if (!watchlistIds.contains(media.id)) {
                    // Notify about a new trending anime
                    animeDao.insertNotification(
                        NotificationEntity(
                            type = "ANIME_RELEASE",
                            title = "New Trending: ${media.title.displayTitle}",
                            message = "Everyone is talking about this! check it out.",
                            animeId = media.id,
                            imageUrl = media.coverImage.medium
                        )
                    )
                }
            }
        } catch (e: Exception) {}
    }
}
