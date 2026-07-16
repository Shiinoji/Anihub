package com.watchlist.anihub.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.watchlist.anihub.R
import com.watchlist.anihub.data.local.WatchlistStatus
import com.watchlist.anihub.data.remote.Media
import com.watchlist.anihub.ui.UiState
import com.watchlist.anihub.ui.components.SimpleAnimeCard
import com.watchlist.anihub.ui.components.SimpleAnimeCardSkeleton
import com.watchlist.anihub.ui.components.ErrorView
import com.watchlist.anihub.ui.theme.LocalTitleLanguage

/**
 * The main landing screen of the app. Displays categorized horizontal lists of anime
 * and an infinite-scrolling vertical grid for general discovery.
 *
 * @param onAnimeClick Callback when an anime card is clicked.
 * @param onNotificationsClick Callback to navigate to the notifications screen.
 * @param onCalendarClick Callback to navigate to the airing calendar.
 * @param onSettingsClick Callback to navigate to settings.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onAnimeClick: (Int) -> Unit,
    onNotificationsClick: () -> Unit,
    onCalendarClick: () -> Unit,
    onSettingsClick: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    // Collecting states from ViewModel
    val trendingState by viewModel.trendingAnime.collectAsState()
    val popularState by viewModel.popularAnime.collectAsState()
    val seasonalState by viewModel.seasonalAnime.collectAsState()
    val discoverAnime by viewModel.discoverAnime.collectAsState()
    val discoverState by viewModel.discoverState.collectAsState()
    val watchlistMap by viewModel.watchlistMap.collectAsState()
    val itemsPerRow by viewModel.homeItemsPerRow.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Home", 
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    ) 
                },
                actions = {
                    IconButton(onClick = onNotificationsClick) {
                        Icon(ImageVector.vectorResource(R.drawable.bell), contentDescription = "Notifications")
                    }
                    IconButton(onClick = onCalendarClick) {
                        Icon(ImageVector.vectorResource(R.drawable.calendar), contentDescription = "Airing Calendar")
                    }
                    IconButton(onClick = onSettingsClick) {
                        Icon(ImageVector.vectorResource(R.drawable.settings), contentDescription = "Settings")
                    }
                }
            )
        }
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { viewModel.refresh() },
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            val titleLanguage = LocalTitleLanguage.current
            
            // Consolidate errors: if any section fails, show a single global error view
            val states = listOf(trendingState, popularState, seasonalState)
            val errorState = states.asSequence().filterIsInstance<UiState.Error>().firstOrNull()

            if (errorState != null) {
                val isConnectionError = errorState.message.contains("network", ignoreCase = true) || 
                                      errorState.message.contains("internet", ignoreCase = true) ||
                                      errorState.message.contains("connection", ignoreCase = true)
                
                ErrorView(
                    message = errorState.message,
                    onRetry = { viewModel.refresh() },
                    icon = if (isConnectionError) {
                        ImageVector.vectorResource(R.drawable.wifi_off)
                    } else {
                        ImageVector.vectorResource(R.drawable.triangle_alert)
                    }
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    // Standard horizontal sections
                    item { AnimeSection("Trending Now", trendingState, onAnimeClick, watchlistMap) }
                    item { AnimeSection("Most Popular", popularState, onAnimeClick, watchlistMap) }
                    item { AnimeSection("Seasonal Anime", seasonalState, onAnimeClick, watchlistMap) }

                    // Discover Vertical Grid Section
                    item {
                        Text(
                            text = "Discover",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
                        )
                    }

                    // Chunking the discover list for a custom grid implementation in LazyColumn
                    val chunkedList = discoverAnime.chunked(itemsPerRow)
                    
                    itemsIndexed(chunkedList) { index, rowItems ->
                        // Pagination trigger: load more when reaching the end
                        if (index >= (chunkedList.size - 2)) {
                            LaunchedEffect(Unit) {
                                viewModel.loadDiscoverNextPage()
                            }
                        }
                        
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            rowItems.forEach { anime ->
                                SimpleAnimeCard(
                                    title = anime.title.getDisplayTitle(titleLanguage),
                                    imageUrl = anime.coverImage.extraLarge ?: anime.coverImage.large ?: "",
                                    onClick = { onAnimeClick(anime.id) },
                                    status = watchlistMap[anime.id]?.getDisplayName(),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            // Filler spacers for incomplete rows (last page)
                            repeat(itemsPerRow - rowItems.size) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }

                    // Loading indicator for pagination
                    if (discoverState is UiState.Loading && discoverAnime.isNotEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(32.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * A reusable horizontal section showing a category of anime.
 */
@Composable
fun AnimeSection(
    title: String,
    state: UiState<List<Media>>,
    onAnimeClick: (Int) -> Unit,
    watchlistMap: Map<Int, WatchlistStatus> = emptyMap()
) {
    val titleLanguage = LocalTitleLanguage.current
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        
        when (state) {
            is UiState.Loading -> {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(5) { SimpleAnimeCardSkeleton() }
                }
            }
            is UiState.Success -> {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.data) { anime ->
                        SimpleAnimeCard(
                            title = anime.title.getDisplayTitle(titleLanguage),
                            imageUrl = anime.coverImage.extraLarge ?: anime.coverImage.large ?: "",
                            onClick = { onAnimeClick(anime.id) },
                            status = watchlistMap[anime.id]?.getDisplayName(),
                            modifier = Modifier.width(140.dp)
                        )
                    }
                }
            }
            else -> {}
        }
    }
}
