# Architecture Overview

Photo Categorizer follows a multi-module clean architecture pattern with clear separation of concerns.

## Architecture Principles

- **Multi-module**: Each feature/concern is a separate module
- **Clean Architecture**: Business logic separated from UI and data layers
- **Dependency Inversion**: Dependencies flow inward toward business logic
- **Single Responsibility**: Each module has one clear purpose

## Module Structure

### Application Layer (`modules/app/`)
- Application entry point
- Dependency injection setup
- Application-level configuration

### Library Layer (`modules/lib/`)
Core business logic and data management:

- **`auth/`** - Authentication system and token management
- **`dropbox/`** - Dropbox API integration and file operations
- **`filemanager/`** - Abstract file management interface 
- **`navigation/`** - Navigation utilities and routing
- **`storage/`** - Local storage and data persistence
- **`utils/`** - Shared utility functions and extensions

### UI Layer (`modules/ui/`)
User interface components and screens:

- **`components/`** - Reusable UI components (swiper, buttons, loaders, etc.)
- **`screens/`** - Feature-specific screen implementations
- **`theme/`** - Design system and theming

## Data Flow

```
UI Layer → ViewModel → Repository → Data Source (Local/Remote)
```

1. **UI Components** trigger user actions
2. **ViewModels** handle business logic and state management
3. **Repositories** coordinate data access from multiple sources
4. **Data Sources** handle actual data operations (API calls, database)

## Technology Stack

- **Language**: Kotlin 
- **UI Framework**: Jetpack Compose
- **Architecture Components**: ViewModel, Repository pattern
- **Dependency Injection**: Hilt
- **Async**: Coroutines + Flow
- **Cloud Integration**: Dropbox API
- **Build System**: Gradle with Kotlin DSL

## Module Dependencies

```
app/ → lib/*, ui/*
ui/screens/* → ui/components/, ui/theme/, lib/*
lib/dropbox/ → lib/auth/, lib/filemanager/
lib/filemanager/ → lib/storage/
```

Dependencies flow from outer layers (UI, app) toward inner layers (lib) following clean architecture principles.