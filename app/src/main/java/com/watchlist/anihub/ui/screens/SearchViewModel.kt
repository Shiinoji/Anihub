package com.watchlist.anihub.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.watchlist.anihub.data.ThemeManager
import com.watchlist.anihub.data.remote.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val aniListService: AniListService,
    private val themeManager: ThemeManager
) : ViewModel() {

    private val _searchResults = MutableStateFlow<List<Media>>(emptyList())
    val searchResults = _searchResults.asStateFlow()

    private val _genres = MutableStateFlow<List<String>>(emptyList())
    val genres = _genres.asStateFlow()

    private val _selectedGenre = MutableStateFlow<String?>(null)
    val selectedGenre = _selectedGenre.asStateFlow()

    private val _selectedYear = MutableStateFlow<Int?>(null)
    val selectedYear = _selectedYear.asStateFlow()

    private val _selectedSeason = MutableStateFlow<String?>(null)
    val selectedSeason = _selectedSeason.asStateFlow()

    private val _selectedStatus = MutableStateFlow<String?>(null)
    val selectedStatus = _selectedStatus.asStateFlow()

    private val _selectedSort = MutableStateFlow("POPULARITY_DESC")
    val selectedSort = _selectedSort.asStateFlow()

    private var currentQuery = ""

    init {
        fetchGenres()
    }

    private fun fetchGenres() {
        viewModelScope.launch {
            try {
                val query = "{ GenreCollection }"
                val response = aniListService.getGenres(GraphQLRequest(query))
                _genres.value = response.data.genreCollection
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun selectGenre(genre: String?) {
        _selectedGenre.value = genre
        searchAnime(currentQuery)
    }

    fun selectYear(year: Int?) {
        _selectedYear.value = year
        searchAnime(currentQuery)
    }

    fun selectSeason(season: String?) {
        _selectedSeason.value = season
        searchAnime(currentQuery)
    }

    fun selectStatus(status: String?) {
        _selectedStatus.value = status
        searchAnime(currentQuery)
    }

    fun selectSort(sort: String) {
        _selectedSort.value = sort
        searchAnime(currentQuery)
    }

    fun searchAnime(query: String) {
        currentQuery = query
        if (query.isBlank() && _selectedGenre.value == null && _selectedYear.value == null && _selectedSeason.value == null && _selectedStatus.value == null) {
            _searchResults.value = emptyList()
            return
        }
        viewModelScope.launch {
            try {
                val isAdult = themeManager.adultContent.first()
                val variables = mutableMapOf<String, Any>(
                    "isAdult" to isAdult,
                    "sort" to listOf(_selectedSort.value)
                )
                if (query.isNotBlank()) {
                    variables["search"] = query
                }
                _selectedGenre.value?.let { variables["genre"] = it }
                _selectedYear.value?.let { variables["seasonYear"] = it }
                _selectedSeason.value?.let { variables["season"] = it }
                _selectedStatus.value?.let { variables["status"] = it }

                val searchQuery = """
                    query (${'$'}search: String, ${'$'}isAdult: Boolean, ${'$'}genre: String, ${'$'}season: MediaSeason, ${'$'}seasonYear: Int, ${'$'}status: MediaStatus, ${'$'}sort: [MediaSort]) {
                      Page(page: 1, perPage: 20) {
                        media(search: ${'$'}search, type: ANIME, isAdult: ${'$'}isAdult, genre: ${'$'}genre, season: ${'$'}season, seasonYear: ${'$'}seasonYear, status: ${'$'}status, sort: ${'$'}sort) {
                          id
                          title { english romaji native }
                          coverImage { extraLarge large medium }
                        }
                      }
                    }
                """
                _searchResults.value = aniListService.getAnimeList(
                    GraphQLRequest(searchQuery, variables)
                ).data.page.media ?: emptyList()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
