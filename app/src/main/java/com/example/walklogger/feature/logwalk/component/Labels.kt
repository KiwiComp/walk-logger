package com.example.walklogger.feature.logwalk.component

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.walklogger.R
import com.example.walklogger.core.model.BehaviourIssue
import com.example.walklogger.core.model.Equipment
import com.example.walklogger.core.model.MedicalConcern
import com.example.walklogger.core.model.OptionalRating
import com.example.walklogger.core.model.Rain
import com.example.walklogger.core.model.Snow
import com.example.walklogger.core.model.Temperature
import com.example.walklogger.core.model.Wind
import com.example.walklogger.feature.logwalk.SaveBlocker


internal val Equipment.labelRes: Int
    @StringRes get() = when (this) {
        Equipment.COLLAR -> R.string.equipment_collar
        Equipment.HARNESS -> R.string.equipment_harness
    }

internal val OptionalRating.labelRes: Int
    @StringRes get() = when (this) {
        OptionalRating.REACTIVITY -> R.string.optional_rating_reactivity
        OptionalRating.ENERGY_LEVEL -> R.string.optional_rating_energy_level
        OptionalRating.RESPONSIVENESS -> R.string.optional_rating_responsiveness
        OptionalRating.ENGAGEMENT -> R.string.optional_rating_engagement
        OptionalRating.DISTRACTION -> R.string.optional_rating_distraction
    }

internal val Rain.labelRes: Int
    @StringRes get() = when (this) {
        Rain.NONE -> R.string.rain_none
        Rain.LIGHT -> R.string.rain_light
        Rain.HEAVY -> R.string.rain_heavy
    }

internal val Temperature.labelRes: Int
    @StringRes get() = when (this) {
        Temperature.COLD -> R.string.temperature_cold
        Temperature.COOL -> R.string.temperature_cool
        Temperature.NEUTRAL -> R.string.temperature_neutral
        Temperature.WARM -> R.string.temperature_warm
        Temperature.HOT -> R.string.temperature_hot
    }

internal val Wind.labelRes: Int
    @StringRes get() = when (this) {
        Wind.STILL -> R.string.wind_still
        Wind.BREEZE -> R.string.wind_breeze
        Wind.WINDY -> R.string.wind_windy
    }

internal val Snow.labelRes: Int
    @StringRes get() = when (this) {
        Snow.NONE -> R.string.snow_none
        Snow.LIGHT -> R.string.snow_light
        Snow.HEAVY -> R.string.snow_heavy
    }

internal val SaveBlocker.messageRes: Int
    @StringRes get() = when (this) {
        SaveBlocker.HANDLER -> R.string.log_past_walk_needs_handler
        SaveBlocker.TIMES -> R.string.log_past_walk_needs_times
        SaveBlocker.END_BEFORE_START -> R.string.log_past_walk_needs_end_after_start
        SaveBlocker.IN_FUTURE -> R.string.log_past_walk_needs_past_end
        SaveBlocker.RATING -> R.string.log_past_walk_needs_rating
    }

/** The seeded issues' keys (see DefaultBehaviourIssues) to their localised labels; null for user-added issues. */
@StringRes
internal fun behaviourIssueLabelRes(key: String?): Int? = when (key) {
    "pulling" -> R.string.issue_pulling
    "barking" -> R.string.issue_barking
    "lunging" -> R.string.issue_lunging
    "dog_reactivity" -> R.string.issue_dog_reactivity
    "people_reactivity" -> R.string.issue_people_reactivity
    "noise_fright" -> R.string.issue_noise_fright
    "vehicle_fright" -> R.string.issue_vehicle_fright
    "not_listening" -> R.string.issue_not_listening
    "not_recalling" -> R.string.issue_not_recalling
    "chasing" -> R.string.issue_chasing
    else -> null
}

/** The seeded concerns' keys (see DefaultMedicalConcerns) to their localised labels; null for user-added concerns. */
@StringRes
internal fun medicalConcernLabelRes(key: String?): Int? = when (key) {
    "diarrhoea" -> R.string.concern_diarrhoea
    "vomit" -> R.string.concern_vomit
    "scooting" -> R.string.concern_scooting
    "limping" -> R.string.concern_limping
    "unwilling_to_walk" -> R.string.concern_unwilling_to_walk
    "breathing_issues" -> R.string.concern_breathing_issues
    "irregular_gait" -> R.string.concern_irregular_gait
    "excessive_drooling" -> R.string.concern_excessive_drooling
    else -> null
}

@Composable
internal fun BehaviourIssue.label(): String = behaviourIssueLabelRes(key)?.let { stringResource(it) } ?: name

@Composable
internal fun MedicalConcern.label(): String = medicalConcernLabelRes(key)?.let { stringResource(it) } ?: name
