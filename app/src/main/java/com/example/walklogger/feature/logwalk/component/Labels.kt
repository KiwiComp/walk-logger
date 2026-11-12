package com.example.walklogger.feature.logwalk.component

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.walklogger.R
import com.example.walklogger.core.model.BehaviourIssue
import com.example.walklogger.core.model.Equipment
import com.example.walklogger.core.model.MedicalConcern
import com.example.walklogger.core.model.OptionalRating
import com.example.walklogger.core.model.StoolColour
import com.example.walklogger.core.model.StoolQuantity
import com.example.walklogger.core.model.StoolTexture
import com.example.walklogger.core.model.TemperatureBand
import com.example.walklogger.core.model.WeatherCondition
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

internal val WeatherCondition.labelRes: Int
    @StringRes get() = when (this) {
        WeatherCondition.CLEAR -> R.string.weather_condition_clear
        WeatherCondition.PARTLY_CLOUDY -> R.string.weather_condition_partly_cloudy
        WeatherCondition.CLOUDY -> R.string.weather_condition_cloudy
        WeatherCondition.FOG -> R.string.weather_condition_fog
        WeatherCondition.LIGHT_RAIN -> R.string.weather_condition_light_rain
        WeatherCondition.RAIN -> R.string.weather_condition_rain
        WeatherCondition.HEAVY_RAIN -> R.string.weather_condition_heavy_rain
        WeatherCondition.FREEZING_RAIN -> R.string.weather_condition_freezing_rain
        WeatherCondition.LIGHT_SNOW -> R.string.weather_condition_light_snow
        WeatherCondition.SNOW -> R.string.weather_condition_snow
        WeatherCondition.HEAVY_SNOW -> R.string.weather_condition_heavy_snow
        WeatherCondition.THUNDERSTORM -> R.string.weather_condition_thunderstorm
    }

/** Clear and partly cloudy skies have a night variant; the rest look the same day and night. */
@DrawableRes
internal fun WeatherCondition.iconRes(isDay: Boolean): Int = when (this) {
    WeatherCondition.CLEAR -> if (isDay) R.drawable.ic_weather_sunny else R.drawable.ic_weather_clear_night
    WeatherCondition.PARTLY_CLOUDY -> if (isDay) R.drawable.ic_weather_partly_cloudy_day else R.drawable.ic_weather_partly_cloudy_night
    WeatherCondition.CLOUDY -> R.drawable.ic_weather_cloud
    WeatherCondition.FOG -> R.drawable.ic_weather_foggy
    WeatherCondition.LIGHT_RAIN -> R.drawable.ic_weather_rainy_light
    WeatherCondition.RAIN -> R.drawable.ic_weather_rainy
    WeatherCondition.HEAVY_RAIN -> R.drawable.ic_weather_rainy_heavy
    WeatherCondition.FREEZING_RAIN -> R.drawable.ic_weather_weather_mix
    WeatherCondition.LIGHT_SNOW -> R.drawable.ic_weather_weather_snowy
    WeatherCondition.SNOW -> R.drawable.ic_weather_snowing
    WeatherCondition.HEAVY_SNOW -> R.drawable.ic_weather_snowing_heavy
    WeatherCondition.THUNDERSTORM -> R.drawable.ic_weather_thunderstorm
}

internal val TemperatureBand.labelRes: Int
    @StringRes get() = when (this) {
        TemperatureBand.EXTREMELY_COLD -> R.string.temperature_band_extremely_cold
        TemperatureBand.VERY_COLD -> R.string.temperature_band_very_cold
        TemperatureBand.COLD -> R.string.temperature_band_cold
        TemperatureBand.COOL -> R.string.temperature_band_cool
        TemperatureBand.MILD -> R.string.temperature_band_mild
        TemperatureBand.WARM -> R.string.temperature_band_warm
        TemperatureBand.VERY_WARM -> R.string.temperature_band_very_warm
    }

internal val StoolQuantity.labelRes: Int
    @StringRes get() = when (this) {
        StoolQuantity.SMALL -> R.string.stool_quantity_small
        StoolQuantity.NORMAL -> R.string.stool_quantity_normal
        StoolQuantity.LARGE -> R.string.stool_quantity_large
    }

internal val StoolTexture.labelRes: Int
    @StringRes get() = when (this) {
        StoolTexture.HARD -> R.string.stool_texture_hard
        StoolTexture.FIRM -> R.string.stool_texture_firm
        StoolTexture.SOFT -> R.string.stool_texture_soft
        StoolTexture.MUSHY -> R.string.stool_texture_mushy
        StoolTexture.LIQUID -> R.string.stool_texture_liquid
    }

internal val StoolColour.labelRes: Int
    @StringRes get() = when (this) {
        StoolColour.LIGHT_BROWN -> R.string.stool_colour_light_brown
        StoolColour.MEDIUM_BROWN -> R.string.stool_colour_medium_brown
        StoolColour.DARK_BROWN -> R.string.stool_colour_dark_brown
        StoolColour.BLACK -> R.string.stool_colour_black
        StoolColour.YELLOWY_BROWN -> R.string.stool_colour_yellowy_brown
        StoolColour.BRIGHT_YELLOW -> R.string.stool_colour_bright_yellow
        StoolColour.GREY -> R.string.stool_colour_grey
        StoolColour.GREEN -> R.string.stool_colour_green
        StoolColour.RED_STREAKED -> R.string.stool_colour_red_streaked
        StoolColour.WHITE_SPECKED -> R.string.stool_colour_white_specked
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
