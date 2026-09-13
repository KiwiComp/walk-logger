package com.example.walklogger.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.walklogger.core.database.entity.WalkEntity

@Database(
    entities = [WalkEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class WalkLoggerDatabase : RoomDatabase() {
    abstract fun walkDao(): WalkDao
}
