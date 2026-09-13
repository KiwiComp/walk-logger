package com.example.walklogger.core.data.di

import com.example.walklogger.core.data.DefaultWalkRepository
import com.example.walklogger.core.data.WalkRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {
    @Binds
    abstract fun bindsWalkRepository(impl: DefaultWalkRepository): WalkRepository
}
