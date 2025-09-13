package com.duchastel.simon.photocategorizer.screens.photoswiper

import coil3.request.ImageRequest
import com.duchastel.simon.photocategorizer.filemanager.Photo
import com.duchastel.simon.photocategorizer.filemanager.PhotoRepository
import com.duchastel.simon.photocategorizer.screens.settings.BackendType
import com.duchastel.simon.photocategorizer.screens.settings.UserSettings
import com.duchastel.simon.photocategorizer.storage.LocalStorageRepository
import com.duchastel.simon.photocategorizer.storage.get
import com.duchastel.simon.photocategorizer.storage.put
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.mockito.kotlin.wheneverBlocking

@OptIn(ExperimentalCoroutinesApi::class)
class PhotoSwiperViewModelTest {

    private lateinit var photoRepository: PhotoRepository
    private lateinit var localStorage: LocalStorageRepository
    private lateinit var viewModel: PhotoSwiperViewModel

    private val testDispatcher = StandardTestDispatcher()

    private val samplePhotos = listOf(
        Photo(
            name = "photo1.jpg",
            uploadDate = "2024-01-01",
            id = "photo1",
            path = "/camera test/camera roll/photo1.jpg"
        ),
        Photo(
            name = "photo2.jpg", 
            uploadDate = "2024-01-01",
            id = "photo2",
            path = "/camera test/camera roll/photo2.jpg"
        )
    )

    @Before
    fun setUp() {
        photoRepository = mock<PhotoRepository>()
        localStorage = mock<LocalStorageRepository>()
        Dispatchers.setMain(testDispatcher)

        wheneverBlocking { photoRepository.getPhotos(any()) } doReturn samplePhotos
        wheneverBlocking { photoRepository.getUnauthenticatedLinkForPhoto(any()) } doReturn "mock-url"

        viewModel = PhotoSwiperViewModel(photoRepository, localStorage, testDispatcher)
    }

    @Test
    fun initial_state_should_have_correct_defaults() = runTest {
        advanceUntilIdle()

        val state = viewModel.state.first()
        
        assertEquals(0, state.photoIndex)
        assertNull(state.newFolderModal)

        assertEquals(2, state.photos.size)
    }

    @Test
    fun view_model_should_use_custom_camera_roll_path_from_settings_on_initialization() = runTest {
        // Setup custom settings with different camera roll path
        val customSettings = UserSettings(
            backendType = BackendType.DROPBOX,
            basePath = "/photos/2024",
            cameraRollPath = "/custom/camera/path",
            destinationFolderPath = "/vacation",
            archiveFolderPath = "/archive"
        )
        
        // Create separate mocks for this test to avoid conflicts
        val testPhotoRepository = mock<PhotoRepository>()
        val testLocalStorage = mock<LocalStorageRepository>()

        wheneverBlocking { testPhotoRepository.getPhotos(any()) } doReturn samplePhotos
        wheneverBlocking { testPhotoRepository.getUnauthenticatedLinkForPhoto(any()) } doReturn "mock-url"
        whenever(testLocalStorage.get<UserSettings>(any())).thenReturn(customSettings)

        // Create new ViewModel to test initialization
        val newViewModel = PhotoSwiperViewModel(testPhotoRepository, testLocalStorage, testDispatcher)
        advanceUntilIdle()

        // Verify it used the custom camera roll path for loading photos
        verify(testPhotoRepository).getPhotos("/custom/camera/path")
    }

    @Test
    fun view_model_should_use_default_camera_roll_path_when_local_storage_is_null() = runTest {
        advanceUntilIdle()
        // This test verifies that the original viewModel (already created in setUp) 
        // used the default camera roll path since localStorage.get returns null by default
        
        // The viewModel was already created in setUp with null localStorage settings
        // Verify it used the default camera roll path for loading photos
        verify(photoRepository).getPhotos("/camera test/camera roll")
    }

    @Test
    fun show_new_folder_modal_should_set_modal_state() = runTest {
        val testPhoto = PhotoSwiperViewModel.DisplayPhoto("/test/photo.jpg")

        viewModel.showNewFolderModal(testPhoto)

        val updatedState = viewModel.state.first()
        assertNotNull(updatedState.newFolderModal)
        assertEquals(testPhoto, updatedState.newFolderModal?.photo)
        assertEquals("", updatedState.newFolderModal?.folderName)
    }

    @Test
    fun hide_new_folder_modal_should_clear_modal_state() = runTest {
        val testPhoto = PhotoSwiperViewModel.DisplayPhoto("/test/photo.jpg")

        viewModel.showNewFolderModal(testPhoto)
        viewModel.hideNewFolderModal()

        val updatedState = viewModel.state.first()
        assertNull(updatedState.newFolderModal)
    }

    @Test
    fun update_new_folder_name_should_update_folder_name_in_modal() = runTest {
        val testPhoto = PhotoSwiperViewModel.DisplayPhoto("/test/photo.jpg")

        viewModel.showNewFolderModal(testPhoto)
        viewModel.updateNewFolderName("vacation")

        val updatedState = viewModel.state.first()
        assertNotNull(updatedState.newFolderModal)
        assertEquals("vacation", updatedState.newFolderModal?.folderName)
        assertEquals(testPhoto, updatedState.newFolderModal?.photo)
    }

    @Test
    fun update_new_folder_name_when_modal_is_null_should_not_crash() = runTest {
        advanceUntilIdle()

        viewModel.updateNewFolderName("vacation")

        val state = viewModel.state.first()
        assertNull(state.newFolderModal)
    }

    @Test
    fun confirm_new_folder_with_blank_folder_name_should_not_process() = runTest {
        advanceUntilIdle()
        val state = viewModel.state.first()
        
        val photo = state.photos[0]

        viewModel.showNewFolderModal(photo)
        viewModel.updateNewFolderName("")
        viewModel.confirmNewFolder()

        // Modal should remain open since folder name is blank
        val updatedState = viewModel.state.first()
        assertNotNull(updatedState.newFolderModal)
    }

    @Test
    fun confirm_new_folder_when_modal_is_null_should_not_crash() = runTest {
        advanceUntilIdle()

        viewModel.confirmNewFolder()

        // Should not crash and no repository calls should be made
        verify(photoRepository, never()).movePhoto(any(), any())
    }

    @Test
    fun confirm_new_folder_should_combine_base_path_with_new_folder_name() = runTest {
        advanceUntilIdle()
        val state = viewModel.state.first()
        val photo = state.photos[0]

        // Setup custom settings with base path
        val customSettings = UserSettings(
            backendType = BackendType.DROPBOX,
            basePath = "/photos/2024",
            cameraRollPath = "/camera test/camera roll",
            destinationFolderPath = "/vacation",
            archiveFolderPath = "/archive"
        )
        whenever(localStorage.get<UserSettings>(any())).thenReturn(customSettings)

        viewModel.showNewFolderModal(photo)
        viewModel.updateNewFolderName("summer-trip")
        viewModel.confirmNewFolder()
        advanceUntilIdle()

        // Verify photo was moved to combined base path + new folder name
        verify(photoRepository).movePhoto(
            originalPath = photo.path,
            newPath = "/photos/2024/summer-trip/photo1.jpg"
        )
        
        // Verify settings were updated with new destination folder
        verify(localStorage).put("user_settings", customSettings.copy(destinationFolderPath = "/summer-trip"))
    }

    @Test
    fun confirm_new_folder_should_use_default_base_path_when_local_storage_is_null() = runTest {
        advanceUntilIdle()
        val state = viewModel.state.first()
        val photo = state.photos[0]
        
        // Ensure localStorage returns null for settings (default mock behavior)
        whenever(localStorage.get<UserSettings>(any())).thenReturn(null)

        viewModel.showNewFolderModal(photo)
        viewModel.updateNewFolderName("new-folder")
        viewModel.confirmNewFolder()
        advanceUntilIdle()

        // Should use UserSettings.DEFAULT base path: "/camera test"
        verify(photoRepository).movePhoto(
            originalPath = photo.path,
            newPath = "/camera test/new-folder/photo1.jpg"
        )
        
        // Verify settings were updated with new destination folder
        verify(localStorage).put("user_settings", UserSettings.DEFAULT.copy(destinationFolderPath = "/new-folder"))
    }

    @Test
    fun confirm_new_folder_should_hide_modal_after_processing() = runTest {
        advanceUntilIdle()
        val state = viewModel.state.first()
        val photo = state.photos[0]

        viewModel.showNewFolderModal(photo)
        viewModel.updateNewFolderName("vacation")
        
        // Verify modal is shown
        val stateWithModal = viewModel.state.first()
        assertNotNull(stateWithModal.newFolderModal)

        viewModel.confirmNewFolder()
        advanceUntilIdle()

        // Verify modal is hidden after confirmation
        val finalState = viewModel.state.first()
        assertNull(finalState.newFolderModal)
    }

    @Test
    fun attach_image_request_should_update_photo_with_image_request() = runTest {
         advanceUntilIdle()
         val state = viewModel.state.first()
         val photo = state.photos[0]
         val mockImageRequest = mock<ImageRequest>()

         viewModel.attachImageRequest(photo, mockImageRequest)

         advanceUntilIdle()
         val updatedState = viewModel.state.first()
         assertEquals(mockImageRequest, updatedState.photos[0].imageRequest)
         // Other photos should remain unchanged
         if (updatedState.photos.size > 1) {
             assertNull(updatedState.photos[1].imageRequest)
         }
     }

    @Test
    fun display_photo_file_name_should_extract_correct_file_name() {
        val photo = PhotoSwiperViewModel.DisplayPhoto("/camera test/camera roll/vacation-photo.jpg")
        assertEquals("vacation-photo.jpg", photo.fileName)
    }

    @Test
    fun display_photo_file_name_with_no_path_separators_should_return_whole_path() {
        val photo = PhotoSwiperViewModel.DisplayPhoto("photo.jpg")
        assertEquals("photo.jpg", photo.fileName)
    }

    @Test
    fun new_folder_modal_state_should_have_correct_default_values() {
        val photo = PhotoSwiperViewModel.DisplayPhoto("/test/photo.jpg")
        val modalState = PhotoSwiperViewModel.NewFolderModalState(photo = photo)

        assertEquals(photo, modalState.photo)
        assertEquals("", modalState.folderName)
    }

    @Test
    fun process_photo_should_advance_photo_index() = runTest {
         advanceUntilIdle()

         viewModel.processPhoto(0, SwipeDirection.Right)

        advanceUntilIdle()
         val updatedState = viewModel.state.first()
         assertEquals(1, updatedState.photoIndex)
     }

     @Test
     fun process_photo_with_left_swipe_should_show_modal_and_advance_index() = runTest {
         advanceUntilIdle()

         viewModel.processPhoto(0, SwipeDirection.Left)

         advanceUntilIdle()
         val updatedState = viewModel.state.first()
         assertEquals(1, updatedState.photoIndex)
         assertNotNull(updatedState.newFolderModal)
     }

    @Test
    fun process_photo_with_right_swipe_should_combine_base_path_with_destination_folder() =
        runTest {
        advanceUntilIdle()
        val state = viewModel.state.first()
        val photo = state.photos[0]
        
        // Setup custom settings with base path
        val customSettings = UserSettings(
            backendType = BackendType.DROPBOX,
            basePath = "/photos/2024",
            cameraRollPath = "/camera test/camera roll",
            destinationFolderPath = "/vacation",
            archiveFolderPath = "/archive"
        )
        whenever(localStorage.get<UserSettings>(any())).thenReturn(customSettings)

        viewModel.processPhoto(0, SwipeDirection.Right)
        advanceUntilIdle()

        // Verify photo was moved to combined base path + destination folder path
        verify(photoRepository).movePhoto(
            originalPath = photo.path,
            newPath = "/photos/2024/vacation/photo1.jpg"
        )
    }

    @Test
    fun process_photo_with_right_swipe_should_use_default_settings_when_local_storage_is_null() =
        runTest {
        advanceUntilIdle()
        val state = viewModel.state.first()
        val photo = state.photos[0]
        
        // Ensure localStorage returns null (default mock behavior)
        whenever(localStorage.get<UserSettings>(any())).thenReturn(null)

        viewModel.processPhoto(0, SwipeDirection.Right)
        advanceUntilIdle()

        // Should use UserSettings.DEFAULT values
        // base path: "/camera test" + destination: "/first event"
        verify(photoRepository).movePhoto(
            originalPath = photo.path,
            newPath = "/camera test/first event/photo1.jpg"
        )
    }

    @Test
    fun process_photo_with_up_swipe_should_combine_base_path_with_archive_folder() = runTest {
        advanceUntilIdle()
        val state = viewModel.state.first()
        val photo = state.photos[0]
        
        // Setup custom settings with base path
        val customSettings = UserSettings(
            backendType = BackendType.DROPBOX,
            basePath = "/photos/2024",
            cameraRollPath = "/camera test/camera roll",
            destinationFolderPath = "/vacation",
            archiveFolderPath = "/archive"
        )
        whenever(localStorage.get<UserSettings>(any())).thenReturn(customSettings)

        viewModel.processPhoto(0, SwipeDirection.Up)
        advanceUntilIdle()

        // Verify photo was moved to combined base path + archive folder path
        verify(photoRepository).movePhoto(
            originalPath = photo.path,
            newPath = "/photos/2024/archive/photo1.jpg"
        )
    }

    @Test
    fun process_photo_with_up_swipe_should_use_default_settings_when_local_storage_is_null() =
        runTest {
        advanceUntilIdle()
        val state = viewModel.state.first()
        val photo = state.photos[0]
        
        // Ensure localStorage returns null (default mock behavior)
        whenever(localStorage.get<UserSettings>(any())).thenReturn(null)

        viewModel.processPhoto(0, SwipeDirection.Up)
        advanceUntilIdle()

        // Should use UserSettings.DEFAULT values
        // base path: "/camera test" + archive: "/camera roll archive"
        verify(photoRepository).movePhoto(
            originalPath = photo.path,
            newPath = "/camera test/camera roll archive/photo1.jpg"
        )
    }
}

