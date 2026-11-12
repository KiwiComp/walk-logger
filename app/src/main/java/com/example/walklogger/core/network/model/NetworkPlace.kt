package com.example.walklogger.core.network.model

import kotlinx.serialization.Serializable

/** `results` is left out of the response when nothing matches. */
@Serializable
data class NetworkPlaceResults(val results: List<NetworkPlace> = emptyList())

@Serializable
data class NetworkPlace(
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val admin1: String? = null,
    val country: String? = null,
)
