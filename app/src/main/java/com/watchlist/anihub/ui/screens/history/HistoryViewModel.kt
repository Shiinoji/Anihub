package com.watchlist.anihub.ui.screens.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.watchlist.anihub.data.local.AnimeDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the History screen, managing the retrieval and purging of viewed anime records.
 */
@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val animeDao: AnimeDao
) : ViewModel() {

    /**
     * Observable flow of recently viewed anime, sorted by timestamp descending.
     */
    val history = animeDao.getHistory().stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    /**
     * Permanently deletes all entries from the local viewing history.
     */
    fun clearHistory() {
        viewModelScope.launch {
            animeDao.clearHistory()
        }
    }
}
