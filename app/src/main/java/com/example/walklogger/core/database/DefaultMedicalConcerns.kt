package com.example.walklogger.core.database

import androidx.sqlite.db.SupportSQLiteDatabase

/** The concerns every install starts with, key to English name; seeded on create and on the migration that added the table. */
internal val DefaultMedicalConcerns = linkedMapOf(
    "diarrhoea" to "Diarrhoea",
    "vomit" to "Vomit",
    "scooting" to "Scooting",
    "limping" to "Limping",
    "unwilling_to_walk" to "Unwilling to walk",
    "breathing_issues" to "Breathing issues",
    "irregular_gait" to "Irregular gait",
    "excessive_drooling" to "Excessive drooling",
)

internal fun SupportSQLiteDatabase.seedDefaultMedicalConcerns() {
    DefaultMedicalConcerns.forEach { (key, name) ->
        execSQL("INSERT OR IGNORE INTO medical_concerns (name, `key`) VALUES (?, ?)", arrayOf(name, key))
    }
}
