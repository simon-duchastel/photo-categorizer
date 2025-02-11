package com.duchastel.simon.photocategorizer.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun <T> OneWayVerticalSwiper(
    modifier: Modifier = Modifier,
    items: List<T>,
    onSwipe: (T) -> Unit,
    content: @Composable (T) -> Unit,
) {
    if (items.isEmpty()) return

    val threshold = 1000f
    var offsetY by remember { mutableFloatStateOf(0f) }
    var showNewContent by remember { mutableStateOf(false) }
    val animatedOffset by animateFloatAsState(
        targetValue = if (showNewContent) -threshold else offsetY,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMedium,
        ),
        label = "OneWayVerticalSwiperOffset"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                coroutineScope {
                    launch {
                        detectVerticalDragGestures(
                            onDragEnd = {
                                if (offsetY < -threshold) {
                                    showNewContent = true
                                    onSwipe(items.first())
                                } else {
                                    // Reset position
                                    offsetY = 0f
                                    showNewContent = false
                                }
                            },
                            onVerticalDrag = { change, dragAmount ->
                                change.consume()
                                // Only allow upward scrolling (negative dragAmount)
                                if (dragAmount < 0 || offsetY < 0) {
                                    offsetY = (offsetY + dragAmount).coerceAtMost(0f)
                                }
                            }
                        )
                    }
                }
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
            content(items.first())
        }

        if (showNewContent) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .offset {
                        IntOffset(
                            x = 0,
                            y = (animatedOffset + threshold).roundToInt()
                        )
                    }
            ) {
                content(items.first())
            }
        }
    }
}