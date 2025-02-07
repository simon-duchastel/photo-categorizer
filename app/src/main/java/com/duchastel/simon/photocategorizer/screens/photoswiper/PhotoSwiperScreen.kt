package com.duchastel.simon.photocategorizer.screens.photoswiper

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.IntOffset
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.duchastel.simon.photocategorizer.screens.photoswiper.PhotoSwiperViewModel.DisplayPhoto
import com.duchastel.simon.photocategorizer.ui.components.SwipeCard
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun PhotoSwiperScreen(
    viewModel: PhotoSwiperViewModel = hiltViewModel(),
    logout: () -> Unit,
) {
    val state by viewModel.state.collectAsState()

    PhotoSwiperContent(
        photos = state.photos.filter { it.displayUrl != null },
        processPhoto = viewModel::processPhoto,
        onLogoutClicked = logout,
    )
}


@Composable
private fun PhotoSwiperContent(
    photos: List<DisplayPhoto>,
    processPhoto: (DisplayPhoto) -> Unit,
    onLogoutClicked: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Button(onClick = onLogoutClicked) { Text("Logout") }

        if (photos.isEmpty()) return
        val pagerState = rememberPagerState(pageCount = { photos.size })
        val coroutineScale = rememberCoroutineScope()
        LaunchedEffect(pagerState, photos) {
            snapshotFlow { pagerState.settledPage }.collect { page ->
                if (page > 0) {
                    processPhoto(photos[page - 1])
                }
            }
        }

        VerticalPager(
            state = pagerState,
            beyondViewportPageCount = 2, // pre-load 2 images after the current one
            modifier = Modifier.pointerInput(Unit) {
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
            val photo = photos[page]
            SwipeCard(
                content = {
                    AsyncImage(
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        model = photo.displayUrl,
                        contentDescription = photo.path,
                    )
                },
                onSwipeRight = {
                    coroutineScale.launch {
                        pagerState.animateScrollToPage(pagerState.currentPage + 1)
                    }
                },
                onSwipeLeft = {
                    coroutineScale.launch {
                        pagerState.animateScrollToPage(pagerState.currentPage + 1)
                    }
                }
            )

        }
    }
}