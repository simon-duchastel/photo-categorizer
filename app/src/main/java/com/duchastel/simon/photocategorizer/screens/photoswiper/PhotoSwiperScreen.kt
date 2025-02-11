package com.duchastel.simon.photocategorizer.screens.photoswiper

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.SubcomposeAsyncImage
import coil3.imageLoader
import coil3.request.ImageRequest
import coil3.size.Size
import com.duchastel.simon.photocategorizer.screens.photoswiper.PhotoSwiperViewModel.Companion.PHOTO_BUFFER_SIZE
import com.duchastel.simon.photocategorizer.screens.photoswiper.PhotoSwiperViewModel.DisplayPhoto
import com.duchastel.simon.photocategorizer.ui.components.HorizontalSwiper
import com.duchastel.simon.photocategorizer.ui.components.OneWayVerticalSwiper
import com.duchastel.simon.photocategorizer.ui.components.SkeletonLoader

@Composable
fun PhotoSwiperScreen(
    viewModel: PhotoSwiperViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val photos = state.photos

    val context = LocalContext.current
    val imageLoader = context.imageLoader
    LaunchedEffect(photos, context, imageLoader) {
        // preload the first few photos in the buffer initially
        photos
            .filter { it.displayUrl != null }
            .filter { it.imageRequest == null }
            .take(PHOTO_BUFFER_SIZE)
            .forEach { photo ->
                val request = ImageRequest.Builder(context)
                    .data(photo.displayUrl)
                    .size(Size.ORIGINAL)
                    .build()
                imageLoader.enqueue(request)
                viewModel.attachImageRequest(photo, request)
            }
    }

    PhotoSwiperContent(
        photos = photos.filter { it.imageRequest != null },
        processPhoto = viewModel::processPhoto,
    )
}

@Composable
private fun PhotoSwiperContent(
    photos: List<DisplayPhoto>,
    processPhoto: (Int) -> Unit,
) {
    if (photos.isEmpty()) {
        SkeletonLoader(modifier = Modifier.padding(16.dp))
        return
    }

    OneWayVerticalSwiper(
        modifier = Modifier.fillMaxSize(),
        onSwipe = { index ->
            processPhoto(index)
        },
    ) { index ->
        val photo = photos[index]
        HorizontalSwiper(
            onSwipeLeft = { processPhoto(index) },
            onSwipeRight = { processPhoto(index) },
        ) {
            SubcomposeAsyncImage(
                modifier = Modifier.fillMaxSize(),
                model = photo.imageRequest,
                contentScale = ContentScale.Crop,
                contentDescription = photo.path,
                loading = {
                    SkeletonLoader(modifier = Modifier.padding(16.dp))
                },
            )
        }

        // pre-render next image to avoid jitter
        if (index < photos.lastIndex) {
            Box(modifier = Modifier.alpha(0f)) {
                SubcomposeAsyncImage(
                    modifier = Modifier.fillMaxSize(),
                    model = photos[index + 1].imageRequest,
                    contentScale = ContentScale.Crop,
                    contentDescription = photos[index + 1].path,
                    loading = {
                        SkeletonLoader(modifier = Modifier.padding(16.dp))
                    },
                )
            }
        }
    }
}