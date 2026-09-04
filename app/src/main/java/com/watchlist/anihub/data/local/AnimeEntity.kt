package com.watchlist.anihub.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room Database entity representing an anime in the user's local collection (Watchlist or Favorites).
 */
@Entity(tableName = "watchlist")
data class AnimeEntity(
    /** Unique identifier for the anime, matching AniList and MAL IDs. */
    @PrimaryKey val id: Int,
    
    /** Display title of the anime. */
    val title: String,
    
    /** URL to the high-resolution cover image. */
    val imageUrl: String,
    
    /** Whether the anime is marked as a favorite by the user. */
    val isFavorite: Boolean = false,
    
    /** The last episode for which the user was successfully notified. */
    val lastNotifiedEpisode: Int = 0,
    
    /** Current tracking status (e.g., Watching, Plan to Watch). */
    val status: WatchlistStatus = WatchlistStatus.PLAN_TO_WATCH,
    
    /** Complete JSON representation of the Media object for offline detail viewing. */
    val detailJson: String? = null,
    
    /** The user's personal score for the anime (1.0 to 10.0). */
    val userScore: Double? = null,
    
    /** Number of episodes the user has watched so far. */
    val episodesWatched: Int? = null,
    
    /** User-specified date when they started watching this anime (YYYY-MM-DD). */
    val startDate: String? = null,
    
    /** User-specified date when they finished watching this anime (YYYY-MM-DD). */
    val finishDate: String? = null,
    
    /** Timestamp when the anime was added to the local database. */
    val addedAt: Long = System.currentTimeMillis()
)
