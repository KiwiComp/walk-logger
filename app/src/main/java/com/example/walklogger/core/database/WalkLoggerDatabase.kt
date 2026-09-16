package com.example.walklogger.core.database

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.DeleteColumn
import androidx.room.RenameColumn
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.AutoMigrationSpec
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.walklogger.core.database.entity.BehaviourIssueEntity
import com.example.walklogger.core.database.entity.DogEntity
import com.example.walklogger.core.database.entity.HandlerEntity
import com.example.walklogger.core.database.entity.WalkBehaviourIssueEntity
import com.example.walklogger.core.database.entity.WalkEntity

@Database(
    entities = [
        WalkEntity::class,
        DogEntity::class,
        HandlerEntity::class,
        BehaviourIssueEntity::class,
        WalkBehaviourIssueEntity::class,
    ],
    version = 6,
    exportSchema = true,
    autoMigrations = [
        AutoMigration(from = 1, to = 2, spec = WalkLoggerDatabase.Migration1To2::class),
        AutoMigration(from = 2, to = 3),
        AutoMigration(from = 3, to = 4, spec = WalkLoggerDatabase.Migration3To4::class),
        AutoMigration(from = 4, to = 5),
        AutoMigration(from = 5, to = 6, spec = WalkLoggerDatabase.Migration5To6::class),
    ],
)
@TypeConverters(StringListConverter::class)
abstract class WalkLoggerDatabase : RoomDatabase() {
    abstract fun walkDao(): WalkDao
    abstract fun dogDao(): DogDao
    abstract fun handlerDao(): HandlerDao
    abstract fun behaviourIssueDao(): BehaviourIssueDao

    @RenameColumn(tableName = "walks", fromColumnName = "startedAtMillis", toColumnName = "startTime")
    @RenameColumn(tableName = "walks", fromColumnName = "endedAtMillis", toColumnName = "endTime")
    class Migration1To2 : AutoMigrationSpec

    /** The single `handler` string is replaced by a `handlers` JSON list; walks also gain a `dogId`. */
    @DeleteColumn(tableName = "walks", columnName = "handler")
    class Migration3To4 : AutoMigrationSpec

    /** Existing installs get the same default issues a fresh database is created with. */
    class Migration5To6 : AutoMigrationSpec {
        override fun onPostMigrate(db: SupportSQLiteDatabase) = db.seedDefaultBehaviourIssues()
    }

    /** Seeds the default issues when the database is first created. */
    class SeedCallback : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) = db.seedDefaultBehaviourIssues()
    }
}
