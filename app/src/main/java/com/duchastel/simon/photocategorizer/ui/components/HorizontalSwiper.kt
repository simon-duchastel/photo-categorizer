package com.duchastel.simon.photocategorizer.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import com.duchastel.simon.photocategorizer.screens.photoswiper.SwipeDirection
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

@Composable
fun HorizontalSwiper(
    modifier: Modifier = Modifier,
    onSwipeLeft: () -> Unit,
    onSwipeRight: () -> Unit,
    swipeLeftBackground: @Composable () -> Unit,
    swipeRightBackground: @Composable () -> Unit,
    content: @Composable () -> Unit,
) {
    val swipeThreshold = 300f
    var offsetX by remember { mutableFloatStateOf(0f) }
    val swipeDirection: SwipeDirection? by remember {
        derivedStateOf {
            if (offsetX < 0) {
                SwipeDirection.Left
            } else if (offsetX > 0) {
                SwipeDirection.Right
            } else {
                null
            }
        }
    }
    val animatedOffset by animateFloatAsState(
        targetValue = offsetX,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMedium,
        ),
        label = "HorizontalSwiperOffset"
    )
    val percentSwiped = animatedOffset
        .coerceIn(-swipeThreshold..swipeThreshold)
        .absoluteValue
        .let { (1 - (swipeThreshold - it) / swipeThreshold).absoluteValue }

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
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
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .alpha(percentSwiped),
        ) {
            when (swipeDirection) {
                SwipeDirection.Left -> swipeLeftBackground()
                SwipeDirection.Right -> swipeRightBackground()
                null -> Unit // do nothing
            }
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    // add an offset, but don't go more than the swipe threshold
                    rotationZ = animatedOffset.coerceIn(-swipeThreshold..swipeThreshold) / 15f
                }
                .offset {
                    // add an offset, but don't go more than the swipe threshold
                    IntOffset(
                        x = animatedOffset.coerceIn(-swipeThreshold..swipeThreshold).roundToInt() * 2,
                        y = animatedOffset.absoluteValue.coerceAtMost(swipeThreshold).times(-0.5f).roundToInt(),
                    )
                }
        ) {
            content()
        }
    }
}
