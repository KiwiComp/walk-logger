package com.example.walklogger.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.walklogger.core.model.Equipment
import com.example.walklogger.core.model.StoolColour
import com.example.walklogger.core.model.StoolQuantity
import com.example.walklogger.core.model.StoolTexture
import com.example.walklogger.core.model.WeatherCondition

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
    val weatherCondition: WeatherCondition? = null,
    val weatherIsDay: Boolean? = null,
    val temperatureCelsius: Double? = null,
    val rainMm: Double? = null,
    val snowfallCm: Double? = null,
    val windSpeedMs: Double? = null,
    val windGustsMs: Double? = null,
    val reactivity: Int? = null,
    val energyLevel: Int? = null,
    val responsiveness: Int? = null,
    val engagement: Int? = null,
    val distraction: Int? = null,
    /** Null until answered; the other stool columns are only set when this is true. */
    val stoolPassed: Boolean? = null,
    val stoolQuantity: StoolQuantity? = null,
    val stoolTexture: StoolTexture? = null,
    val stoolColour: StoolColour? = null,
)
