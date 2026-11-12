package com.example.walklogger.testing

import com.example.walklogger.core.data.LocationRepository
import com.example.walklogger.core.model.Coordinates

/** [LocationRepository] for unit tests; grant or deny with [isGranted] and place the device with [location]. */
class FakeLocationRepository(
    var isGranted: Boolean = true,
    var location: Coordinates? = Coordinates(59.33, 18.07),
) : LocationRepository {

    override fun hasPermission(): Boolean = isGranted

    override suspend fun currentLocation(): Coordinates? = if (isGranted) location else null
}
