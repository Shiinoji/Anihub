package com.watchlist.anihub.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
class CharacterDetailViewModel @Inject constructor(
    private val aniListService: AniListService
) : ViewModel() {

    private val _characterDetail = MutableStateFlow<UiState<Character>>(UiState.Loading)
    val characterDetail = _characterDetail.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    fun fetchCharacterDetail(id: Int) {
        viewModelScope.launch {
            _characterDetail.value = UiState.Loading
            try {
                val response = aniListService.getCharacterDetail(
                    GraphQLRequest(AniListQueries.CHARACTER_DETAIL, mapOf("id" to id))
                )
                _characterDetail.value = UiState.Success(response.data.character)
            } catch (e: Exception) {
                _characterDetail.value = UiState.Error(getErrorMessage(e))
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun refresh(id: Int) {
        _isRefreshing.value = true
        fetchCharacterDetail(id)
    }

    private fun getErrorMessage(e: Exception): String {
        return when (e) {
            is UnknownHostException, is IOException -> "No internet connection."
            else -> "Failed to load character details."
        }
    }
}
