package com.example.walklogger.core.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NetworkForecast(val hourly: NetworkHourly)

/** Parallel lists, one entry per hour in [time] (epoch seconds); any value may be missing. */
@Serializable
data class NetworkHourly(
    val time: List<Long>,
    @SerialName("temperature_2m") val temperature: List<Double?> = emptyList(),
    @SerialName("weather_code") val weatherCode: List<Int?> = emptyList(),
    @SerialName("is_day") val isDay: List<Int?> = emptyList(),
    val rain: List<Double?> = emptyList(),
    val showers: List<Double?> = emptyList(),
    val snowfall: List<Double?> = emptyList(),
    @SerialName("wind_speed_10m") val windSpeed: List<Double?> = emptyList(),
    @SerialName("wind_gusts_10m") val windGusts: List<Double?> = emptyList(),
)
