package com.duchastel.simon.photocategorizer.filemanager

import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface DropboxNetworkApi {

    @POST("files/list_folder")
    suspend fun fetchData(
        @Header("Authorization") accessToken: String,
        @Body body: ListFolderRequest,
    ): ListFolderResponse
}

data class ListFolderRequest(
    val path: String,
)

data class ListFolderResponse(
    val entries: List<FileEntry>,
)

data class FileEntry(
    val name: String,
)