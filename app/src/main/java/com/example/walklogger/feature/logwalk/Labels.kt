package com.example.walklogger.feature.logwalk

import androidx.annotation.StringRes
import com.example.walklogger.R
import com.example.walklogger.core.model.Equipment
import com.example.walklogger.core.model.Rain
import com.example.walklogger.core.model.Temperature
import com.example.walklogger.core.model.Wind

internal val Equipment.labelRes: Int
    @StringRes get() = when (this) {
        Equipment.COLLAR -> R.string.equipment_collar
        Equipment.HARNESS -> R.string.equipment_harness
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
