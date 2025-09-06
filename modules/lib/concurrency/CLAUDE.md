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
- **Provides**: Binds RateLimiterImpl to RateLimiter interface

## Usage Example

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

- `kotlinx.coroutines`: For coroutine support and concurrency primitives
- `dagger.hilt`: For dependency injection
- `javax.inject`: For injection annotations
- `kotlin.time`: For precise timing operations

## Thread Safety

The implementation is fully thread-safe using coroutine-safe synchronization primitives. All operations properly handle concurrent access to shared state.