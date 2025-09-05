# Development Setup

Guide for setting up the development environment and contributing to Photo Categorizer.

## Prerequisites

### Required Software
- **Android Studio**: Jellyfish | 2023.3.1 or later
- **JDK**: OpenJDK 17 or later
- **Android SDK**: API level 24+ (Android 7.0)
- **Kotlin**: 1.9+
- **Git**: For version control

### Optional Tools
- **Android Emulator**: For testing without physical device
- **Physical Android Device**: For real-world testing

## Environment Setup

### 1. Clone Repository
```bash
git clone <repository-url>
cd photo-categorizer
```

### 2. Android Studio Setup
1. Open project in Android Studio
2. Let Studio download required SDK components
3. Wait for Gradle sync to complete

### 3. Build Configuration
```bash
# Debug build
./gradlew assembleDebug

# Install on connected device
./gradlew installDebug

# Run tests
./gradlew test
```

## Project Configuration

### Dropbox Integration
1. Create a Dropbox app at [Dropbox App Console](https://www.dropbox.com/developers/apps)
2. Add your API keys to `local.properties`:
   ```
   dropbox.app.key=your_app_key_here
   dropbox.app.secret=your_app_secret_here
   ```

### Build Variants
- **Debug**: Development builds with debugging enabled
- **Release**: Production builds with optimization

## Development Guidelines

### Code Style
- Follow [Android Kotlin Style Guide](https://developer.android.com/kotlin/style-guide)
- Use consistent formatting (Android Studio default)
- Write descriptive commit messages

### Module Structure
- Keep modules focused on single responsibility
- Avoid circular dependencies between modules
- Use dependency injection for cross-module communication

### Testing
- Write unit tests for business logic in `lib/` modules
- Write UI tests for `ui/` modules
- Aim for good test coverage of core functionality

### Git Workflow
1. Create feature branch from `main`
2. Make changes in focused commits
3. Test thoroughly before pushing
4. Create pull request with clear description

## Common Tasks

### Adding New Module
```bash
# Create module directory
mkdir modules/lib/newmodule

# Add to settings.gradle.kts
include(":modules:lib:newmodule")

# Create module build.gradle.kts
```

### Running Specific Tests
```bash
# Run tests for specific module
./gradlew :modules:lib:auth:test

# Run all tests
./gradlew test
```

### Debugging
- Use Android Studio debugger
- Check Logcat for runtime logs
- Use Layout Inspector for UI issues

## Getting Help

- Check existing documentation in `docs/`
- Review module-specific READMEs
- Look at similar implementations in other modules
- Create GitHub issue for bugs or questions