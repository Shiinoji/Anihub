package com.watchlist.anihub.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.watchlist.anihub.data.remote.*
import com.watchlist.anihub.ui.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Character Detail screen, responsible for fetching character metadata,
 * biographical info, and their anime appearances from the AniList API.
 */
@HiltViewModel
class CharacterDetailViewModel @Inject constructor(
    private val aniListService: AniListService
) : ViewModel() {

    private val _characterDetail = MutableStateFlow<UiState<Character>>(UiState.Loading)
    /**
     * Observable state of the character details.
     */
    val characterDetail = _characterDetail.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    /**
     * Fetches metadata for a specific character ID from the network.
     */
    fun fetchCharacterDetail(id: Int) {
        viewModelScope.launch {
            _characterDetail.value = UiState.Loading
            try {
                val response = aniListService.getCharacterDetail(
                    GraphQLRequest(AniListQueries.CHARACTER_DETAIL, mapOf("id" to id))
                )
                
                if (response.errors != null) {
                    throw Exception(response.errors.firstOrNull()?.message ?: "Unknown error")
                }
                
                val character = response.data?.character ?: throw Exception("No data found")
                _characterDetail.value = UiState.Success(character)
            } catch (e: Exception) {
                _characterDetail.value = UiState.Error(getErrorMessage(e))
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    /**
     * Triggers a manual network refresh for the current character.
     */
    fun refresh(id: Int) {
        _isRefreshing.value = true
        fetchCharacterDetail(id)
    }

    private fun getErrorMessage(e: Exception): String = NetworkUtils.getErrorMessage(e)
}
