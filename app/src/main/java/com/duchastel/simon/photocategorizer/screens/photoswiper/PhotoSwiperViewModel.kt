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
            // TODO - remove when done testing
//            val photos = listOf(
//                DisplayPhoto(path = "path1", displayUrl = "https://uc22e31aef6a36e181d57a6476bb.dl.dropboxusercontent.com/cd/0/get/Cj5qyoPud7yM0bkFLj_ahit0-XS8f3FD-57rCYLNCpd0yOiWdPRShRWW3jMsPY3ia5UU06upCTtbLOKJgFoJW2oMUnDAZlVw0z89b-WwFi0AKjXJYO9n_IeBlkrymrPFQL4lxcssPivwfmj7v12sFSpsFmNqIj3Lg4nSJL7MbQVTaA/file"),
//                DisplayPhoto(path = "path2", displayUrl = "https://ucd9a1924bb018833562b691ca8c.dl.dropboxusercontent.com/cd/0/get/Cj7owZ5K5-eloVvbt9Ohh9bcB4F4PUbmXxu6xoU0oFvES6M4DI5Fq_Ll63QoxkWiFTEqQLyJRVDJReguiPEelUwxA7BPHh866KDaGcLi7OFEt0MBBrhgeAGy3_DhPFkoS5COJ1BmBQQRBOWnXaTjejpP8rvxTcgqkVfhX4BdZYQ54A/file"),
//                DisplayPhoto(path = "path3", displayUrl = "https://uc22e31aef6a36e181d57a6476bb.dl.dropboxusercontent.com/cd/0/get/Cj5qyoPud7yM0bkFLj_ahit0-XS8f3FD-57rCYLNCpd0yOiWdPRShRWW3jMsPY3ia5UU06upCTtbLOKJgFoJW2oMUnDAZlVw0z89b-WwFi0AKjXJYO9n_IeBlkrymrPFQL4lxcssPivwfmj7v12sFSpsFmNqIj3Lg4nSJL7MbQVTaA/file"),
//                DisplayPhoto(path = "path4", displayUrl = "https://ucd9a1924bb018833562b691ca8c.dl.dropboxusercontent.com/cd/0/get/Cj7owZ5K5-eloVvbt9Ohh9bcB4F4PUbmXxu6xoU0oFvES6M4DI5Fq_Ll63QoxkWiFTEqQLyJRVDJReguiPEelUwxA7BPHh866KDaGcLi7OFEt0MBBrhgeAGy3_DhPFkoS5COJ1BmBQQRBOWnXaTjejpP8rvxTcgqkVfhX4BdZYQ54A/file"),
//                DisplayPhoto(path = "path5", displayUrl = "https://uc22e31aef6a36e181d57a6476bb.dl.dropboxusercontent.com/cd/0/get/Cj5qyoPud7yM0bkFLj_ahit0-XS8f3FD-57rCYLNCpd0yOiWdPRShRWW3jMsPY3ia5UU06upCTtbLOKJgFoJW2oMUnDAZlVw0z89b-WwFi0AKjXJYO9n_IeBlkrymrPFQL4lxcssPivwfmj7v12sFSpsFmNqIj3Lg4nSJL7MbQVTaA/file"),
//                DisplayPhoto(path = "path6", displayUrl = "https://ucd9a1924bb018833562b691ca8c.dl.dropboxusercontent.com/cd/0/get/Cj7owZ5K5-eloVvbt9Ohh9bcB4F4PUbmXxu6xoU0oFvES6M4DI5Fq_Ll63QoxkWiFTEqQLyJRVDJReguiPEelUwxA7BPHh866KDaGcLi7OFEt0MBBrhgeAGy3_DhPFkoS5COJ1BmBQQRBOWnXaTjejpP8rvxTcgqkVfhX4BdZYQ54A/file"),
//                DisplayPhoto(path = "path7", displayUrl = "https://uc22e31aef6a36e181d57a6476bb.dl.dropboxusercontent.com/cd/0/get/Cj5qyoPud7yM0bkFLj_ahit0-XS8f3FD-57rCYLNCpd0yOiWdPRShRWW3jMsPY3ia5UU06upCTtbLOKJgFoJW2oMUnDAZlVw0z89b-WwFi0AKjXJYO9n_IeBlkrymrPFQL4lxcssPivwfmj7v12sFSpsFmNqIj3Lg4nSJL7MbQVTaA/file"),
//                DisplayPhoto(path = "path8", displayUrl = "https://ucd9a1924bb018833562b691ca8c.dl.dropboxusercontent.com/cd/0/get/Cj7owZ5K5-eloVvbt9Ohh9bcB4F4PUbmXxu6xoU0oFvES6M4DI5Fq_Ll63QoxkWiFTEqQLyJRVDJReguiPEelUwxA7BPHh866KDaGcLi7OFEt0MBBrhgeAGy3_DhPFkoS5COJ1BmBQQRBOWnXaTjejpP8rvxTcgqkVfhX4BdZYQ54A/file"),
//            )
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
        const val PHOTO_BUFFER_SIZE = 3
    }
}