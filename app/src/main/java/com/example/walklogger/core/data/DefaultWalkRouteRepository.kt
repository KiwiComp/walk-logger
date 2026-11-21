package com.example.walklogger.core.data

import com.example.walklogger.core.common.di.Dispatcher
import com.example.walklogger.core.common.di.WalkLoggerDispatchers
import com.example.walklogger.core.database.RoutePointDao
import com.example.walklogger.core.model.RoutePoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class DefaultWalkRouteRepository @Inject constructor(
    private val routePointDao: RoutePointDao,
    @param:Dispatcher(WalkLoggerDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
) : WalkRouteRepository {

    override fun observeRoute(walkId: Long): Flow<List<RoutePoint>> =
        routePointDao.observeRoute(walkId).map { points -> points.map { it.toModel() } }

    override suspend fun addPoint(walkId: Long, point: RoutePoint) = withContext(ioDispatcher) {
        routePointDao.insertPoint(point.toEntity(walkId))
    }
}
