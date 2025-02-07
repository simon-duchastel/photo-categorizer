package com.duchastel.simon.photocategorizer.screens.photoswiper

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.duchastel.simon.photocategorizer.dropbox.di.Dropbox
import com.duchastel.simon.photocategorizer.filemanager.FileManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.awaitAll
import javax.inject.Inject

@HiltViewModel
class PhotoSwiperViewModel @Inject constructor(
    @Dropbox private val fileManager: FileManager
) : ViewModel() {

    private val _state: MutableStateFlow<State> = MutableStateFlow(State())
    val state: StateFlow<State> = _state

    init {
        viewModelScope.launch {
            val photos = fileManager.getPhotos()
                .map { DisplayPhoto(path = it.path, displayUrl = null) }
            _state.update { it.copy(photos = photos) }
        }

        viewModelScope.launch {
            _state.collect {
                syncDisplayUrls(it.photos)
            }
        }
    }

    fun processPhoto(photo: DisplayPhoto) {
        _state.update { oldState ->
            println("TODO PROCESSING ${_state.value.photos.indexOf(photo)}")
            oldState.copy(photos = oldState.photos.filter { it != photo })
        }
    }

    private suspend fun syncDisplayUrls(
        photos: List<DisplayPhoto>,
    ) = coroutineScope {
        photos
            .take(10)
            .filter { it.displayUrl == null }
            .map { photo ->
                async {
                    val url = fileManager.getUnauthenticatedLinkForPhoto(photo.path)
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
    )

    data class DisplayPhoto(
        val path: String,
        val displayUrl: String?
    )
}