package com.example.walklogger.core.network

import com.example.walklogger.core.network.model.NetworkForecast
import retrofit2.http.GET
import retrofit2.http.Query

/** https://open-meteo.com/en/docs; the free tier only reaches 92 days back. */
interface OpenMeteoForecastApi {
    @GET("v1/forecast")
    suspend fun hourlyForecast(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        /** `yyyy-MM-dd'T'HH:mm` in GMT, see [timezone]. */
        @Query("start_hour") startHour: String,
        @Query("end_hour") endHour: String,
        @Query("hourly") hourly: String = HOURLY_VARIABLES,
        @Query("wind_speed_unit") windSpeedUnit: String = "ms",
        @Query("timeformat") timeFormat: String = "unixtime",
        @Query("timezone") timezone: String = "GMT",
    ): NetworkForecast

    companion object {
        const val BASE_URL = "https://api.open-meteo.com/"
        const val MAX_PAST_DAYS = 92L
        private const val HOURLY_VARIABLES =
            "temperature_2m,weather_code,is_day,rain,showers,snowfall,wind_speed_10m,wind_gusts_10m"
    }
}
