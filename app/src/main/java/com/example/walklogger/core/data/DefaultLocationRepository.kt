package com.example.walklogger.core.data

import com.example.walklogger.core.location.LocationProvider
import com.example.walklogger.core.model.Coordinates
import javax.inject.Inject

class DefaultLocationRepository @Inject constructor(
    private val locationProvider: LocationProvider,
) : LocationRepository {

    override fun hasPermission(): Boolean = locationProvider.hasPermission()

    override suspend fun currentLocation(): Coordinates? = locationProvider.currentLocation()
}
