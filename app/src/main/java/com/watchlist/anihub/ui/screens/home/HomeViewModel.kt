package com.watchlist.anihub.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.watchlist.anihub.data.ThemeManager
import com.watchlist.anihub.data.remote.*
import com.watchlist.anihub.data.local.AnimeDao
import com.watchlist.anihub.ui.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Home screen, responsible for managing trending, popular, and seasonal anime lists,
 * as well as the infinite-scrolling "Discover" feed.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val aniListService: AniListService,
    private val themeManager: ThemeManager,
    private val animeDao: AnimeDao,
) : ViewModel() {

    /**
     * A map of anime IDs to their current watchlist status, allowing for quick lookups
     * when displaying status badges on anime cards.
     */
    val watchlistMap = animeDao.getWatchlist().map { list ->
        list.associateBy({ it.id }, { it.status })
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyMap())

    private val _isRefreshing = MutableStateFlow(value = false)
    val isRefreshing = _isRefreshing.asStateFlow()

    private val _refreshTrigger = MutableStateFlow(0)

    // Combined flow that triggers data reloading when adult content settings or manual refresh occurs
    private val adultContentFlow = themeManager.adultContent
    private val combinedTrigger = combine(adultContentFlow, _refreshTrigger) { isAdult, trigger ->
        isAdult to trigger
    }

    /**
     * Flow of trending anime media. Re-fetches when Adult Content settings change.
     */
    val trendingAnime: StateFlow<UiState<List<Media>>> = combinedTrigger.flatMapLatest { (isAdult, _) ->
        flow<UiState<List<Media>>> {
            emit(UiState.Loading)
            try {
                val res = aniListService.getAnimeList(GraphQLRequest(AniListQueries.TRENDING_NOW, mapOf("page" to 1, "perPage" to 10, "isAdult" to isAdult)))
                if (res.errors != null) {
                    emit(UiState.Error(res.errors.firstOrNull()?.message ?: "Unknown error"))
                } else {
                    emit(UiState.Success(res.data?.page?.media ?: emptyList()))
                }
            } catch (e: Exception) {
                emit(UiState.Error(getErrorMessage(e)))
            } finally {
                _isRefreshing.value = false
            }
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, UiState.Loading)

    /**
     * Flow of currently popular anime media.
     */
    val popularAnime: StateFlow<UiState<List<Media>>> = combinedTrigger.flatMapLatest { (isAdult, _) ->
        flow<UiState<List<Media>>> {
            emit(UiState.Loading)
            try {
                val res = aniListService.getAnimeList(GraphQLRequest(AniListQueries.MOST_POPULAR, mapOf("page" to 1, "perPage" to 10, "isAdult" to isAdult)))
                if (res.errors != null) {
                    emit(UiState.Error(res.errors.firstOrNull()?.message ?: "Unknown error"))
                } else {
                    emit(UiState.Success(res.data?.page?.media ?: emptyList()))
                }
            } catch (e: Exception) {
                emit(UiState.Error(getErrorMessage(e)))
            }
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, UiState.Loading)

    /**
     * Flow of seasonal anime media (e.g., Spring 2024).
     */
    val seasonalAnime: StateFlow<UiState<List<Media>>> = combinedTrigger.flatMapLatest { (isAdult, _) ->
        flow<UiState<List<Media>>> {
            emit(UiState.Loading)
            try {
                val res = aniListService.getAnimeList(GraphQLRequest(AniListQueries.SEASONAL_ANIME, mapOf("page" to 1, "perPage" to 10, "season" to "SPRING", "seasonYear" to 2024, "isAdult" to isAdult)))
                if (res.errors != null) {
                    emit(UiState.Error(res.errors.firstOrNull()?.message ?: "Unknown error"))
                } else {
                    emit(UiState.Success(res.data?.page?.media ?: emptyList()))
                }
            } catch (e: Exception) {
                emit(UiState.Error(getErrorMessage(e)))
            }
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, UiState.Loading)

    /**
     * User preference for how many items to display per row in the infinite grid.
     */
    val homeItemsPerRow = themeManager.homeItemsPerRow.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), 2
    )

    private val _discoverAnime = MutableStateFlow<List<Media>>(emptyList())
    /**
     * Accumulated list of anime for the infinite-scrolling Discover section.
     */
    val discoverAnime = _discoverAnime.asStateFlow()

    private val _discoverState = MutableStateFlow<UiState<Unit>>(UiState.Loading)
    /**
     * State of the current Discovery fetch operation.
     */
    val discoverState = _discoverState.asStateFlow()

    private var discoverPage = 1
    private var isEndReached = false
    private var isDiscoverLoading = false

    init {
        loadDiscoverNextPage()
    }

    /**
     * Fetches the next page of results for the Discover feed.
     * Prevents duplicate loads if a fetch is already in progress or if the end of data is reached.
     */
    fun loadDiscoverNextPage() {
        if (isDiscoverLoading || isEndReached) return
        
        isDiscoverLoading = true
        viewModelScope.launch {
            try {
                val isAdult = themeManager.adultContent.first()
                val res = aniListService.getAnimeList(
                    GraphQLRequest(
                        AniListQueries.ALL_TIME_POPULAR, 
                        mapOf("page" to discoverPage, "perPage" to 20, "isAdult" to isAdult)
                    )
                )
                
                val newMedia = res.data?.page?.media ?: emptyList()
                if (newMedia.isEmpty()) {
                    isEndReached = true
                } else {
                    _discoverAnime.value += newMedia
                    discoverPage++
                }
                _discoverState.value = UiState.Success(Unit)
            } catch (e: Exception) {
                _discoverState.value = UiState.Error(getErrorMessage(e))
            } finally {
                isDiscoverLoading = false
                _isRefreshing.value = false
            }
        }
    }

    /**
     * Refreshes all Home screen data, resetting pagination and clearing discovery cache.
     */
    fun refresh() {
        _isRefreshing.value = true
        _refreshTrigger.value += 1
        
        // Reset discover
        discoverPage = 1
        isEndReached = false
        _discoverAnime.value = emptyList()
        loadDiscoverNextPage()
    }

    private fun getErrorMessage(e: Exception): String = NetworkUtils.getErrorMessage(e)
}
