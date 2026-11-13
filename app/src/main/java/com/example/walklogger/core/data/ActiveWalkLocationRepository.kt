package com.example.walklogger.core.data

import com.example.walklogger.core.model.WalkLocation
import kotlinx.coroutines.flow.Flow

/** Where the active walk started; read when it ends so the weather fetch doesn't wait for a new position. */
interface ActiveWalkLocationRepository {
    val activeWalkLocation: Flow<WalkLocation?>

    suspend fun save(location: WalkLocation)

    suspend fun clear()
}
