package com.watchlist.anihub.ui.screens.animedetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.squareup.moshi.Moshi
import com.watchlist.anihub.data.local.AnimeDao
import com.watchlist.anihub.data.local.AnimeEntity
import com.watchlist.anihub.data.local.HistoryEntity
import com.watchlist.anihub.data.remote.*
import com.watchlist.anihub.ui.UiState
import com.watchlist.anihub.data.local.WatchlistStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Anime Detail screen, managing the fetching and caching of anime details,
 * and user interactions with their watchlist (toggle, favorite, status update).
 */
@HiltViewModel
class AnimeDetailViewModel @Inject constructor(
    private val aniListService: AniListService,
    private val animeDao: AnimeDao,
    moshi: Moshi
) : ViewModel() {

    private val mediaAdapter = moshi.adapter(Media::class.java)

    private val _animeDetail = MutableStateFlow<UiState<Media>>(UiState.Loading)
    /**
     * Complete metadata state for the current anime. Initialized from local cache if available,
     * then updated with fresh data from the remote API. Represents loading, success, or error states.
     */
    val animeDetail = _animeDetail.asStateFlow()

    private val _isInWatchlist = MutableStateFlow(false)
    /**
     * State flow representing whether the current anime is saved in the user's local watchlist.
     */
    val isInWatchlist = _isInWatchlist.asStateFlow()

    private val _isFavorite = MutableStateFlow(false)
    /**
     * State flow representing whether the current anime is marked as a favorite by the user.
     */
    val isFavorite = _isFavorite.asStateFlow()

    private val _watchlistStatus = MutableStateFlow(WatchlistStatus.PLAN_TO_WATCH)
    /**
     * State flow representing the user's watching progress status (e.g., Planning, Watching, Completed) for this anime.
     */
    val watchlistStatus = _watchlistStatus.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    /**
     * State flow indicating whether a manual user-triggered refresh of the anime details is currently in progress.
     */
    val isRefreshing = _isRefreshing.asStateFlow()

    /**
     * Fetches detailed information for a specific anime ID.
     * Implements a "Cache-then-Network" strategy for fast initial loading.
     *
     * @param id The unique identifier of the anime to fetch.
     */
    fun fetchAnimeDetail(id: Int) {
        viewModelScope.launch {
            _animeDetail.value = UiState.Loading
            
            // Step 1: Try to load from local cache first
            val localAnime = animeDao.getAnime(id)
            if (localAnime?.detailJson != null) {
                try {
                    mediaAdapter.fromJson(localAnime.detailJson)?.let { cachedMedia ->
                        _animeDetail.value = UiState.Success(cachedMedia)
                        _isInWatchlist.value = true
                        _isFavorite.value = localAnime.isFavorite
                        _watchlistStatus.value = localAnime.status
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            // Step 2: Fetch fresh data from network
            try {
                val response = aniListService.getAnimeDetail(
                    GraphQLRequest(AniListQueries.ANIME_DETAIL, mapOf("id" to id))
                )
                
                if (response.errors != null) {
                    throw Exception(response.errors.firstOrNull()?.message ?: "Unknown error")
                }
                
                val media = response.data?.media ?: throw Exception("No data found")
                _animeDetail.value = UiState.Success(media)
                
                val currentLocalAnime = animeDao.getAnime(id)
                _isInWatchlist.value = currentLocalAnime != null
                _isFavorite.value = currentLocalAnime?.isFavorite ?: false
                _watchlistStatus.value = currentLocalAnime?.status ?: WatchlistStatus.PLAN_TO_WATCH

                // Step 3: If it's in watchlist, update the cache with fresh data
                if (currentLocalAnime != null) {
                    val updatedEntity = currentLocalAnime.copy(
                        title = media.title.displayTitle,
                        imageUrl = media.coverImage.extraLarge ?: media.coverImage.large ?: "",
                        detailJson = mediaAdapter.toJson(media)
                    )
                    animeDao.insertAnime(updatedEntity)
                }

                // Step 4: Add to viewing history
                animeDao.insertHistory(
                    HistoryEntity(
                        id = media.id,
                        title = media.title.displayTitle,
                        imageUrl = media.coverImage.large ?: ""
                    )
                )
            } catch (e: Exception) {
                // If we already have cached data, don't show error state to avoid flickering
                if (_animeDetail.value !is UiState.Success) {
                    _animeDetail.value = UiState.Error(getErrorMessage(e))
                }
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    /**
     * Manually triggers a network refresh of the anime details.
     *
     * @param id The unique identifier of the anime to refresh.
     */
    fun refresh(id: Int) {
        _isRefreshing.value = true
        fetchAnimeDetail(id)
    }

    /**
     * Adds or removes the current anime from the user's watchlist.
     */
    fun toggleWatchlist() {
        val state = _animeDetail.value
        if (state is UiState.Success) {
            val media = state.data
            viewModelScope.launch {
                if (_isInWatchlist.value) {
                    animeDao.deleteAnime(AnimeEntity(media.id, media.title.displayTitle, media.coverImage.extraLarge ?: "", _isFavorite.value, status = _watchlistStatus.value))
                } else {
                    val detailJson = mediaAdapter.toJson(media)
                    val currentEpisode = media.nextAiringEpisode?.episode?.minus(1) ?: 0
                    animeDao.insertAnime(
                        AnimeEntity(
                            id = media.id,
                            title = media.title.displayTitle,
                            imageUrl = media.coverImage.extraLarge ?: "",
                            isFavorite = _isFavorite.value,
                            status = _watchlistStatus.value,
                            detailJson = detailJson,
                            lastNotifiedEpisode = currentEpisode
                        )
                    )
                }
                _isInWatchlist.value = !_isInWatchlist.value
            }
        }
    }

    /**
     * Toggles the favorite status of the current anime.
     * Automatically adds the anime to the watchlist if it wasn't already there.
     */
    fun toggleFavorite() {
        val state = _animeDetail.value
        if (state is UiState.Success) {
            val media = state.data
            viewModelScope.launch {
                val currentlyFavorite = _isFavorite.value
                val currentEpisode = media.nextAiringEpisode?.episode?.minus(1) ?: 0
                val anime = animeDao.getAnime(media.id) ?: AnimeEntity(
                    id = media.id,
                    title = media.title.displayTitle,
                    imageUrl = media.coverImage.extraLarge ?: "",
                    isFavorite = false,
                    detailJson = mediaAdapter.toJson(media),
                    lastNotifiedEpisode = currentEpisode
                )
                
                val updatedAnime = anime.copy(isFavorite = !currentlyFavorite)
                animeDao.insertAnime(updatedAnime)
                _isFavorite.value = !currentlyFavorite
                _isInWatchlist.value = true 
            }
        }
    }

    /**
     * Updates the user's progress status for this anime.
     *
     * @param status The new [WatchlistStatus] (e.g., WATCHING, COMPLETED, etc.) to set.
     */
    fun updateWatchlistStatus(status: WatchlistStatus) {
        val state = _animeDetail.value
        if (state is UiState.Success) {
            val media = state.data
            viewModelScope.launch {
                val currentEpisode = media.nextAiringEpisode?.episode?.minus(1) ?: 0
                val anime = animeDao.getAnime(media.id) ?: AnimeEntity(
                    id = media.id,
                    title = media.title.displayTitle,
                    imageUrl = media.coverImage.extraLarge ?: "",
                    isFavorite = _isFavorite.value,
                    detailJson = mediaAdapter.toJson(media),
                    lastNotifiedEpisode = currentEpisode
                )
                val updatedAnime = anime.copy(status = status)
                animeDao.insertAnime(updatedAnime)
                _watchlistStatus.value = status
                _isInWatchlist.value = true
            }
        }
    }

    private fun getErrorMessage(e: Exception): String = NetworkUtils.getErrorMessage(e)
}
