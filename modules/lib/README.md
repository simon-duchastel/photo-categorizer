# Library Modules

Core business logic and data management modules providing foundational functionality for the Photo Categorizer app.

## Module Overview

```
lib/
├── auth/                           # Authentication system and token management
├── concurrency/                    # Rate limiting and concurrency utilities
├── dropbox/                        # Dropbox API integration and file operations  
├── filemanager/                    # Abstract file management interface
├── navigation/                     # Navigation utilities and routing
├── storage/                        # Local storage and data persistence
├── time/                           # Clock and time utilities, including DI for System Clock
└── utils/                          # Shared utility functions and extensions
```

## Module Descriptions

### Authentication (`auth/`)
- Core authentication repository interface
- HTTP interceptors for auth token management
- Session state management
- **Key Components**: `AuthRepository`, `AccessTokenAuthInterceptor`, `LoggedOutInterceptor`

### Dropbox Integration (`dropbox/`)
- Dropbox API client and service implementations
- File upload, download, and management operations
- OAuth authentication flow for Dropbox
- **Key Components**: `DropboxApiService`, `DropboxFileRepository`, `DropboxAuthRepository`

### File Manager (`filemanager/`)
- Abstract interface for photo repository operations
- Defines contracts for file management across different providers
- **Key Components**: `PhotoRepository` interface

### Navigation (`navigation/`)
- Navigation utilities and route definitions
- Screen routing and deep link handling
- **Key Components**: Navigation graph definitions and utilities

### Storage (`storage/`)
- Local data persistence and caching
- Database operations and local file management
- **Key Components**: Local storage repositories and database entities

### Utilities (`utils/`)
- Shared utility functions and extensions
- Common helper classes used across modules
- **Key Components**: Extension functions, utility classes

## Dependencies

Library modules follow clean architecture dependency rules:
- `dropbox/` → `auth/`, `filemanager/`
- `filemanager/` → `storage/`
- All modules can depend on `utils/`
- No circular dependencies allowed

## Usage

These library modules provide the core functionality used by UI screens and the main application. They encapsulate business logic and data operations, keeping the UI layer focused purely on presentation.