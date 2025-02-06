package com.duchastel.simon.photocategorizer.dropbox.di

import com.duchastel.simon.photocategorizer.auth.AuthProvider
import com.duchastel.simon.photocategorizer.dropbox.network.AccessTokenAuthInterceptor
import com.duchastel.simon.photocategorizer.dropbox.network.DropboxFileApi
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
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
    fun provideGson(): Gson {
        return GsonBuilder().create()
    }

    @Provides
    @Dropbox
    @Singleton
    fun provideDropboxRetrofit(
        @Dropbox okHttpClient: OkHttpClient,
        @Dropbox gson: Gson,
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://api.dropboxapi.com/2/")
            .addConverterFactory(GsonConverterFactory.create(gson))
            .client(okHttpClient)
            .build()
    }

    @Provides
    @Singleton
    fun provideApiService(@Dropbox retrofit: Retrofit): DropboxFileApi {
        return retrofit.create(DropboxFileApi::class.java)
    }
}