package com.watchlist.anihub.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.watchlist.anihub.data.ThemeManager
import com.watchlist.anihub.domain.UpdateManager
import com.watchlist.anihub.ui.theme.AiringFormat
import com.watchlist.anihub.ui.theme.ColorPalette
import com.watchlist.anihub.ui.theme.ScoreFormat
import com.watchlist.anihub.ui.theme.StaffNameLanguage
import com.watchlist.anihub.ui.theme.ThemeMode
import com.watchlist.anihub.ui.theme.TitleLanguage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Global ViewModel responsible for managing application-wide settings, themes,
 * and background updates. This ViewModel is typically scoped to the Activity.
 */
@HiltViewModel
class ThemeViewModel @Inject constructor(
    private val themeManager: ThemeManager,
    private val updateManager: UpdateManager,
) : ViewModel() {
    
    private val _updateState = MutableStateFlow<UpdateState>(UpdateState.Idle)
    val updateState: StateFlow<UpdateState> = _updateState.asStateFlow()

    // Observable preference states
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
    val dynamicTheme: StateFlow<Boolean> = themeManager.dynamicTheme.stateIn(
        scope = viewModelScope, 
        started = SharingStarted.WhileSubscribed(5000), 
        initialValue = true
    )
    val showAiringCountdown: StateFlow<Boolean> = themeManager.showAiringCountdown.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), true
    )
    val notificationsEnabled: StateFlow<Boolean> = themeManager.notificationsEnabled.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), true
    )
    val homeItemsPerRow: StateFlow<Int> = themeManager.homeItemsPerRow.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), 2
    )
    val displayScale: StateFlow<Float> = themeManager.displayScale.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), 1.0f
    )

    // Update methods for persisting preferences
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

    fun setDynamicTheme(enabled: Boolean) {
        viewModelScope.launch {
            themeManager.setDynamicTheme(enabled)
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

    /**
     * Updates the column count for the Home Discover grid.
     */
    fun setHomeItemsPerRow(count: Int) {
        viewModelScope.launch {
            themeManager.setHomeItemsPerRow(count)
        }
    }

    fun setDisplayScale(scale: Float) {
        viewModelScope.launch {
            themeManager.setDisplayScale(scale)
        }
    }

    /**
     * Triggers an asynchronous check for application updates.
     */
    fun checkForUpdates() {
        viewModelScope.launch {
            _updateState.value = UpdateState.Checking
            val updateUrl = updateManager.checkForUpdates()
            if (updateUrl != null) {
                _updateState.value = UpdateState.UpdateAvailable(updateUrl)
            } else {
                _updateState.value = UpdateState.UpToDate
            }
        }
    }

    fun resetUpdateState() {
        _updateState.value = UpdateState.Idle
    }
}
