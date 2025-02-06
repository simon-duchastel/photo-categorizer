package com.duchastel.simon.photocategorizer.dropbox.network

import retrofit2.http.Body
import retrofit2.http.POST

interface DropboxFileApi {

    @POST("files/list_folder")
    suspend fun fetchData(
        @Body body: ListFolderRequest,
    ): ListFolderResponse
}
