# API Integration

Documentation for cloud storage integrations, primarily Dropbox API implementation.

## Dropbox Integration

The Photo Categorizer app integrates with Dropbox to provide cloud photo storage and synchronization.

### Authentication Flow

1. **OAuth 2.0**: App uses Dropbox OAuth for secure authentication
2. **Token Management**: Tokens stored securely using Android Keystore
3. **Token Refresh**: Automatic refresh of expired access tokens

### Key Components

#### Auth Module (`modules/lib/auth/`)
- **`AuthRepository`** - Core authentication interface
- **`AccessTokenAuthInterceptor`** - Adds auth headers to API requests  
- **`LoggedOutInterceptor`** - Handles session expiration

#### Dropbox Module (`modules/lib/dropbox/`)
- **`DropboxAuthRepository`** - Dropbox-specific auth implementation
- **`DropboxApiService`** - API service for Dropbox endpoints
- **`DropboxFileRepository`** - File operations (upload, download, list)

### API Operations

#### File Operations
```kotlin
// List files in folder
suspend fun listFiles(path: String): List<PhotoFile>

// Upload photo
suspend fun uploadPhoto(localPath: String, remotePath: String): Boolean

// Download photo
suspend fun downloadPhoto(remotePath: String, localPath: String): Boolean

// Move photo to category folder
suspend fun categorizePhoto(photoPath: String, category: String): Boolean
```

#### Authentication
```kotlin
// Check authentication status
suspend fun isAuthenticated(): Boolean

// Start OAuth flow
suspend fun authenticate(): AuthResult

// Sign out user
suspend fun signOut()
```

### Configuration

#### API Keys Setup
Add to `local.properties`:
```properties
dropbox.app.key=your_dropbox_app_key
dropbox.app.secret=your_dropbox_app_secret
```

#### Permissions
Required Android permissions:
- `INTERNET` - Network access
- `READ_EXTERNAL_STORAGE` - Read local photos
- `WRITE_EXTERNAL_STORAGE` - Write downloaded photos

### Error Handling

#### Common Error Scenarios
- **Network Errors**: Retry with exponential backoff
- **Auth Errors**: Redirect to login screen
- **Rate Limiting**: Respect API rate limits
- **Storage Errors**: Handle insufficient space

#### Implementation
```kotlin
sealed class ApiResult<T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Error<T>(val exception: ApiException) : ApiResult<T>()
}

sealed class ApiException : Exception() {
    object NetworkError : ApiException()
    object AuthenticationError : ApiException()
    object RateLimitError : ApiException()
    data class UnknownError(val cause: Throwable) : ApiException()
}
```

### Testing

#### Unit Tests
- Mock API responses for testing
- Test error handling scenarios
- Verify authentication flows

#### Integration Tests  
- Test against Dropbox API sandbox
- Verify file upload/download operations
- Test OAuth flow end-to-end

### Future Integrations

The architecture supports adding additional cloud storage providers:
- Google Drive
- OneDrive  
- AWS S3

Each provider would implement the same `PhotoRepository` interface, following the same patterns as the Dropbox implementation.