package com.watchlist.anihub.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val type: String, // "EPISODE", "ANIME_RELEASE", "APP_UPDATE"
    val title: String,
    val message: String,
    val animeId: Int? = null,
    val imageUrl: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)
