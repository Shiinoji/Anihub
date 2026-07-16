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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.IntSize

/**
 * A custom [Modifier] that applies a "disappearing appearing" shimmer effect to a component.
 * It combines a linear gradient sweep with a pulsing alpha animation for a modern loading feel.
 */
fun Modifier.shimmerEffect(): Modifier = composed {
    var size by remember { mutableStateOf(IntSize.Zero) }
    val transition = rememberInfiniteTransition(label = "shimmer")
    
    // Horizontal sweep animation for the highlight line
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer"
    )

    // Pulsing transparency animation (0.4 to 0.8)
    val alphaAnim by transition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    val baseColor = MaterialTheme.colorScheme.surfaceVariant
    val highlightColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
    
    val shimmerColors = listOf(
        baseColor,
        highlightColor,
        baseColor,
    )

    val brush = if (size.width > 0) {
        val width = size.width.toFloat()
        val height = size.height.toFloat()
        
        // Move the gradient from -width to 2*width
        val xOffset = (translateAnim * 3 * width) - width
        
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
    }
    .graphicsLayer(alpha = alphaAnim) // Apply the pulsing alpha
    .background(brush) // Apply the sweep gradient
}
