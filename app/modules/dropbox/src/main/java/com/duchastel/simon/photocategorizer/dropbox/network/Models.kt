package com.duchastel.simon.photocategorizer.dropbox.network

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ListFolderRequest(
    @Json(name = "path") val path: String,
    @Json(name = "limit") val limit: Int? = 2000
)

@JsonClass(generateAdapter = true)
data class ListFolderContinueRequest(
    @Json(name = "cursor") val cursor: String
)

@JsonClass(generateAdapter = true)
data class ListFolderResponse(
    @Json(name = "entries") val entries: List<FileMetadata>,
    @Json(name = "cursor") val cursor: String?,
    @Json(name = "has_more") val hasMore: Boolean
)

@JsonClass(generateAdapter = true)
data class FileMetadata(
    @Json(name = "name") val name: String,
    @Json(name = "id") val id: String?,
    @Json(name = "path_lower") val pathLower: String?,
    @Json(name = ".tag") val tag: FileTag,
)

enum class FileTag {
    @Json(name = "file") FILE,
    @Json(name = "folder") FOLDER,
    @Json(name = "deleted") DELETED,
}

@JsonClass(generateAdapter = true)
data class TemporaryLinkRequest(
    @Json(name = "path") val path: String,
)

@JsonClass(generateAdapter = true)
data class TemporaryLinkResponse(
    @Json(name = "link") val link: String,
)