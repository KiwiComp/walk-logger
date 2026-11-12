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
import com.example.walklogger.core.database.entity.MedicalConcernEntity
import com.example.walklogger.core.database.entity.WalkBehaviourIssueEntity
import com.example.walklogger.core.database.entity.WalkEntity
import com.example.walklogger.core.database.entity.WalkMedicalConcernEntity

@Database(
    entities = [
        WalkEntity::class,
        DogEntity::class,
        HandlerEntity::class,
        BehaviourIssueEntity::class,
        WalkBehaviourIssueEntity::class,
        MedicalConcernEntity::class,
        WalkMedicalConcernEntity::class,
    ],
    version = 11,
    exportSchema = true,
    autoMigrations = [
        AutoMigration(from = 1, to = 2, spec = WalkLoggerDatabase.Migration1To2::class),
        AutoMigration(from = 2, to = 3),
        AutoMigration(from = 3, to = 4, spec = WalkLoggerDatabase.Migration3To4::class),
        AutoMigration(from = 4, to = 5),
        AutoMigration(from = 5, to = 6, spec = WalkLoggerDatabase.Migration5To6::class),
        AutoMigration(from = 6, to = 7, spec = WalkLoggerDatabase.Migration6To7::class),
        AutoMigration(from = 7, to = 8),
        AutoMigration(from = 8, to = 9),
        AutoMigration(from = 9, to = 10),
        AutoMigration(from = 10, to = 11, spec = WalkLoggerDatabase.Migration10To11::class),
    ],
)
@TypeConverters(StringListConverter::class)
abstract class WalkLoggerDatabase : RoomDatabase() {
    abstract fun walkDao(): WalkDao
    abstract fun dogDao(): DogDao
    abstract fun handlerDao(): HandlerDao
    abstract fun behaviourIssueDao(): BehaviourIssueDao
    abstract fun medicalConcernDao(): MedicalConcernDao

    @RenameColumn(tableName = "walks", fromColumnName = "startedAtMillis", toColumnName = "startTime")
    @RenameColumn(tableName = "walks", fromColumnName = "endedAtMillis", toColumnName = "endTime")
    class Migration1To2 : AutoMigrationSpec

    /** The single `handler` string is replaced by a `handlers` JSON list; walks also gain a `dogId`. */
    @DeleteColumn(tableName = "walks", columnName = "handler")
    class Migration3To4 : AutoMigrationSpec

    /** Existing installs get the same default issues a fresh database is created with. */
    class Migration5To6 : AutoMigrationSpec {
        override fun onPostMigrate(db: SupportSQLiteDatabase) = db.seedDefaultBehaviourIssuesV6()
    }

    /**
     * Adds medical concerns, and gives behaviour issues a localisation `key` plus soft-delete `isHidden`
     * in place of `isDefault`. Existing installs get the same defaults a fresh database is created with.
     */
    @DeleteColumn(tableName = "behaviour_issues", columnName = "isDefault")
    class Migration6To7 : AutoMigrationSpec {
        override fun onPostMigrate(db: SupportSQLiteDatabase) {
            db.keyDefaultBehaviourIssues()
            db.seedDefaultMedicalConcerns()
        }
    }

    /** The coarse, hand-picked weather is dropped for the fetched values; old answers are not carried over. */
    @DeleteColumn(tableName = "walks", columnName = "rain")
    @DeleteColumn(tableName = "walks", columnName = "temperature")
    @DeleteColumn(tableName = "walks", columnName = "wind")
    @DeleteColumn(tableName = "walks", columnName = "snow")
    class Migration10To11 : AutoMigrationSpec

    /** Seeds the default issues and concerns when the database is first created. */
    class SeedCallback : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            db.seedDefaultBehaviourIssues()
            db.seedDefaultMedicalConcerns()
        }
    }
}
