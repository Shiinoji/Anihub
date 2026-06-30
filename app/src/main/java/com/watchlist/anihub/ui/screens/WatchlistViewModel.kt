package com.watchlist.anihub.ui.screens

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

@HiltViewModel
class WatchlistViewModel @Inject constructor(
    private val animeDao: AnimeDao,
    private val themeManager: ThemeManager
) : ViewModel() {

    val filterStatus = themeManager.watchlistFilterStatus.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), null
    )

    val sortOrder = themeManager.watchlistSortOrder.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), WatchlistSort.LAST_ADDED
    )

    val itemsPerRow = themeManager.watchlistItemsPerRow.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), 2
    )

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

    fun toggleFavorite(anime: AnimeEntity) {
        viewModelScope.launch {
            animeDao.insertAnime(anime.copy(isFavorite = !anime.isFavorite))
        }
    }

    fun removeFromWatchlist(anime: AnimeEntity) {
        viewModelScope.launch {
            animeDao.deleteAnime(anime)
        }
    }
}
