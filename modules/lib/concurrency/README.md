# Concurrency Module

Provides concurrency utilities for scheduling asynchronous work with rate limiting and queue management.

## Purpose

The concurrency module contains utilities for managing asynchronous work execution, specifically designed to handle rate limiting scenarios where work items need to be executed with specific timing constraints.

## Components

### RateLimiter Interface
- **Purpose**: Defines contract for scheduling work with rate limiting
- **Key Method**: `suspend fun <T> scheduleWork(work: suspend () -> T): T`
- **Behavior**: 
  - Executes work immediately if no other work is scheduled
  - Queues work items when other work is in progress
  - Applies rate limiting between work completions

### RateLimiterImpl Class
- **Purpose**: Concrete implementation of RateLimiter with proper rate limiting
- **Rate Limiting**: Ensures at most 1 operation per second in a sliding window
- **Queue Management**: FIFO queue with proper coroutine cancellation support
- **Thread Safety**: Uses coroutine-safe mutex for state management
- **Dependency Injection**: Configured as a singleton through Hilt

### ConcurrencyModule
- **Purpose**: Hilt dependency injection module
- **Provides**: 
  - Binds RateLimiterImpl to RateLimiter interface
  - Provides IoDispatcher-qualified Dispatchers.IO
  - Provides MainDispatcher-qualified Dispatchers.Main

### Dispatcher Qualifiers
- **IoDispatcher**: Qualifier annotation for injecting IO dispatcher
- **MainDispatcher**: Qualifier annotation for injecting Main dispatcher
- **Purpose**: Enables dependency injection of specific coroutine dispatchers
- **Usage**: Use `@IoDispatcher` or `@MainDispatcher` annotations when injecting CoroutineDispatcher

## Usage Examples

### Rate Limiter Usage
```kotlin
@Inject
private lateinit var rateLimiter: RateLimiter

suspend fun performRateLimitedOperation() {
    val result = rateLimiter.scheduleWork {
        // Your work that needs rate limiting
        apiCall()
    }
}
```

### Dispatcher Injection Usage
```kotlin
class MyRepository @Inject constructor(
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    @MainDispatcher private val mainDispatcher: CoroutineDispatcher
) {
    suspend fun performBackgroundWork() {
        withContext(ioDispatcher) {
            // IO-bound operations
            fileOperations()
        }
        
        withContext(mainDispatcher) {
            // UI updates
            updateUI()
        }
    }
}
```

## Rate Limiting Behavior

- **Immediate execution**: When no work is queued, work executes immediately
- **Sequential processing**: Work items are processed in FIFO order
- **Rate limiting**: Ensures at most 1 completion per second
- **Sliding window**: Uses actual completion times, not just delays between starts
- **Proper cancellation**: Supports coroutine cancellation throughout the queue

## Testing

The module includes comprehensive tests covering:
- Immediate execution scenarios
- Queue management and sequential processing
- Rate limiting enforcement
- Exception handling and propagation
- Cancellation behavior
- Different return types support

## Dependencies

- `kotlinx.coroutines`: For coroutine support, concurrency primitives, and dispatchers
- `dagger.hilt`: For dependency injection
- `kotlin.time`: For precise timing operations

## Thread Safety

The implementation is fully thread-safe using coroutine-safe synchronization primitives. All operations properly handle concurrent access to shared state.
