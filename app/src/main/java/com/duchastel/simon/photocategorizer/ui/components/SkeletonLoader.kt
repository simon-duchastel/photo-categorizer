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
    blockSpacing: Dp = 24.dp,
    groupSpacing: Dp = 12.dp,
    barHeight: Dp = 16.dp,
    blockHeight: Dp = 120.dp,
    bottomSpacing: Dp = 32.dp
) {
    val density = LocalDensity.current
    var containerHeight by remember { mutableIntStateOf(0) }

    // Calculate number of block groups that can fit
    val numberOfGroups by with(density) {
        remember(containerHeight, blockSpacing, blockHeight, bottomSpacing) {
            derivedStateOf {
                val totalGroupHeight = blockHeight + (barHeight * 3) + (groupSpacing * 3)
                val availableHeight = containerHeight - bottomSpacing.toPx()
                (availableHeight / totalGroupHeight.toPx()).toInt().coerceAtLeast(1)
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .onSizeChanged { containerHeight = it.height },
        verticalArrangement = Arrangement.spacedBy(blockSpacing)
    ) {
        repeat(numberOfGroups) {
            SkeletonGroup(
                modifier = Modifier.fillMaxWidth(),
                blockHeight = blockHeight,
                barHeight = barHeight,
                groupSpacing = groupSpacing
            )
        }
    }
}

@Composable
private fun SkeletonGroup(
    modifier: Modifier = Modifier,
    blockHeight: Dp,
    barHeight: Dp,
    groupSpacing: Dp,
    shimmerDelayMillis: Int = 0,
) {
    var containerWidth by remember { mutableIntStateOf(0) }
    Column(
        verticalArrangement = Arrangement.spacedBy(groupSpacing)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .onSizeChanged { containerWidth = it.width }
                .height(blockHeight)
                .clip(RoundedCornerShape(8.dp))
                .background(
                    shimmerBrush(
                        delayMillis = shimmerDelayMillis,
                        width = containerWidth.toFloat(),
                    )
                )
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(groupSpacing)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .height(barHeight)
                    .clip(RoundedCornerShape(4.dp))
                    .background(
                        shimmerBrush(
                            delayMillis = shimmerDelayMillis + 150, // offset 1st box by 150ms
                            width = containerWidth.toFloat(),
                        )
                    )
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(barHeight)
                    .clip(RoundedCornerShape(4.dp))
                    .background(
                        shimmerBrush(
                            delayMillis = shimmerDelayMillis + 300, // offset 2nd box by 300ms
                            width = containerWidth.toFloat(),
                        )
                    )
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(barHeight)
                    .clip(RoundedCornerShape(4.dp))
                    .background(
                        shimmerBrush(
                            delayMillis = shimmerDelayMillis + 450, // offset 2nd box by 450ms
                            width = containerWidth.toFloat(),
                        )
                    )
            )
        }
    }
}