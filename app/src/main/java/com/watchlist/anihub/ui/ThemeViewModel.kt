package com.watchlist.anihub.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.watchlist.anihub.data.ThemeManager
import com.watchlist.anihub.data.UpdateManager
import com.watchlist.anihub.ui.theme.AiringFormat
import com.watchlist.anihub.ui.theme.ColorPalette
import com.watchlist.anihub.ui.theme.ScoreFormat
import com.watchlist.anihub.ui.theme.StaffNameLanguage
import com.watchlist.anihub.ui.theme.ThemeMode
import com.watchlist.anihub.ui.theme.TitleLanguage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ThemeViewModel @Inject constructor(
    private val themeManager: ThemeManager,
    private val updateManager: UpdateManager,
) : ViewModel() {
    val themeMode = themeManager.themeMode.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), ThemeMode.SYSTEM
    )
    val colorPalette = themeManager.colorPalette.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), ColorPalette.DYNAMIC
    )
    val titleLanguage: StateFlow<TitleLanguage> = themeManager.titleLanguage.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), TitleLanguage.ROMAJI
    )
    val staffLanguage: StateFlow<StaffNameLanguage> = themeManager.staffLanguage.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), StaffNameLanguage.ROMAJI_WESTERN
    )
    val scoreFormat: StateFlow<ScoreFormat> = themeManager.scoreFormat.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), ScoreFormat.POINT_10
    )
    val airingFormat: StateFlow<AiringFormat> = themeManager.airingFormat.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), AiringFormat.COUNTDOWN
    )
    val adultContent: StateFlow<Boolean> = themeManager.adultContent.stateIn(
        scope = viewModelScope, 
        started = SharingStarted.WhileSubscribed(5000), 
        initialValue = false
    )
    val showAiringCountdown: StateFlow<Boolean> = themeManager.showAiringCountdown.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), true
    )
    val notificationsEnabled: StateFlow<Boolean> = themeManager.notificationsEnabled.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), true
    )

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            themeManager.setThemeMode(mode)
        }
    }

    fun setColorPalette(palette: ColorPalette) {
        viewModelScope.launch {
            themeManager.setColorPalette(palette)
        }
    }

    fun setTitleLanguage(language: TitleLanguage) {
        viewModelScope.launch {
            themeManager.setTitleLanguage(language)
        }
    }

    fun setStaffLanguage(language: StaffNameLanguage) {
        viewModelScope.launch {
            themeManager.setStaffLanguage(language)
        }
    }

    fun setScoreFormat(format: ScoreFormat) {
        viewModelScope.launch {
            themeManager.setScoreFormat(format)
        }
    }

    fun setAiringFormat(format: AiringFormat) {
        viewModelScope.launch {
            themeManager.setAiringFormat(format)
        }
    }

    fun setAdultContent(show: Boolean) {
        viewModelScope.launch {
            themeManager.setAdultContent(show)
        }
    }

    fun setShowAiringCountdown(show: Boolean) {
        viewModelScope.launch {
            themeManager.setShowAiringCountdown(show)
        }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            themeManager.setNotificationsEnabled(enabled)
        }
    }

    fun checkForUpdates() {
        viewModelScope.launch {
            updateManager.checkForUpdates()
        }
    }
}
