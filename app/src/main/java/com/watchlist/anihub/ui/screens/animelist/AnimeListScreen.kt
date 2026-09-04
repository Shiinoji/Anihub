package com.watchlist.anihub.ui.screens.animelist

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.watchlist.anihub.R
import com.watchlist.anihub.ui.UiState
import com.watchlist.anihub.ui.components.SimpleAnimeCard
import com.watchlist.anihub.ui.components.SimpleAnimeCardSkeleton
import com.watchlist.anihub.ui.theme.LocalTitleLanguage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnimeListScreen(
    category: String,
    onBackClick: () -> Unit,
    onAnimeClick: (Int) -> Unit,
    viewModel: AnimeListViewModel = hiltViewModel(),
) {
    val animeList by viewModel.animeList.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val watchlistMap by viewModel.watchlistMap.collectAsState()
    val titleLanguage = LocalTitleLanguage.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = category,
                        style = MaterialTheme.typography.headlineSmall.copy(fontSize = 20.sp),
                        fontWeight = FontWeight.Medium
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(ImageVector.vectorResource(R.drawable.arrow_left), contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = (uiState is UiState.Loading && animeList.isNotEmpty()),
            onRefresh = { viewModel.refresh() },
            modifier = Modifier.padding(padding)
        ) {
            if (uiState is UiState.Loading && animeList.isEmpty()) {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(140.dp),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(12) { SimpleAnimeCardSkeleton() }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(140.dp),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    itemsIndexed(animeList) { index, anime ->
                        if (index >= animeList.size - 5) {
                            LaunchedEffect(Unit) {
                                viewModel.loadNextPage()
                            }
                        }
                        
                        SimpleAnimeCard(
                            title = anime.title.getDisplayTitle(titleLanguage),
                            imageUrl = anime.coverImage.extraLarge ?: anime.coverImage.large ?: "",
                            onClick = { onAnimeClick(anime.id) },
                            status = watchlistMap[anime.id]?.getDisplayName()
                        )
                    }

                    if (uiState is UiState.Loading && animeList.isNotEmpty()) {
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
