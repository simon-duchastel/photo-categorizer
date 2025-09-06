package com.duchastel.simon.photocategorizer.concurrency.di

import com.duchastel.simon.photocategorizer.concurrency.RateLimiter
import com.duchastel.simon.photocategorizer.concurrency.RateLimiterImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ConcurrencyModule {

    @Provides
    @Singleton
    fun provideRateLimiter(): RateLimiter {
        return RateLimiterImpl()
    }
}