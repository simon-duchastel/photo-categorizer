package com.duchastel.simon.photocategorizer.dropbox.di

import android.content.Context
import com.duchastel.simon.photocategorizer.auth.AuthProvider
import com.duchastel.simon.photocategorizer.dropbox.auth.DropboxAuthProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AuthModule {

    @Provides
    @Singleton
    fun provideDropboxAuthProvider(
        @ApplicationContext context: Context
    ): AuthProvider {
        return DropboxAuthProvider(context)
    }
}
