package com.duchastel.simon.photocategorizer.dropbox.files

import com.duchastel.simon.photocategorizer.dropbox.network.DropboxFileApi
import com.duchastel.simon.photocategorizer.dropbox.network.ListFolderRequest
import com.duchastel.simon.photocategorizer.filemanager.FileManager
import com.duchastel.simon.photocategorizer.filemanager.Photo
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class DropboxFileManager @Inject constructor(
    private val networkApi: DropboxFileApi,
): FileManager {
    override suspend fun listPhotos(): List<Photo> {
        return networkApi
            .fetchData(ListFolderRequest("id:QjViWc9B0fAAAAAAAAAjtg"))
            .entries
            .map { Photo(it.name) }
    }
}
