package com.watchlist.anihub.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AnimeDao {
    @Query("SELECT * FROM watchlist")
    fun getWatchlist(): Flow<List<AnimeEntity>>

    @Query("SELECT * FROM watchlist WHERE isFavorite = 1")
    fun getFavorites(): Flow<List<AnimeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnime(anime: AnimeEntity)

    @Delete
    suspend fun deleteAnime(anime: AnimeEntity)

    @Query("SELECT EXISTS(SELECT * FROM watchlist WHERE id = :id)")
    suspend fun isAnimeInWatchlist(id: Int): Boolean

    @Query("SELECT isFavorite FROM watchlist WHERE id = :id")
    suspend fun getFavoriteStatus(id: Int): Boolean?

    @Query("SELECT * FROM watchlist WHERE id = :id")
    suspend fun getAnime(id: Int): AnimeEntity?

    // History
    @Query("SELECT * FROM history ORDER BY timestamp DESC")
    fun getHistory(): Flow<List<HistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: HistoryEntity)

    @Query("DELETE FROM history")
    suspend fun clearHistory()

    // Notifications
    @Query("SELECT * FROM notifications ORDER BY timestamp DESC")
    fun getNotifications(): Flow<List<NotificationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity)

    @Query("UPDATE notifications SET isRead = 1 WHERE id = :id")
    suspend fun markAsRead(id: Int)

    @Query("DELETE FROM notifications")
    suspend fun clearNotifications()
}
