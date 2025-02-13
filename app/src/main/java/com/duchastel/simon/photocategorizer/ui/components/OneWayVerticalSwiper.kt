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
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntOffset
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

@Composable
fun OneWayVerticalSwiper(
    modifier: Modifier = Modifier,
    swiperState: VerticalSwiperState,
    onSwipe: (Int) -> Unit,
    swipeUpBackground:  @Composable () -> Unit,
    content: @Composable (Int) -> Unit,
) {
    var containerHeight by remember { mutableIntStateOf(0) }
    val threshold by remember(containerHeight) { derivedStateOf { containerHeight / 2.5f } }

    var offsetY by remember { mutableFloatStateOf(0f) }
    var newContainerAnimatingIn by remember { mutableStateOf(false) }

    // animate in the new container from offset=containerHeight to offset=0
    LaunchedEffect(newContainerAnimatingIn) {
        delay(50)
        offsetY = 0f
    }

    // key(currentIndex) forces the animation to reset every time the index changes
    val animatedOffset by key(swiperState.currentPage) {
        animateFloatAsState(
            targetValue = if (swiperState.containerFlyingOffScreen) {
                containerHeight * -1.10f // add 10% make sure we go past the top of the container
            } else if (!newContainerAnimatingIn) {
                // ensure the container doesn't go below the screen when not animating in
                offsetY.coerceIn(-threshold..0f) // don't scroll past threshold
            } else {
                offsetY.coerceAtLeast(-threshold) // don't scroll past threshold
            },
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessMedium,
            ),
            finishedListener = {
                if (swiperState.containerFlyingOffScreen) {
                    // Begin animating in new container once old one is off-screen
                    swiperState.stopContainerAnimation()
                    newContainerAnimatingIn = true
                    offsetY = containerHeight.toFloat()
                    onSwipe(swiperState.currentPage)
                } else if (newContainerAnimatingIn) {
                    // Update state once animation is done
                    newContainerAnimatingIn = false
                }
            },
            label = "OneWayVerticalSwiperOffset"
        )
    }
    val percentSwiped = animatedOffset
        .coerceIn(-threshold..0f)
        .absoluteValue
        .let { (1 - (threshold - it) / threshold).absoluteValue }

    Box(
        modifier = modifier
            .fillMaxSize()
            .onSizeChanged { containerHeight = it.height }
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onDragEnd = {
                        if (offsetY < -threshold) {
                            swiperState.animateContainerOffScreenImmediate()
                        } else {
                            // Reset position without changing indices
                            offsetY = 0f
                        }
                    },
                    onVerticalDrag = { change, dragAmount ->
                        change.consume()
                        // Update scrolling and disallow downward scrolling when not dragging
                        if (!swiperState.containerFlyingOffScreen && (dragAmount < 0 || offsetY < 0)) {
                            offsetY += dragAmount
                        }
                    }
                )
            }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .alpha(percentSwiped),
        ) {
            swipeUpBackground()
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .alpha(1f)
                .offset {
                    IntOffset(
                        x = 0,
                        y = animatedOffset.roundToInt()
                    )
                }
        ) {
            content(swiperState.currentPage)
        }
    }
}

interface VerticalSwiperState {
    val pageCount: Int
    val currentPage: Int
    val containerFlyingOffScreen: Boolean

    fun swipeToNextPage()
    suspend fun animateContainerOffScreen()
    fun animateContainerOffScreenImmediate()
    fun stopContainerAnimation()
}

@Composable
fun rememberVerticalSwiperState(pageCount: () -> Int): VerticalSwiperState {
    return rememberSaveable(saver = DefaultVerticalSwiperState.Saver) {
        DefaultVerticalSwiperState(pageCount = pageCount)
    }
}

private class DefaultVerticalSwiperState(
    currentPage: Int = 0,
    pageCount: () -> Int
): VerticalSwiperState {


    override var currentPage by mutableIntStateOf(currentPage)
        private set

    override var containerFlyingOffScreen by mutableStateOf(false)
        private set

    var pageCountState = mutableStateOf(pageCount)
    override val pageCount: Int
        get() = pageCountState.value.invoke()

    override fun swipeToNextPage() {
        currentPage++
    }

    override fun animateContainerOffScreenImmediate() {
        containerFlyingOffScreen = true
    }

    override suspend fun animateContainerOffScreen() {
        containerFlyingOffScreen = true
        snapshotFlow { containerFlyingOffScreen }.first { println("TODO - GOT IT"); !it } // wait for it to be false again
    }

    override fun stopContainerAnimation() {
        containerFlyingOffScreen = false
    }

    companion object {
        val Saver: Saver<DefaultVerticalSwiperState, *> = listSaver(
            save = {
                listOf(
                    it.currentPage,
                    it.pageCount,
                )
            },
            restore = {
                DefaultVerticalSwiperState(
                    currentPage = it[0],
                    pageCount = { it[1] }
                )
            }
        )
    }
}