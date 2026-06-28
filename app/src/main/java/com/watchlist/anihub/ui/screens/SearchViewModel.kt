package com.watchlist.anihub.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.watchlist.anihub.data.ThemeManager
import com.watchlist.anihub.data.remote.*
import com.watchlist.anihub.ui.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.IOException
import java.net.UnknownHostException
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val aniListService: AniListService,
    private val themeManager: ThemeManager
) : ViewModel() {

    private val _searchResults = MutableStateFlow<UiState<List<Media>>>(UiState.Success(emptyList()))
    val searchResults = _searchResults.asStateFlow()

    fun searchAnime(query: String) {
        if (query.isBlank()) {
            _searchResults.value = UiState.Success(emptyList())
            return
        }
        viewModelScope.launch {
            _searchResults.value = UiState.Loading
            try {
                val isAdult = themeManager.adultContent.first()
                val searchQuery = """
                    query (${'$'}search: String, ${'$'}isAdult: Boolean) {
                      Page(page: 1, perPage: 20) {
                        media(search: ${'$'}search, type: ANIME, sort: POPULARITY_DESC, isAdult: ${'$'}isAdult) {
                          id
                          title { english romaji native }
                          coverImage { extraLarge large medium }
                        }
                      }
                    }
                """
                val response = aniListService.getAnimeList(
                    GraphQLRequest(searchQuery, mapOf("search" to query, "isAdult" to isAdult))
                )
                _searchResults.value = UiState.Success(response.data.Page.media)
            } catch (e: Exception) {
                _searchResults.value = UiState.Error(getErrorMessage(e))
            }
        }
    }

    private fun getErrorMessage(e: Exception): String {
        return when (e) {
            is UnknownHostException, is IOException -> "No internet connection."
            else -> "Search failed. Please try again."
        }
    }
}
