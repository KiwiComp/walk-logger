package com.example.walklogger.feature.walkdetail

import com.example.walklogger.core.model.BehaviourIssue
import com.example.walklogger.core.model.Equipment
import com.example.walklogger.core.model.MedicalConcern
import com.example.walklogger.core.model.OptionalRating
import com.example.walklogger.core.model.Rain
import com.example.walklogger.core.model.Snow
import com.example.walklogger.core.model.Stool
import com.example.walklogger.core.model.Temperature
import com.example.walklogger.core.model.Wind
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime


data class WalkDetailUiState(
    /** True until the walk has been read; every other field is empty meanwhile. */
    val isLoading: Boolean = true,
    /** True when the walk no longer exists; the screen closes. */
    val isNotFound: Boolean = false,
    val date: LocalDate? = null,
    val startTime: LocalTime? = null,
    val endTime: LocalTime? = null,
    val dogName: String? = null,
    /** A deleted dog's walks are read-only. */
    val isDogDeleted: Boolean = false,
    val handlers: List<String> = emptyList(),
    val equipment: Equipment? = null,
    val rating: Int? = null,
    val optionalRatings: Map<OptionalRating, Int> = emptyMap(),
    val stool: Stool? = null,
    val rain: Rain? = null,
    val temperature: Temperature? = null,
    val wind: Wind? = null,
    val snow: Snow? = null,
    val behaviourIssues: List<BehaviourIssue> = emptyList(),
    val medicalConcerns: List<MedicalConcern> = emptyList(),
    val notes: String = "",
    val isDiscarding: Boolean = false,
    val isDiscarded: Boolean = false,
) {
    val durationMinutes: Int? =
        if (startTime != null && endTime != null) Duration.between(startTime, endTime).toMinutes().toInt() else null

    val hasWeather: Boolean = listOf(rain, temperature, wind, snow).any { it != null }

    val canEdit: Boolean = !isLoading && !isDogDeleted

    val canDiscard: Boolean = !isLoading && !isDogDeleted && !isDiscarding && !isDiscarded
}
