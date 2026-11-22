package com.example.walklogger.core.data

import com.example.walklogger.core.model.RoutePoint
import kotlinx.coroutines.flow.Flow

/** The positions recorded during active walks; kept apart from [WalkRepository] so lists of walks stay light. */
interface WalkRouteRepository {
    /** Every point of the walk with [walkId], oldest first; empty for a walk logged afterwards. */
    fun observeRoute(walkId: Long): Flow<List<RoutePoint>>

    suspend fun addPoint(walkId: Long, point: RoutePoint)
}
