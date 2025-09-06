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
 * Each work item coordinates with others through shared state, without needing a background
 * processor.
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

        val isFirst = mutex.withLock {
            val wasEmpty = workQueue.isEmpty()
            workQueue.add(workItem)
            
            if (wasEmpty) {
                workItem.readyToExecute.complete(Unit)
            }
            wasEmpty
        }

        workItem.readyToExecute.await()

        try {
            if (!isFirst) {
                enforceRateLimit()
            }

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
            completionTimes.removeAll { it < now - RATE_LIMIT_WINDOW }

            // If we're at the rate limit, calculate how long to wait
            if (completionTimes.size >= MAX_OPERATIONS_PER_WINDOW) {
                val oldestInWindow = completionTimes.minOrNull()
                if (oldestInWindow != null) {
                    val timeUntilCanProceed = (oldestInWindow + RATE_LIMIT_WINDOW) - now
                    if (timeUntilCanProceed.isPositive()) {
                        timeUntilCanProceed
                    } else null
                } else null
            } else null
        }

        delayTime?.let { delay(it) }
    }
    
    private suspend fun recordCompletion() {
        val now = clock.now()
        mutex.withLock {
            completionTimes.add(now)
            completionTimes.removeAll { it < now - RATE_LIMIT_WINDOW }
        }
    }
}