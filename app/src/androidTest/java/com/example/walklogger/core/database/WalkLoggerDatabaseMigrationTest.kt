package com.example.walklogger.core.database

import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WalkLoggerDatabaseMigrationTest {

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        WalkLoggerDatabase::class.java,
    )

    @Test
    fun migrate1To2_renamesTimeColumnsAndKeepsRows() {
        helper.createDatabase(DB_NAME, 1).use { db ->
            db.execSQL("INSERT INTO walks (id, startedAtMillis, endedAtMillis) VALUES (1, 100, 200)")
        }

        val db = helper.runMigrationsAndValidate(DB_NAME, 2, true)

        db.query("SELECT startTime, endTime, handler, notes, rating FROM walks WHERE id = 1").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals(100L, cursor.getLong(0))
            assertEquals(200L, cursor.getLong(1))
            assertEquals("", cursor.getString(2))
            assertEquals("", cursor.getString(3))
            assertTrue(cursor.isNull(4))
        }
    }

    @Test
    fun migrate2To3_addsDogsTableAndKeepsWalks() {
        helper.createDatabase(DB_NAME, 2).use { db ->
            db.execSQL("INSERT INTO walks (id, startTime) VALUES (1, 100)")
        }

        val db = helper.runMigrationsAndValidate(DB_NAME, 3, true)

        db.execSQL("INSERT INTO dogs (name) VALUES ('Sixten')")
        db.query("SELECT COUNT(*) FROM walks").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals(1, cursor.getInt(0))
        }
        db.query("SELECT name, breed, birthDate, weightKg FROM dogs").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals("Sixten", cursor.getString(0))
            assertEquals("", cursor.getString(1))
            assertTrue(cursor.isNull(2))
            assertTrue(cursor.isNull(3))
        }
    }

    @Test
    fun migrate3To4_replacesHandlerWithHandlersListAndAddsDogIdAndHandlersTable() {
        helper.createDatabase(DB_NAME, 3).use { db ->
            db.execSQL("INSERT INTO walks (id, startTime, handler) VALUES (1, 100, 'Kim')")
        }

        val db = helper.runMigrationsAndValidate(DB_NAME, 4, true)

        db.query("SELECT dogId, handlers FROM walks WHERE id = 1").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals(0L, cursor.getLong(0))
            assertEquals("[]", cursor.getString(1))
        }
        db.execSQL("INSERT INTO handlers (name) VALUES ('Anna')")
        db.query("SELECT name FROM handlers").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals("Anna", cursor.getString(0))
        }
    }

    @Test
    fun migrate4To5_addsNullableSnowColumnAndKeepsWalks() {
        helper.createDatabase(DB_NAME, 4).use { db ->
            db.execSQL("INSERT INTO walks (id, startTime, rain) VALUES (1, 100, 'LIGHT')")
        }

        val db = helper.runMigrationsAndValidate(DB_NAME, 5, true)

        db.query("SELECT rain, snow FROM walks WHERE id = 1").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals("LIGHT", cursor.getString(0))
            assertTrue(cursor.isNull(1))
        }
    }

    @Test
    fun migrate5To6_addsIssueTablesSeededWithDefaultsAndKeepsWalks() {
        helper.createDatabase(DB_NAME, 5).use { db ->
            db.execSQL("INSERT INTO walks (id, startTime) VALUES (1, 100)")
        }

        val db = helper.runMigrationsAndValidate(DB_NAME, 6, true)

        db.query("SELECT COUNT(*) FROM walks").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals(1, cursor.getInt(0))
        }
        db.query("SELECT name, isDefault FROM behaviour_issues ORDER BY id").use { cursor ->
            val names = buildList { while (cursor.moveToNext()) { assertEquals(1, cursor.getInt(1)); add(cursor.getString(0)) } }
            assertEquals(DefaultBehaviourIssues.values.toList(), names)
        }
        db.execSQL("INSERT INTO walk_behaviour_issues (walkId, issueId) VALUES (1, 1)")
        db.query("SELECT COUNT(*) FROM walk_behaviour_issues").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals(1, cursor.getInt(0))
        }
    }

    @Test
    fun migrate6To7_addsConcernTablesKeysSeededIssuesAndKeepsWalks() {
        helper.createDatabase(DB_NAME, 6).use { db ->
            db.execSQL("INSERT INTO walks (id, startTime) VALUES (1, 100)")
            db.execSQL("INSERT INTO behaviour_issues (id, name, isDefault) VALUES (1, 'Pulling', 1), (2, 'Digging', 0)")
            db.execSQL("INSERT INTO walk_behaviour_issues (walkId, issueId) VALUES (1, 2)")
        }

        val db = helper.runMigrationsAndValidate(DB_NAME, 7, true)

        db.query("SELECT COUNT(*) FROM walks").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals(1, cursor.getInt(0))
        }
        db.query("SELECT name, `key`, isHidden FROM behaviour_issues ORDER BY id").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals("Pulling", cursor.getString(0))
            assertEquals("pulling", cursor.getString(1))
            assertEquals(0, cursor.getInt(2))
            assertTrue(cursor.moveToNext())
            assertEquals("Digging", cursor.getString(0))
            assertTrue(cursor.isNull(1))
            assertEquals(0, cursor.getInt(2))
        }
        db.query("SELECT COUNT(*) FROM walk_behaviour_issues WHERE walkId = 1 AND issueId = 2").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals(1, cursor.getInt(0))
        }
        db.query("SELECT name, `key`, isHidden FROM medical_concerns ORDER BY id").use { cursor ->
            val rows = buildList { while (cursor.moveToNext()) { assertEquals(0, cursor.getInt(2)); add(cursor.getString(1) to cursor.getString(0)) } }
            assertEquals(DefaultMedicalConcerns.toList(), rows)
        }
        db.execSQL("INSERT INTO walk_medical_concerns (walkId, concernId) VALUES (1, 1)")
        db.query("SELECT COUNT(*) FROM walk_medical_concerns").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals(1, cursor.getInt(0))
        }
    }

    @Test
    fun migrate7To8_addsNullableOptionalRatingColumnsAndKeepsWalks() {
        helper.createDatabase(DB_NAME, 7).use { db ->
            db.execSQL("INSERT INTO walks (id, startTime, rating) VALUES (1, 100, 5)")
        }

        val db = helper.runMigrationsAndValidate(DB_NAME, 8, true)

        db.query("SELECT rating, reactivity, energyLevel, responsiveness, engagement, distraction FROM walks WHERE id = 1").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals(5, cursor.getInt(0))
            (1..5).forEach { assertTrue(cursor.isNull(it)) }
        }
    }

    @Test
    fun migrate8To9_addsNullableStoolColumnsAndKeepsWalks() {
        helper.createDatabase(DB_NAME, 8).use { db ->
            db.execSQL("INSERT INTO walks (id, startTime, rating) VALUES (1, 100, 5)")
        }

        val db = helper.runMigrationsAndValidate(DB_NAME, 9, true)

        db.query("SELECT rating, stoolPassed, stoolQuantity, stoolTexture, stoolColour FROM walks WHERE id = 1").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals(5, cursor.getInt(0))
            (1..4).forEach { assertTrue(cursor.isNull(it)) }
        }
    }

    @Test
    fun migrate9To10_addsIsHiddenAndKeepsDogsVisible() {
        helper.createDatabase(DB_NAME, 9).use { db ->
            db.execSQL("INSERT INTO dogs (id, name) VALUES (1, 'Sixten')")
        }

        val db = helper.runMigrationsAndValidate(DB_NAME, 10, true)

        db.query("SELECT name, isHidden FROM dogs WHERE id = 1").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals("Sixten", cursor.getString(0))
            assertEquals(0, cursor.getInt(1))
        }
    }

    private companion object {
        const val DB_NAME = "migration-test.db"
    }
}
