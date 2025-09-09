# Photo Categorizer

An Android app for organizing photos through intuitive swipe gestures to categorize them into folders.

**MANDATORY READING** - You MUST read [**How to Contribute**](docs/development-setup.md) before contributing.

## Project Structure

```
photo-categorizer/
├── docs/                           # Comprehensive project documentation
└── modules/
    ├── app/                        # Main application module
    ├── lib/                        # Library modules for core functionality  
    │   ├── auth/                   # Authentication system
    │   ├── concurrency/            # Rate limiting and concurrency utilities
    │   ├── dropbox/                # Dropbox integration
    │   ├── filemanager/            # Abstract file management interface
    │   ├── navigation/             # Navigation utilities
    │   ├── storage/                # Local storage management
    │   ├── time/                   # Clock and time utilities, including DI for System Clock
    │   └── utils/                  # Shared utility functions
    └── ui/                         # User interface modules
        ├── components/             # Reusable UI components
        ├── screens/                # Application screens
        │   ├── login/              # Authentication screens
        │   ├── photoswiper/        # Main photo categorization interface
        │   ├── settings/           # App configuration screens
        │   └── splash/             # Splash screen
        └── theme/                  # UI theming and design system
```

## Features

- **Swipe-based categorization**: Intuitive swipe gestures to organize photos
- **Cloud integration**: Dropbox support for photo storage and synchronization
- **Modular architecture**: Clean separation of concerns across feature modules
- **Android modern stack**: Built with Kotlin, Jetpack Compose, and Android best practices

## Getting Started

### Prerequisites
- Android Studio Jellyfish | 2023.3.1 or later
- JDK 17+
- Android SDK with minimum API level 24+
- Kotlin 1.9+

### Quick Start
```bash
git clone <repository-url>
cd photo-categorizer
./gradlew assembleDebug
```

Install on device:
```bash
./gradlew installDebug
```

## Architecture

This project follows a multi-module clean architecture pattern:

- **app/**: Application entry point and dependency injection setup
- **lib/**: Core business logic and data management modules
- **ui/**: User interface components and screens

Each module contains its own README.md with specific implementation details.

## Contributing

See [docs/development-setup.md](docs/development-setup.md) for development environment setup and contribution guidelines.

## Documentation

- [Architecture Overview](docs/architecture.md) - Detailed technical architecture
- [Development Setup](docs/development-setup.md) - Environment setup and contribution guide
- [API Integration](docs/api-integration.md) - Cloud storage integration details

## License

This project is licensed under the MIT License.
