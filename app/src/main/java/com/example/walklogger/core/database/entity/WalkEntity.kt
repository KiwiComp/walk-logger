package com.example.walklogger.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.walklogger.core.model.Equipment
import com.example.walklogger.core.model.Rain
import com.example.walklogger.core.model.Temperature
import com.example.walklogger.core.model.Wind

@Entity(tableName = "walks")
data class WalkEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(defaultValue = "0") val dogId: Long,
    /** JSON array, see [com.example.walklogger.core.database.StringListConverter]. */
    @ColumnInfo(defaultValue = "[]") val handlers: List<String>,
    val startTime: Long,
    val endTime: Long? = null,
    @ColumnInfo(defaultValue = "") val notes: String = "",
    val equipment: Equipment? = null,
    val rating: Int? = null,
    val rain: Rain? = null,
    val temperature: Temperature? = null,
    val wind: Wind? = null,
)
