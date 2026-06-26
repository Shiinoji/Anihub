package com.watchlist.anihub.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.watchlist.anihub.data.local.AnimeDao
import com.watchlist.anihub.data.local.NotificationEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val animeDao: AnimeDao
) : ViewModel() {

    val notifications = animeDao.getNotifications().stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    fun markAsRead(id: Int) {
        viewModelScope.launch {
            animeDao.markAsRead(id)
        }
    }

    fun clearNotifications() {
        viewModelScope.launch {
            animeDao.clearNotifications()
        }
    }

    // Mock an app update notification for demonstration
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
