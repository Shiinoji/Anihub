package com.watchlist.anihub.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "history")
data class HistoryEntity(
    @PrimaryKey val id: Int,
    val title: String,
    val imageUrl: String,
    val timestamp: Long = System.currentTimeMillis()
)
