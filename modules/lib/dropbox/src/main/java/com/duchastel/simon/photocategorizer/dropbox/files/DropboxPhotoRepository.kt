package com.duchastel.simon.photocategorizer.dropbox.files

import com.duchastel.simon.photocategorizer.dropbox.network.DropboxFileApi
import com.duchastel.simon.photocategorizer.dropbox.network.FileTag
import com.duchastel.simon.photocategorizer.dropbox.network.ListFolderContinueRequest
import com.duchastel.simon.photocategorizer.dropbox.network.ListFolderRequest
import com.duchastel.simon.photocategorizer.dropbox.network.MoveFileRequest
import com.duchastel.simon.photocategorizer.dropbox.network.TemporaryLinkRequest
import com.duchastel.simon.photocategorizer.filemanager.PhotoRepository
import com.duchastel.simon.photocategorizer.filemanager.Photo
import com.duchastel.simon.photocategorizer.filemanager.SUPPORTED_FILE_EXTENSIONS
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.util.concurrent.atomic.AtomicLong
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Clock
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@Singleton
internal class DropboxPhotoRepository @Inject constructor(
    private val networkApi: DropboxFileApi,
): PhotoRepository {

    private data class MovePhotoRequest(
        val originalPath: String,
        val newPath: String,
        val completion: CompletableDeferred<Unit>
    )

    private class RateLimiter(maxOperationsPerSecond: Int) {
        private val intervalMs = 1.seconds / maxOperationsPerSecond
        private val lastExecutionTime = AtomicLong(0)

        @OptIn(ExperimentalTime::class)
        suspend fun acquire() {
            val currentTime = Clock.System.now()
            val lastTime = Instant.fromEpochMilliseconds(lastExecutionTime.get())
            val timeSinceLastExecution = currentTime - lastTime
            
            if (timeSinceLastExecution < intervalMs) {
                val delayTime = intervalMs - timeSinceLastExecution
                delay(delayTime)
            }

            lastExecutionTime.set(Clock.System.now().toEpochMilliseconds())
        }
    }

    companion object {
        private const val MAX_OPERATIONS_PER_SECOND = 1
        private const val BATCH_THRESHOLD = 3
        private const val MAX_RETRY_ATTEMPTS = 3
        private const val RETRY_DELAY_MS = 2000L
    }

    private val moveQueue = Channel<MovePhotoRequest>(capacity = Channel.UNLIMITED)
    private val rateLimiter = RateLimiter(MAX_OPERATIONS_PER_SECOND)
    private val queueProcessorScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    init {
        queueProcessorScope.launch {
            processQueueContinuously()
        }
    }

    override suspend fun getPhotos(path: String): List<Photo> {
        if (!path.startsWith("/")) {
            throw IllegalArgumentException("Path must start with '/'")
        }

        val photos = mutableListOf<Photo>()
        var cursor: String? = null

        do {
            val response = if (cursor == null) {
                networkApi.listFolder(
                    ListFolderRequest(
                        path = "/camera test/camera roll",
                    )
                )
            } else {
                networkApi.listFolderContinue(ListFolderContinueRequest(cursor))
            }

            photos += response.entries
                .filter { it.tag == FileTag.FILE && it.pathLower != null && it.id != null }
                .map {
                    Photo(
                        name = it.name,
                        id = it.id!!,
                        path = it.pathLower!!,
                        uploadDate = it.clientModified,
                    )
                }
                .filter { photo ->
                    // filter for photos that match one of the supported extensions
                    SUPPORTED_FILE_EXTENSIONS.any { photo.path.endsWith(it) }
                }

            cursor = response.cursor
        } while (response.hasMore)

        // make sure the photos are most-recent to least-recent before returning
        return photos.sortedByDescending { it.uploadDate }
    }

    override suspend fun getUnauthenticatedLinkForPhoto(path: String): String {
        if (!path.startsWith("/")) {
            throw IllegalArgumentException("Path must start with '/'")
        }

        return networkApi.getUnauthenticatedLink(TemporaryLinkRequest(path)).link
    }

    override suspend fun movePhoto(originalPath: String, newPath: String) {
        if (!originalPath.startsWith("/") || !newPath.startsWith("/")) {
            throw IllegalArgumentException("Path must start with '/'")
        }

        val completionDeferred = CompletableDeferred<Unit>()
        val request = MovePhotoRequest(originalPath, newPath, completionDeferred)
        
        moveQueue.trySend(request)
        
        // Suspend until the operation is actually completed
        completionDeferred.await()
    }

    private suspend fun processQueueContinuously() {
        val batchBuffer = mutableListOf<MovePhotoRequest>()

        for (request in moveQueue) {
            batchBuffer.add(request)

            if (batchBuffer.size >= BATCH_THRESHOLD) {
                processBatch(batchBuffer.toList())
                batchBuffer.clear()
            } else {
                val singleRequest = batchBuffer.removeAt(0)
                processSingleRequest(singleRequest)

                while (batchBuffer.isNotEmpty()) {
                    val bufferedRequest = batchBuffer.removeAt(0)
                    processSingleRequest(bufferedRequest)
                }
            }
        }
    }

    private suspend fun processSingleRequest(request: MovePhotoRequest) {
        try {
            rateLimiter.acquire()
            executeActualMove(request.originalPath, request.newPath)
            request.completion.complete(Unit)
        } catch (e: Exception) {
            if (shouldRetry(e)) {
                retryWithBackoff(request)
            } else {
                request.completion.completeExceptionally(e)
            }
        }
    }

    private suspend fun processBatch(requests: List<MovePhotoRequest>) {
        // For batch processing, we process them sequentially with rate limiting
        // This can be optimized later with actual batch API calls if available
        for (request in requests) {
            processSingleRequest(request)
        }
    }

    private suspend fun executeActualMove(originalPath: String, newPath: String) {
        val response = networkApi.moveFile(MoveFileRequest(from = originalPath, to = newPath))
        if (response.error != null) {
            val errorMessage = buildString {
                append(response.error)
                if (response.errorSummary != null) {
                    append(": ${response.errorSummary}")
                }
            }
            throw IllegalArgumentException(errorMessage)
        }
    }

    private fun shouldRetry(exception: Exception): Boolean {
        return when (exception) {
            is HttpException -> exception.code() == 429
            else -> false
        }
    }

    private suspend fun retryWithBackoff(request: MovePhotoRequest, attempt: Int = 1) {
        if (attempt > MAX_RETRY_ATTEMPTS) {
            request.completion.completeExceptionally(
                Exception("Max retry attempts reached for moving ${request.originalPath}")
            )
            return
        }

        try {
            delay((RETRY_DELAY_MS * attempt).milliseconds)
            rateLimiter.acquire()
            executeActualMove(request.originalPath, request.newPath)
            request.completion.complete(Unit)
        } catch (e: Exception) {
            if (shouldRetry(e)) {
                retryWithBackoff(request, attempt + 1)
            } else {
                request.completion.completeExceptionally(e)
            }
        }
    }
}
