package com.duchastel.simon.photocategorizer.screens.photoswiper

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil3.request.ImageRequest
import com.duchastel.simon.photocategorizer.dropbox.di.Dropbox
import com.duchastel.simon.photocategorizer.filemanager.PhotoRepository
import com.duchastel.simon.photocategorizer.screens.settings.UserSettings
import com.duchastel.simon.photocategorizer.storage.LocalStorageRepository
import com.duchastel.simon.photocategorizer.storage.get
import com.duchastel.simon.photocategorizer.storage.put
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
    @Dropbox private val photoRepository: PhotoRepository,
    private val localStorage: LocalStorageRepository,
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
        when (direction) {
            SwipeDirection.Left -> processLeftSwipe(photo)
            SwipeDirection.Right -> {
                viewModelScope.launch {
                    processRightSwipe(photo)
                }
            }
            SwipeDirection.Up -> {
                viewModelScope.launch {
                    processUpSwipe(photo)
                }
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

    fun showNewFolderModal(photo: DisplayPhoto) {
        _state.update { oldState ->
            oldState.copy(
                newFolderModal = NewFolderModalState(photo = photo)
            )
        }
    }

    fun hideNewFolderModal() {
        _state.update { oldState ->
            oldState.copy(newFolderModal = null)
        }
    }

    fun updateNewFolderName(name: String) {
        _state.update { oldState ->
            oldState.newFolderModal?.let { modal ->
                oldState.copy(
                    newFolderModal = modal.copy(folderName = name)
                )
            } ?: oldState
        }
    }

    fun confirmNewFolder() {
        val currentState = _state.value
        val modal = currentState.newFolderModal ?: return

        if (modal.folderName.isBlank()) return

        hideNewFolderModal()

        viewModelScope.launch {
            processPhotoToNewFolder(modal.photo, modal.folderName)
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

    private fun processLeftSwipe(photo: DisplayPhoto) {
        showNewFolderModal(photo)
    }

    private suspend fun processPhotoToNewFolder(photo: DisplayPhoto, folderName: String) {
        withContext(Dispatchers.IO) {
            val newPath = "/$folderName/${photo.fileName}"
            photoRepository.movePhoto(
                originalPath = photo.path,
                newPath = newPath,
            )

            // Update settings to use this as the new destination folder
            updateDestinationFolder("/$folderName")
        }
    }

    private fun updateDestinationFolder(newFolderPath: String) {
        val currentSettings = localStorage.get<UserSettings>(SETTINGS_KEY) ?: UserSettings.DEFAULT
        val updatedSettings = currentSettings.copy(destinationFolderPath = newFolderPath)
        localStorage.put(SETTINGS_KEY, updatedSettings)
    }

    private suspend fun processRightSwipe(photo: DisplayPhoto) {
        withContext(Dispatchers.IO) {
            photoRepository.movePhoto(
                originalPath = photo.path,
                newPath ="/camera test/first event/${photo.fileName}",
            )
        }
    }

    private suspend fun processUpSwipe(photo: DisplayPhoto) {
        withContext(Dispatchers.IO) {
            photoRepository.movePhoto(
                originalPath = photo.path,
                newPath ="/camera test/camera roll archive/${photo.fileName}",
            )
        }
    }

    // state definitions and constants

    data class State(
        val photos: List<DisplayPhoto> = emptyList(),
        val photoIndex: Int = 0,
        val newFolderModal: NewFolderModalState? = null,
    )

    data class NewFolderModalState(
        val photo: DisplayPhoto,
        val folderName: String = "",
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
        private const val SETTINGS_KEY = "user_settings"
    }
}