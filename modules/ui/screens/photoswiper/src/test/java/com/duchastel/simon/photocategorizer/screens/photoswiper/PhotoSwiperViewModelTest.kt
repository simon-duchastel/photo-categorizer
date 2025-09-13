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
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

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

        // Setup default mock behavior for suspend functions
        runBlocking { 
            whenever(photoRepository.getPhotos(any())).thenReturn(samplePhotos)
            doAnswer { /* do nothing */ }.whenever(photoRepository).movePhoto(any(), any())
            whenever(photoRepository.getUnauthenticatedLinkForPhoto(any())).thenReturn("mock-url")
        }
        
        // Setup localStorage mocks using doReturn/whenever pattern for generic methods
        whenever(localStorage.getString(any())).thenReturn(null)
        doAnswer { /* do nothing */ }.whenever(localStorage).putString(any(), any())
        doReturn(null).whenever(localStorage).get<UserSettings>(any())
        doAnswer { /* do nothing */ }.whenever(localStorage).put(any(), any<UserSettings>())

        viewModel = PhotoSwiperViewModel(photoRepository, localStorage, testDispatcher)
    }

    @Test
    fun `initial state should have correct defaults`() = runTest {
        advanceUntilIdle()

        val state = viewModel.state.first()
        
        assertEquals(0, state.photoIndex)
        assertNull(state.newFolderModal)

        assertEquals(2, state.photos.size)
    }

    @Test
    fun `ViewModel should use custom camera roll path from settings on initialization`() = runTest {
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
        
        runBlocking {
            whenever(testPhotoRepository.getPhotos(any())).thenReturn(samplePhotos)
            whenever(testPhotoRepository.getUnauthenticatedLinkForPhoto(any())).thenReturn("mock-url")
        }
        doReturn(customSettings).whenever(testLocalStorage).get<UserSettings>(any())

        // Create new ViewModel to test initialization
        val newViewModel = PhotoSwiperViewModel(testPhotoRepository, testLocalStorage, testDispatcher)
        advanceUntilIdle()

        // Verify it used the custom camera roll path for loading photos
        verify(testPhotoRepository).getPhotos("/custom/camera/path")
    }

    @Test 
    fun `ViewModel should use default camera roll path when localStorage is null`() = runTest {
        // This test verifies that the original viewModel (already created in setUp) 
        // used the default camera roll path since localStorage.get returns null by default
        
        // The viewModel was already created in setUp with null localStorage settings
        // Verify it used the default camera roll path for loading photos
        verify(photoRepository).getPhotos("/camera test/camera roll")
    }

    @Test
    fun `showNewFolderModal should set modal state`() = runTest {
        val testPhoto = PhotoSwiperViewModel.DisplayPhoto("/test/photo.jpg")

        viewModel.showNewFolderModal(testPhoto)

        val updatedState = viewModel.state.first()
        assertNotNull(updatedState.newFolderModal)
        assertEquals(testPhoto, updatedState.newFolderModal?.photo)
        assertEquals("", updatedState.newFolderModal?.folderName)
    }

    @Test
    fun `hideNewFolderModal should clear modal state`() = runTest {
        val testPhoto = PhotoSwiperViewModel.DisplayPhoto("/test/photo.jpg")

        viewModel.showNewFolderModal(testPhoto)
        viewModel.hideNewFolderModal()

        val updatedState = viewModel.state.first()
        assertNull(updatedState.newFolderModal)
    }

    @Test
    fun `updateNewFolderName should update folder name in modal`() = runTest {
        val testPhoto = PhotoSwiperViewModel.DisplayPhoto("/test/photo.jpg")

        viewModel.showNewFolderModal(testPhoto)
        viewModel.updateNewFolderName("vacation")

        val updatedState = viewModel.state.first()
        assertNotNull(updatedState.newFolderModal)
        assertEquals("vacation", updatedState.newFolderModal?.folderName)
        assertEquals(testPhoto, updatedState.newFolderModal?.photo)
    }

    @Test
    fun `updateNewFolderName when modal is null should not crash`() = runTest {
        advanceUntilIdle()

        viewModel.updateNewFolderName("vacation")

        val state = viewModel.state.first()
        assertNull(state.newFolderModal)
    }

    @Test
    fun `confirmNewFolder with blank folder name should not process`() = runTest {
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
    fun `confirmNewFolder when modal is null should not crash`() = runTest {
        advanceUntilIdle()

        viewModel.confirmNewFolder()

        // Should not crash and no repository calls should be made
        verify(photoRepository, never()).movePhoto(any(), any())
    }

    @Test
    fun `confirmNewFolder should combine base path with new folder name`() = runTest {
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
        doReturn(customSettings).whenever(localStorage).get<UserSettings>("user_settings")

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
    fun `confirmNewFolder should use default base path when localStorage is null`() = runTest {
        advanceUntilIdle()
        val state = viewModel.state.first()
        val photo = state.photos[0]
        
        // Ensure localStorage returns null for settings (default mock behavior)
        doReturn(null).whenever(localStorage).get<UserSettings>("user_settings")

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
    fun `confirmNewFolder should hide modal after processing`() = runTest {
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
     fun `attachImageRequest should update photo with image request`() = runTest {
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
    fun `DisplayPhoto fileName should extract correct file name`() {
        val photo = PhotoSwiperViewModel.DisplayPhoto("/camera test/camera roll/vacation-photo.jpg")
        assertEquals("vacation-photo.jpg", photo.fileName)
    }

    @Test
    fun `DisplayPhoto fileName with no path separators should return whole path`() {
        val photo = PhotoSwiperViewModel.DisplayPhoto("photo.jpg")
        assertEquals("photo.jpg", photo.fileName)
    }

    @Test
    fun `NewFolderModalState should have correct default values`() {
        val photo = PhotoSwiperViewModel.DisplayPhoto("/test/photo.jpg")
        val modalState = PhotoSwiperViewModel.NewFolderModalState(photo = photo)

        assertEquals(photo, modalState.photo)
        assertEquals("", modalState.folderName)
    }

    @Test
     fun `processPhoto should advance photo index`() = runTest {
         advanceUntilIdle()

         viewModel.processPhoto(0, SwipeDirection.Right)

        advanceUntilIdle()
         val updatedState = viewModel.state.first()
         assertEquals(1, updatedState.photoIndex)
     }

     @Test
     fun `processPhoto with left swipe should show modal and advance index`() = runTest {
         advanceUntilIdle()

         viewModel.processPhoto(0, SwipeDirection.Left)

         advanceUntilIdle()
         val updatedState = viewModel.state.first()
         assertEquals(1, updatedState.photoIndex)
         assertNotNull(updatedState.newFolderModal)
     }

    @Test
    fun `processPhoto with right swipe should combine base path with destination folder`() = runTest {
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
        doReturn(customSettings).whenever(localStorage).get<UserSettings>("user_settings")

        viewModel.processPhoto(0, SwipeDirection.Right)
        advanceUntilIdle()

        // Verify photo was moved to combined base path + destination folder path
        verify(photoRepository).movePhoto(
            originalPath = photo.path,
            newPath = "/photos/2024/vacation/photo1.jpg"
        )
    }

    @Test
    fun `processPhoto with right swipe should use default settings when localStorage is null`() = runTest {
        advanceUntilIdle()
        val state = viewModel.state.first()
        val photo = state.photos[0]
        
        // Ensure localStorage returns null (default mock behavior)
        doReturn(null).whenever(localStorage).get<UserSettings>("user_settings")

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
    fun `processPhoto with up swipe should combine base path with archive folder`() = runTest {
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
        doReturn(customSettings).whenever(localStorage).get<UserSettings>("user_settings")

        viewModel.processPhoto(0, SwipeDirection.Up)
        advanceUntilIdle()

        // Verify photo was moved to combined base path + archive folder path
        verify(photoRepository).movePhoto(
            originalPath = photo.path,
            newPath = "/photos/2024/archive/photo1.jpg"
        )
    }

    @Test
    fun `processPhoto with up swipe should use default settings when localStorage is null`() = runTest {
        advanceUntilIdle()
        val state = viewModel.state.first()
        val photo = state.photos[0]
        
        // Ensure localStorage returns null (default mock behavior)
        doReturn(null).whenever(localStorage).get<UserSettings>("user_settings")

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