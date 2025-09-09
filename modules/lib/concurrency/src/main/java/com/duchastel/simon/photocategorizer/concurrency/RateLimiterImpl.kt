package com.duchastel.simon.photocategorizer.concurrency

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.delay
import javax.inject.Inject
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * Implementation of RateLimiter that ensures rate limiting with cooperative scheduling.
 * 
 * This implementation ensures that at most [MAX_OPERATIONS_PER_WINDOW] tasks complete in any
 * [RATE_LIMIT_WINDOW] seconds window.
 */
@OptIn(ExperimentalTime::class)
class RateLimiterImpl @Inject internal constructor(
    private val clock: Clock = Clock.System,
) : RateLimiter {

    companion object {
        private const val MAX_OPERATIONS_PER_WINDOW = 1
        private val RATE_LIMIT_WINDOW = 1.seconds
    }

    private data class WorkItem<T>(
        val work: suspend () -> T,
        val completion: CompletableDeferred<T>,
        val readyToExecute: CompletableDeferred<Unit> = CompletableDeferred()
    )

    private val mutex = Mutex()
    private val workQueue = mutableListOf<WorkItem<*>>()
    private val completionTimes = mutableListOf<Instant>()

    override suspend fun <T> withRateLimit(work: suspend () -> T): T {
        val completion = CompletableDeferred<T>()
        val workItem = WorkItem(work, completion)

        mutex.withLock {
            workQueue.add(workItem)

            if (workQueue.size == 1) {
                workItem.readyToExecute.complete(Unit)
            }
        }

        workItem.readyToExecute.await()

        try {
            enforceRateLimit()

            val result = work()
            recordCompletion()

            return result
        } finally {
            signalNextWorkItem()
        }
    }
    
    private suspend fun signalNextWorkItem() {
        mutex.withLock {
            if (workQueue.isNotEmpty()) {
                workQueue.removeAt(0)
            }

            if (workQueue.isNotEmpty()) {
                val nextItem = workQueue[0]
                nextItem.readyToExecute.complete(Unit)
            }
        }
    }

    private suspend fun enforceRateLimit() {
        val delayTime = mutex.withLock {
            val now = clock.now()
            completionTimes.apply {
                removeAll { it < now - RATE_LIMIT_WINDOW }
                sort()
            }

            // If we're at the rate limit, calculate how long to wait
            if (completionTimes.size >= MAX_OPERATIONS_PER_WINDOW) {
                // since the list is sorted, we know element #MAX_OPERATIONS_PER_WINDOW
                // is the oldest current time allowed in the new window
                //
                // ex. if the window size is 1s, the max operations per window is 3, and the
                // current operations are [0.5, 0.7, 0.9] then the operations at 0.7s and 0.9s are
                // allowed in the new window and the one at 0.5s isn't - leaving 2 existing
                // operations plus the next operation being scheduled in the new window for 3 total
                // operations.
                //
                // In this example, we need to wait 1s past the 0.5s operation to get to the point
                // where only the 0.7s and 0.9s operations remain, meaning we need to get to at
                // least time 1.5s before starting any new operations
                val allowedCarryoverOperations = MAX_OPERATIONS_PER_WINDOW - 1 // leave one for the new operation
                val lastAllowedOperationIndex = completionTimes.lastIndex - allowedCarryoverOperations
                val oldestAllowedInNewWindow = completionTimes[lastAllowedOperationIndex]
                val timeUntilCanProceed = (oldestAllowedInNewWindow + RATE_LIMIT_WINDOW) - now
                if (timeUntilCanProceed.isPositive()) {
                    timeUntilCanProceed
                } else null
            } else null
        }

        delayTime?.let { delay(it) }
    }

    private suspend fun recordCompletion() {
        val now = clock.now()
        mutex.withLock {
            completionTimes.apply {
                add(now)
                removeAll { it < now - RATE_LIMIT_WINDOW }
            }
        }
    }
}