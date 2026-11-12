package com.example.walklogger.core.data

import com.example.walklogger.core.model.WalkWeather
import com.example.walklogger.core.model.WeatherCondition
import com.example.walklogger.core.network.model.NetworkHourly

/** WMO weather interpretation codes, see the `weather_code` table on https://open-meteo.com/en/docs. */
internal fun weatherConditionOf(wmoCode: Int): WeatherCondition? = when (wmoCode) {
    0 -> WeatherCondition.CLEAR
    1, 2 -> WeatherCondition.PARTLY_CLOUDY
    3 -> WeatherCondition.CLOUDY
    45, 48 -> WeatherCondition.FOG
    51, 53, 55, 56, 57, 61, 80 -> WeatherCondition.LIGHT_RAIN
    63, 81 -> WeatherCondition.RAIN
    65, 82 -> WeatherCondition.HEAVY_RAIN
    66, 67 -> WeatherCondition.FREEZING_RAIN
    71, 77, 85 -> WeatherCondition.LIGHT_SNOW
    73 -> WeatherCondition.SNOW
    75, 86 -> WeatherCondition.HEAVY_SNOW
    95, 96, 99 -> WeatherCondition.THUNDERSTORM
    else -> null
}

/** Rain includes showers, which Open-Meteo reports separately. Null when the hour is missing. */
internal fun NetworkHourly.toWalkWeather(index: Int): WalkWeather? {
    if (index !in time.indices) return null
    val rainMm = listOfNotNull(rain.getOrNull(index), showers.getOrNull(index)).takeIf {
        it.isNotEmpty()
    }?.sum()
    return WalkWeather(
        condition = weatherCode.getOrNull(index)?.let(::weatherConditionOf),
        isDay = isDay.getOrNull(index) != 0,
        temperatureCelsius = temperature.getOrNull(index),
        rainMm = rainMm,
        snowfallCm = snowfall.getOrNull(index),
        windSpeedMs = windSpeed.getOrNull(index),
        windGustsMs = windGusts.getOrNull(index),
    ).takeUnless { it.isEmpty }
}
