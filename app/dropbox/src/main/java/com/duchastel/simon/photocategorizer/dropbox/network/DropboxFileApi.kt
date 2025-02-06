package com.duchastel.simon.photocategorizer.dropbox.network

import retrofit2.http.Body
import retrofit2.http.POST

interface DropboxFileApi {

    @POST("files/list_folder")
    suspend fun listFolder(
        @Body body: ListFolderRequest,
    ): ListFolderResponse
}
