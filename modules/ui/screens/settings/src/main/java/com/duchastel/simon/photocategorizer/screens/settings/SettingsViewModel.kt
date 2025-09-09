package com.duchastel.simon.photocategorizer.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.duchastel.simon.photocategorizer.auth.AuthRepository
import com.duchastel.simon.photocategorizer.dropbox.di.Dropbox
import com.duchastel.simon.photocategorizer.storage.LocalStorageRepository
import com.duchastel.simon.photocategorizer.storage.get
import com.duchastel.simon.photocategorizer.storage.put
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @Dropbox private val authRepository: AuthRepository,
    private val localStorage: LocalStorageRepository,
) : ViewModel() {

    private val _state: MutableStateFlow<State> = MutableStateFlow(
        State(
            userSettings = UserSettings.DEFAULT,
            isLoading = true,
            isSaving = false,
            showSuccessMessage = false,
            showErrorMessage = false,
            cameraRollPathError = null,
            destinationFolderPathError = null,
            archiveFolderPathError = null,
        )
    )
    val state: StateFlow<State> = _state

    init {
        loadSettings()
    }

    fun onBackendTypeChanged(backendType: BackendType) {
        _state.update { it.copy(userSettings = it.userSettings.copy(backendType = backendType)) }
    }

    fun onCameraRollPathChanged(path: String) {
        _state.update { it.copy(
            userSettings = it.userSettings.copy(cameraRollPath = path),
            cameraRollPathError = null
        ) }
    }

    fun onDestinationFolderPathChanged(path: String) {
        _state.update { it.copy(
            userSettings = it.userSettings.copy(destinationFolderPath = path),
            destinationFolderPathError = null
        ) }
    }

    fun onArchiveFolderPathChanged(path: String) {
        _state.update { it.copy(
            userSettings = it.userSettings.copy(archiveFolderPath = path),
            archiveFolderPathError = null
        ) }
    }

    fun onSaveClicked() {
        val currentState = _state.value
        val validationErrors = validateSettings(currentState.userSettings)
        
        if (validationErrors.isEmpty()) {
            saveSettings(currentState.userSettings)
        } else {
            _state.update { currentState.copy(
                cameraRollPathError = validationErrors[ValidationField.CAMERA_ROLL_PATH],
                destinationFolderPathError = validationErrors[ValidationField.DESTINATION_FOLDER_PATH],
                archiveFolderPathError = validationErrors[ValidationField.ARCHIVE_FOLDER_PATH]
            ) }
        }
    }

    fun onResetToDefaultsClicked() {
        _state.update { it.copy(userSettings = UserSettings.DEFAULT) }
    }

    fun onLogoutClicked() {
        authRepository.logout()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            _state.update { currentState ->
                val savedSettings = localStorage.get<UserSettings>(SETTINGS_KEY) ?: UserSettings.DEFAULT
                currentState.copy(
                    userSettings = savedSettings,
                    isLoading = false
                )
            }
        }
    }

    private fun saveSettings(userSettings: UserSettings) {
        viewModelScope.launch {
            _state.update { it.copy(isSaving = true) }
            
            try {
                localStorage.put(SETTINGS_KEY, userSettings)
                _state.update { it.copy(
                    isSaving = false,
                    showSuccessMessage = true
                ) }
            } catch (e: Exception) {
                _state.update { it.copy(
                    isSaving = false,
                    showErrorMessage = true
                ) }
            }
        }
    }

    private fun validateSettings(settings: UserSettings): Map<ValidationField, String> {
        val errors = mutableMapOf<ValidationField, String>()
        
        if (settings.cameraRollPath.isBlank()) {
            errors[ValidationField.CAMERA_ROLL_PATH] = "Camera roll path is required"
        }
        
        if (settings.destinationFolderPath.isBlank()) {
            errors[ValidationField.DESTINATION_FOLDER_PATH] = "Destination folder path is required"
        }
        
        if (settings.archiveFolderPath.isBlank()) {
            errors[ValidationField.ARCHIVE_FOLDER_PATH] = "Archive folder path is required"
        }
        
        return errors
    }

    fun onSuccessMessageShown() {
        _state.update { it.copy(showSuccessMessage = false) }
    }

    fun onErrorMessageShown() {
        _state.update { it.copy(showErrorMessage = false) }
    }

    data class State(
        val userSettings: UserSettings,
        val isLoading: Boolean,
        val isSaving: Boolean,
        val showSuccessMessage: Boolean,
        val showErrorMessage: Boolean,
        val cameraRollPathError: String?,
        val destinationFolderPathError: String?,
        val archiveFolderPathError: String?,
    )

    companion object {
        private const val SETTINGS_KEY = "user_settings"
    }
}

@Serializable
data class UserSettings(
    val backendType: BackendType = BackendType.DROPBOX,
    val cameraRollPath: String = "/camera test/camera roll",
    val destinationFolderPath: String = "/camera test/first event",
    val archiveFolderPath: String = "/camera test/camera roll archive",
) {
    companion object {
        val DEFAULT = UserSettings()
    }
}

enum class ValidationField {
    CAMERA_ROLL_PATH,
    DESTINATION_FOLDER_PATH,
    ARCHIVE_FOLDER_PATH
}

@Serializable
enum class BackendType(val displayName: String) {
    DROPBOX("Dropbox")
}