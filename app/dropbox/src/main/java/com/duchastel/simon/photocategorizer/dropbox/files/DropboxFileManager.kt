package com.duchastel.simon.photocategorizer.dropbox.files

import com.duchastel.simon.photocategorizer.dropbox.network.DropboxFileApi
import com.duchastel.simon.photocategorizer.dropbox.network.FileTag
import com.duchastel.simon.photocategorizer.dropbox.network.ListFolderContinueRequest
import com.duchastel.simon.photocategorizer.dropbox.network.ListFolderRequest
import com.duchastel.simon.photocategorizer.dropbox.network.TemporaryLinkRequest
import com.duchastel.simon.photocategorizer.filemanager.FileManager
import com.duchastel.simon.photocategorizer.filemanager.Photo
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class DropboxFileManager @Inject constructor(
    private val networkApi: DropboxFileApi,
): FileManager {
    override suspend fun getPhotos(): List<Photo> {
        val photos = mutableListOf<Photo>()
        var cursor: String? = null

        do {
            val response = if (cursor == null) {
                networkApi.listFolder(
                    ListFolderRequest(
                        path = "id:QjViWc9B0fAAAAAAAAAjtg",
                    )
                )
            } else {
                networkApi.listFolderContinue(ListFolderContinueRequest(cursor))
            }

            photos += response.entries
                .filter { it.tag == FileTag.FILE && it.pathLower != null && it.id != null }
                .map { Photo(name = it.name, id = it.id!!, path = it.pathLower!!) }

            cursor = response.cursor
        } while (response.hasMore)

        return photos.reversed() // make sure the photos are most-recent to least-recent
    }

    override suspend fun getUnauthenticatedLinkForPhoto(path: String): String {
        return networkApi.getUnauthenticatedLink(TemporaryLinkRequest(path)).link
    }
}
