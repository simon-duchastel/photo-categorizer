package com.duchastel.simon.photocategorizer.concurrency.di

import com.duchastel.simon.photocategorizer.concurrency.BufferedScheduler
import com.duchastel.simon.photocategorizer.concurrency.BufferedSchedulerImpl
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
    fun provideBufferedScheduler(): BufferedScheduler {
        return BufferedSchedulerImpl()
    }
}