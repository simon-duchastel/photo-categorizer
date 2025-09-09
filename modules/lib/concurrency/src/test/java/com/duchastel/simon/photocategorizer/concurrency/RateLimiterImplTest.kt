package com.duchastel.simon.photocategorizer.concurrency

import com.duchastel.simon.photocategorizer.time.testing.TestClock
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime

@ExperimentalTime
class RateLimiterImplTest {

    @Test
    fun scheduleWorkShouldExecuteImmediatelyWhenNoOtherWorkIsScheduled() = runTest {
        val rateLimiter = RateLimiterImpl(TestClock(testScheduler))
        var executed = false

        val result = rateLimiter.withRateLimit {
            executed = true
            "result"
        }

        assertTrue(executed)
        assertEquals("result", result)
    }

    @Test
    fun scheduleWorkShouldReturnWorkResult() = runTest {
        val rateLimiter = RateLimiterImpl(TestClock(testScheduler))
        val result = rateLimiter.withRateLimit {
            42
        }
        
        assertEquals(42, result)
    }

    @Test
    fun scheduleWorkShouldPropagateExceptions() = runTest {
        val rateLimiter = RateLimiterImpl(TestClock(testScheduler))
        assertFailsWith<RuntimeException> {
            rateLimiter.withRateLimit {
                throw RuntimeException("Test exception")
            }
        }
    }

    @Test
    fun concurrentScheduleWorkCallsShouldBeProcessedSequentially() = runTest {
        val rateLimiter = RateLimiterImpl(TestClock(testScheduler))
        val executionOrder = mutableListOf<Int>()

        val job1 = async {
            rateLimiter.withRateLimit {
                delay(10) // Simulate some work
                executionOrder.add(1)
                1
            }
        }
        
        val job2 = async {
            rateLimiter.withRateLimit {
                delay(10) // Simulate some work
                executionOrder.add(2)
                2
            }
        }
        
        val job3 = async {
            rateLimiter.withRateLimit {
                delay(10) // Simulate some work
                executionOrder.add(3)
                3
            }
        }

        val results = listOf(job1.await(), job2.await(), job3.await())
        
        // All work should complete successfully
        assertEquals(listOf(1, 2, 3), results.sorted())
        
        // Work should be executed in order (sequentially)
        assertEquals(listOf(1, 2, 3), executionOrder)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun rateLimitingEnforcesMaxOperationsPerWindow() = runTest {
        val rateLimiter = RateLimiterImpl(TestClock(testScheduler))
        val executionOrder = mutableListOf<String>()

        val job1 = launch {
            rateLimiter.withRateLimit {
                executionOrder.add("work1-start")
                delay(50) // Simulate work
                executionOrder.add("work1-end")
                1
            }
        }

        val job2 = launch {
            rateLimiter.withRateLimit {
                executionOrder.add("work2-start")
                delay(50) // Simulate work
                executionOrder.add("work2-end")
                2
            }
        }

        val job3 = launch {
            rateLimiter.withRateLimit {
                executionOrder.add("work3-start")
                delay(50) // Simulate work
                executionOrder.add("work3-end")
                3
            }
        }

        // Verify only one tasks started and then completed within the first window (< 1 second)
        testScheduler.runCurrent()
        assertEquals(listOf("work1-start"), executionOrder)
        assertTrue(job1.isActive)
        assertTrue(job2.isActive)
        assertTrue(job3.isActive)

        testScheduler.advanceTimeBy(50.milliseconds)
        testScheduler.runCurrent()
        assertEquals(listOf("work1-start", "work1-end"), executionOrder)
        assertTrue(job1.isCompleted)
        assertTrue(job2.isActive)
        assertTrue(job3.isActive)

        // Verify second task started once we enter the next 1 second window
        testScheduler.advanceTimeBy(1.seconds)
        testScheduler.runCurrent()
        assertContains(executionOrder, "work2-start")
        assertTrue(job2.isActive)
        assertTrue(job3.isActive)

        // Verify second task complete
        testScheduler.advanceTimeBy(50.milliseconds)
        testScheduler.runCurrent()
        assertEquals(
            expected = listOf(
                "work1-start",
                "work1-end",
                "work2-start",
                "work2-end",
            ),
            actual = executionOrder,
        )
        assertTrue(job2.isCompleted)
        assertTrue(job3.isActive)

        // Verify third task started and completed in third window
        testScheduler.advanceTimeBy(1.seconds + 50.milliseconds)
        testScheduler.runCurrent()
        assertEquals(
            expected = listOf(
                "work1-start",
                "work1-end",
                "work2-start",
                "work2-end",
                "work3-start",
                "work3-end",
            ),
            actual = executionOrder,
        )
        assertTrue(job3.isCompleted)
    }

    @Test
    fun failedWorkShouldNotAffectSubsequentWork() = runTest {
        val rateLimiter = RateLimiterImpl(TestClock(testScheduler))
        val results = mutableListOf<String>()
        
        // First work item succeeds
        val job1 = async {
            rateLimiter.withRateLimit {
                results.add("work1")
                "success1"
            }
        }
        
        // Second work item fails
        val job2 = async {
            try {
                rateLimiter.withRateLimit {
                    results.add("work2")
                    throw RuntimeException("Test failure")
                }
            } catch (_: Exception) {
                "failed2"
            }
        }
        
        // Third work item succeeds
        val job3 = async {
            rateLimiter.withRateLimit {
                results.add("work3")
                "success3"
            }
        }

        val finalResults = listOf(job1.await(), job2.await(), job3.await())
        
        // First and third should succeed, second should fail
        assertEquals(listOf("success1", "failed2", "success3"), finalResults)
        assertEquals(listOf("work1", "work2", "work3"), results)
    }
}