package com.example.walklogger.core.database

import androidx.room.TypeConverter
import kotlinx.serialization.json.Json

/** Stores a list of strings as a JSON array in a single TEXT column. */
class StringListConverter {
    @TypeConverter
    fun fromList(value: List<String>): String = Json.encodeToString(value)

    @TypeConverter
    fun toList(value: String): List<String> = Json.decodeFromString(value)
}
