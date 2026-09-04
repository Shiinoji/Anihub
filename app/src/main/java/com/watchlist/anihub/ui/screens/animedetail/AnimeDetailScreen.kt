package com.watchlist.anihub.ui.screens.animedetail

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
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
import androidx.compose.material.icons.automirrored.filled.*
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
import androidx.compose.ui.text.style.TextAlign
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
                    AnimeDetailSkeleton()
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
                                        .heightIn(min = 400.dp)
                                ) {
                                    // Ambient background effect using the anime's cover image
                                    AsyncImage(
                                        model = media.coverImage.extraLarge ?: media.coverImage.large,
                                        contentDescription = null,
                                        modifier = Modifier
                                            .matchParentSize()
                                            .blur(40.dp),
                                        contentScale = ContentScale.Crop
                                    )
                                    // Modern gradient overlay for readability and depth
                                    Box(
                                        modifier = Modifier
                                            .matchParentSize()
                                            .background(
                                                Brush.verticalGradient(
                                                    colors = listOf(
                                                        Color.Transparent,
                                                        MaterialTheme.colorScheme.background.copy(alpha = 0.5f),
                                                        MaterialTheme.colorScheme.background
                                                    )
                                                )
                                            )
                                    )
                                    
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .statusBarsPadding()
                                            .padding(bottom = 24.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Spacer(modifier = Modifier.height(64.dp))

                                        // Large Centered Poster
                                        Card(
                                            shape = RoundedCornerShape(16.dp),
                                            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
                                            modifier = Modifier
                                                .width(200.dp)
                                                .height(300.dp)
                                                .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
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
                                        
                                        Spacer(modifier = Modifier.height(24.dp))

                                        // Title and High-level metadata
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 24.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            val titleLanguage = LocalTitleLanguage.current
                                            val scoreFormat = LocalScoreFormat.current
                                            val showCountdown = LocalShowAiringCountdown.current
                                            
                                            Text(
                                                text = media.title.getDisplayTitle(titleLanguage),
                                                style = MaterialTheme.typography.headlineMedium,
                                                fontWeight = FontWeight.Medium,
                                                color = metadataColor,
                                                textAlign = TextAlign.Center,
                                                lineHeight = MaterialTheme.typography.headlineMedium.lineHeight * 0.9f
                                            )
                                            
                                            Spacer(modifier = Modifier.height(16.dp))

                                            // Metadata Chips Row
                                            FlowRow(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
                                                verticalArrangement = Arrangement.spacedBy(10.dp)
                                            ) {
                                                MetadataBadge(text = media.format ?: "TV", color = metadataColor)
                                                MetadataBadge(text = media.status ?: "RELEASING", color = metadataColor, isPrimary = true)
                                                
                                                val episodesText = if (media.nextAiringEpisode != null) {
                                                    "${media.nextAiringEpisode.episode - 1} / ${media.episodes ?: "?"} eps"
                                                } else {
                                                    "${media.episodes ?: "?"} episodes"
                                                }
                                                MetadataBadge(text = episodesText, color = metadataColor, icon = Icons.Default.Dvr)
                                                MetadataBadge(text = media.getFormattedScore(scoreFormat), color = Color(0xFFFFD700), icon = Icons.Default.Star)
                                            }

                                            val mainStudio = media.studios?.nodes?.firstOrNull()?.name
                                            if (mainStudio != null) {
                                                Spacer(modifier = Modifier.height(16.dp))
                                                Surface(
                                                    color = metadataColor.copy(alpha = 0.05f),
                                                    shape = RoundedCornerShape(8.dp),
                                                    border = BorderStroke(1.dp, metadataColor.copy(alpha = 0.1f))
                                                ) {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.Business,
                                                            contentDescription = null,
                                                            modifier = Modifier.size(16.dp),
                                                            tint = metadataColorSecondary
                                                        )
                                                        Spacer(modifier = Modifier.width(8.dp))
                                                        Text(
                                                            text = mainStudio,
                                                            style = MaterialTheme.typography.labelLarge,
                                                            color = metadataColor,
                                                            fontWeight = FontWeight.Medium
                                                        )
                                                    }
                                                }
                                            }

                                            // Airing countdown alert for ongoing series
                                            if (showCountdown && media.nextAiringEpisode != null) {
                                                Spacer(modifier = Modifier.height(12.dp))
                                                Surface(
                                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                                    shape = RoundedCornerShape(12.dp),
                                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                                                ) {
                                                    Text(
                                                        "Episode ${media.nextAiringEpisode.episode} airing soon",
                                                        style = MaterialTheme.typography.labelLarge,
                                                        color = MaterialTheme.colorScheme.primary,
                                                        fontWeight = FontWeight.Medium,
                                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
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
                                    Text(text = "Description", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
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

                            // Section 3: Information Details
                            item {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(text = "Information", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
                                    Spacer(modifier = Modifier.height(12.dp))
                                    
                                    FlowRow(
                                        modifier = Modifier.fillMaxWidth(),
                                        maxItemsInEachRow = 2,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        verticalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        val infoItems = listOf(
                                            "Source" to (media.source?.replace("_", " ")?.lowercase()?.replaceFirstChar { it.uppercase() } ?: "Unknown"),
                                            "Season" to (if (media.season != null && media.seasonYear != null) "${media.season.lowercase().replaceFirstChar { it.uppercase() }} ${media.seasonYear}" else "Unknown"),
                                            "Start Date" to media.startDate.formatDate(),
                                            "End Date" to media.endDate.formatDate()
                                        )
                                        
                                        infoItems.forEach { (label, value) ->
                                            Card(
                                                modifier = Modifier.weight(1f),
                                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)),
                                                shape = RoundedCornerShape(16.dp),
                                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                                            ) {
                                                Column(modifier = Modifier.padding(12.dp)) {
                                                    Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                    Spacer(modifier = Modifier.height(2.dp))
                                                    Text(text = value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            item { HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)) }

                            // Section 4: Watchlist Status (Visible only if anime is in collection)
                            if (isInWatchlist) {
                                item {
                                    Column(modifier = Modifier.padding(vertical = 12.dp)) {
                                        Text(
                                            text = "Watchlist Status", 
                                            style = MaterialTheme.typography.titleMedium, 
                                            fontWeight = FontWeight.Medium,
                                            modifier = Modifier.padding(horizontal = 16.dp)
                                        )
                                        Spacer(modifier = Modifier.height(12.dp))
                                        
                                        LazyRow(
                                            modifier = Modifier.fillMaxWidth(),
                                            contentPadding = PaddingValues(horizontal = 16.dp),
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            WatchlistStatus.entries.forEach { status ->
                                                item {
                                                    val isSelected = watchlistStatus == status
                                                    val statusColor = when (status) {
                                                        WatchlistStatus.WATCHING -> Color(0xFF4CAF50)
                                                        WatchlistStatus.PLAN_TO_WATCH -> Color(0xFF2196F3)
                                                        WatchlistStatus.FINISHED -> Color(0xFF9C27B0)
                                                        WatchlistStatus.ON_HOLD -> Color(0xFFFF9800)
                                                        WatchlistStatus.DROPPED -> Color(0xFFF44336)
                                                    }

                                                    Surface(
                                                        onClick = { viewModel.updateWatchlistStatus(status) },
                                                        shape = RoundedCornerShape(16.dp),
                                                        color = if (isSelected) statusColor.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                                        border = BorderStroke(
                                                            width = if (isSelected) 2.dp else 1.dp,
                                                            color = if (isSelected) statusColor else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                                        ),
                                                        modifier = Modifier.animateContentSize()
                                                    ) {
                                                        Box(
                                                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            Text(
                                                                text = status.getDisplayName(),
                                                                style = MaterialTheme.typography.labelLarge,
                                                                fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                                                                color = if (isSelected) statusColor else MaterialTheme.colorScheme.onSurfaceVariant
                                                            )
                                                        }
                                                    }
                                                }
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
                                        Text(text = "Trailer", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
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
                                    Text(text = "Characters", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
                                    LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                        items(charactersNodes) { character ->
                                            Card(
                                                modifier = Modifier.width(100.dp).clickable { onCharacterClick(character.id) },
                                                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                                                shape = RoundedCornerShape(12.dp)
                                            ) {
                                                Column(
                                                    horizontalAlignment = Alignment.CenterHorizontally,
                                                    modifier = Modifier.padding(vertical = 8.dp)
                                                ) {
                                                    AsyncImage(
                                                        model = character.image.large,
                                                        contentDescription = null,
                                                        modifier = Modifier.size(80.dp).clip(CircleShape),
                                                        contentScale = ContentScale.Crop
                                                    )
                                                    Spacer(modifier = Modifier.height(8.dp))
                                                    Text(text = character.name.full ?: "", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp))
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            item { HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)) }

                            // Section 6: Staff Horizontal List
                            val staffEdges = media.staff?.edges
                            if (!staffEdges.isNullOrEmpty()) {
                                item {
                                    Text(text = "Production Staff", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
                                    LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                        items(staffEdges) { edge ->
                                            val staff = edge.node
                                            if (staff != null) {
                                                Card(
                                                    modifier = Modifier.width(100.dp),
                                                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                                                    shape = RoundedCornerShape(12.dp)
                                                ) {
                                                    Column(
                                                        horizontalAlignment = Alignment.CenterHorizontally,
                                                        modifier = Modifier.padding(vertical = 8.dp)
                                                    ) {
                                                        AsyncImage(
                                                            model = staff.image.large,
                                                            contentDescription = null,
                                                            modifier = Modifier.size(80.dp).clip(CircleShape),
                                                            contentScale = ContentScale.Crop
                                                        )
                                                        Spacer(modifier = Modifier.height(8.dp))
                                                        Text(text = staff.name.full ?: "", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp))
                                                        Text(text = edge.role ?: "", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp))
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            item { HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)) }

                            // Section 7: External Links
                            val externalLinks = media.externalLinks
                            if (!externalLinks.isNullOrEmpty()) {
                                item {
                                    Text(text = "External Links", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
                                    val uriHandler = LocalUriHandler.current
                                    FlowRow(
                                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        verticalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        for (link in externalLinks) {
                                            AssistChip(
                                                onClick = { uriHandler.openUri(link.url) },
                                                label = { Text(link.site, fontWeight = FontWeight.SemiBold) },
                                                leadingIcon = { Icon(Icons.AutoMirrored.Filled.OpenInNew, null, modifier = Modifier.size(16.dp)) },
                                                shape = RoundedCornerShape(12.dp),
                                                colors = AssistChipDefaults.assistChipColors(
                                                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                                                    labelColor = MaterialTheme.colorScheme.onSurface
                                                ),
                                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                                            )
                                        }
                                    }
                                }
                            }

                            // Section 8: Tags
                            val tags = media.tags?.filter { !it.isGeneralSpoiler && !it.isMediaSpoiler }
                            if (!tags.isNullOrEmpty()) {
                                item {
                                    var tagsExpanded by remember { mutableStateOf(false) }
                                    val displayedTags = if (tagsExpanded) tags else tags.take(10)

                                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(text = "Tags", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
                                            if (tags.size > 10) {
                                                TextButton(onClick = { tagsExpanded = !tagsExpanded }) {
                                                    Text(if (tagsExpanded) "Show Less" else "Show More (${tags.size})")
                                                }
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                        FlowRow(
                                            modifier = Modifier.fillMaxWidth().animateContentSize(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            for (tag in displayedTags) {
                                                SuggestionChip(
                                                    onClick = { },
                                                    label = { Text("${tag.name} ${tag.rank}%", style = MaterialTheme.typography.labelSmall) },
                                                    shape = RoundedCornerShape(8.dp),
                                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                                                    colors = SuggestionChipDefaults.suggestionChipColors(
                                                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                                    )
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // Section 9: Recommended Anime Horizontal List
                            val recommendationsNodes = media.recommendations?.nodes
                            if (!recommendationsNodes.isNullOrEmpty()) {
                                item {
                                    Text(text = "Recommendation", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium, modifier = Modifier.padding(16.dp))
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

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f).padding(horizontal = 12.dp)
                                ) {
                                    androidx.compose.animation.AnimatedVisibility(
                                        visible = showStickyHeader,
                                        enter = androidx.compose.animation.fadeIn() + androidx.compose.animation.slideInHorizontally(),
                                        exit = androidx.compose.animation.fadeOut() + androidx.compose.animation.slideOutHorizontally()
                                    ) {
                                        Text(
                                            text = media.title.getDisplayTitle(LocalTitleLanguage.current),
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Medium,
                                            color = metadataColor,
                                            maxLines = 1,
                                            modifier = Modifier.basicMarquee()
                                        )
                                    }
                                }

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

@Composable
private fun MetadataBadge(
    text: String,
    color: Color,
    icon: ImageVector? = null,
    isPrimary: Boolean = false
) {
    Surface(
        color = if (isPrimary) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, if (isPrimary) MaterialTheme.colorScheme.primary.copy(alpha = 0.3f) else color.copy(alpha = 0.15f))
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = if (isPrimary) MaterialTheme.colorScheme.primary else color.copy(alpha = 0.8f)
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                color = if (isPrimary) MaterialTheme.colorScheme.primary else color,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun DetailItem(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(0.4f))
        Text(text = value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, modifier = Modifier.weight(0.6f))
    }
}

private fun com.watchlist.anihub.data.remote.FuzzyDate?.formatDate(): String {
    if (this == null || (year == null && month == null && day == null)) return "Unknown"
    val monthNames = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
    val m = month?.let { if (it in 1..12) monthNames[it - 1] else null }
    val d = day?.toString()
    val y = year?.toString()
    return listOfNotNull(m, d, y).joinToString(" ")
}
