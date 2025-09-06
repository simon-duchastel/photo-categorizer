package com.duchastel.simon.photocategorizer.dropbox.di

import com.duchastel.simon.photocategorizer.concurrency.BufferedScheduler
import com.duchastel.simon.photocategorizer.dropbox.files.DropboxPhotoRepository
import com.duchastel.simon.photocategorizer.dropbox.network.DropboxFileApi
import com.duchastel.simon.photocategorizer.filemanager.PhotoRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FileManagerModule {

    @Provides
    @Dropbox
    @Singleton
    fun provideDropboxFileManager(
        networkApi: DropboxFileApi,
        bufferedScheduler: BufferedScheduler,
    ): PhotoRepository {
        return DropboxPhotoRepository(networkApi, bufferedScheduler)
    }
}