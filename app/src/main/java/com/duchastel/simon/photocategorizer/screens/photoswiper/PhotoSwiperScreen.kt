package com.duchastel.simon.photocategorizer.screens.photoswiper

import android.content.Context
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.SubcomposeAsyncImage
import coil.imageLoader
import coil.request.ImageRequest
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

    PhotoSwiperContent(
        photos = state.photos.filter { it.displayUrl != null },
        processPhoto = viewModel::processPhoto,
    )
}

@Composable
private fun PhotoSwiperContent(
    photos: List<DisplayPhoto>,
    processPhoto: (DisplayPhoto) -> Unit,
) {
    SkeletonLoader()
    return
    if (photos.isEmpty()) return
    val context = LocalContext.current
    LaunchedEffect(photos) {
        // preload the first few photos in the buffer initially
        for (index in 1..<PHOTO_BUFFER_SIZE) {
            if (index <= photos.lastIndex) {
                println("TODO - preloading $index")
                context.preloadPhoto(photos[index])
            }
        }
    }
    LaunchedEffect(photos, currentIndex) {
        // every time the index changes, preload the next photo in the index
        val index = currentIndex + PHOTO_BUFFER_SIZE
        if (index <= photos.lastIndex) {
            println("TODO - preloading $index + PHOTO_BUFFER_SIZE")
            context.preloadPhoto(photos[index])
        }
    }


    OneWayVerticalSwiper(
        modifier = Modifier.fillMaxSize(),
        items = photos,
        onSwipe = { photo ->
            processPhoto(photo)
        },
    ) { photo ->
        HorizontalSwiper(
            onSwipeLeft = { processPhoto(photo) },
            onSwipeRight = { processPhoto(photo) },
        ) {
            SubcomposeAsyncImage(
                modifier = Modifier.fillMaxSize(),
                model = photo.displayUrl,
                contentScale = ContentScale.Crop,
                contentDescription = photo.path,
                loading = {
                    SkeletonLoader()
                },
            )
        }
    }
}