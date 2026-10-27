package com.example.walklogger.core.database

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.example.walklogger.core.database.entity.BehaviourIssueEntity
import com.example.walklogger.core.database.entity.MedicalConcernEntity
import com.example.walklogger.core.database.entity.WalkBehaviourIssueEntity
import com.example.walklogger.core.database.entity.WalkEntity
import com.example.walklogger.core.database.entity.WalkMedicalConcernEntity

/** A walk row together with the issues and concerns ticked on it. */
data class WalkWithBehaviourIssuesAndMedicalConcerns(
    @Embedded val walk: WalkEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(WalkBehaviourIssueEntity::class, parentColumn = "walkId", entityColumn = "issueId"),
    )
    val behaviourIssues: List<BehaviourIssueEntity>,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(WalkMedicalConcernEntity::class, parentColumn = "walkId", entityColumn = "concernId"),
    )
    val medicalConcerns: List<MedicalConcernEntity>,
)
