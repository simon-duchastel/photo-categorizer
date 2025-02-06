package com.duchastel.simon.photocategorizer.photoswiper

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage

@Composable
fun PhotoSwiperScreen(viewModel: PhotoSwiperViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsState()
    PhotoSwiperContent(
        photoUris = state.photos.map { it.previewUrl },
    )
}

@Composable
private fun PhotoSwiperContent(photoUris: List<String>) {
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