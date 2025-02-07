package com.duchastel.simon.photocategorizer.screens.photoswiper

import androidx.compose.foundation.layout.Column
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
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage

@Composable
fun PhotoSwiperScreen(
    viewModel: PhotoSwiperViewModel = hiltViewModel(),
    logout: () -> Unit,
) {
    val state by viewModel.state.collectAsState()

    PhotoSwiperContent(
        photoUris = state.photos.map { it.displayUrl }.filterNotNull(),
        onLogoutClicked = logout,
    )
}

@Composable
private fun PhotoSwiperContent(
    photoUris: List<String>,
    onLogoutClicked: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {

        val pagerState = rememberPagerState(pageCount = { photoUris.size })
        VerticalPager(state = pagerState) { page ->
            val photoUri = photoUris[page]
            AsyncImage(
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                model = photoUri,
                contentDescription = "Photo",
            )
        }

        Button(onClick = onLogoutClicked) { Text("Logout") }
    }
}