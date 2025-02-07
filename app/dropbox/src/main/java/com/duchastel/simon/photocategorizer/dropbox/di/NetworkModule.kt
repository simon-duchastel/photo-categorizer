package com.duchastel.simon.photocategorizer.dropbox.di

import com.duchastel.simon.photocategorizer.auth.AuthManager
import com.duchastel.simon.photocategorizer.auth.AccessTokenAuthInterceptor
import com.duchastel.simon.photocategorizer.auth.LoggedOutInterceptor
import com.duchastel.simon.photocategorizer.dropbox.network.DropboxFileApi
import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.logging.HttpLoggingInterceptor.Level
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Dropbox
    @Singleton
    fun provideAuthInterceptor(
        @Dropbox authManager: AuthManager
    ): AccessTokenAuthInterceptor {
        return AccessTokenAuthInterceptor(authManager)
    }

    @Provides
    @Dropbox
    @Singleton
    fun provideLoggedOutInterceptor(
        @Dropbox authManager: AuthManager
    ): LoggedOutInterceptor {
        return LoggedOutInterceptor(authManager)
    }

    @Provides
    @Dropbox
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = Level.BODY
        }
    }

    @Provides
    @Dropbox
    @Singleton
    fun provideOkHttpClient(
        @Dropbox authInterceptor: AccessTokenAuthInterceptor,
        @Dropbox loggedOutInterceptor: LoggedOutInterceptor,
        @Dropbox loggingInterceptor: HttpLoggingInterceptor,
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor(authInterceptor)
            .addInterceptor(loggedOutInterceptor)
            .build()
    }

    @Provides
    @Dropbox
    @Singleton
    fun provideMoshi(): Moshi {
        return Moshi.Builder().build()
    }

    @Provides
    @Dropbox
    @Singleton
    fun provideRetrofit(
        @Dropbox okHttpClient: OkHttpClient,
        @Dropbox moshi: Moshi,
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://api.dropboxapi.com/2/")
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .client(okHttpClient)
            .build()
    }

    @Provides
    @Singleton
    fun provideApiService(@Dropbox retrofit: Retrofit): DropboxFileApi {
        return retrofit.create(DropboxFileApi::class.java)
    }
}