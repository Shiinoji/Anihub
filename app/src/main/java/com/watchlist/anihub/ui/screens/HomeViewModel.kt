package com.watchlist.anihub.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.watchlist.anihub.data.ThemeManager
import com.watchlist.anihub.data.remote.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val aniListService: AniListService,
    private val themeManager: ThemeManager
) : ViewModel() {

    val trendingAnime: StateFlow<List<Media>> = themeManager.adultContent.flatMapLatest { isAdult ->
        flow {
            try {
                val res = aniListService.getAnimeList(GraphQLRequest(AniListQueries.TRENDING_NOW, mapOf("page" to 1, "perPage" to 10, "isAdult" to isAdult)))
                emit(res.data.Page.media)
            } catch (e: Exception) {
                emit(emptyList())
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val popularAnime: StateFlow<List<Media>> = themeManager.adultContent.flatMapLatest { isAdult ->
        flow {
            try {
                val res = aniListService.getAnimeList(GraphQLRequest(AniListQueries.MOST_POPULAR, mapOf("page" to 1, "perPage" to 10, "isAdult" to isAdult)))
                emit(res.data.Page.media)
            } catch (e: Exception) {
                emit(emptyList())
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val seasonalAnime: StateFlow<List<Media>> = themeManager.adultContent.flatMapLatest { isAdult ->
        flow {
            try {
                val res = aniListService.getAnimeList(GraphQLRequest(AniListQueries.SEASONAL_ANIME, mapOf("page" to 1, "perPage" to 10, "season" to "SPRING", "seasonYear" to 2024, "isAdult" to isAdult)))
                emit(res.data.Page.media)
            } catch (e: Exception) {
                emit(emptyList())
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val topRatedAnime: StateFlow<List<Media>> = themeManager.adultContent.flatMapLatest { isAdult ->
        flow {
            try {
                val res = aniListService.getAnimeList(GraphQLRequest(AniListQueries.TOP_RATED, mapOf("page" to 1, "perPage" to 10, "isAdult" to isAdult)))
                emit(res.data.Page.media)
            } catch (e: Exception) {
                emit(emptyList())
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTimePopular: StateFlow<List<Media>> = themeManager.adultContent.flatMapLatest { isAdult ->
        flow {
            try {
                val res = aniListService.getAnimeList(GraphQLRequest(AniListQueries.ALL_TIME_POPULAR, mapOf("page" to 1, "perPage" to 10, "isAdult" to isAdult)))
                emit(res.data.Page.media)
            } catch (e: Exception) {
                emit(emptyList())
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
