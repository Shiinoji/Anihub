package com.watchlist.anihub.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PlayArrow
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
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.watchlist.anihub.R
import com.watchlist.anihub.data.local.WatchlistStatus
import com.watchlist.anihub.data.remote.Media
import com.watchlist.anihub.ui.UiState
import com.watchlist.anihub.ui.components.*
import com.watchlist.anihub.ui.theme.LocalScoreFormat
import com.watchlist.anihub.ui.theme.LocalShowAiringCountdown
import com.watchlist.anihub.ui.theme.LocalTitleLanguage

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
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
    val watchlistStatus by viewModel.watchlistStatus.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    
    LaunchedEffect(animeId) {
        viewModel.fetchAnimeDetail(animeId)
    }
    
    //pull to refresh
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
                is UiState.Success<*> -> {
                    val media = state.data as Media
                    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
                    val metadataColor = if (isDark) Color.White else Color.Black
                    val metadataColorSecondary = metadataColor.copy(alpha = 0.8f)

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

                                        // Metadata Header
                                        Row(
                                            modifier = Modifier
                                                .padding(horizontal = 16.dp)
                                                .fillMaxWidth(),
                                            verticalAlignment = Alignment.Bottom
                                        ) {
                                            Card(
                                                shape = RoundedCornerShape(12.dp),
                                                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                                                modifier = Modifier
                                                    .width(130.dp)
                                                    .height(190.dp)
                                                    .border(2.dp, MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
                                            ) {
                                                AsyncImage(
                                                    model = media.coverImage.extraLarge ?: media.coverImage.large,
                                                    contentDescription = null,
                                                    modifier = Modifier.fillMaxSize(),
                                                    contentScale = ContentScale.Crop
                                                )
                                            }
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
                                                    color = metadataColor,
                                                    maxLines = 3,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Spacer(modifier = Modifier.height(8.dp))
                                                
                                                // Status and Episodes Chips/Rows
                                                Surface(
                                                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f),
                                                    shape = RoundedCornerShape(4.dp)
                                                ) {
                                                    Text(
                                                        text = "Status: ${media.status ?: "Unknown"}",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = metadataColorSecondary,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                                
                                                Spacer(modifier = Modifier.height(4.dp))
                                                
                                                val episodesText = if (media.nextAiringEpisode != null) {
                                                    "Episodes: ${media.nextAiringEpisode.episode - 1} / ${media.episodes ?: "?"}"
                                                } else {
                                                    "Episodes: ${media.episodes ?: "?"}"
                                                }
                                                Text(episodesText, style = MaterialTheme.typography.bodyMedium, color = metadataColorSecondary)
                                                
                                                Text("Score: ${media.getFormattedScore(scoreFormat)}", style = MaterialTheme.typography.bodyMedium, color = metadataColorSecondary)
                                                
                                                if (showCountdown && media.nextAiringEpisode != null) {
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    Text(
                                                        "Ep ${media.nextAiringEpisode.episode} airing soon",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = MaterialTheme.colorScheme.primary,
                                                        fontWeight = FontWeight.ExtraBold
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
                                    val genresList = media.genres
                                    if (!genresList.isNullOrEmpty()) {
                                        Spacer(modifier = Modifier.height(12.dp))
                                        FlowRow(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            for (genre in genresList) {
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

                            // Watchlist Status Selector
                            if (isInWatchlist) {
                                item {
                                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                                        Text(
                                            text = "Watchlist Status",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            WatchlistStatus.entries.forEach { status ->
                                                FilterChip(
                                                    selected = watchlistStatus == status,
                                                    onClick = { viewModel.updateWatchlistStatus(status) },
                                                    label = { Text(status.getDisplayName()) },
                                                    leadingIcon = if (watchlistStatus == status) {
                                                        { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                                    } else null,
                                                    colors = FilterChipDefaults.filterChipColors(
                                                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                                    )
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // Trailer
                            val trailer = media.trailer
                            if (trailer != null && trailer.url != null) {
                                item {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text(
                                            text = "Trailer",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        val uriHandler = LocalUriHandler.current
                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(200.dp)
                                                .clickable { uriHandler.openUri(trailer.url!!) },
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Box(modifier = Modifier.fillMaxSize()) {
                                                AsyncImage(
                                                    model = trailer.thumbnail,
                                                    contentDescription = "Trailer Thumbnail",
                                                    modifier = Modifier.fillMaxSize(),
                                                    contentScale = ContentScale.Crop
                                                )
                                                // Play Icon Overlay
                                                Box(
                                                    modifier = Modifier
                                                        .size(64.dp)
                                                        .align(Alignment.Center)
                                                        .background(Color.Black.copy(alpha = 0.6f), CircleShape),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.PlayArrow,
                                                        contentDescription = null,
                                                        tint = Color.White,
                                                        modifier = Modifier.size(32.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // Characters
                            val charactersNodes = media.characters?.nodes
                            if (!charactersNodes.isNullOrEmpty()) {
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
                                        for (character in charactersNodes) {
                                            item {
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
                            }

                            // Recommendations
                            val recommendationsNodes = media.recommendations?.nodes
                            if (!recommendationsNodes.isNullOrEmpty()) {
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
                                        for (rec in recommendationsNodes) {
                                            val recommendedMedia = rec.mediaRecommendation
                                            if (recommendedMedia != null) {
                                                item {
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
                                onClick = onBackClick,
                                tint = metadataColor
                            )
                            Row {
                                HeaderIconButton(
                                    icon = ImageVector.vectorResource(R.drawable.bookmark),
                                    onClick = { viewModel.toggleWatchlist() },
                                    tint = if (isInWatchlist) MaterialTheme.colorScheme.primary else metadataColor
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                HeaderIconButton(
                                    icon = ImageVector.vectorResource(R.drawable.heart),
                                    onClick = { viewModel.toggleFavorite() },
                                    tint = if (isFavorite) Color.Red else metadataColor
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
