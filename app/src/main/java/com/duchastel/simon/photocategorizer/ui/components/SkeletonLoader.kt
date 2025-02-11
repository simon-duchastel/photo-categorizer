package com.duchastel.simon.photocategorizer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.random.Random

@Composable
fun SkeletonLoader(
    modifier: Modifier = Modifier,
    spacing: Dp = 12.dp,
    barHeight: Dp = 16.dp
) {
    // Calculate number of bars based on available height
    val density = LocalDensity.current
    var containerHeight by remember { mutableIntStateOf(0) }
    val numberOfBars by with(density) {
        remember(containerHeight, spacing, barHeight) {
            derivedStateOf {
                ((containerHeight / (barHeight + spacing).toPx()).toInt()).coerceAtLeast(1)
            }
        }
    }

    // Random widths for bars between 60% and 95%
    val randomWidths = remember(numberOfBars) {
        List(numberOfBars) { Random.nextFloat() * 0.35f + 0.6f }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .onSizeChanged { containerHeight = it.height },
        verticalArrangement = Arrangement.spacedBy(spacing)
    ) {
        repeat(numberOfBars) { index ->
            Box(
                modifier = Modifier
                    .fillMaxWidth(randomWidths[index])
                    .height(barHeight)
                    .clip(RoundedCornerShape(4.dp))
                    .background(shimmerBrush(delayMillis = index * 150))
            )
        }
    }
}