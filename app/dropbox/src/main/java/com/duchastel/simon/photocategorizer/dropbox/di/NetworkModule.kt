package com.duchastel.simon.photocategorizer.dropbox.di

import com.duchastel.simon.photocategorizer.auth.AuthProvider
import com.duchastel.simon.photocategorizer.dropbox.network.AccessTokenAuthInterceptor
import com.duchastel.simon.photocategorizer.dropbox.network.DropboxFileApi
import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Dropbox
    @Singleton
    fun provideDropboxAuthInterceptor(
        @Dropbox authProvider: AuthProvider
    ): AccessTokenAuthInterceptor {
        return AccessTokenAuthInterceptor(authProvider)
    }

    @Provides
    @Dropbox
    @Singleton
    fun provideOkHttpClient(
        @Dropbox authInterceptor: AccessTokenAuthInterceptor,
    ): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor()
        loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
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
    fun provideDropboxRetrofit(
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