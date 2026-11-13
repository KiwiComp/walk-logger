package com.example.walklogger.service.di

import com.example.walklogger.core.data.WalkLocationCapturer
import com.example.walklogger.service.ServiceWalkLocationCapturer
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class ServiceModule {
    @Binds
    abstract fun bindsWalkLocationCapturer(impl: ServiceWalkLocationCapturer): WalkLocationCapturer
}
