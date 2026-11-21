package com.example.walklogger.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/** One row per recorded position, so each new point is a single insert; rows go with the walk. */
@Entity(
    tableName = "walk_route_points",
    indices = [Index("walkId")],
    foreignKeys = [
        ForeignKey(
            entity = WalkEntity::class,
            parentColumns = ["id"],
            childColumns = ["walkId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class RoutePointEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val walkId: Long,
    val time: Long,
    val latitude: Double,
    val longitude: Double,
)
