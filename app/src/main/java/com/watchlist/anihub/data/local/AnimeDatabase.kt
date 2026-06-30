package com.watchlist.anihub.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        AnimeEntity::class,
        HistoryEntity::class,
        NotificationEntity::class,
        AiringScheduleEntity::class
    ],
    version = 8
)
abstract class AnimeDatabase : RoomDatabase() {
    abstract val animeDao: AnimeDao
}
