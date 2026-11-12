package com.example.walklogger.core.network.di

import com.example.walklogger.core.network.OpenMeteoForecastApi
import com.example.walklogger.core.network.OpenMeteoGeocodingApi
import com.example.walklogger.core.network.OpenMeteoJson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun providesOkHttpClient(): OkHttpClient = OkHttpClient()

    @Provides
    @Singleton
    fun providesOpenMeteoForecastApi(client: OkHttpClient): OpenMeteoForecastApi =
        retrofit(client, OpenMeteoForecastApi.BASE_URL).create(OpenMeteoForecastApi::class.java)

    @Provides
    @Singleton
    fun providesOpenMeteoGeocodingApi(client: OkHttpClient): OpenMeteoGeocodingApi =
        retrofit(client, OpenMeteoGeocodingApi.BASE_URL).create(OpenMeteoGeocodingApi::class.java)

    private fun retrofit(client: OkHttpClient, baseUrl: String): Retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(client)
        .addConverterFactory(OpenMeteoJson.asConverterFactory("application/json".toMediaType()))
        .build()
}
