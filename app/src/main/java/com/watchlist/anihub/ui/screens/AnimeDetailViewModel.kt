package com.watchlist.anihub.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.watchlist.anihub.data.local.AnimeDao
import com.watchlist.anihub.data.local.AnimeEntity
import com.watchlist.anihub.data.local.HistoryEntity
import com.watchlist.anihub.data.remote.*
import com.watchlist.anihub.ui.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.IOException
import java.net.UnknownHostException
import javax.inject.Inject

@HiltViewModel
class AnimeDetailViewModel @Inject constructor(
    private val aniListService: AniListService,
    private val animeDao: AnimeDao
) : ViewModel() {

    private val _animeDetail = MutableStateFlow<UiState<Media>>(UiState.Loading)
    val animeDetail = _animeDetail.asStateFlow()

    private val _isInWatchlist = MutableStateFlow(false)
    val isInWatchlist = _isInWatchlist.asStateFlow()

    private val _isFavorite = MutableStateFlow(false)
    val isFavorite = _isFavorite.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    fun fetchAnimeDetail(id: Int) {
        viewModelScope.launch {
            _animeDetail.value = UiState.Loading
            try {
                val response = aniListService.getAnimeDetail(
                    GraphQLRequest(AniListQueries.ANIME_DETAIL, mapOf("id" to id))
                )
                val media = response.data.Media
                _animeDetail.value = UiState.Success(media)
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
                _animeDetail.value = UiState.Error(getErrorMessage(e))
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun refresh(id: Int) {
        _isRefreshing.value = true
        fetchAnimeDetail(id)
    }

    fun toggleWatchlist() {
        val state = _animeDetail.value
        if (state is UiState.Success) {
            val media = state.data
            viewModelScope.launch {
                if (_isInWatchlist.value) {
                    animeDao.deleteAnime(AnimeEntity(media.id, media.title.displayTitle, media.coverImage.extraLarge ?: "", _isFavorite.value))
                } else {
                    animeDao.insertAnime(AnimeEntity(media.id, media.title.displayTitle, media.coverImage.extraLarge ?: "", _isFavorite.value))
                }
                _isInWatchlist.value = !_isInWatchlist.value
            }
        }
    }

    fun toggleFavorite() {
        val state = _animeDetail.value
        if (state is UiState.Success) {
            val media = state.data
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
                _isInWatchlist.value = true 
            }
        }
    }

    private fun getErrorMessage(e: Exception): String {
        return when (e) {
            is UnknownHostException, is IOException -> "No internet connection."
            else -> "Failed to load anime details."
        }
    }
}
