package com.duchastel.simon.photocategorizer.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.IntOffset
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun SwipeCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
    onSwipeLeft: () -> Unit,
    onSwipeRight: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val offsetX = remember { Animatable(0f) }
    val swipeThreshold = 300f

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectHorizontalDragGestures { _, dragAmount ->
                    coroutineScope.launch {
                        offsetX.snapTo(targetValue = offsetX.value + dragAmount)
                    }
                }
            }
            .offset { IntOffset(offsetX.value.roundToInt(), 0) }
            .graphicsLayer {
                rotationZ = offsetX.value / 15f
            }
            .onGloballyPositioned {
                if (offsetX.value > swipeThreshold) {
                    onSwipeRight()
                    coroutineScope.launch {
                        offsetX.snapTo(0f)
                    }
                } else if (offsetX.value < -swipeThreshold) {
                    onSwipeLeft()
                    coroutineScope.launch {
                        offsetX.snapTo(0f)
                    }
                }
            }
    ) {
        content()
    }
}
