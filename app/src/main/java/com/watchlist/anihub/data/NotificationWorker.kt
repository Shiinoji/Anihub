package com.watchlist.anihub.data

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.watchlist.anihub.data.local.AnimeDao
import com.watchlist.anihub.data.local.NotificationEntity
import com.watchlist.anihub.data.remote.AniListQueries
import com.watchlist.anihub.data.remote.AniListService
import com.watchlist.anihub.data.remote.GraphQLRequest
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first

@HiltWorker
class NotificationWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val aniListService: AniListService,
    private val animeDao: AnimeDao,
    private val notificationHelper: NotificationHelper
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        try {
            val watchlist = animeDao.getWatchlist().first()
            if (watchlist.isEmpty()) return Result.success()

            val ids = watchlist.map { it.id }
            val response = aniListService.getAnimeList(
                GraphQLRequest(AniListQueries.AIRING_CHECK, mapOf("ids" to ids))
            )

            response.data.Page.media.forEach { media ->
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

    private suspend fun checkNewReleases() {
        try {
            val response = aniListService.getAnimeList(
                GraphQLRequest(AniListQueries.TRENDING_NOW, mapOf("page" to 1, "perPage" to 5, "isAdult" to false))
            )
            val watchlistIds = animeDao.getWatchlist().first().map { it.id }
            
            response.data.Page.media.forEach { media ->
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
