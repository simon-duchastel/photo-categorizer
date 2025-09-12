package com.duchastel.simon.photocategorizer.screens.photoswiper

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.SubcomposeAsyncImage
import coil3.imageLoader
import coil3.request.ImageRequest
import coil3.size.Size
import com.duchastel.simon.photocategorizer.screens.photoswiper.PhotoSwiperViewModel.Companion.PHOTO_BUFFER_SIZE
import com.duchastel.simon.photocategorizer.screens.photoswiper.PhotoSwiperViewModel.DisplayPhoto
import com.duchastel.simon.photocategorizer.ui.components.HorizontalSwiper
import com.duchastel.simon.photocategorizer.ui.components.HorizontalSwipeDirection
import com.duchastel.simon.photocategorizer.ui.components.OneWayVerticalSwiper
import com.duchastel.simon.photocategorizer.ui.components.SkeletonLoader
import com.duchastel.simon.photocategorizer.ui.components.rememberVerticalSwiperState

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
        modalState = state.newFolderModal,
        onUpdateFolderName = viewModel::updateNewFolderName,
        onConfirmFolder = viewModel::confirmNewFolder,
        onDismissModal = viewModel::hideNewFolderModal,
    )
}

@Composable
private fun PhotoSwiperContent(
    photos: List<DisplayPhoto>,
    processPhoto: (Int, SwipeDirection) -> Unit,
    modalState: PhotoSwiperViewModel.NewFolderModalState?,
    onUpdateFolderName: (String) -> Unit,
    onConfirmFolder: () -> Unit,
    onDismissModal: () -> Unit,
) {
    if (photos.isEmpty()) {
        SkeletonLoader(modifier = Modifier.padding(16.dp))
        return
    }

    val swiperState = rememberVerticalSwiperState { photos.size }
    OneWayVerticalSwiper(
        modifier = Modifier.fillMaxSize(),
        swiperState = swiperState,
        onSwipe = { index ->
            processPhoto(index, SwipeDirection.Up)
            swiperState.swipeToNextPage()
        },
        swipeUpBackground = {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.DarkGray),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(modifier = Modifier.weight(3f))
                Text(
                    modifier = Modifier
                        .weight(1f)
                        .padding(32.dp),
                    text = "Archive",
                    color = Color.White,
                    fontSize = 32.sp,
                )
            }
        }
    ) { index ->
        val photo = photos[index]
        HorizontalSwiper(
            swipeLeftBackground = {
                HorizontalSwipeBackground(
                    text = "New Category",
                    color = Color.Red,
                    direction = HorizontalSwipeDirection.Left,
                )
            },
            swipeRightBackground = {
                HorizontalSwipeBackground(
                    text = "Categorize",
                    color = Color.Green,
                    direction = HorizontalSwipeDirection.Right,
                )
            },
            onSwipeLeft = {
                processPhoto(swiperState.currentPage, SwipeDirection.Left)
                swiperState.swipeToNextPage()
            },
            onSwipeRight = {
                processPhoto(swiperState.currentPage, SwipeDirection.Right)
                swiperState.swipeToNextPage()
            },
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

        // pre-render next image invisibly to avoid jitter
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

    // Show modal when state is not null
    modalState?.let { modal ->
        NewFolderModal(
            folderName = modal.folderName,
            onFolderNameChanged = onUpdateFolderName,
            onConfirm = onConfirmFolder,
            onDismiss = onDismissModal,
        )
    }
}

@Composable
private fun HorizontalSwipeBackground(
    text: String,
    color: Color,
    direction: HorizontalSwipeDirection,
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(color),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (direction == HorizontalSwipeDirection.Left) {
            Spacer(modifier = Modifier.weight(1f))
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .weight(2f),
        ) {
            Text(
                modifier = Modifier
                    .weight(1f)
                    .padding(32.dp),
                text = text,
                color = Color.White,
                fontSize = 32.sp,
            )
            Spacer(modifier = Modifier.weight(3f))
        }
        if (direction == HorizontalSwipeDirection.Right) {
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun NewFolderModal(
    folderName: String,
    onFolderNameChanged: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // Header with close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "New Category",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(24.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }

                Spacer(modifier = Modifier.padding(8.dp))

                // Folder name input
                OutlinedTextField(
                    value = folderName,
                    onValueChange = onFolderNameChanged,
                    label = { Text("Folder Name") },
                    placeholder = { Text("Enter folder name") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            keyboardController?.hide()
                            if (folderName.isNotBlank()) {
                                onConfirm()
                            }
                        }
                    )
                )

                Spacer(modifier = Modifier.padding(16.dp))

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            if (folderName.isNotBlank()) {
                                keyboardController?.hide()
                                onConfirm()
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = folderName.isNotBlank(),
                    ) {
                        Text("Create")
                    }
                }
            }
        }
    }

    // Auto-focus on the text field when modal appears
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
}
