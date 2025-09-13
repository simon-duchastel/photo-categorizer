package com.duchastel.simon.photocategorizer.screens.settings

import com.duchastel.simon.photocategorizer.auth.AuthRepository
import com.duchastel.simon.photocategorizer.storage.LocalStorageRepository
import org.mockito.kotlin.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private lateinit var authRepository: AuthRepository
    private lateinit var localStorage: LocalStorageRepository
    private lateinit var viewModel: SettingsViewModel

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        authRepository = mock<AuthRepository>()
        localStorage = mock<LocalStorageRepository>()
        Dispatchers.setMain(testDispatcher)

        // Setup default mock behavior
        whenever(localStorage.getString(any())).thenReturn(null)
        doNothing().whenever(authRepository).logout()
        
        viewModel = SettingsViewModel(authRepository, localStorage)
    }

    @Test
    fun `initial state should have default settings and loading false`() = runTest {
        advanceUntilIdle()

        val state = viewModel.state.first()

        assertEquals(UserSettings.DEFAULT, state.userSettings)
        assertFalse(state.isLoading)
        assertFalse(state.isSaving)
        assertFalse(state.showSuccessMessage)
        assertFalse(state.showErrorMessage)
        assertNull(state.basePathError)
        assertNull(state.cameraRollPathError)
        assertNull(state.destinationFolderPathError)
        assertNull(state.archiveFolderPathError)
    }

    @Test
    fun `loadSettings should update state with saved settings`() = runTest {
        val savedSettings = UserSettings(
            backendType = BackendType.DROPBOX,
            basePath = "/custom/base",
            cameraRollPath = "/custom/camera",
            destinationFolderPath = "/custom/destination",
            archiveFolderPath = "/custom/archive"
        )
        whenever(localStorage.getString("user_settings")).thenReturn(
            """{"backendType":"DROPBOX","basePath":"/custom/base","cameraRollPath":"/custom/camera","destinationFolderPath":"/custom/destination","archiveFolderPath":"/custom/archive"}""")
        
        viewModel = SettingsViewModel(authRepository, localStorage)
        advanceUntilIdle()

        val state = viewModel.state.first()
        assertEquals(savedSettings, state.userSettings)
    }

    @Test
    fun `onBackendTypeChanged should update backend type`() = runTest {
        advanceUntilIdle()

        viewModel.onBackendTypeChanged(BackendType.DROPBOX)

        val state = viewModel.state.first()
        assertEquals(BackendType.DROPBOX, state.userSettings.backendType)
    }

    @Test
    fun `onBasePathChanged should update base path and clear error`() = runTest {
        advanceUntilIdle()

        val newPath = "/new/base/path"
        viewModel.onBasePathChanged(newPath)

        val state = viewModel.state.first()
        assertEquals(newPath, state.userSettings.basePath)
        assertNull(state.basePathError)
    }

    @Test
    fun `onCameraRollPathChanged should update path and clear error`() = runTest {
        advanceUntilIdle()

        val newPath = "/new/camera/path"
        viewModel.onCameraRollPathChanged(newPath)

        val state = viewModel.state.first()
        assertEquals(newPath, state.userSettings.cameraRollPath)
        assertNull(state.cameraRollPathError)
    }

    @Test
    fun `onDestinationFolderPathChanged should update path and clear error`() = runTest {
        advanceUntilIdle()

        val newPath = "/new/destination/path"
        viewModel.onDestinationFolderPathChanged(newPath)

        val state = viewModel.state.first()
        assertEquals(newPath, state.userSettings.destinationFolderPath)
        assertNull(state.destinationFolderPathError)
    }

    @Test
    fun `onArchiveFolderPathChanged should update path and clear error`() = runTest {
        advanceUntilIdle()

        val newPath = "/new/archive/path"
        viewModel.onArchiveFolderPathChanged(newPath)

        val state = viewModel.state.first()
        assertEquals(newPath, state.userSettings.archiveFolderPath)
        assertNull(state.archiveFolderPathError)
    }

    @Test
    fun `onSaveClicked with valid settings should save successfully`() = runTest {
        advanceUntilIdle()

        // Set valid settings
        viewModel.onBasePathChanged("/valid/base")
        viewModel.onCameraRollPathChanged("/valid/camera")
        viewModel.onDestinationFolderPathChanged("/valid/destination")
        viewModel.onArchiveFolderPathChanged("/valid/archive")
        advanceUntilIdle()

        viewModel.onSaveClicked()
        advanceUntilIdle() 

        verify(localStorage).putString(eq("user_settings"), any())
        
        val state = viewModel.state.first()
        assertTrue(state.showSuccessMessage)
    }

    @Test
    fun `onSaveClicked with empty base path should show validation error`() = runTest {
        advanceUntilIdle()

        // Set empty base path
        viewModel.onBasePathChanged("")
        viewModel.onCameraRollPathChanged("/valid/camera")
        viewModel.onDestinationFolderPathChanged("/valid/destination")
        viewModel.onArchiveFolderPathChanged("/valid/archive")
        advanceUntilIdle()

        viewModel.onSaveClicked()

        val state = viewModel.state.first()
        assertEquals("Base path is required", state.basePathError)
    }

    @Test
    fun `onSaveClicked with empty camera roll path should show validation error`() = runTest {
        advanceUntilIdle()

        // Set empty camera roll path
        viewModel.onBasePathChanged("/valid/base")
        viewModel.onCameraRollPathChanged("")
        viewModel.onDestinationFolderPathChanged("/valid/destination")
        viewModel.onArchiveFolderPathChanged("/valid/archive")
        advanceUntilIdle()

        viewModel.onSaveClicked()

        val state = viewModel.state.first()
        assertEquals("Camera roll path is required", state.cameraRollPathError)
    }

    @Test
    fun `onSaveClicked with empty destination folder path should show validation error`() = runTest {
        advanceUntilIdle()

        // Set empty destination folder path
        viewModel.onBasePathChanged("/valid/base")
        viewModel.onCameraRollPathChanged("/valid/camera")
        viewModel.onDestinationFolderPathChanged("")
        viewModel.onArchiveFolderPathChanged("/valid/archive")
        advanceUntilIdle()

        viewModel.onSaveClicked()

        val state = viewModel.state.first()
        assertEquals("Destination folder path is required", state.destinationFolderPathError)
    }

    @Test
    fun `onSaveClicked with empty archive folder path should show validation error`() = runTest {
        advanceUntilIdle()

        // Set empty archive folder path
        viewModel.onBasePathChanged("/valid/base")
        viewModel.onCameraRollPathChanged("/valid/camera")
        viewModel.onDestinationFolderPathChanged("/valid/destination")
        viewModel.onArchiveFolderPathChanged("")
        advanceUntilIdle()

        viewModel.onSaveClicked()

        val state = viewModel.state.first()
        assertEquals("Archive folder path is required", state.archiveFolderPathError)
    }

    @Test
    fun `onResetToDefaultsClicked should reset to default settings`() = runTest {
        advanceUntilIdle()

        // Change settings first
        viewModel.onBasePathChanged("/custom/base")
        viewModel.onCameraRollPathChanged("/custom/camera")
        advanceUntilIdle()

        viewModel.onResetToDefaultsClicked()

        val state = viewModel.state.first()
        assertEquals(UserSettings.DEFAULT, state.userSettings)
    }

    @Test
    fun `onLogoutClicked should call auth repository logout`() = runTest {
        viewModel.onLogoutClicked()
        
        verify(authRepository).logout()
    }

    @Test
    fun `onSuccessMessageShown should hide success message`() = runTest {
        // Trigger success message first
        advanceUntilIdle()
        viewModel.onBasePathChanged("/valid/base")
        viewModel.onCameraRollPathChanged("/valid/camera")
        viewModel.onDestinationFolderPathChanged("/valid/destination")
        viewModel.onArchiveFolderPathChanged("/valid/archive")
        advanceUntilIdle()
        viewModel.onSaveClicked()
        advanceUntilIdle()

        viewModel.onSuccessMessageShown()

        val state = viewModel.state.first()
        assertFalse(state.showSuccessMessage)
    }

    @Test
    fun `save failure should show error message`() = runTest {
        doThrow(RuntimeException("Save failed")).whenever(localStorage).putString(any(), any())
        
        advanceUntilIdle()
        viewModel.onBasePathChanged("/valid/base")
        viewModel.onCameraRollPathChanged("/valid/camera")
        viewModel.onDestinationFolderPathChanged("/valid/destination")
        viewModel.onArchiveFolderPathChanged("/valid/archive")
        advanceUntilIdle()

        viewModel.onSaveClicked()
        advanceUntilIdle()

        val state = viewModel.state.first()
        assertTrue(state.showErrorMessage)
        assertFalse(state.isSaving)
    }

    @Test
    fun `UserSettings default values should match expected defaults`() {
        val defaultSettings = UserSettings.DEFAULT

        assertEquals(BackendType.DROPBOX, defaultSettings.backendType)
        assertEquals("/camera test", defaultSettings.basePath)
        assertEquals("/camera test/camera roll", defaultSettings.cameraRollPath)
        assertEquals("/first event", defaultSettings.destinationFolderPath)
        assertEquals("/camera roll archive", defaultSettings.archiveFolderPath)
    }

    @Test
    fun `BackendType enum should have correct display name`() {
        assertEquals("Dropbox", BackendType.DROPBOX.displayName)
    }
}