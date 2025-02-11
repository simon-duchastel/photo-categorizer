package com.duchastel.simon.photocategorizer.storage.di

import android.content.Context
import com.duchastel.simon.photocategorizer.storage.LocalStorageRepository
import com.duchastel.simon.photocategorizer.storage.SharedPrefsLocalStorageRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object StorageModule {

    @Provides
    @Singleton
    fun provideLocalStorageRepository(
        @ApplicationContext context: Context
    ): LocalStorageRepository {
        return SharedPrefsLocalStorageRepository(
            context
        )
    }
}