package com.watchlist.anihub.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.watchlist.anihub.R
import com.watchlist.anihub.ui.components.HeaderIconButton
import com.watchlist.anihub.ui.components.SimpleAnimeCard
import com.watchlist.anihub.ui.theme.LocalTitleLanguage

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CharacterDetailScreen(
    characterId: Int,
    onBackClick: () -> Unit,
    onAnimeClick: (Int) -> Unit,
    viewModel: CharacterDetailViewModel = hiltViewModel()
) {
    val character by viewModel.characterDetail.collectAsState()
    
    LaunchedEffect(characterId) {
        viewModel.fetchCharacterDetail(characterId)
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        val char = character
        if (char != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = padding.calculateBottomPadding())
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
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
                            // Dark Gradient
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
                                Spacer(modifier = Modifier.height(80.dp))

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
                                        Text(
                                            text = char.name.full ?: "Unknown",
                                            style = MaterialTheme.typography.headlineSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        
                                        char.gender?.let {
                                            Text("Gender: $it", style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.9f))
                                        }
                                        char.age?.let {
                                            Text("Age: $it", style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.9f))
                                        }
                                        char.bloodType?.let {
                                            Text("Blood Type: $it", style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.9f))
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
                                text = char.description?.replace(Regex("<.*?>"), "") ?: "No biography available",
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
                    char.media?.nodes?.let { animeList ->
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
                }
            }
        } else {
            // Character Skeleton or Loading
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
    }
}
