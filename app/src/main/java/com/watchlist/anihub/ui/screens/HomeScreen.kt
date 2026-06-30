package com.watchlist.anihub.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.watchlist.anihub.R
import com.watchlist.anihub.data.remote.Media
import com.watchlist.anihub.ui.UiState
import com.watchlist.anihub.ui.components.SimpleAnimeCard
import com.watchlist.anihub.ui.components.SimpleAnimeCardSkeleton
import com.watchlist.anihub.ui.theme.LocalTitleLanguage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onAnimeClick: (Int) -> Unit,
    onNotificationsClick: () -> Unit,
    onCalendarClick: () -> Unit,
    onSettingsClick: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val trendingState by viewModel.trendingAnime.collectAsState()
    val popularState by viewModel.popularAnime.collectAsState()
    val seasonalState by viewModel.seasonalAnime.collectAsState()
    val topRatedState by viewModel.topRatedAnime.collectAsState()
    val allTimePopularState by viewModel.allTimePopular.collectAsState()
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
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                item { AnimeSection("Trending Now", trendingState, onAnimeClick, onRetry = { viewModel.refresh() }) }
                item { AnimeSection("Recommended for You", topRatedState, onAnimeClick, onRetry = { viewModel.refresh() }) }
                item { AnimeSection("Most Popular", popularState, onAnimeClick, onRetry = { viewModel.refresh() }) }
                item { AnimeSection("Seasonal Anime", seasonalState, onAnimeClick, onRetry = { viewModel.refresh() }) }
                item { AnimeSection("Explore More", allTimePopularState, onAnimeClick, onRetry = { viewModel.refresh() }) }
            }
        }
    }
}

@Composable
fun AnimeSection(
    title: String,
    state: UiState<List<Media>>,
    onAnimeClick: (Int) -> Unit,
    onRetry: () -> Unit
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
                            modifier = Modifier.width(140.dp)
                        )
                    }
                }
            }
            is UiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = ImageVector.vectorResource(R.drawable.wifi_off),
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(state.message, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
                        TextButton(onClick = onRetry) {
                            Text("Retry")
                        }
                    }
                }
            }
        }
    }
}
