package com.watchlist.anihub.ui.screens.animelist

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.watchlist.anihub.data.ThemeManager
import com.watchlist.anihub.data.local.AnimeDao
import com.watchlist.anihub.data.remote.AniListQueries
import com.watchlist.anihub.data.remote.AniListService
import com.watchlist.anihub.data.remote.GraphQLRequest
import com.watchlist.anihub.data.remote.Media
import com.watchlist.anihub.ui.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AnimeListViewModel @Inject constructor(
    private val aniListService: AniListService,
    private val themeManager: ThemeManager,
    private val animeDao: AnimeDao,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val category: String = savedStateHandle["category"] ?: "Trending Now"

    val watchlistMap = animeDao.getWatchlist().map { list ->
        list.associateBy({ it.id }, { it.status })
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyMap())

    private val _animeList = MutableStateFlow<List<Media>>(emptyList())
    val animeList = _animeList.asStateFlow()

    private val _uiState = MutableStateFlow<UiState<Unit>>(UiState.Loading)
    val uiState = _uiState.asStateFlow()

    private var page = 1
    private var isEndReached = false
    private var isLoading = false

    init {
        loadNextPage()
    }

    fun loadNextPage() {
        if (isLoading || isEndReached) return
        
        isLoading = true
        viewModelScope.launch {
            try {
                val isAdult = themeManager.adultContent.first()
                val query = when (category) {
                    "Trending Now" -> AniListQueries.TRENDING_NOW
                    "Most Popular" -> AniListQueries.MOST_POPULAR
                    "Seasonal Anime" -> AniListQueries.SEASONAL_ANIME
                    else -> AniListQueries.ALL_TIME_POPULAR
                }

                val variables = mutableMapOf("page" to page, "perPage" to 20, "isAdult" to isAdult)
                if (category == "Seasonal Anime") {
                    variables["season"] = "SPRING"
                    variables["seasonYear"] = 2024
                }

                val res = aniListService.getAnimeList(GraphQLRequest(query, variables))
                
                if (res.errors != null) {
                    _uiState.value = UiState.Error(res.errors.firstOrNull()?.message ?: "Unknown error")
                } else {
                    val newMedia = res.data?.page?.media ?: emptyList()
                    if (newMedia.isEmpty()) {
                        isEndReached = true
                    } else {
                        _animeList.value += newMedia
                        page++
                    }
                    _uiState.value = UiState.Success(Unit)
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Unknown error")
            } finally {
                isLoading = false
            }
        }
    }

    fun refresh() {
        page = 1
        isEndReached = false
        _animeList.value = emptyList()
        _uiState.value = UiState.Loading
        loadNextPage()
    }
}
