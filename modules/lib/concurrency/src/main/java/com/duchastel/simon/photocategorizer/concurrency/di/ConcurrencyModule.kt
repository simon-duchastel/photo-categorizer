package com.duchastel.simon.photocategorizer.concurrency.di

import com.duchastel.simon.photocategorizer.concurrency.RateLimiter
import com.duchastel.simon.photocategorizer.concurrency.RateLimiterImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import kotlin.time.ExperimentalTime

@Module
@InstallIn(SingletonComponent::class)
abstract class ConcurrencyModule {

    @OptIn(ExperimentalTime::class)
    @Binds
    @Singleton
    abstract fun bindRateLimiter(rateLimiterImpl: RateLimiterImpl): RateLimiter
}