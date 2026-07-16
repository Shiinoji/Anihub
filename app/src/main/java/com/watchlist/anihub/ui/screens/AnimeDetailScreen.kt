package com.watchlist.anihub.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.FavoriteBorder
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
import com.watchlist.anihub.ui.cleanDescription
import com.watchlist.anihub.ui.components.*
import com.watchlist.anihub.ui.theme.LocalScoreFormat
import com.watchlist.anihub.ui.theme.LocalShowAiringCountdown
import com.watchlist.anihub.ui.theme.LocalTitleLanguage

/**
 * Screen displaying comprehensive details about a specific anime, including characters,
 * recommendations, and trailer. Allows users to manage the anime in their watchlist.
 *
 * @param animeId The ID of the anime to display.
 * @param onBackClick Callback to navigate back.
 * @param onAnimeClick Callback to navigate to another anime's details (e.g., from recommendations).
 * @param onCharacterClick Callback to navigate to character details.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class, ExperimentalFoundationApi::class)
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
    
    var showImageViewer by remember { mutableStateOf(false) }

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
                    AnimeDetailSkeleton() // Display pulse loading effect
                }
                is UiState.Success<*> -> {
                    val media = state.data as Media
                    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
                    val metadataColor = if (isDark) Color.White else Color.Black
                    val metadataColorSecondary = metadataColor.copy(alpha = 0.8f)
                    
                    val scrollState = rememberLazyListState()
                    
                    // Logic to show/hide the sticky frosted glass header based on scroll position
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
                            // Section 1: Visual Header with Blurred Background
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(320.dp)
                                ) {
                                    // Ambient background effect using the anime's cover image
                                    AsyncImage(
                                        model = media.coverImage.extraLarge ?: media.coverImage.large,
                                        contentDescription = null,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .blur(20.dp),
                                        contentScale = ContentScale.Crop
                                    )
                                    // Modern gradient overlay for readability and depth
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

                                        // Anime Poster and high-level metadata (Type, Status, Episodes, Score)
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
                                                    .combinedClickable(
                                                        onClick = { /* Standard click could do something else or nothing */ },
                                                        onLongClick = { showImageViewer = true }
                                                    )
                                            ) {
                                                AsyncImage(
                                                    model = media.coverImage.extraLarge ?: media.coverImage.large,
                                                    contentDescription = null,
                                                    modifier = Modifier.fillMaxSize(),
                                                    contentScale = ContentScale.Crop
                                                )
                                            }

                                            if (showImageViewer) {
                                                ImageViewerDialog(
                                                    imageUrl = media.coverImage.extraLarge ?: media.coverImage.large ?: "",
                                                    title = media.title.displayTitle,
                                                    onDismiss = { showImageViewer = false }
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
                                                
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text("Type: ", style = MaterialTheme.typography.bodyMedium, color = metadataColorSecondary)
                                                    Text(media.format ?: "Unknown", style = MaterialTheme.typography.bodyMedium, color = metadataColorSecondary, fontWeight = FontWeight.Bold)
                                                }
                                                
                                                Spacer(modifier = Modifier.height(4.dp))

                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text("Status: ", style = MaterialTheme.typography.bodyMedium, color = metadataColorSecondary)
                                                    Surface(
                                                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f),
                                                        shape = RoundedCornerShape(4.dp)
                                                    ) {
                                                        Text(
                                                            text = media.status ?: "Unknown",
                                                            style = MaterialTheme.typography.bodyMedium,
                                                            color = metadataColorSecondary,
                                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                        )
                                                    }
                                                }
                                                
                                                Spacer(modifier = Modifier.height(4.dp))
                                                
                                                val episodesText = if (media.nextAiringEpisode != null) {
                                                    "Episodes: ${media.nextAiringEpisode.episode - 1} / ${media.episodes ?: "?"}"
                                                } else {
                                                    "Episodes: ${media.episodes ?: "?"}"
                                                }
                                                Text(episodesText, style = MaterialTheme.typography.bodyMedium, color = metadataColorSecondary)
                                                
                                                Text("Score: ${media.getFormattedScore(scoreFormat)}", style = MaterialTheme.typography.bodyMedium, color = metadataColorSecondary)
                                                
                                                // Airing countdown alert for ongoing series
                                                if (showCountdown && media.nextAiringEpisode != null) {
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    Text(
                                                        "Ep ${media.nextAiringEpisode.episode} airing soon",
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        color = MaterialTheme.colorScheme.primary,
                                                        fontWeight = FontWeight.ExtraBold
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // Section 2: Expandable Description and Genre Chips
                            item {
                                var expanded by remember { mutableStateOf(false) }
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(text = "Description", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = media.description.cleanDescription().ifEmpty { "No description available" },
                                        style = MaterialTheme.typography.bodyMedium,
                                        maxLines = if (expanded) Int.MAX_VALUE else 4,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier
                                            .animateContentSize()
                                            .clickable { expanded = !expanded }
                                    )

                                    val genresList = media.genres
                                    if (!genresList.isNullOrEmpty()) {
                                        Spacer(modifier = Modifier.height(12.dp))
                                        FlowRow(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            for (genre in genresList) {
                                                Surface(
                                                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                                                    shape = RoundedCornerShape(16.dp),
                                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                                                ) {
                                                    Text(
                                                        text = genre,
                                                        style = MaterialTheme.typography.labelMedium,
                                                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // Section 3: Watchlist Status (Visible only if anime is in collection)
                            if (isInWatchlist) {
                                item {
                                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                                        Text(text = "Watchlist Status", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        FlowRow(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
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

                            // Section 4: Video Trailer
                            val trailer = media.trailer
                            if (trailer != null && trailer.url != null) {
                                item {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text(text = "Trailer", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        val uriHandler = LocalUriHandler.current
                                        Card(
                                            modifier = Modifier.fillMaxWidth().height(200.dp).clickable { uriHandler.openUri(trailer.url!!) },
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Box(modifier = Modifier.fillMaxSize()) {
                                                AsyncImage(
                                                    model = trailer.thumbnail,
                                                    contentDescription = "Trailer Thumbnail",
                                                    modifier = Modifier.fillMaxSize(),
                                                    contentScale = ContentScale.Crop
                                                )
                                                // Play overlay
                                                Box(
                                                    modifier = Modifier.size(64.dp).align(Alignment.Center).background(Color.Black.copy(alpha = 0.6f), CircleShape),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(32.dp))
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // Section 5: Characters Horizontal List
                            val charactersNodes = media.characters?.nodes
                            if (!charactersNodes.isNullOrEmpty()) {
                                item {
                                    Text(text = "Characters", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
                                    LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                        items(charactersNodes) { character ->
                                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(80.dp).clickable { onCharacterClick(character.id) }) {
                                                AsyncImage(
                                                    model = character.image.large,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(70.dp).clip(CircleShape),
                                                    contentScale = ContentScale.Crop
                                                )
                                                Text(text = character.name.full ?: "", style = MaterialTheme.typography.labelSmall, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(top = 4.dp))
                                            }
                                        }
                                    }
                                }
                            }

                            // Section 6: Recommended Anime Horizontal List
                            val recommendationsNodes = media.recommendations?.nodes
                            if (!recommendationsNodes.isNullOrEmpty()) {
                                item {
                                    Text(text = "Recommendation", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(16.dp))
                                    LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                        items(recommendationsNodes) { rec ->
                                            val recommendedMedia = rec.mediaRecommendation
                                            if (recommendedMedia != null) {
                                                AnimeCard(media = recommendedMedia, onClick = { onAnimeClick(recommendedMedia.id) })
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Floating Sticky Header (Back button and Watchlist/Favorite controls)
                        Box(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Blurred glass effect revealed on scroll
                            androidx.compose.animation.AnimatedVisibility(
                                visible = showStickyHeader,
                                enter = androidx.compose.animation.fadeIn(),
                                exit = androidx.compose.animation.fadeOut()
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

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    HeaderIconButton(
                                        icon = if (isFavorite) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                                        onClick = { viewModel.toggleFavorite() },
                                        tint = if (isFavorite) Color.Red else metadataColor
                                    )
                                    HeaderIconButton(
                                        icon = if (isInWatchlist) Icons.Default.Bookmark else Icons.Outlined.BookmarkBorder,
                                        onClick = { viewModel.toggleWatchlist() },
                                        tint = if (isInWatchlist) MaterialTheme.colorScheme.primary else metadataColor
                                    )
                                }
                            }
                        }
                    }
                }
                is UiState.Error -> {
                    val isConnectionError = state.message.contains("network", ignoreCase = true) || 
                                          state.message.contains("internet", ignoreCase = true) ||
                                          state.message.contains("connection", ignoreCase = true)
                    ErrorView(
                        message = state.message,
                        onRetry = { viewModel.refresh(animeId) },
                        icon = if (isConnectionError) {
                            ImageVector.vectorResource(R.drawable.wifi_off)
                        } else {
                            ImageVector.vectorResource(R.drawable.triangle_alert)
                        }
                    )
                }
            }
        }
    }
}
