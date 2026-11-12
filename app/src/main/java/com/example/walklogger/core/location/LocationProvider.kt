package com.example.walklogger.core.location

import com.example.walklogger.core.model.Coordinates

interface LocationProvider {
    /** True with either coarse or fine location, so a later precise-location grant still counts. */
    fun hasPermission(): Boolean

    /** Null without permission or when the device cannot tell where it is. */
    suspend fun currentLocation(): Coordinates?
}
