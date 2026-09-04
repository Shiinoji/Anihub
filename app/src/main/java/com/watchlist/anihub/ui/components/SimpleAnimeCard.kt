package com.watchlist.anihub.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

/**
 * A standardized card component for displaying anime media with an optional status badge.
 * Supports both click and long-click interactions.
 *
 * @param title The display title of the anime.
 * @param imageUrl The URL of the anime's cover image.
 * @param onClick Triggered when the card is clicked.
 * @param modifier Custom modifier for the component.
 * @param onLongClick Triggered when the card is long-pressed (optional).
 * @param status Optional text for a status badge (e.g., "Watching", "Finished") displayed in the top-left.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SimpleAnimeCard(
    title: String,
    imageUrl: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onLongClick: (() -> Unit)? = null,
    status: String? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
    ) {
        Card(
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .aspectRatio(0.7f)
                .fillMaxWidth()
                .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // High-quality anime cover image
                AsyncImage(
                    model = imageUrl,
                    contentDescription = title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Watchlist status badge (appears if the anime is in the user's list)
                if (status != null) {
                    val statusColor = when (status.lowercase()) {
                        "watching" -> Color(0xFF4CAF50)
                        "plan to watch" -> Color(0xFF2196F3)
                        "finished" -> Color(0xFF9C27B0)
                        "on hold" -> Color(0xFFFF9800)
                        "dropped" -> Color(0xFFF44336)
                        else -> MaterialTheme.colorScheme.primary
                    }

                    Surface(
                        color = statusColor.copy(alpha = 0.7f),
                        shape = RoundedCornerShape(bottomEnd = 12.dp),
                        modifier = Modifier.align(Alignment.TopStart),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
                    ) {
                        Text(
                            text = status.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            color = Color.White,
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        // Anime title with 2-line overflow protection
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            modifier = Modifier
                .padding(horizontal = 4.dp)
                .basicMarquee(),
            lineHeight = 18.sp
        )
    }
}
