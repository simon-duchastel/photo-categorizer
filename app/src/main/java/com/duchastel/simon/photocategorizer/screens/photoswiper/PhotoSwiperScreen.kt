package com.duchastel.simon.photocategorizer.screens.photoswiper

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.SubcomposeAsyncImage
import com.duchastel.simon.photocategorizer.screens.photoswiper.PhotoSwiperViewModel.DisplayPhoto
import com.duchastel.simon.photocategorizer.ui.components.HorizontalSwiper
import com.duchastel.simon.photocategorizer.ui.components.OneWayVerticalSwiper
import com.duchastel.simon.photocategorizer.ui.components.SkeletonLoader

@Composable
fun PhotoSwiperScreen(
    viewModel: PhotoSwiperViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()

//    val context = LocalContext.current
//    LaunchedEffect(photos) {
//        // preload the first few photos in the buffer initially
//        for (index in 1..<PHOTO_BUFFER_SIZE) {
//            if (index <= photos.lastIndex) {
//                println("TODO - preloading $index")
//                context.preloadPhoto(photos[index])
//            }
//        }
//    }
//    LaunchedEffect(photos, currentIndex) {
//        // every time the index changes, preload the next photo in the index
//        val index = currentIndex + PHOTO_BUFFER_SIZE
//        if (index <= photos.lastIndex) {
//            println("TODO - preloading $index + PHOTO_BUFFER_SIZE")
//            context.preloadPhoto(photos[index])
//        }
//    }

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
    if (photos.isEmpty()) {
        SkeletonLoader(modifier = Modifier.padding(16.dp))
        return
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
                    SkeletonLoader(modifier = Modifier.padding(16.dp))
                },
            )
        }
    }
}