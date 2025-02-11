package com.duchastel.simon.photocategorizer.filemanager

interface PhotoRepository {
    suspend fun getPhotos(): List<Photo>
    suspend fun getUnauthenticatedLinkForPhoto(path: String): String
}

data class Photo(
    val name: String,
    val id: String,
    val path: String,
)
