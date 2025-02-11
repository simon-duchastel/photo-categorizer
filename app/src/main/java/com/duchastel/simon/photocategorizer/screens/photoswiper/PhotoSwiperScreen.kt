package com.duchastel.simon.photocategorizer.screens.photoswiper

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.duchastel.simon.photocategorizer.screens.photoswiper.PhotoSwiperViewModel.DisplayPhoto
import com.duchastel.simon.photocategorizer.ui.components.HorizontalSwiper
import com.duchastel.simon.photocategorizer.ui.components.OneWayVerticalSwiper

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
    if (photos.isEmpty()) return
    val context = LocalContext.current
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
            AsyncImage(
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                model = photo.displayUrl,
                contentDescription = photo.path,
            )
        }
    }
}