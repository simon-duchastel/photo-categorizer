package com.duchastel.simon.photocategorizer.screens.photoswiper

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest

@Composable
fun PhotoSwiperScreen(
    viewModel: PhotoSwiperViewModel = hiltViewModel(),
    logout: () -> Unit,
) {
    val state by viewModel.state.collectAsState()

    PhotoSwiperContent(
        photoUris = state.photos.map { it.previewUrl },
    )
    Button(onClick = logout) { Text("Logout") }
}

@Composable
private fun PhotoSwiperContent(photoUris: List<String>) {
    Text("PHOTO SWIPER")
    val pagerState = rememberPagerState(pageCount = { photoUris.size })
    VerticalPager(state = pagerState) { page ->
        val photoUri = photoUris[page]
        AsyncImage(
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            model = photoUri,
            contentDescription = "My photo",
        )
    }
}