package com.example.walklogger.core.common.di

import com.example.walklogger.core.common.ErrorReporter
import com.example.walklogger.core.common.LogcatErrorReporter
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ErrorReporterModule {

    @Binds
    @Singleton
    abstract fun bindsErrorReporter(reporter: LogcatErrorReporter): ErrorReporter
}
