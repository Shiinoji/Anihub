package com.watchlist.anihub.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.watchlist.anihub.R
import com.watchlist.anihub.data.remote.Media
import com.watchlist.anihub.ui.UiState
import com.watchlist.anihub.ui.components.*
import com.watchlist.anihub.ui.theme.LocalScoreFormat
import com.watchlist.anihub.ui.theme.LocalShowAiringCountdown
import com.watchlist.anihub.ui.theme.LocalTitleLanguage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnimeDetailScreen(
    animeId: Int,
    onBackClick: () -> Unit,
    onAnimeClick: (Int) -> Unit,
    onCharacterClick: (Int) -> Unit,
    viewModel: AnimeDetailViewModel = hiltViewModel()
) {
    val animeState by viewModel.animeDetail.collectAsState()
    val isInWatchlist by viewModel.isInWatchlist.collectAsState()
    val isFavorite by viewModel.isFavorite.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    
    LaunchedEffect(animeId) {
        viewModel.fetchAnimeDetail(animeId)
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { viewModel.refresh(animeId) },
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = padding.calculateBottomPadding())
        ) {
            when (val state = animeState) {
                is UiState.Loading -> {
                    AnimeDetailSkeleton()
                }
                is UiState.Success -> {
                    val media = state.data
                    Box(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            // Blurred Background & Banner
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(320.dp)
                                ) {
                                    // Blurred Cover Background
                                    AsyncImage(
                                        model = media.coverImage.extraLarge ?: media.coverImage.large,
                                        contentDescription = null,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .blur(20.dp),
                                        contentScale = ContentScale.Crop
                                    )
                                    // Dark Gradient Overlay
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(
                                                Brush.verticalGradient(
                                                    colors = listOf(
                                                        Color.Black.copy(alpha = 0.5f),
                                                        Color.Transparent,
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
                                        Spacer(modifier = Modifier.height(80.dp)) // Space for overlaid buttons at the top

                                        // Metadata Header
                                        Row(
                                            modifier = Modifier
                                                .padding(horizontal = 16.dp)
                                                .fillMaxWidth(),
                                            verticalAlignment = Alignment.Bottom
                                        ) {
                                            AsyncImage(
                                                model = media.coverImage.extraLarge ?: media.coverImage.large,
                                                contentDescription = null,
                                                modifier = Modifier
                                                    .width(130.dp)
                                                    .height(190.dp)
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .border(2.dp, MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp)),
                                                contentScale = ContentScale.Crop
                                            )
                                            Column(
                                                modifier = Modifier
                                                    .padding(start = 16.dp)
                                                    .fillMaxWidth()
                                            ) {
                                                val titleLanguage = LocalTitleLanguage.current
                                                val scoreFormat = LocalScoreFormat.current
                                                val showCountdown = LocalShowAiringCountdown.current
                                                
                                                Text(
                                                    text = media.title.getDisplayTitle(titleLanguage),
                                                    style = MaterialTheme.typography.titleLarge,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White,
                                                    maxLines = 3,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Spacer(modifier = Modifier.height(8.dp))
                                                Text("Status: ${media.status ?: "Unknown"}", style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.9f))
                                                
                                                val episodesText = if (media.nextAiringEpisode != null) {
                                                    "Episodes: ${media.nextAiringEpisode.episode - 1} / ${media.episodes ?: "?"}"
                                                } else {
                                                    "Episodes: ${media.episodes ?: "?"}"
                                                }
                                                Text(episodesText, style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.9f))
                                                
                                                Text("Score: ${media.getFormattedScore(scoreFormat)}", style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.9f))
                                                
                                                if (showCountdown && media.nextAiringEpisode != null) {
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    Text(
                                                        "Ep ${media.nextAiringEpisode.episode} airing soon",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = MaterialTheme.colorScheme.primaryContainer,
                                                        fontWeight = FontWeight.Bold
                                                    )
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
                                        text = "Description",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = media.description?.replace(Regex("<.*?>"), "") ?: "No description available",
                                        style = MaterialTheme.typography.bodyMedium,
                                        maxLines = if (expanded) Int.MAX_VALUE else 4,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier
                                            .animateContentSize()
                                            .clickable { expanded = !expanded }
                                    )

                                    // Genres
                                    media.genres?.let { genres ->
                                        Spacer(modifier = Modifier.height(12.dp))
                                        FlowRow(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            genres.forEach { genre ->
                                                SuggestionChip(
                                                    onClick = { /* TODO: Filter by genre */ },
                                                    label = { Text(genre, style = MaterialTheme.typography.labelSmall) },
                                                    shape = RoundedCornerShape(16.dp),
                                                    colors = SuggestionChipDefaults.suggestionChipColors(
                                                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                                                        labelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                                    ),
                                                    border = null
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // Characters
                            media.characters?.nodes?.let { characters ->
                                item {
                                    Text(
                                        text = "Characters",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                    )
                                    LazyRow(
                                        contentPadding = PaddingValues(horizontal = 16.dp),
                                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        items(characters) { character ->
                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                modifier = Modifier
                                                    .width(80.dp)
                                                    .clickable { onCharacterClick(character.id) }
                                            ) {
                                                AsyncImage(
                                                    model = character.image.large,
                                                    contentDescription = null,
                                                    modifier = Modifier
                                                        .size(70.dp)
                                                        .clip(CircleShape),
                                                    contentScale = ContentScale.Crop
                                                )
                                                Text(
                                                    text = character.name.full ?: "",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis,
                                                    modifier = Modifier.padding(top = 4.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // Recommendations
                            media.recommendations?.nodes?.let { recommendations ->
                                item {
                                    Text(
                                        text = "Recommendation",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(16.dp)
                                    )
                                    LazyRow(
                                        contentPadding = PaddingValues(horizontal = 16.dp),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        items(recommendations) { rec ->
                                            rec.mediaRecommendation?.let { recommendedMedia ->
                                                AnimeCard(
                                                    media = recommendedMedia,
                                                    onClick = { onAnimeClick(recommendedMedia.id) }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Header Overlay (Buttons)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .statusBarsPadding()
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            HeaderIconButton(
                                icon = ImageVector.vectorResource(R.drawable.arrow_left),
                                onClick = onBackClick
                            )
                            Row {
                                HeaderIconButton(
                                    icon = if (isInWatchlist) ImageVector.vectorResource(R.drawable.bookmark) else ImageVector.vectorResource(R.drawable.bookmark),
                                    onClick = { viewModel.toggleWatchlist() },
                                    tint = if (isInWatchlist) MaterialTheme.colorScheme.primary else Color.White
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                HeaderIconButton(
                                    icon = ImageVector.vectorResource(R.drawable.heart),
                                    onClick = { viewModel.toggleFavorite() },
                                    tint = if (isFavorite) Color.Red else Color.White
                                )
                            }
                        }
                    }
                }
                is UiState.Error -> {
                    ErrorView(
                        message = state.message,
                        onRetry = { viewModel.refresh(animeId) }
                    )
                }
            }
        }
    }
}
