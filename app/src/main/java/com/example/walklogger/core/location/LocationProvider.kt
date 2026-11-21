package com.example.walklogger.core.location

import com.example.walklogger.core.model.Coordinates

interface LocationProvider {
    /** True only with precise location; approximate location is too coarse for a route and counts as none. */
    fun hasPermission(): Boolean

    /** Null without permission or when the device cannot tell where it is. */
    suspend fun currentLocation(): Coordinates?
}
