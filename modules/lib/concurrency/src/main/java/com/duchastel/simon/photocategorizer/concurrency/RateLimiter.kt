package com.duchastel.simon.photocategorizer.concurrency

/**
 * A rate limiter that queues work items and executes them with rate limiting to ensure
 * that at most Y tasks are completed in X time window.
 * 
 * When no work is scheduled, the first work item is executed immediately.
 * When work is already scheduled, new work items are queued and executed sequentially
 * with proper rate limiting between completions.
 */
interface RateLimiter {
    
    /**
     * Schedules work to be executed with rate limiting.
     * 
     * If no other work is currently scheduled, the work is executed immediately.
     * If work is already scheduled, this work is queued and will be executed
     * after the previous work completes, with proper rate limiting delays.
     * 
     * @param work The work to be executed
     * @return The result of the work execution
     * @throws Exception if the work throws an exception or if the coroutine is cancelled
     */
    suspend fun <T> withRateLimit(work: suspend () -> T): T
}