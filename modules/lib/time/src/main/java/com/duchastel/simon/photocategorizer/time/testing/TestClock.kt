package com.duchastel.simon.photocategorizer.time.testing

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class, ExperimentalCoroutinesApi::class)
class TestClock(
    private val scheduler: TestCoroutineScheduler
) : Clock {
    override fun now(): Instant {
        return Instant.fromEpochMilliseconds(scheduler.currentTime)
    }
}