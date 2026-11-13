package com.example.walklogger.feature.logwalk

import com.example.walklogger.core.model.BehaviourIssue
import com.example.walklogger.core.model.Equipment
import com.example.walklogger.core.model.MedicalConcern
import com.example.walklogger.core.model.OptionalRating
import com.example.walklogger.core.model.Stool

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
    val weather: WeatherFormState = WeatherFormState(),
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
