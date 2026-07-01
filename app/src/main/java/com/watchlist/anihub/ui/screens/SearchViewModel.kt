package com.watchlist.anihub.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.watchlist.anihub.data.ThemeManager
import com.watchlist.anihub.data.remote.*
import com.watchlist.anihub.ui.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val aniListService: AniListService,
    private val themeManager: ThemeManager
) : ViewModel() {

    private val _searchResults = MutableStateFlow<UiState<List<Media>>>(UiState.Success(emptyList()))
    val searchResults = _searchResults.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

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

    private var searchJob: Job? = null

    init {
        fetchGenres()
    }

    private data class SearchParameters(
        val query: String,
        val genre: String?,
        val year: Int?,
        val season: String?,
        val status: String?,
        val sort: String
    )

    private fun fetchGenres() {
        viewModelScope.launch {
            try {
                val query = "{ GenreCollection }"
                val response = aniListService.getGenres(GraphQLRequest(query))
                _genres.value = response.data?.genreCollection ?: emptyList()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun selectGenre(genre: String?) {
        _selectedGenre.value = genre
        performSearch()
    }

    fun selectYear(year: Int?) {
        _selectedYear.value = year
        performSearch()
    }

    fun selectSeason(season: String?) {
        _selectedSeason.value = season
        performSearch()
    }

    fun selectStatus(status: String?) {
        _selectedStatus.value = status
        performSearch()
    }

    fun selectSort(sort: String) {
        _selectedSort.value = sort
        performSearch()
    }

    fun onQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onSearchExplicit(query: String) {
        _searchQuery.value = query
        performSearch()
    }

    private fun performSearch() {
        val params = SearchParameters(
            _searchQuery.value,
            _selectedGenre.value,
            _selectedYear.value,
            _selectedSeason.value,
            _selectedStatus.value,
            _selectedSort.value
        )

        if (params.query.isBlank() && params.genre == null && params.year == null && params.season == null && params.status == null) {
            _searchResults.value = UiState.Success(emptyList())
            return
        }

        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            _searchResults.value = UiState.Loading
            try {
                val isAdult = themeManager.adultContent.first()
                val variables = mutableMapOf<String, Any>(
                    "isAdult" to isAdult,
                    "sort" to listOf(params.sort)
                )
                if (params.query.isNotBlank()) {
                    variables["search"] = params.query
                }
                params.genre?.let { variables["genre"] = it }
                params.year?.let { variables["seasonYear"] = it }
                params.season?.let { variables["season"] = it }
                params.status?.let { variables["status"] = it }

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
                val response = aniListService.getAnimeList(
                    GraphQLRequest(searchQuery, variables)
                )
                
                if (response.errors != null) {
                    throw Exception(response.errors.firstOrNull()?.message ?: "Unknown error")
                }
                
                val mediaList = response.data?.page?.media ?: emptyList()
                _searchResults.value = UiState.Success(mediaList)
            } catch (e: Exception) {
                if (e !is kotlinx.coroutines.CancellationException) {
                    _searchResults.value = UiState.Error(NetworkUtils.getErrorMessage(e))
                }
            }
        }
    }
}
