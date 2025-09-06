# Auth Module

Handles authentication logic for the Photo Categorizer app.

## Components
- `AuthRepository` - Core authentication repository interface
- `AccessTokenAuthInterceptor` - HTTP interceptor for adding auth tokens
- `LoggedOutInterceptor` - Handles logged out state transitions

## Purpose
Provides a centralized authentication system that can be used by different auth providers (e.g., Dropbox).