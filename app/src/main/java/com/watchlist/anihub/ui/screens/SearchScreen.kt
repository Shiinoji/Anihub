package com.watchlist.anihub.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.watchlist.anihub.R
import com.watchlist.anihub.ui.UiState
import com.watchlist.anihub.ui.components.ErrorView
import com.watchlist.anihub.ui.components.SimpleAnimeCard
import com.watchlist.anihub.ui.components.SimpleAnimeCardSkeleton
import com.watchlist.anihub.ui.theme.LocalTitleLanguage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onAnimeClick: (Int) -> Unit,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val resultsState by viewModel.searchResults.collectAsState()
    var query by remember { mutableStateOf("") }
    val titleLanguage = LocalTitleLanguage.current

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
                modifier = Modifier
                    .statusBarsPadding()
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {}
        }
    ) { padding ->
        when (val state = resultsState) {
            is UiState.Loading -> {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(140.dp),
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(10) { SimpleAnimeCardSkeleton() }
                }
            }
            is UiState.Success -> {
                if (state.data.isEmpty() && query.isNotEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No results found for \"$query\"", style = MaterialTheme.typography.bodyLarge)
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(140.dp),
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding),
                        contentPadding = PaddingValues(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(state.data) { anime ->
                            SimpleAnimeCard(
                                title = anime.title.getDisplayTitle(titleLanguage),
                                imageUrl = anime.coverImage.extraLarge ?: anime.coverImage.large ?: "",
                                onClick = { onAnimeClick(anime.id) }
                            )
                        }
                    }
                }
            }
            is UiState.Error -> {
                ErrorView(
                    message = state.message,
                    onRetry = { viewModel.searchAnime(query) },
                    modifier = Modifier.padding(padding)
                )
            }
        }
    }
}
