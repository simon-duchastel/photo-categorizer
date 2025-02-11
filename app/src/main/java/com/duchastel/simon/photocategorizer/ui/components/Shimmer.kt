package com.duchastel.simon.photocategorizer.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

@Composable
fun shimmerBrush(
    delayMillis: Int = 0,
    durationMillis: Int = 1000,
): Brush {
    val width = 1500f
    val colors = listOf(
        Color.LightGray.copy(alpha = 0.6f),
        Color.LightGray.copy(alpha = 0.2f),
        Color.LightGray.copy(alpha = 0.6f),
    )

    val transition = rememberInfiniteTransition(label = "Shimmer")
    val translateAnimation by transition.animateFloat(
        initialValue = 0f,
        targetValue = width,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = durationMillis,
                easing = LinearEasing,
                delayMillis = delayMillis
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "Shimmer"
    )

    return Brush.linearGradient(
        colors = colors,
        start = Offset(x = translateAnimation - width, y = 0.0f),
        end = Offset(x = translateAnimation, y = 0.0f),
    )
}