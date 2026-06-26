package com.watchlist.anihub.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.watchlist.anihub.R
import com.watchlist.anihub.data.remote.Media
import com.watchlist.anihub.ui.components.SimpleAnimeCard
import com.watchlist.anihub.ui.theme.LocalTitleLanguage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onAnimeClick: (Int) -> Unit,
    onNotificationsClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onSettingsClick: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val trending by viewModel.trendingAnime.collectAsState()
    val popular by viewModel.popularAnime.collectAsState()
    val seasonal by viewModel.seasonalAnime.collectAsState()
    val topRated by viewModel.topRatedAnime.collectAsState()
    val allTimePopular by viewModel.allTimePopular.collectAsState()

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
                    IconButton(onClick = onHistoryClick) {
                        Icon(ImageVector.vectorResource(R.drawable.history), contentDescription = "History")
                    }
                    IconButton(onClick = onSettingsClick) {
                        Icon(ImageVector.vectorResource(R.drawable.settings), contentDescription = "Settings")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            item { AnimeSection("Trending Now", trending, onAnimeClick) }
            item { AnimeSection("Recommended for You", topRated, onAnimeClick) }
            item { AnimeSection("Most Popular", popular, onAnimeClick) }
            item { AnimeSection("Seasonal Anime", seasonal, onAnimeClick) }
            item { AnimeSection("Explore More", allTimePopular, onAnimeClick) }
        }
    }
}

@Composable
fun AnimeSection(
    title: String,
    animeList: List<Media>,
    onAnimeClick: (Int) -> Unit
) {
    val titleLanguage = LocalTitleLanguage.current
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(animeList) { anime ->
                SimpleAnimeCard(
                    title = anime.title.getDisplayTitle(titleLanguage),
                    imageUrl = anime.coverImage.extraLarge ?: anime.coverImage.large ?: "",
                    onClick = { onAnimeClick(anime.id) },
                    modifier = Modifier.width(140.dp)
                )
            }
        }
    }
}
