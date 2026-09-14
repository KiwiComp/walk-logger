package com.example.walklogger.core.database

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RenameColumn
import androidx.room.RoomDatabase
import androidx.room.migration.AutoMigrationSpec
import com.example.walklogger.core.database.entity.WalkEntity

@Database(
    entities = [WalkEntity::class],
    version = 2,
    exportSchema = true,
    autoMigrations = [
        AutoMigration(from = 1, to = 2, spec = WalkLoggerDatabase.Migration1To2::class),
    ],
)
abstract class WalkLoggerDatabase : RoomDatabase() {
    abstract fun walkDao(): WalkDao

    @RenameColumn(tableName = "walks", fromColumnName = "startedAtMillis", toColumnName = "startTime")
    @RenameColumn(tableName = "walks", fromColumnName = "endedAtMillis", toColumnName = "endTime")
    class Migration1To2 : AutoMigrationSpec
}
