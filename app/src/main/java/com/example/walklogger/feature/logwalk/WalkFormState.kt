package com.example.walklogger.feature.logwalk

import com.example.walklogger.core.model.BehaviourIssue
import com.example.walklogger.core.model.Equipment
import com.example.walklogger.core.model.MedicalConcern
import com.example.walklogger.core.model.OptionalRating
import com.example.walklogger.core.model.Rain
import com.example.walklogger.core.model.Snow
import com.example.walklogger.core.model.Stool
import com.example.walklogger.core.model.Temperature
import com.example.walklogger.core.model.Wind

/** The fields the after-walk and past-walk forms share; each screen's UiState carries one. */
data class WalkFormState(
    val rating: Int? = null,
    val optionalRatings: Map<OptionalRating, Int> = emptyMap(),
    /** True while the optional rating scales are shown; opens by itself when a stored walk has any. */
    val isOptionalRatingsExpanded: Boolean = false,
    /** Null until the handler answers Yes or No. */
    val stool: Stool? = null,
    /** True while the description rows are shown; false folds them into a summary line. A stored answer starts folded. */
    val isStoolExpanded: Boolean = false,
    val equipment: Equipment = Equipment.COLLAR,
    val rain: Rain? = null,
    val temperature: Temperature? = null,
    val wind: Wind? = null,
    val snow: Snow? = null,
    /** True while the optional weather rows are shown; opens by itself when a stored walk has any. */
    val isWeatherExpanded: Boolean = false,
    val behaviourIssues: List<BehaviourIssue> = emptyList(),
    val selectedBehaviourIssueIds: Set<Long> = emptySet(),
    /** True while the optional issue chips are shown; opens by itself when a stored walk has any. */
    val isBehaviourIssuesExpanded: Boolean = false,
    val medicalConcerns: List<MedicalConcern> = emptyList(),
    val selectedMedicalConcernIds: Set<Long> = emptySet(),
    /** True while the optional concern chips are shown; opens by itself when a stored walk has any. */
    val isMedicalConcernsExpanded: Boolean = false,
    val notes: String = "",
)
