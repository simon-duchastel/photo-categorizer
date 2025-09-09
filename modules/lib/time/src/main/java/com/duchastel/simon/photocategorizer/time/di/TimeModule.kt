package com.duchastel.simon.photocategorizer.time.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@Module
@InstallIn(SingletonComponent::class)
object TimeModule {

    @OptIn(ExperimentalTime::class)
    @Provides
    fun provideClock(): Clock = Clock.System
}