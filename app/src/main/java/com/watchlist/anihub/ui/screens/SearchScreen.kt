package com.watchlist.anihub.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.watchlist.anihub.R
import com.watchlist.anihub.ui.components.SimpleAnimeCard
import com.watchlist.anihub.ui.theme.LocalTitleLanguage
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onAnimeClick: (Int) -> Unit,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val results by viewModel.searchResults.collectAsState()
    val genres by viewModel.genres.collectAsState()
    val selectedGenre by viewModel.selectedGenre.collectAsState()
    val selectedYear by viewModel.selectedYear.collectAsState()
    val selectedSeason by viewModel.selectedSeason.collectAsState()
    val selectedStatus by viewModel.selectedStatus.collectAsState()
    val selectedSort by viewModel.selectedSort.collectAsState()

    var query by remember { mutableStateOf("") }
    var showFilterSheet by remember { mutableStateOf(false) }

    if (showFilterSheet) {
        ModalBottomSheet(
            onDismissRequest = { showFilterSheet = false },
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .padding(bottom = 32.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    "Search Filters",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(24.dp))

                // Sort Section
                Text("Sort By", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                val sortOptions = listOf(
                    "POPULARITY_DESC" to "Popularity",
                    "SCORE_DESC" to "Average Score",
                    "TRENDING_DESC" to "Trending",
                    "START_DATE_DESC" to "Release Date",
                    "UPDATED_AT_DESC" to "Recently Updated"
                )
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    sortOptions.forEach { (value, label) ->
                        FilterChip(
                            selected = selectedSort == value,
                            onClick = { viewModel.selectSort(value) },
                            label = { Text(label) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                // Genres Section
                if (genres.isNotEmpty()) {
                    Text("Genres", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        genres.forEach { genre ->
                            FilterChip(
                                selected = selectedGenre == genre,
                                onClick = { viewModel.selectGenre(if (selectedGenre == genre) null else genre) },
                                label = { Text(genre) }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Season Section
                Text("Season", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                val seasons = listOf("WINTER", "SPRING", "SUMMER", "FALL")
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    seasons.forEach { season ->
                        FilterChip(
                            selected = selectedSeason == season,
                            onClick = { viewModel.selectSeason(if (selectedSeason == season) null else season) },
                            label = { Text(season.lowercase().replaceFirstChar { it.uppercase() }) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                // Year Section
                Text("Year", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                val currentYear = Calendar.getInstance().get(Calendar.YEAR)
                val years = (currentYear + 1 downTo 1940).toList()
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    years.forEach { year ->
                        FilterChip(
                            selected = selectedYear == year,
                            onClick = { viewModel.selectYear(if (selectedYear == year) null else year) },
                            label = { Text(year.toString()) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                // Status Section
                Text("Airing Status", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                val statuses = listOf(
                    "FINISHED" to "Finished",
                    "RELEASING" to "Releasing",
                    "NOT_YET_RELEASED" to "Not Yet Released",
                    "CANCELLED" to "Cancelled",
                    "HIATUS" to "Hiatus"
                )
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    statuses.forEach { (value, label) ->
                        FilterChip(
                            selected = selectedStatus == value,
                            onClick = { viewModel.selectStatus(if (selectedStatus == value) null else value) },
                            label = { Text(label) }
                        )
                    }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            SearchBar(
                query = query,
                onQueryChange = {
                    query = it
                    viewModel.searchAnime(it)
                },
                onSearch = { viewModel.searchAnime(it) },
                active = false,
                onActiveChange = {},
                placeholder = { Text("Search Anime") },
                leadingIcon = { Icon(ImageVector.vectorResource(R.drawable.search), contentDescription = null) },
                trailingIcon = {
                    IconButton(onClick = { showFilterSheet = true }) {
                        Icon(Icons.Default.Tune, contentDescription = "Filter")
                    }
                },
                modifier = Modifier
                    .statusBarsPadding()
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {}
        }
    ) { padding ->
        val titleLanguage = LocalTitleLanguage.current
        LazyVerticalGrid(
            columns = GridCells.Adaptive(140.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(results) { anime ->
                SimpleAnimeCard(
                    title = anime.title.getDisplayTitle(titleLanguage),
                    imageUrl = anime.coverImage.extraLarge ?: anime.coverImage.large ?: "",
                    onClick = { onAnimeClick(anime.id) }
                )
            }
        }
    }
}
