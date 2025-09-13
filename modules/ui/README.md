# UI Modules

User interface modules containing screens, reusable components, and design system for the Photo Categorizer app.

## Module Overview  

```
ui/
├── components/                     # Reusable UI components and widgets
├── screens/                        # Feature-specific screen implementations
│   ├── login/                      # Authentication screens
│   ├── photoswiper/                # Main photo categorization interface
│   ├── settings/                   # App configuration screens  
│   └── splash/                     # Application splash screen
└── theme/                          # Design system and app theming
```

## Module Descriptions

### Components (`components/`)
Reusable UI components used across multiple screens:
- **`CenteredLoadingState`** - Centered loading indicator with descriptive message text
- **`HorizontalSwiper`** - Swipeable photo viewer component
- **`OneWayVerticalSwiper`** - Vertical swipe component  
- **`TextInputModal`** - Generic modal dialog with text input field and customizable text
- **`TitledCard`** - Generic card component with title and customizable content slot
- **`Shimmer`** - Loading shimmer effect
- **`SkeletonLoader`** - Skeleton loading animations
- **`SwipeDirection`** - Swipe direction enumeration

### Screens (`screens/`)
Feature-specific screen implementations:

#### Login (`login/`)
- User authentication and sign-in screens
- OAuth flow handling for cloud storage providers

#### Photo Swiper (`photoswiper/`)  
- Main photo categorization interface with swipe gestures
- **Key Components**: `PhotoSwiperScreen`, `PhotoSwiperViewModel`
- Core feature for viewing and categorizing photos

#### Settings (`settings/`)
- App configuration and preferences screens
- User account management
- Cloud storage settings

#### Splash (`splash/`)
- Application startup screen
- Initial loading and authentication check

### Theme (`theme/`)
- App-wide design system and theming
- Color palettes, typography, and component styles
- Material Design theme customizations

## Architecture

UI modules follow MVVM (Model-View-ViewModel) pattern:
- **Screens** contain Composable UI functions
- **ViewModels** manage UI state and business logic
- **Components** provide reusable UI elements
- **Theme** ensures consistent visual design

## Dependencies

UI modules depend on library modules for business logic:
- `screens/*` → `lib/*` (for data and business logic)
- `screens/*` → `components/`, `theme/` (for UI consistency)
- `components/` → `theme/` (for consistent styling)

## Usage

These UI modules provide the complete user interface for the Photo Categorizer app, from individual reusable components to full-screen implementations with integrated business logic.