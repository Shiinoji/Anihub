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

/**
 * Background worker that periodically syncs airing schedules, checks for new episodes
 * of watched anime, discovers trending releases, and checks for app updates.
 */
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

    /**
     * Primary background task execution. 
     * Performs a series of synchronization and checking operations.
     */
    override suspend fun doWork(): Result {
        try {
            // Check if user has enabled notifications in settings
            if (!themeManager.notificationsEnabled.first()) return Result.success()

            // 1. Check for remote App Updates
            updateManager.checkForUpdates()

            // 2. Refresh local Airing Schedule (2-week window)
            fetchAiringSchedule()

            val watchlist = animeDao.getWatchlist().first()
            if (watchlist.isEmpty()) return Result.success()

            // 3. Check for new episodes of anime in the user's watchlist
            // Chunk watchlist check in groups of 50 to handle large collections reliably
            watchlist.chunked(50).forEach { chunk ->
                val ids = chunk.map { it.id }
                val response = aniListService.getAnimeList(
                    GraphQLRequest(AniListQueries.AIRING_CHECK, mapOf("ids" to ids))
                )

                response.data?.page?.media?.forEach { media ->
                    val localAnime = watchlist.find { it.id == media.id } ?: return@forEach
                    
                    // nextAiringEpisode is null for finished anime or those between seasons
                    val currentEpisode = media.nextAiringEpisode?.episode?.minus(1) 
                        ?: if (media.status == "FINISHED") media.episodes ?: 0 else 0
                    
                    // Notify only if a new episode has aired since the last check
                    if (currentEpisode > localAnime.lastNotifiedEpisode && currentEpisode > 0) {
                        notificationHelper.showEpisodeNotification(
                            animeTitle = media.title.displayTitle,
                            episodeNumber = currentEpisode,
                            animeId = media.id
                        )
                        // Persist notification for the in-app inbox
                        animeDao.insertNotification(
                            NotificationEntity(
                                type = "EPISODE",
                                title = "New Episode: ${media.title.displayTitle}",
                                message = "Episode $currentEpisode is now available!",
                                animeId = media.id,
                                imageUrl = media.coverImage.medium
                            )
                        )
                        // Sync the last notified episode locally to prevent duplicate alerts
                        animeDao.insertAnime(localAnime.copy(lastNotifiedEpisode = currentEpisode))
                    }
                }
            }

            // 4. Discover and notify about globally trending releases
            checkNewReleases()

            return Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            // Allow WorkManager to reschedule the task later if a network error occurred
            return Result.retry()
        }
    }

    /**
     * Fetches the upcoming 14-day airing schedule from AniList and caches it locally.
     */
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
            
            // Paginate through AniList response to capture full schedule
            for (page in 1..6) {
                val response = aniListService.getAnimeList(
                    GraphQLRequest(
                        AniListQueries.AIRING_SCHEDULE,
                        mapOf("start" to start, "end" to end, "page" to page)
                    )
                )

                val schedules = response.data?.page?.airingSchedules?.map { 
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
                // Purge stale data older than 2 days
                animeDao.deleteOldSchedules(start - TimeUnit.DAYS.toSeconds(2))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Checks for trending anime releases and notifies the user if it's not already in their watchlist.
     */
    private suspend fun checkNewReleases() {
        try {
            val response = aniListService.getAnimeList(
                GraphQLRequest(AniListQueries.TRENDING_NOW, mapOf("page" to 1, "perPage" to 5, "isAdult" to false))
            )
            val watchlistIds = animeDao.getWatchlist().first().map { it.id }
            
            response.data?.page?.media?.forEach { media ->
                if (!watchlistIds.contains(media.id)) {
                    // Only notify if this is a fresh recommendation the user hasn't seen
                    if (!animeDao.hasNotification(media.id, "ANIME_RELEASE")) {
                        notificationHelper.showNewAnimeNotification(
                            animeTitle = media.title.displayTitle,
                            animeId = media.id
                        )
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
            }
        } catch (e: Exception) {}
    }
}
