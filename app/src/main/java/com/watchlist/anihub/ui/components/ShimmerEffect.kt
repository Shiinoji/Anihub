package com.watchlist.anihub.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.IntSize

fun Modifier.shimmerEffect(): Modifier = composed {
    var size by remember { mutableStateOf(IntSize.Zero) }
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer"
    )

    val baseColor = MaterialTheme.colorScheme.surfaceVariant
    val highlightColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
    
    val shimmerColors = listOf(
        baseColor,
        highlightColor,
        baseColor,
    )

    val brush = if (size.width > 0) {
        val width = size.width.toFloat()
        val height = size.height.toFloat()
        
        // Move the gradient from -2*width to 2*width to ensure it fully passes through
        val xOffset = (translateAnim * 4 * width) - 2 * width
        
        Brush.linearGradient(
            colors = shimmerColors,
            start = Offset(xOffset, 0f),
            end = Offset(xOffset + width, height)
        )
    } else {
        Brush.linearGradient(
            colors = listOf(baseColor, baseColor),
            start = Offset.Zero,
            end = Offset.Zero
        )
    }

    this.onGloballyPositioned {
        size = it.size
    }.background(brush)
}
