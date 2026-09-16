package com.example.walklogger.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

/** One row per concern ticked on a walk; rows go with the walk or the concern when either is deleted. */
@Entity(
    tableName = "walk_medical_concerns",
    primaryKeys = ["walkId", "concernId"],
    indices = [Index("concernId")],
    foreignKeys = [
        ForeignKey(entity = WalkEntity::class, parentColumns = ["id"], childColumns = ["walkId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = MedicalConcernEntity::class, parentColumns = ["id"], childColumns = ["concernId"], onDelete = ForeignKey.CASCADE),
    ],
)
data class WalkMedicalConcernEntity(
    val walkId: Long,
    val concernId: Long,
)
