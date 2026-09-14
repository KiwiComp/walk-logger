package com.example.walklogger.core.database

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.DeleteColumn
import androidx.room.RenameColumn
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.AutoMigrationSpec
import com.example.walklogger.core.database.entity.DogEntity
import com.example.walklogger.core.database.entity.HandlerEntity
import com.example.walklogger.core.database.entity.WalkEntity

@Database(
    entities = [WalkEntity::class, DogEntity::class, HandlerEntity::class],
    version = 4,
    exportSchema = true,
    autoMigrations = [
        AutoMigration(from = 1, to = 2, spec = WalkLoggerDatabase.Migration1To2::class),
        AutoMigration(from = 2, to = 3),
        AutoMigration(from = 3, to = 4, spec = WalkLoggerDatabase.Migration3To4::class),
    ],
)
@TypeConverters(StringListConverter::class)
abstract class WalkLoggerDatabase : RoomDatabase() {
    abstract fun walkDao(): WalkDao
    abstract fun dogDao(): DogDao
    abstract fun handlerDao(): HandlerDao

    @RenameColumn(tableName = "walks", fromColumnName = "startedAtMillis", toColumnName = "startTime")
    @RenameColumn(tableName = "walks", fromColumnName = "endedAtMillis", toColumnName = "endTime")
    class Migration1To2 : AutoMigrationSpec

    /** The single `handler` string is replaced by a `handlers` JSON list; walks also gain a `dogId`. */
    @DeleteColumn(tableName = "walks", columnName = "handler")
    class Migration3To4 : AutoMigrationSpec
}
