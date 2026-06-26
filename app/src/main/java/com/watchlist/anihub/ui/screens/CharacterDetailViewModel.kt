package com.watchlist.anihub.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.watchlist.anihub.data.remote.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CharacterDetailViewModel @Inject constructor(
    private val aniListService: AniListService
) : ViewModel() {

    private val _characterDetail = MutableStateFlow<Character?>(null)
    val characterDetail = _characterDetail.asStateFlow()

    fun fetchCharacterDetail(id: Int) {
        viewModelScope.launch {
            try {
                val response = aniListService.getCharacterDetail(
                    GraphQLRequest(AniListQueries.CHARACTER_DETAIL, mapOf("id" to id))
                )
                _characterDetail.value = response.data.Character
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
