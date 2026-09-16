package com.example.walklogger.core.database

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.example.walklogger.core.database.entity.BehaviourIssueEntity
import com.example.walklogger.core.database.entity.WalkBehaviourIssueEntity
import com.example.walklogger.core.database.entity.WalkEntity

/** A walk row together with the issues ticked on it. */
data class WalkWithIssues(
    @Embedded val walk: WalkEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(WalkBehaviourIssueEntity::class, parentColumn = "walkId", entityColumn = "issueId"),
    )
    val issues: List<BehaviourIssueEntity>,
)
