package com.example.walklogger.core.ui.component

import com.example.walklogger.core.model.Equipment
import java.time.LocalTime
import kotlin.time.Duration

/** One completed walk as the day timelines on Log and Calendar show it. */
data class TimelineWalk(
    val id: Long,
    val startTime: LocalTime,
    val duration: Duration,
    /** Null for a walk without a recorded route. */
    val distanceMeters: Double?,
    val equipment: Equipment?,
    val rating: Int?,
    val note: String,
    val hasPassedStool: Boolean?,
)
