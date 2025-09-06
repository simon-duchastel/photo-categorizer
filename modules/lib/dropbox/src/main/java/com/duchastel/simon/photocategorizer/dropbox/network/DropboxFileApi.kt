package com.duchastel.simon.photocategorizer.dropbox.network

import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Retrofit API interface for Dropbox file operations.
 */
interface DropboxFileApi {

    @POST("files/list_folder")
    suspend fun listFolder(
        @Body body: ListFolderRequest,
    ): ListFolderResponse

    @POST("files/list_folder/continue")
    suspend fun listFolderContinue(
        @Body request: ListFolderContinueRequest,
    ): ListFolderResponse

    @POST("files/get_temporary_link")
    suspend fun getUnauthenticatedLink(
        @Body body: TemporaryLinkRequest,
    ): TemporaryLinkResponse

    @POST("files/move_v2")
    suspend fun moveFile(
        @Body body: MoveFileRequest,
    ): MoveFileResponse
}
