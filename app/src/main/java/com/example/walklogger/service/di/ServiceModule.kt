package com.example.walklogger.service.di

import com.example.walklogger.core.data.WalkTracker
import com.example.walklogger.service.ServiceWalkTracker
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class ServiceModule {
    @Binds
    abstract fun bindsWalkTracker(impl: ServiceWalkTracker): WalkTracker
}
