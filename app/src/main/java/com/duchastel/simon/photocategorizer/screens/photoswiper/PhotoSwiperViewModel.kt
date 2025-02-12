package com.duchastel.simon.photocategorizer.screens.photoswiper

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil3.request.ImageRequest
import com.duchastel.simon.photocategorizer.dropbox.di.Dropbox
import com.duchastel.simon.photocategorizer.filemanager.PhotoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PhotoSwiperViewModel @Inject constructor(
    @Dropbox private val photoRepository: PhotoRepository
) : ViewModel() {

    private val _state: MutableStateFlow<State> = MutableStateFlow(State())
    val state: StateFlow<State> = _state

    init {
        viewModelScope.launch {
            val photos = photoRepository.getPhotos().map { DisplayPhoto(path = it.path) }
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

    fun processPhoto(index: Int, direction: SwipeDirection) {
        _state.update { oldState ->
            oldState.copy(photoIndex = index + 1)
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

    data class State(
        val photos: List<DisplayPhoto> = emptyList(),
        val photoIndex: Int = 0,
    )

    data class DisplayPhoto(
        val path: String,
        val displayUrl: String? = null,
        val imageRequest: ImageRequest? = null,
    )

    companion object {
        const val PHOTO_BUFFER_SIZE = 5
    }
}