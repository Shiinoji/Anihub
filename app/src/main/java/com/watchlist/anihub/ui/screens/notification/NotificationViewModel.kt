package com.watchlist.anihub.ui.screens.notification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.watchlist.anihub.data.local.AnimeDao
import com.watchlist.anihub.data.local.NotificationEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Notifications screen, managing the user's in-app inbox
 * for episode alerts, app updates, and new releases.
 */
@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val animeDao: AnimeDao
) : ViewModel() {

    /**
     * Observable flow of in-app notifications, sorted by most recent first.
     */
    val notifications = animeDao.getNotifications().stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    /**
     * Updates a specific notification's status to 'read' in the local database.
     */
    fun markAsRead(id: Int) {
        viewModelScope.launch {
            animeDao.markAsRead(id)
        }
    }

    /**
     * Deletes all notifications from the local inbox.
     */
    fun clearNotifications() {
        viewModelScope.launch {
            animeDao.clearNotifications()
        }
    }

    /**
     * Utility function for testing: adds a mock app update notification to the database.
     */
    fun addMockUpdate() {
        viewModelScope.launch {
            animeDao.insertNotification(
                NotificationEntity(
                    type = "APP_UPDATE",
                    title = "AniHub v1.1 Update",
                    message = "We've added a new Notification screen and improved performance!"
                )
            )
        }
    }
}
