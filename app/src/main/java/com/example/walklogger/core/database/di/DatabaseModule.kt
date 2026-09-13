package com.example.walklogger.core.database.di

import android.content.Context
import androidx.room.Room
import com.example.walklogger.core.database.WalkDao
import com.example.walklogger.core.database.WalkLoggerDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun providesDatabase(@ApplicationContext context: Context): WalkLoggerDatabase =
        Room.databaseBuilder(context, WalkLoggerDatabase::class.java, "walklogger.db")
            .build()

    @Provides
    fun providesWalkDao(database: WalkLoggerDatabase): WalkDao = database.walkDao()
}
