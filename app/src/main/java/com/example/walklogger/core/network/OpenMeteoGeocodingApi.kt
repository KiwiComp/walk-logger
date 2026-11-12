package com.example.walklogger.core.network

import com.example.walklogger.core.network.model.NetworkPlaceResults
import retrofit2.http.GET
import retrofit2.http.Query

/** https://open-meteo.com/en/docs/geocoding-api */
interface OpenMeteoGeocodingApi {
    @GET("v1/search")
    suspend fun search(
        @Query("name") name: String,
        @Query("language") language: String,
        @Query("count") count: Int = 10,
        @Query("format") format: String = "json",
    ): NetworkPlaceResults

    companion object {
        const val BASE_URL = "https://geocoding-api.open-meteo.com/"
    }
}
