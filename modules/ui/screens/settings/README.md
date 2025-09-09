# Settings Screen Module

Comprehensive app configuration and user preferences management for the Photo Categorizer app.

## Features

### Settings Configuration
- **Backend Selection**: Choose cloud storage provider (currently Dropbox supported)
- **Folder Configuration**: Configure paths for photo organization:
  - **Camera Roll Location**: Source folder containing photos to categorize
  - **Destination Folder**: Target folder for right swipe categorization  
  - **Archive Folder**: Target folder for up swipe archiving
- **Persistent Storage**: Settings automatically saved and restored across app sessions
- **Form Validation**: Real-time validation with user-friendly error messages

### User Interface
- **Material 3 Design**: Modern, intuitive interface following Material Design guidelines
- **Responsive Layout**: Scrollable layout with organized sections
- **Loading States**: Visual feedback during settings load and save operations
- **Success/Error Feedback**: Toast messages for save confirmation and error handling
- **Reset Functionality**: One-click reset to default settings

## Components

### Core Components
- **`SettingsScreen`** - Main settings UI with comprehensive form interface
- **`SettingsViewModel`** - Settings state management and business logic
- **`UserSettings`** - Data model for user preferences with serialization support
- **`BackendType`** - Enum defining available cloud storage backends

### Data Model
```kotlin
@Serializable
data class UserSettings(
    val backendType: BackendType,
    val cameraRollPath: String,
    val destinationFolderPath: String,
    val archiveFolderPath: String,
) {
    companion object {
        val DEFAULT = UserSettings(
            backendType = BackendType.DROPBOX,
            cameraRollPath = "/camera test/camera roll",
            destinationFolderPath = "/camera test/first event",
            archiveFolderPath = "/camera test/camera roll archive",
        )
    }
}
```

### State Management
The `SettingsViewModel.State` includes:
- User settings data
- Loading and saving states
- Form validation error messages
- Success/error message flags

## Architecture

### Persistence
- Uses `LocalStorageRepository` for persistent storage via SharedPreferences
- Automatic JSON serialization/deserialization using Kotlinx Serialization
- Settings loaded on ViewModel initialization
- Immediate persistence on save with error handling

### Validation
- Required field validation for all folder paths
- Real-time error clearing on user input
- Comprehensive error message display
- Form submission prevention when validation fails

## Usage

### Navigation Integration
The Settings screen integrates with the app's navigation system and can be accessed through the main navigation flow.

### Dependencies
- **UI Dependencies**: `ui:theme`, `ui:components` for consistent design
- **Business Logic**: `lib:auth`, `lib:storage` for authentication and persistence
- **Cloud Integration**: `lib:dropbox` for backend storage functionality

## Default Settings
- **Backend**: Dropbox (currently the only supported option)
- **Camera Roll**: `/camera test/camera roll`
- **Destination Folder**: `/camera test/first event`
- **Archive Folder**: `/camera test/camera roll archive`

## Future Enhancements
- Support for additional cloud storage providers (Google Drive, OneDrive)
- Custom folder browsing and selection
- Batch settings import/export
- Settings synchronization across devices