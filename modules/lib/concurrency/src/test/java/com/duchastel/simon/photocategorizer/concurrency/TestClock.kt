package com.duchastel.simon.photocategorizer.concurrency

import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
class TestClock(
    private val scheduler: TestCoroutineScheduler
) : Clock {
    override fun now(): Instant {
        return Instant.fromEpochMilliseconds(scheduler.currentTime)
    }
}