package com.example.walklogger.core.database

import androidx.sqlite.db.SupportSQLiteDatabase

/** The issues every install starts with, key to English name; seeded on create and keyed on the migration that added `key`. */
internal val DefaultBehaviourIssues = linkedMapOf(
    "pulling" to "Pulling",
    "barking" to "Barking",
    "lunging" to "Lunging",
    "dog_reactivity" to "Dog reactivity",
    "people_reactivity" to "People reactivity",
    "noise_fright" to "Noise fright",
    "vehicle_fright" to "Vehicle fright",
    "not_listening" to "Not listening",
    "not_recalling" to "Not recalling",
    "chasing" to "Chasing",
)

internal fun SupportSQLiteDatabase.seedDefaultBehaviourIssues() {
    DefaultBehaviourIssues.forEach { (key, name) ->
        execSQL(
            "INSERT OR IGNORE INTO behaviour_issues (name, `key`) VALUES (?, ?)",
            arrayOf(name, key),
        )
    }
}

/** The version 6 seed, frozen so the shipped 5→6 migration keeps producing the rows it always did. */
internal fun SupportSQLiteDatabase.seedDefaultBehaviourIssuesV6() {
    DefaultBehaviourIssues.values.forEach { name ->
        execSQL(
            "INSERT OR IGNORE INTO behaviour_issues (name, isDefault) VALUES (?, 1)",
            arrayOf(name),
        )
    }
}

/** Gives the rows seeded by an earlier version their key, matched on the English name they were created with. */
internal fun SupportSQLiteDatabase.keyDefaultBehaviourIssues() {
    DefaultBehaviourIssues.forEach { (key, name) ->
        execSQL("UPDATE behaviour_issues SET `key` = ? WHERE name = ?", arrayOf(key, name))
    }
}
