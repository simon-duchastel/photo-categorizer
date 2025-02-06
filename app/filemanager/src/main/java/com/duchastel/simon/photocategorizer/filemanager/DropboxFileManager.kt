package com.duchastel.simon.photocategorizer.filemanager

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class DropboxFileManager @Inject constructor(
    private val networkApi: DropboxNetworkApi,
): FileManager {
    override suspend fun listPhotos(accessToken: String): List<Photo> {
        return networkApi
            .fetchData("Bearer $accessToken", ListFolderRequest("id:QjViWc9B0fAAAAAAAAAjtg"))
            .entries
            .map { Photo(it.name) }
    }
}

@Module
@InstallIn(SingletonComponent::class)
object DropboxFileManagerModule {

    @Provides
    @Singleton
    fun provideDropboxFileManager(
        networkApi: DropboxNetworkApi,
    ): FileManager {
        return DropboxFileManager(networkApi)
    }
}