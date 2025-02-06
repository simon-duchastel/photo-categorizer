package com.duchastel.simon.photocategorizer.dropbox.network

data class ListFolderRequest(
    val path: String,
)

data class ListFolderResponse(
    val entries: List<FileEntry>,
)

data class FileEntry(
    val name: String,
)
