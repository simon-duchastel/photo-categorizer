package com.duchastel.simon.photocategorizer.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntOffset
import kotlin.math.roundToInt

@Composable
fun <T> OneWayVerticalSwiper(
    modifier: Modifier = Modifier,
    items: List<T>,
    onSwipe: (T) -> Unit,
    content: @Composable (T) -> Unit,
) {
    var currentIndex by remember { mutableIntStateOf(0) }
    val currentIndexSafe by remember(currentIndex) {
        derivedStateOf { currentIndex.coerceAtMost(items.lastIndex) }
    }
    if (currentIndex > items.lastIndex) return // don't render with no elements left

    var containerHeight by remember { mutableIntStateOf(0) }
    val threshold by remember(containerHeight) { derivedStateOf { containerHeight / 2.5f } }

    var offsetY by remember { mutableFloatStateOf(0f) }
    var isAnimating by remember { mutableStateOf(false) }

    // using key(currentIndex) forces the animation to reset every time the index changes
    val animatedOffset by key(currentIndex) {
        animateFloatAsState(
            targetValue = if (isAnimating) {
                containerHeight * -1.20f // add 20% make sure we go past the top of the container
            } else {
                offsetY.coerceIn(-threshold..0f) // don't scroll past the threshold
            },
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessMedium,
            ),
            finishedListener = {
                if (isAnimating) {
                    // Reset position and update indices after animation completes
                    offsetY = 0f
                    isAnimating = false
                    currentIndex++
                }
            },
            label = "OneWayVerticalSwiperOffset"
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .onSizeChanged { containerHeight = it.height }
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onDragEnd = {
                        if (offsetY < -threshold) {
                            isAnimating = true
                            onSwipe(items[currentIndexSafe])
                        } else {
                            // Reset position without changing indices
                            offsetY = 0f
                        }
                    },
                    onVerticalDrag = { change, dragAmount ->
                        change.consume()
                        // Update scrolling and disallow downward scrolling when not dragging
                        if (!isAnimating && (dragAmount < 0 || offsetY < 0)) {
                            offsetY += dragAmount
                        }
                    }
                )
            }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset {
                    IntOffset(
                        x = 0,
                        y = animatedOffset.roundToInt()
                    )
                }
        ) {
            content(items[currentIndexSafe])
        }
    }
}