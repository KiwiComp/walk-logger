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

    private companion object {
        const val DB_NAME = "migration-test.db"
    }
}
