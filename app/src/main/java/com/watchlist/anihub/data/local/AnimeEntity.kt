package com.watchlist.anihub.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "watchlist")
data class AnimeEntity(
    @PrimaryKey val id: Int,
    val title: String,
    val imageUrl: String,
    val isFavorite: Boolean = false,
    val lastNotifiedEpisode: Int = 0,
    val status: WatchlistStatus = WatchlistStatus.PLAN_TO_WATCH,
    val detailJson: String? = null,
    val addedAt: Long = System.currentTimeMillis()
)
