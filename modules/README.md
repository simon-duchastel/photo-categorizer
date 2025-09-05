# Modules

This directory contains all application modules organized by architectural layer.

## Module Structure

```
modules/
├── app/                            # Application entry point and configuration
├── lib/                            # Core business logic and data management
└── ui/                             # User interface components and screens
```

## Module Types

### Application (`app/`)
- Application entry point and initialization
- Dependency injection configuration
- Application-level settings

### Libraries (`lib/`)
Core functionality modules that provide business logic and data management:
- Authentication systems
- Cloud storage integrations  
- File management abstractions
- Navigation utilities
- Local storage and caching
- Shared utility functions

### UI (`ui/`)
User interface modules containing screens and reusable components:
- Screen implementations for specific features
- Reusable UI components
- Design system and theming

## Navigation

- **[App Module](app/README.md)** - Application entry point documentation
- **[Library Modules](lib/README.md)** - Core functionality modules
- **[UI Modules](ui/README.md)** - User interface modules

## Architecture

Modules follow clean architecture principles:
- Dependencies flow inward (UI → Business Logic → Data)
- Each module has a single, well-defined responsibility
- Modules communicate through well-defined interfaces
- Cross-cutting concerns are handled by shared library modules