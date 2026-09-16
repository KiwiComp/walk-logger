package com.example.walklogger.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/** A behavioural issue that can be ticked on a walk. Defaults are seeded; the rest are added by the user. */
@Entity(tableName = "behaviour_issues", indices = [Index("name", unique = true)])
data class BehaviourIssueEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val isDefault: Boolean = false,
)
