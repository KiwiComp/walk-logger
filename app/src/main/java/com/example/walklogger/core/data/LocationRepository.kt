package com.example.walklogger.core.data

import com.example.walklogger.core.model.Coordinates

/** Where the device is. */
interface LocationRepository {
    /** True with either coarse or fine location, so a later precise-location grant still counts. */
    fun hasPermission(): Boolean

    /** Null without permission or when the device cannot tell where it is. */
    suspend fun currentLocation(): Coordinates?
}
