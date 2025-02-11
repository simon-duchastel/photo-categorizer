package com.duchastel.simon.photocategorizer.ui.components

import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

@Composable
fun HorizontalSwiper(
    modifier: Modifier = Modifier,
    onSwipeLeft: () -> Unit,
    onSwipeRight: () -> Unit,
    content: @Composable () -> Unit,
) {
    var offsetX by remember { mutableFloatStateOf(0f) }
    val swipeThreshold = 300f

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                coroutineScope {
                    launch {
                        detectHorizontalDragGestures(
                            onDragEnd = {
                                // reset offset when user stops scrolling
                                if (offsetX > swipeThreshold) {
                                    onSwipeRight()
                                } else if (offsetX < -swipeThreshold) {
                                    onSwipeLeft()
                                }
                                offsetX = 0f
                            },
                            onHorizontalDrag = { _, dragAmount ->
                                // update offset as the user scrolls
                                offsetX += dragAmount
                            }
                        )
                    }
                }
            }
            .graphicsLayer {
                // add an offset, but don't go more than the swipe threshold
                rotationZ = offsetX.coerceIn(-swipeThreshold..swipeThreshold) / 15f
            }
            .offset {
                // add an offset, but don't go more than the swipe threshold
                IntOffset(
                    x = offsetX.coerceIn(-swipeThreshold..swipeThreshold).roundToInt() * 2,
                    y = offsetX.absoluteValue.coerceAtMost(swipeThreshold).times(-0.5f).roundToInt(),
                )
            }
    ) {
        content()
    }
}
