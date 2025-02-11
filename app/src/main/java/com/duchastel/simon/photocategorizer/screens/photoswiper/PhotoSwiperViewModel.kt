package com.duchastel.simon.photocategorizer.screens.photoswiper

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.duchastel.simon.photocategorizer.dropbox.di.Dropbox
import com.duchastel.simon.photocategorizer.filemanager.PhotoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject

@HiltViewModel
class PhotoSwiperViewModel @Inject constructor(
    @Dropbox private val photoRepository: PhotoRepository
) : ViewModel() {

    private val _state: MutableStateFlow<State> = MutableStateFlow(State())
    val state: StateFlow<State> = _state

    init {
        viewModelScope.launch {
            val photos = photoRepository.getPhotos()
                .map { DisplayPhoto(path = it.path, displayUrl = null) }
            _state.update { it.copy(photos = photos) }
        }

        viewModelScope.launch {
            fun State.foo() = (photos.map { f -> f.path } to photoIndex)
            _state
                .distinctUntilChanged { old, new -> old.foo() == new.foo() }
                .collectLatest {
                    syncDisplayUrls(
                        photos = it.photos,
                        photoIndex = it.photoIndex,
                    )
                }
        }
    }

    fun processPhoto(photo: DisplayPhoto) {
        _state.update { oldState ->
            oldState.copy(photoIndex = oldState.photos.indexOf(photo))
        }
    }

    private suspend fun syncDisplayUrls(
        photos: List<DisplayPhoto>,
        photoIndex: Int,
    ) = coroutineScope {
        photos
            .drop(photoIndex)
            .take(PHOTO_BUFFER_SIZE)
            .filter { it.displayUrl == null }
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
        val displayUrl: String?
    )

    companion object {
        const val PHOTO_BUFFER_SIZE = 10
    }
}