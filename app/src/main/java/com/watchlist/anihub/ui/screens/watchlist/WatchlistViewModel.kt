package com.watchlist.anihub.ui.screens.watchlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.watchlist.anihub.data.ThemeManager
import com.watchlist.anihub.data.local.AnimeDao
import com.watchlist.anihub.data.local.AnimeEntity
import com.watchlist.anihub.data.local.WatchlistStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Watchlist screen, managing the user's personal collection,
 * including filtering, sorting, and favoriting.
 */
@HiltViewModel
class WatchlistViewModel @Inject constructor(
    private val animeDao: AnimeDao,
    private val themeManager: ThemeManager
) : ViewModel() {

    /**
     * Current status filter (e.g., Watching, Finished). If null, all items are shown.
     */
    val filterStatus = themeManager.watchlistFilterStatus.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), null
    )

    /**
     * Current sort criteria for the watchlist.
     */
    val sortOrder = themeManager.watchlistSortOrder.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), WatchlistSort.LAST_ADDED
    )

    /**
     * User preference for the number of columns in the watchlist grid.
     */
    val itemsPerRow = themeManager.watchlistItemsPerRow.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), 2
    )

    /**
     * The processed watchlist, combining local data with current filters and sorting.
     */
    val watchlist = combine(
        animeDao.getWatchlist(),
        filterStatus,
        sortOrder
    ) { list, filter, sort ->
        var filteredList = if (filter != null) {
            list.filter { it.status == filter }
        } else {
            list
        }

        when (sort) {
            WatchlistSort.ALPHABETICAL -> filteredList = filteredList.sortedBy { it.title }
            WatchlistSort.LAST_ADDED -> filteredList = filteredList.sortedByDescending { it.addedAt }
            WatchlistSort.DATE_ADDED -> filteredList = filteredList.sortedBy { it.addedAt }
        }
        filteredList
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /**
     * List of favorite anime, sorted according to user preference.
     */
    val favorites = combine(
        animeDao.getFavorites(),
        sortOrder
    ) { list, sort ->
        var sortedList = list
        when (sort) {
            WatchlistSort.ALPHABETICAL -> sortedList = sortedList.sortedBy { it.title }
            WatchlistSort.LAST_ADDED -> sortedList = sortedList.sortedByDescending { it.addedAt }
            WatchlistSort.DATE_ADDED -> sortedList = sortedList.sortedBy { it.addedAt }
        }
        sortedList
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setFilterStatus(status: WatchlistStatus?) {
        viewModelScope.launch {
            themeManager.setWatchlistFilterStatus(status)
        }
    }

    fun setSortOrder(sort: WatchlistSort) {
        viewModelScope.launch {
            themeManager.setWatchlistSortOrder(sort)
        }
    }

    fun setItemsPerRow(count: Int) {
        viewModelScope.launch {
            themeManager.setWatchlistItemsPerRow(count)
        }
    }

    /**
     * Toggles the favorite status of a specific anime entry.
     */
    fun toggleFavorite(anime: AnimeEntity) {
        viewModelScope.launch {
            animeDao.insertAnime(anime.copy(isFavorite = !anime.isFavorite))
        }
    }

    /**
     * Removes an anime from the user's collection.
     */
    fun removeFromWatchlist(anime: AnimeEntity) {
        viewModelScope.launch {
            animeDao.deleteAnime(anime)
        }
    }
}
