package com.duchastel.simon.photocategorizer.dropbox.files

import com.duchastel.simon.photocategorizer.dropbox.network.DropboxFileApi
import com.duchastel.simon.photocategorizer.dropbox.network.FileTag
import com.duchastel.simon.photocategorizer.dropbox.network.ListFolderRequest
import com.duchastel.simon.photocategorizer.filemanager.FileManager
import com.duchastel.simon.photocategorizer.filemanager.Photo
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class DropboxFileManager @Inject constructor(
    private val networkApi: DropboxFileApi,
): FileManager {
    override suspend fun getAllPhotos(): List<Photo> {
        return networkApi
            .listFolder(ListFolderRequest("id:QjViWc9B0fAAAAAAAAAjtg"))
            .entries
            .filter { it.tag == FileTag.FILE }
            .filter { it.previewUrl != null && it.id != null }
            .mapNotNull {
                Photo(
                    name = it.name,
                    id = it.id ?: return@mapNotNull null,
                    previewUrl = it.previewUrl ?: return@mapNotNull null,
                )
            }
    }
}
