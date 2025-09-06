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
 * Implementation of BufferedScheduler that ensures rate limiting with cooperative scheduling.
 * 
 * This implementation ensures that at most [MAX_OPERATIONS_PER_SECOND] tasks complete in any 1-second window.
 * Each work item coordinates with others through shared state, without needing a background processor.
 */
@OptIn(ExperimentalTime::class)
class BufferedSchedulerImpl @Inject internal constructor() : BufferedScheduler {

    companion object {
        private const val MAX_OPERATIONS_PER_SECOND = 1
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

    override suspend fun <T> scheduleWork(work: suspend () -> T): T {
        val completion = CompletableDeferred<T>()
        val workItem = WorkItem(work, completion)
        
        val isFirst = mutex.withLock {
            val wasEmpty = workQueue.isEmpty()
            workQueue.add(workItem)
            
            if (wasEmpty) {
                // First item can execute immediately
                workItem.readyToExecute.complete(Unit)
            }
            wasEmpty
        }
        
        // Wait for our turn
        workItem.readyToExecute.await()
        
        try {
            // Apply rate limiting if not first
            if (!isFirst) {
                enforceRateLimit()
            }
            
            // Execute work
            val result = work()
            recordCompletion()
            
            return result
        } finally {
            // Always signal next work item, even on failure/cancellation
            signalNextWorkItem()
        }
    }
    
    private suspend fun signalNextWorkItem() {
        mutex.withLock {
            // Remove ourselves from the queue (we're done)
            if (workQueue.isNotEmpty()) {
                workQueue.removeAt(0) // Remove the item that just completed
            }
            
            // Signal the next item to start (if any)
            if (workQueue.isNotEmpty()) {
                val nextItem = workQueue[0]
                nextItem.readyToExecute.complete(Unit)
            }
        }
    }
    
    private suspend fun enforceRateLimit() {
        val delayTime = mutex.withLock {
            // Clean up old completion times outside the window
            val now = Clock.System.now()
            completionTimes.removeAll { it < now - RATE_LIMIT_WINDOW }

            // If we're at the rate limit, calculate how long to wait
            if (completionTimes.size >= MAX_OPERATIONS_PER_SECOND) {
                val oldestInWindow = completionTimes.minOrNull()
                if (oldestInWindow != null) {
                    val timeUntilCanProceed = (oldestInWindow + RATE_LIMIT_WINDOW) - now
                    if (timeUntilCanProceed.isPositive()) {
                        timeUntilCanProceed
                    } else null
                } else null
            } else null
        }
        
        // Apply delay outside of mutex to avoid blocking other operations
        delayTime?.let { delay(it) }
    }
    
    private suspend fun recordCompletion() {
        val now = Clock.System.now()
        mutex.withLock {
            completionTimes.add(now)
            // Clean up old times to prevent memory leaks
            completionTimes.removeAll { it < now - RATE_LIMIT_WINDOW }
        }
    }
}