package com.watchlist.anihub.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.watchlist.anihub.data.ThemeManager
import com.watchlist.anihub.data.remote.*
import com.watchlist.anihub.ui.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HomeViewModel @Inject constructor(
    aniListService: AniListService,
    themeManager: ThemeManager,
) : ViewModel() {

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    private val _refreshTrigger = MutableStateFlow(0)

    private val adultContentFlow = themeManager.adultContent
    private val combinedTrigger = combine(adultContentFlow, _refreshTrigger) { isAdult, trigger ->
        isAdult to trigger
    }

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

    val topRatedAnime: StateFlow<UiState<List<Media>>> = combinedTrigger.flatMapLatest { (isAdult, _) ->
        flow<UiState<List<Media>>> {
            emit(UiState.Loading)
            try {
                val res = aniListService.getAnimeList(GraphQLRequest(AniListQueries.TOP_RATED, mapOf("page" to 1, "perPage" to 10, "isAdult" to isAdult)))
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

    val allTimePopular: StateFlow<UiState<List<Media>>> = combinedTrigger.flatMapLatest { (isAdult, _) ->
        flow<UiState<List<Media>>> {
            emit(UiState.Loading)
            try {
                val res = aniListService.getAnimeList(GraphQLRequest(AniListQueries.ALL_TIME_POPULAR, mapOf("page" to 1, "perPage" to 10, "isAdult" to isAdult)))
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

    fun refresh() {
        _isRefreshing.value = true
        _refreshTrigger.value += 1
    }

    private fun getErrorMessage(e: Exception): String = NetworkUtils.getErrorMessage(e)
}
