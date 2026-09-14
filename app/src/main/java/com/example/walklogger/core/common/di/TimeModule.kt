package com.example.walklogger.core.common.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.time.Clock
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object TimeModule {

    /** Injected rather than called directly so tests can fix "now". */
    @Provides
    @Singleton
    fun providesClock(): Clock = Clock.systemDefaultZone()
}
