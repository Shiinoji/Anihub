package com.watchlist.anihub.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [AnimeEntity::class, HistoryEntity::class, NotificationEntity::class], version = 4)
abstract class AnimeDatabase : RoomDatabase() {
    abstract val animeDao: AnimeDao
}
