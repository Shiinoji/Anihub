package com.watchlist.anihub.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.watchlist.anihub.R
import com.watchlist.anihub.data.remote.Character
import com.watchlist.anihub.ui.UiState
import com.watchlist.anihub.ui.cleanDescription
import com.watchlist.anihub.ui.components.*
import com.watchlist.anihub.ui.theme.LocalTitleLanguage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CharacterDetailScreen(
    characterId: Int,
    onBackClick: () -> Unit,
    onAnimeClick: (Int) -> Unit,
    viewModel: CharacterDetailViewModel = hiltViewModel()
) {
    val characterState by viewModel.characterDetail.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    
    LaunchedEffect(characterId) {
        viewModel.fetchCharacterDetail(characterId)
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { viewModel.refresh(characterId) },
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = padding.calculateBottomPadding())
        ) {
            when (val state = characterState) {
                is UiState.Loading -> {
                    AnimeDetailSkeleton() // Using the same skeleton as AnimeDetail as it matches the layout
                }
                is UiState.Success<Character> -> {
                    val char = state.data
                    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
                    val metadataColor = if (isDark) Color.White else Color.Black
                    val metadataColorSecondary = metadataColor.copy(alpha = 0.8f)

                    val scrollState = rememberLazyListState()
                    val showStickyHeader by remember {
                        derivedStateOf {
                            scrollState.firstVisibleItemIndex > 0 || scrollState.firstVisibleItemScrollOffset > 200
                        }
                    }

                    Box(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            state = scrollState
                        ) {
                            // Blurred Header
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(320.dp)
                                ) {
                                    // Blurred Image Background
                                    AsyncImage(
                                        model = char.image.large,
                                        contentDescription = null,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .blur(20.dp),
                                        contentScale = ContentScale.Crop
                                    )
                                    // Gradient Overlay
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(
                                                Brush.verticalGradient(
                                                    colors = listOf(
                                                        if (isDark) Color.Black.copy(alpha = 0.6f) else Color.White.copy(alpha = 0.6f),
                                                        if (isDark) Color.Black.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.2f),
                                                        MaterialTheme.colorScheme.background
                                                    )
                                                )
                                            )
                                    )
                                    
                                    Column(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .statusBarsPadding()
                                    ) {
                                        Spacer(modifier = Modifier.height(72.dp))

                                        Row(
                                            modifier = Modifier
                                                .padding(horizontal = 16.dp)
                                                .fillMaxWidth(),
                                            verticalAlignment = Alignment.Bottom
                                        ) {
                                            AsyncImage(
                                                model = char.image.large,
                                                contentDescription = null,
                                                modifier = Modifier
                                                    .size(130.dp)
                                                    .clip(CircleShape)
                                                    .border(4.dp, MaterialTheme.colorScheme.surface, CircleShape),
                                                contentScale = ContentScale.Crop
                                            )
                                            Column(
                                                modifier = Modifier
                                                    .padding(start = 16.dp)
                                                    .fillMaxWidth()
                                            ) {
                                                Text(
                                                    text = char.name.full ?: "Unknown",
                                                    style = MaterialTheme.typography.headlineSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = metadataColor
                                                )
                                                Spacer(modifier = Modifier.height(8.dp))
                                                
                                                char.gender?.let {
                                                    Text("Gender: $it", style = MaterialTheme.typography.bodyMedium, color = metadataColorSecondary)
                                                }
                                                char.age?.let {
                                                    Text("Age: $it", style = MaterialTheme.typography.bodyMedium, color = metadataColorSecondary)
                                                }
                                                char.bloodType?.let {
                                                    Text("Blood Type: $it", style = MaterialTheme.typography.bodyMedium, color = metadataColorSecondary)
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // Description
                            item {
                                var expanded by remember { mutableStateOf(false) }
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = "Biography",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = char.description.cleanDescription().ifEmpty { "No biography available" },
                                        style = MaterialTheme.typography.bodyMedium,
                                        maxLines = if (expanded) Int.MAX_VALUE else 6,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier
                                            .animateContentSize()
                                            .clickable { expanded = !expanded }
                                    )
                                }
                            }

                            // Appearances (Anime)
                            val animeList = char.media?.nodes
                            if (!animeList.isNullOrEmpty()) {
                                item {
                                    Text(
                                        text = "Appearances",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(16.dp)
                                    )
                                    val titleLanguage = LocalTitleLanguage.current
                                    LazyRow(
                                        contentPadding = PaddingValues(horizontal = 16.dp),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        items(animeList) { anime ->
                                            SimpleAnimeCard(
                                                title = anime.title.getDisplayTitle(titleLanguage),
                                                imageUrl = anime.coverImage.extraLarge ?: anime.coverImage.large ?: "",
                                                onClick = { onAnimeClick(anime.id) },
                                                modifier = Modifier.width(120.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Header Buttons
                        Box(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Frosted Glass Header Background (appears on scroll)
                            AnimatedVisibility(
                                visible = showStickyHeader,
                                enter = fadeIn(),
                                exit = fadeOut()
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(140.dp)
                                        .background(
                                            Brush.verticalGradient(
                                                0f to MaterialTheme.colorScheme.surface.copy(alpha = 0.98f),
                                                0.6f to MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                                                1f to Color.Transparent
                                            )
                                        )
                                )
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .statusBarsPadding()
                                    .padding(horizontal = 8.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                HeaderIconButton(
                                    icon = ImageVector.vectorResource(R.drawable.arrow_left),
                                    onClick = onBackClick,
                                    tint = metadataColor
                                )
                            }
                        }
                    }
                }
                is UiState.Error -> {
                    ErrorView(
                        message = state.message,
                        onRetry = { viewModel.refresh(characterId) }
                    )
                }
            }
        }
    }
}
