package com.watchlist.anihub.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.watchlist.anihub.data.local.WatchlistStatus
import com.watchlist.anihub.ui.screens.WatchlistSort
import com.watchlist.anihub.ui.theme.AiringFormat
import com.watchlist.anihub.ui.theme.ColorPalette
import com.watchlist.anihub.ui.theme.ScoreFormat
import com.watchlist.anihub.ui.theme.StaffNameLanguage
import com.watchlist.anihub.ui.theme.ThemeMode
import com.watchlist.anihub.ui.theme.TitleLanguage
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class ThemeManager @Inject constructor(
    @param:ApplicationContext private val context: Context,
) {
    private val themeModeKey = stringPreferencesKey("theme_mode")
    private val colorPaletteKey = stringPreferencesKey("color_palette")
    private val titleLanguageKey = stringPreferencesKey("title_language")
    private val staffLanguageKey = stringPreferencesKey("staff_language")
    private val scoreFormatKey = stringPreferencesKey("score_format")
    private val airingFormatKey = stringPreferencesKey("airing_format")
    private val adultContentKey = booleanPreferencesKey("adult_content")
    private val showAiringCountdownKey = booleanPreferencesKey("show_airing_countdown")
    private val notificationsKey = booleanPreferencesKey("notifications_enabled")

    // Watchlist Personalization
    private val watchlistFilterKey = stringPreferencesKey("watchlist_filter")
    private val watchlistSortKey = stringPreferencesKey("watchlist_sort")
    private val watchlistItemsPerRowKey = intPreferencesKey("watchlist_items_per_row")

    val themeMode: Flow<ThemeMode> = context.dataStore.data.map { preferences ->
        val mode = preferences[themeModeKey] ?: ThemeMode.SYSTEM.name
        runCatching { ThemeMode.valueOf(mode) }.getOrDefault(ThemeMode.SYSTEM)
    }.distinctUntilChanged()

    val colorPalette: Flow<ColorPalette> = context.dataStore.data.map { preferences ->
        val palette = preferences[colorPaletteKey] ?: ColorPalette.DYNAMIC.name
        runCatching { ColorPalette.valueOf(palette) }.getOrDefault(ColorPalette.DYNAMIC)
    }.distinctUntilChanged()

    val titleLanguage: Flow<TitleLanguage> = context.dataStore.data.map { preferences ->
        val language = preferences[titleLanguageKey] ?: TitleLanguage.ROMAJI.name
        runCatching { TitleLanguage.valueOf(language) }.getOrDefault(TitleLanguage.ROMAJI)
    }.distinctUntilChanged()

    val staffLanguage: Flow<StaffNameLanguage> = context.dataStore.data.map { preferences ->
        val language = preferences[staffLanguageKey] ?: StaffNameLanguage.ROMAJI_WESTERN.name
        runCatching { StaffNameLanguage.valueOf(language) }.getOrDefault(StaffNameLanguage.ROMAJI_WESTERN)
    }.distinctUntilChanged()

    val scoreFormat: Flow<ScoreFormat> = context.dataStore.data.map { preferences ->
        val format = preferences[scoreFormatKey] ?: ScoreFormat.POINT_10.name
        runCatching { ScoreFormat.valueOf(format) }.getOrDefault(ScoreFormat.POINT_10)
    }.distinctUntilChanged()

    val airingFormat: Flow<AiringFormat> = context.dataStore.data.map { preferences ->
        val format = preferences[airingFormatKey] ?: AiringFormat.COUNTDOWN.name
        runCatching { AiringFormat.valueOf(format) }.getOrDefault(AiringFormat.COUNTDOWN)
    }.distinctUntilChanged()

    val adultContent: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[adultContentKey] ?: false
    }.distinctUntilChanged()

    val showAiringCountdown: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[showAiringCountdownKey] ?: true
    }.distinctUntilChanged()

    val notificationsEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[notificationsKey] ?: true
    }.distinctUntilChanged()

    val watchlistFilterStatus: Flow<WatchlistStatus?> = context.dataStore.data.map { preferences ->
        preferences[watchlistFilterKey]?.let { runCatching { WatchlistStatus.valueOf(it) }.getOrNull() }
    }.distinctUntilChanged()

    val watchlistSortOrder: Flow<WatchlistSort> = context.dataStore.data.map { preferences ->
        val sort = preferences[watchlistSortKey] ?: WatchlistSort.LAST_ADDED.name
        runCatching { WatchlistSort.valueOf(sort) }.getOrDefault(WatchlistSort.LAST_ADDED)
    }.distinctUntilChanged()

    val watchlistItemsPerRow: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[watchlistItemsPerRowKey] ?: 2
    }.distinctUntilChanged()

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { preferences ->
            preferences[themeModeKey] = mode.name
        }
    }

    suspend fun setColorPalette(palette: ColorPalette) {
        context.dataStore.edit { preferences ->
            preferences[colorPaletteKey] = palette.name
        }
    }

    suspend fun setTitleLanguage(language: TitleLanguage) {
        context.dataStore.edit { preferences ->
            preferences[titleLanguageKey] = language.name
        }
    }

    suspend fun setStaffLanguage(language: StaffNameLanguage) {
        context.dataStore.edit { preferences ->
            preferences[staffLanguageKey] = language.name
        }
    }

    suspend fun setScoreFormat(format: ScoreFormat) {
        context.dataStore.edit { preferences ->
            preferences[scoreFormatKey] = format.name
        }
    }

    suspend fun setAiringFormat(format: AiringFormat) {
        context.dataStore.edit { preferences ->
            preferences[airingFormatKey] = format.name
        }
    }

    suspend fun setAdultContent(show: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[adultContentKey] = show
        }
    }

    suspend fun setShowAiringCountdown(show: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[showAiringCountdownKey] = show
        }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[notificationsKey] = enabled
        }
    }

    suspend fun setWatchlistFilterStatus(status: WatchlistStatus?) {
        context.dataStore.edit { preferences ->
            if (status == null) {
                preferences.remove(watchlistFilterKey)
            } else {
                preferences[watchlistFilterKey] = status.name
            }
        }
    }

    suspend fun setWatchlistSortOrder(sort: WatchlistSort) {
        context.dataStore.edit { preferences ->
            preferences[watchlistSortKey] = sort.name
        }
    }

    suspend fun setWatchlistItemsPerRow(count: Int) {
        context.dataStore.edit { preferences ->
            preferences[watchlistItemsPerRowKey] = count
        }
    }
}
