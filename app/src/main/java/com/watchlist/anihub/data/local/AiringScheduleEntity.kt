package com.watchlist.anihub.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "airing_schedule")
data class AiringScheduleEntity(
    @PrimaryKey val id: Int,
    val animeId: Int,
    val episode: Int,
    val airingAt: Long,
    val title: String,
    val imageUrl: String
)
