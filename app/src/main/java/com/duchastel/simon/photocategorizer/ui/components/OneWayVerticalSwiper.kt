package com.duchastel.simon.photocategorizer.ui.components

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.pager.PagerScope
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput

@Composable
fun <T> OneWayVerticalSwiper(
    modifier: Modifier = Modifier,
    items: List<T>,
    onSwipe: (T) -> Unit,
    content: @Composable PagerScope.(T, PagerState) -> Unit,
) {
    if (items.isEmpty()) return
    val pagerState = rememberPagerState(pageCount = { items.size })
    LaunchedEffect(pagerState, items) {
        snapshotFlow { pagerState.settledPage }.collect { page ->
            if (page > 0) {
                onSwipe(items[page - 1])
            }
        }
    }

    VerticalPager(
        state = pagerState,
        beyondViewportPageCount = 2, // pre-load 2 items after the current one
        modifier = modifier.pointerInput(Unit) {
            awaitEachGesture {
                val currentPageOffsetFraction = pagerState.currentPageOffsetFraction

                val isUpwardsScroll = currentPageOffsetFraction < 0
                val down = awaitFirstDown(pass = PointerEventPass.Initial)
                if (isUpwardsScroll) {
                    // block upwards scrolling
                    down.consume()
                }

                do {
                    val event: PointerEvent = awaitPointerEvent(
                        pass = PointerEventPass.Initial
                    )

                    event.changes.forEach {
                        val diffY = it.position.y - it.previousPosition.y
                        if (diffY > 0) {
                            // block upwards paging
                            it.consume()
                        }
                    }

                } while (event.changes.any { it.pressed })
            }
        },
    ) { page ->
        val item = items[page]
        content(item, pagerState)
    }
}