package com.duchastel.simon.photocategorizer.dropbox.files

import com.duchastel.simon.photocategorizer.concurrency.RateLimiter
import com.duchastel.simon.photocategorizer.dropbox.network.DropboxFileApi
import com.duchastel.simon.photocategorizer.dropbox.network.FileTag
import com.duchastel.simon.photocategorizer.dropbox.network.ListFolderContinueRequest
import com.duchastel.simon.photocategorizer.dropbox.network.ListFolderRequest
import com.duchastel.simon.photocategorizer.dropbox.network.MoveFileRequest
import com.duchastel.simon.photocategorizer.dropbox.network.TemporaryLinkRequest
import com.duchastel.simon.photocategorizer.filemanager.PhotoRepository
import com.duchastel.simon.photocategorizer.filemanager.Photo
import com.duchastel.simon.photocategorizer.filemanager.SUPPORTED_FILE_EXTENSIONS
import kotlinx.coroutines.delay
import retrofit2.HttpException
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration.Companion.milliseconds

@Singleton
internal class DropboxPhotoRepository @Inject constructor(
    private val networkApi: DropboxFileApi,
    private val rateLimiter: RateLimiter,
): PhotoRepository {

    companion object {
        private const val MAX_RETRY_ATTEMPTS = 3
        private const val RETRY_DELAY_MS = 2000L
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

        rateLimiter.withRateLimit {
            movePhotoWithRetry(originalPath, newPath)
        }
    }

    private suspend fun movePhotoWithRetry(originalPath: String, newPath: String, attempt: Int = 1) {
        try {
            executeActualMove(originalPath, newPath)
        } catch (e: Exception) {
            if (shouldRetry(e) && attempt <= MAX_RETRY_ATTEMPTS) {
                delay((RETRY_DELAY_MS * attempt).milliseconds)
                movePhotoWithRetry(originalPath, newPath, attempt + 1)
            } else {
                throw e
            }
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
}