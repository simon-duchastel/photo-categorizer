# Time Module

This module provides a `Clock` instance for the application, following the dependency injection pattern. It also provides a `TestClock` for use in tests.

## Usage

### Application Code

To use the clock in your application, inject it into your classes:

```kotlin
import kotlin.time.Clock
import javax.inject.Inject

class MyClass @Inject constructor(private val clock: Clock) {
    fun doSomething() {
        val now = clock.now()
        // ...
    }
}
```

The `TimeModule` will provide the system clock (`Clock.System`).

### Test Code

In your tests, you can use the `TestClock` to control the time:

```kotlin
import com.duchastel.simon.photocategorizer.time.testing.TestClock
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.runTest
import kotlin.time.Duration.Companion.seconds

@Test
fun myTest() = runTest {
    val scheduler = TestCoroutineScheduler()
    val clock = TestClock(scheduler)

    val now = clock.now()
    scheduler.advanceTimeBy(10.seconds.inWholeMilliseconds)
    val later = clock.now()

    // ...
}
```

## Dependencies

*   [kotlin.time.Clock](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.time/-clock/)
*   [Hilt](https://dagger.dev/hilt/)
