package com.watchlist.anihub.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.watchlist.anihub.data.remote.Media
import com.watchlist.anihub.ui.theme.LocalTitleLanguage

@Composable
fun AnimeCard(
    media: Media,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val titleLanguage = LocalTitleLanguage.current
    val displayTitle = media.title.getDisplayTitle(titleLanguage)

    Column(
        modifier = modifier
            .width(140.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .aspectRatio(0.7f)
                .fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            )
        ) {
            AsyncImage(
                model = media.coverImage.extraLarge ?: media.coverImage.large,
                contentDescription = displayTitle,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = displayTitle,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 4.dp),
            lineHeight = 18.sp
        )
    }
}
