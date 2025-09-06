package com.duchastel.simon.photocategorizer.dropbox.network

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Request model for Dropbox folder listing API.
 */
@JsonClass(generateAdapter = true)
data class ListFolderRequest(
    @Json(name = "path") val path: String,
    @Json(name = "limit") val limit: Int? = 2000
)

/**
 * Request model for continuing paginated folder listing.
 */
@JsonClass(generateAdapter = true)
data class ListFolderContinueRequest(
    @Json(name = "cursor") val cursor: String
)

/**
 * Response model for Dropbox folder listing API.
 */
@JsonClass(generateAdapter = true)
data class ListFolderResponse(
    @Json(name = "entries") val entries: List<FileMetadata>,
    @Json(name = "cursor") val cursor: String?,
    @Json(name = "has_more") val hasMore: Boolean
)

/**
 * Metadata for a file or folder in Dropbox.
 */
@JsonClass(generateAdapter = true)
data class FileMetadata(
    @Json(name = "name") val name: String,
    @Json(name = "id") val id: String?,
    @Json(name = "path_lower") val pathLower: String?,
    @Json(name = "client_modified") val clientModified: String,
    @Json(name = ".tag") val tag: FileTag,
)

enum class FileTag {
    @Json(name = "file") FILE,
    @Json(name = "folder") FOLDER,
    @Json(name = "deleted") DELETED,
}

/**
 * Request model for generating temporary file links.
 */
@JsonClass(generateAdapter = true)
data class TemporaryLinkRequest(
    @Json(name = "path") val path: String,
)

/**
 * Response model containing temporary file link.
 */
@JsonClass(generateAdapter = true)
data class TemporaryLinkResponse(
    @Json(name = "link") val link: String,
)

/**
 * Request model for moving files in Dropbox.
 */
@JsonClass(generateAdapter = true)
data class MoveFileRequest(
    @Json(name = "from_path") val from: String,
    @Json(name = "to_path") val to: String,
)

/**
 * Response model for file move operations.
 */
@JsonClass(generateAdapter = true)
data class MoveFileResponse(
    @Json(name = "error") val error: DropboxApiError?,
    @Json(name = "error_summary") val errorSummary: String?,
)

/**
 * Error information from Dropbox API responses.
 */
@JsonClass(generateAdapter = true)
data class DropboxApiError(
    @Json(name = ".tag") val tag: String,
)