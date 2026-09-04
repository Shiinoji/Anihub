package com.watchlist.anihub.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

@Composable
fun SimpleAnimeCardSkeleton() {
    Box(
        modifier = Modifier
            .width(140.dp)
            .aspectRatio(0.7f)
            .clip(RoundedCornerShape(12.dp))
            .shimmerEffect()
    )
}

@Composable
fun AiringAnimeRowSkeleton() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(12.dp))
            .shimmerEffect()
    )
}

@Composable
fun CharacterCircleSkeleton() {
    Box(
        modifier = Modifier
            .size(70.dp)
            .clip(CircleShape)
            .shimmerEffect()
    )
}

@Composable
fun AnimeDetailSkeleton() {
    Column(modifier = Modifier.fillMaxSize()) {
        // Banner Area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(320.dp)
                .shimmerEffect()
        )
        
        Column(modifier = Modifier.padding(16.dp)) {
            // Header/Title Area
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .shimmerEffect()
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            // Large Description/Body Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .shimmerEffect()
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Lower Content Area
            repeat(3) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .shimmerEffect()
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
