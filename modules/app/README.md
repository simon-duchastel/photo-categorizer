# App Module

Main application module containing the entry point, configuration, and top-level application setup.

## Purpose

The app module serves as the application's entry point and handles:
- Application initialization and lifecycle management
- Dependency injection configuration
- Application-level settings and configuration
- Activity and navigation setup

## Key Components

### Application Entry Point
- **MainActivity** - Primary application activity
- **PhotoCategorizerApplication** - Application class for initialization
- **Application manifest** - App permissions and component declarations

### Dependency Injection
- Hilt/Dagger setup for dependency injection
- Application-level module definitions
- Provides dependencies to all other modules

### Configuration
- Application-level configuration and settings
- Build configuration and variants
- Proguard rules for release builds

## Dependencies

The app module depends on all other modules to compose the complete application:
- `lib/*` modules for core functionality
- `ui/*` modules for user interface
- External dependencies defined in module's build.gradle.kts

## Architecture Role

In the clean architecture pattern, the app module represents the outermost layer:
- Composes all feature modules into a working application
- Handles application-level concerns (DI, configuration, lifecycle)
- Provides the Android application framework integration

## Build Configuration

The app module contains the main build configuration:
- Application ID and version information
- Signing configuration for releases
- Build types (debug/release)
- Dependency declarations for all modules