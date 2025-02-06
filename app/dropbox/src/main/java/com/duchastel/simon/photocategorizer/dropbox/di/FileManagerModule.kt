package com.duchastel.simon.photocategorizer.dropbox.di

import com.duchastel.simon.photocategorizer.dropbox.files.DropboxFileManager
import com.duchastel.simon.photocategorizer.dropbox.network.DropboxFileApi
import com.duchastel.simon.photocategorizer.filemanager.FileManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FileManagerModule {

    @Provides
    @Singleton
    fun provideDropboxFileManager(
        networkApi: DropboxFileApi,
    ): FileManager {
        return DropboxFileManager(networkApi)
    }
}