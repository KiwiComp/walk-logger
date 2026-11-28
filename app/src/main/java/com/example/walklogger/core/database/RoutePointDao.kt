package com.example.walklogger.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.walklogger.core.database.entity.RoutePointEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RoutePointDao {

    @Query("SELECT * FROM walk_route_points WHERE walkId = :walkId ORDER BY time")
    fun observeRoute(walkId: Long): Flow<List<RoutePointEntity>>

    @Query("SELECT * FROM walk_route_points WHERE walkId IN (:walkIds) ORDER BY walkId, time")
    fun observeRoutes(walkIds: List<Long>): Flow<List<RoutePointEntity>>

    @Insert
    suspend fun insertPoint(point: RoutePointEntity)
}
