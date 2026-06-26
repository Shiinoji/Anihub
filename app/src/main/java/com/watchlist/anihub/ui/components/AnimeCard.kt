package com.watchlist.anihub.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
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
            .clickable { onClick() }
    ) {
        Card(
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .height(200.dp)
                .fillMaxWidth()
        ) {
            AsyncImage(
                model = media.coverImage.extraLarge ?: media.coverImage.large,
                contentDescription = displayTitle,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = displayTitle,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}
