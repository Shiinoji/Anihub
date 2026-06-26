package com.watchlist.anihub.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.watchlist.anihub.data.local.AnimeDao
import com.watchlist.anihub.data.local.AnimeEntity
import com.watchlist.anihub.data.local.HistoryEntity
import com.watchlist.anihub.data.remote.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AnimeDetailViewModel @Inject constructor(
    private val aniListService: AniListService,
    private val animeDao: AnimeDao
) : ViewModel() {

    private val _animeDetail = MutableStateFlow<Media?>(null)
    val animeDetail = _animeDetail.asStateFlow()

    private val _isInWatchlist = MutableStateFlow(false)
    val isInWatchlist = _isInWatchlist.asStateFlow()

    private val _isFavorite = MutableStateFlow(false)
    val isFavorite = _isFavorite.asStateFlow()

    fun fetchAnimeDetail(id: Int) {
        viewModelScope.launch {
            try {
                val response = aniListService.getAnimeDetail(
                    GraphQLRequest(AniListQueries.ANIME_DETAIL, mapOf("id" to id))
                )
                val media = response.data.Media
                _animeDetail.value = media
                _isInWatchlist.value = animeDao.isAnimeInWatchlist(id)
                _isFavorite.value = animeDao.getFavoriteStatus(id) ?: false
                
                // Add to history
                animeDao.insertHistory(
                    HistoryEntity(
                        id = media.id,
                        title = media.title.displayTitle,
                        imageUrl = media.coverImage.large ?: ""
                    )
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun toggleWatchlist() {
        val media = _animeDetail.value ?: return
        viewModelScope.launch {
            if (_isInWatchlist.value) {
                animeDao.deleteAnime(AnimeEntity(media.id, media.title.displayTitle, media.coverImage.extraLarge ?: "", _isFavorite.value))
            } else {
                animeDao.insertAnime(AnimeEntity(media.id, media.title.displayTitle, media.coverImage.extraLarge ?: "", _isFavorite.value))
            }
            _isInWatchlist.value = !_isInWatchlist.value
        }
    }

    fun toggleFavorite() {
        val media = _animeDetail.value ?: return
        viewModelScope.launch {
            val currentlyFavorite = _isFavorite.value
            val anime = animeDao.getAnime(media.id) ?: AnimeEntity(
                id = media.id,
                title = media.title.displayTitle,
                imageUrl = media.coverImage.extraLarge ?: "",
                isFavorite = false
            )
            
            val updatedAnime = anime.copy(isFavorite = !currentlyFavorite)
            animeDao.insertAnime(updatedAnime)
            _isFavorite.value = !currentlyFavorite
            _isInWatchlist.value = true // If favorited, it's effectively in the watchlist/local db
        }
    }
}
