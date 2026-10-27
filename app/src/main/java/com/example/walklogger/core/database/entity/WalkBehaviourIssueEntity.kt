package com.example.walklogger.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

/** One row per issue ticked on a walk; rows go with the walk or the issue when either is deleted. */
@Entity(
    tableName = "walk_behaviour_issues",
    primaryKeys = ["walkId", "issueId"],
    indices = [Index("issueId")],
    foreignKeys = [
        ForeignKey(entity = WalkEntity::class, parentColumns = ["id"], childColumns = ["walkId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = BehaviourIssueEntity::class, parentColumns = ["id"], childColumns = ["issueId"], onDelete = ForeignKey.CASCADE),
    ],
)
data class WalkBehaviourIssueEntity(
    val walkId: Long,
    @ColumnInfo(name = "issueId") val behaviourIssueId: Long,
)
