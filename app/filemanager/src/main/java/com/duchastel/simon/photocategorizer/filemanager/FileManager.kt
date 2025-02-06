package com.duchastel.simon.photocategorizer.filemanager

interface FileManager {
    suspend fun listPhotos(accessToken: String): List<Photo>
}

data class Photo(
    val name: String,
)