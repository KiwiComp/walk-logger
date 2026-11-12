package com.example.walklogger.core.network

import kotlinx.serialization.json.Json

/** Open-Meteo responses carry more fields than the app reads. */
internal val OpenMeteoJson = Json { ignoreUnknownKeys = true }
