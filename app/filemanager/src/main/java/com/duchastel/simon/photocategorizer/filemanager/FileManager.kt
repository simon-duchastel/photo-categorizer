package com.duchastel.simon.photocategorizer.filemanager

interface FileManager {
    suspend fun getAllPhotos(): List<Photo>
}

data class Photo(
    val name: String,
    val id: String,
    val previewUrl: String,
)
