package com.example.walklogger.core.location.di

import com.example.walklogger.core.location.FusedLocationProvider
import com.example.walklogger.core.location.LocationProvider
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class LocationModule {
    @Binds
    abstract fun bindsLocationProvider(impl: FusedLocationProvider): LocationProvider
}
