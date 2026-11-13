package com.example.walklogger.core.data

import com.example.walklogger.core.model.Coordinates
import com.example.walklogger.core.model.WalkWeather
import java.time.Instant

interface WeatherRepository {
    /** False when [time] is further back than the weather source reaches. */
    fun isAvailable(time: Instant): Boolean

    /** The weather in the hour that contains [time]; throws when it cannot be fetched. */
    suspend fun weatherAt(coordinates: Coordinates, time: Instant): WalkWeather
}
