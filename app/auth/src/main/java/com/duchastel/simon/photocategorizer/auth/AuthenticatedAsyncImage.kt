package com.duchastel.simon.photocategorizer.auth

import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.request.ImageRequest
import kotlinx.coroutines.launch

@Composable
fun AuthenticatedAsyncImage(
    modifier: Modifier = Modifier,
    url: String,
    contentScale: ContentScale = ContentScale.Fit,
    authManager: AuthManager,
    contentDescription: String?,
) {
    val coroutineScope = rememberCoroutineScope()
    var token by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(url) {
        coroutineScope.launch {
            token = authManager.executeWithAuthToken { it.accessToken }
        }
    }

    if (token != null) {
        AsyncImage(
            modifier = modifier,
            model = ImageRequest.Builder(LocalContext.current)
                .data(url)
                .addHeader("Authorization", "Bearer $token")
                .crossfade(true)
                .build(),
            contentDescription = contentDescription,
            contentScale = contentScale,
        )
    } else {
        CircularProgressIndicator()
    }
}
