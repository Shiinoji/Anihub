package com.watchlist.anihub.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.watchlist.anihub.data.local.AnimeDao
import com.watchlist.anihub.data.local.AnimeEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WatchlistViewModel @Inject constructor(
    private val animeDao: AnimeDao
) : ViewModel() {

    val watchlist = animeDao.getWatchlist().stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val favorites = animeDao.getFavorites().stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

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
