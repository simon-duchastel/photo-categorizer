package com.duchastel.simon.photocategorizer.filemanager

interface FileManager {
    suspend fun listPhotos(): List<Photo>
}

data class Photo(
    val name: String,
)
