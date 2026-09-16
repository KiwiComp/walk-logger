package com.example.walklogger.core.database

import androidx.sqlite.db.SupportSQLiteDatabase

/** The issues every install starts with; seeded on database creation and on the migration that adds the table. */
internal val DefaultBehaviourIssues = listOf(
    "Pulling",
    "Barking",
    "Lunging",
    "Dog reactivity",
    "People reactivity",
    "Noise fright",
    "Vehicle fright",
    "Not listening",
    "Not recalling",
    "Chasing",
)

internal fun SupportSQLiteDatabase.seedDefaultBehaviourIssues() {
    DefaultBehaviourIssues.forEach { name ->
        execSQL("INSERT OR IGNORE INTO behaviour_issues (name, isDefault) VALUES (?, 1)", arrayOf(name))
    }
}
