package com.duchastel.simon.photocategorizer.dropbox.network

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ListFolderRequest(
    @Json(name = "path") val path: String,
)

@JsonClass(generateAdapter = true)
data class ListFolderResponse(
    @Json(name = "entries") val entries: List<FileEntry>,
)

@JsonClass(generateAdapter = true)
data class FileEntry(
    @Json(name = ".tag") val tag: FileTag,
    @Json(name = "name") val name: String,
    @Json(name = "id") val id: String?, // not present for deleted files
    @Json(name = "preview_url") val previewUrl: String?, // not guaranteed to be present
)

enum class FileTag {
    @Json(name = "file") FILE,
    @Json(name = "folder") FOLDER,
    @Json(name = "deleted") DELETED,
}
