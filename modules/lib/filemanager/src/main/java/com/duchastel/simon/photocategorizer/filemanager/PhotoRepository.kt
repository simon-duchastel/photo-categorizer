package com.duchastel.simon.photocategorizer.filemanager

/**
 * Repository interface for photo management operations.
 */
interface PhotoRepository {
    suspend fun getPhotos(path: String): List<Photo>
    suspend fun getUnauthenticatedLinkForPhoto(path: String): String

    suspend fun movePhoto(originalPath: String, newPath: String)
}

val SUPPORTED_FILE_EXTENSIONS: List<String> = listOf(".png", ".jpg")

/**
 * Represents a photo with its metadata.
 */
data class Photo(
    val name: String,
    val uploadDate: String,
    val id: String,
    val path: String,
)
