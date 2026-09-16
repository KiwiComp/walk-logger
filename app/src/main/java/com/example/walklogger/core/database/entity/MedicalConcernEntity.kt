package com.example.walklogger.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * A medical concern that can be ticked on a walk. Seeded defaults carry a [key] the UI localises;
 * user-added rows have none and show their [name] as typed. Hidden rows stay for walk history.
 */
@Entity(tableName = "medical_concerns", indices = [Index("name", unique = true)])
data class MedicalConcernEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val key: String? = null,
    @ColumnInfo(defaultValue = "0") val isHidden: Boolean = false,
)
