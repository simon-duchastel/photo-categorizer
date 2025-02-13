package com.duchastel.simon.photocategorizer.screens.photoswiper

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil3.request.ImageRequest
import com.duchastel.simon.photocategorizer.dropbox.di.Dropbox
import com.duchastel.simon.photocategorizer.filemanager.PhotoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.updateAndGet
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class PhotoSwiperViewModel @Inject constructor(
    @Dropbox private val photoRepository: PhotoRepository
) : ViewModel() {

    // state and init

    private val _state: MutableStateFlow<State> = MutableStateFlow(State())
    val state: StateFlow<State> = _state

    init {
        viewModelScope.launch {
            val photos = withContext(Dispatchers.IO) {
                photoRepository.getPhotos("/camera test/camera roll")
                    .map { DisplayPhoto(path = it.path) }
            }
            _state.update { it.copy(photos = photos) }
        }

        viewModelScope.launch {
            fun State.photoKeys() = (photos.map { f -> f.path } to photoIndex)
            _state
                .distinctUntilChanged { old, new -> old.photoKeys() == new.photoKeys() }
                .collectLatest {
                    syncDisplayUrls(
                        photos = it.photos,
                        photoIndex = it.photoIndex,
                    )
                }
        }
    }

    // public functions

    fun processPhoto(index: Int, direction: SwipeDirection) {
        val currentState = _state.updateAndGet { oldState ->
            oldState.copy(photoIndex = index + 1)
        }

        val photo = currentState.photos[index]
        viewModelScope.launch {
            when (direction) {
                SwipeDirection.Left -> processLeftSwipe(photo)
                SwipeDirection.Right -> processRightSwipe(photo)
                SwipeDirection.Up -> processUpSwipe(photo)
            }
        }
    }

    fun attachImageRequest(photo: DisplayPhoto, request: ImageRequest) {
        _state.update { oldState ->
            oldState.copy(photos = oldState.photos.map { oldPhoto ->
                if (oldPhoto.path == photo.path) {
                    oldPhoto.copy(imageRequest = request)
                } else {
                    oldPhoto
                }
            })
        }
    }

    // private functions

    private suspend fun syncDisplayUrls(
        photos: List<DisplayPhoto>,
        photoIndex: Int,
    ) = coroutineScope {
        photos
            .drop(photoIndex) // drop the current photo
            .take(PHOTO_BUFFER_SIZE) // take the next photos for the buffer
            .filter { it.displayUrl == null } // ignore already-loaded photos
            .map { photo ->
                async {
                    val url = photoRepository.getUnauthenticatedLinkForPhoto(photo.path)
                    _state.update { oldState ->
                        oldState.copy(
                            photos = oldState.photos.map { existingPhoto ->
                                if (existingPhoto.path == photo.path) {
                                    existingPhoto.copy(displayUrl = url)
                                } else {
                                    existingPhoto
                                }
                            }
                        )
                    }
                }
            }.awaitAll()
    }

    private suspend fun processLeftSwipe(photo: DisplayPhoto) {

    }

    private var count = 0

    private suspend fun processRightSwipe(photo: DisplayPhoto) {
        withContext(Dispatchers.IO) {
            photoRepository.movePhoto(
                originalPath = photo.path,
                newPath ="/camera test/first event/${photo.fileName}",
            )
        }
    }

    private suspend fun processUpSwipe(photo: DisplayPhoto) {

    }

    // state definitions and constants

    data class State(
        val photos: List<DisplayPhoto> = emptyList(),
        val photoIndex: Int = 0,
    )

    data class DisplayPhoto(
        val path: String,
        val displayUrl: String? = null,
        val imageRequest: ImageRequest? = null,
    ) {
        val fileName: String = path.split("/").last()
    }

    companion object {
        const val PHOTO_BUFFER_SIZE = 5
    }
}