package com.watchlist.anihub.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.watchlist.anihub.data.local.AiringScheduleEntity
import com.watchlist.anihub.data.local.AnimeDao
import com.watchlist.anihub.data.remote.AniListQueries
import com.watchlist.anihub.data.remote.AniListService
import com.watchlist.anihub.data.remote.GraphQLRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val animeDao: AnimeDao,
    private val aniListService: AniListService
) : ViewModel() {

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    private val _selectedDate = MutableStateFlow(Calendar.getInstance())
    val selectedDate = _selectedDate.asStateFlow()

    init {
        refresh()
    }

    val availableDates: StateFlow<List<Calendar>> = flow {
        val dates = mutableListOf<Calendar>()
        val cal = Calendar.getInstance()
        // Show 7 days (today + next 6 days)
        repeat(7) {
            val date = cal.clone() as Calendar
            dates.add(date)
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }
        emit(dates)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, listOf(Calendar.getInstance()))

    val airingSchedule: StateFlow<List<AiringScheduleEntity>> = _selectedDate
        .flatMapLatest { selectedCal ->
            val startOfDay = (selectedCal.clone() as Calendar).apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis / 1000

            val endOfDay = (selectedCal.clone() as Calendar).apply {
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
                set(Calendar.MILLISECOND, 999)
            }.timeInMillis / 1000

            animeDao.getAiringSchedule(startOfDay, endOfDay)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun selectDate(calendar: Calendar) {
        _selectedDate.value = calendar
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                // Fetch from the start of today to ensure we have all data for the current day
                val start = (Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis / 1000).toInt()
                val end = start + TimeUnit.DAYS.toSeconds(14).toInt()
                
                val allSchedules = mutableListOf<AiringScheduleEntity>()
                
                // Fetch up to 6 pages to get more airing data
                for (page in 1..6) {
                    val response = aniListService.getAnimeList(
                        GraphQLRequest(
                            AniListQueries.AIRING_SCHEDULE,
                            mapOf("start" to start, "end" to end, "page" to page)
                        )
                    )

                    if (response.errors != null) break

                    val schedules = response.data?.page?.airingSchedules?.map {
                        AiringScheduleEntity(
                            id = it.id,
                            animeId = it.media.id,
                            episode = it.episode,
                            airingAt = it.airingAt,
                            title = it.media.title.displayTitle,
                            imageUrl = it.media.coverImage.large ?: ""
                        )
                    } ?: emptyList()

                    if (schedules.isEmpty()) break
                    allSchedules.addAll(schedules)
                    // If we have fewer than 50 items, it means there are no more pages
                    if (schedules.size < 50) break
                }

                if (allSchedules.isNotEmpty()) {
                    animeDao.insertAiringSchedules(allSchedules)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isRefreshing.value = false
            }
        }
    }
}
