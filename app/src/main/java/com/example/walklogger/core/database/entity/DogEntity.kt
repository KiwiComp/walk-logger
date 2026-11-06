package com.example.walklogger.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "dogs")
data class DogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    @ColumnInfo(defaultValue = "") val breed: String = "",
    /** Epoch day, see [java.time.LocalDate.toEpochDay]. */
    val birthDate: Long? = null,
    val weightKg: Double? = null,
    @ColumnInfo(defaultValue = "0") val isHidden: Boolean = false,
)
