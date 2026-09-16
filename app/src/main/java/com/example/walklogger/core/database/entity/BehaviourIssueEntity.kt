package com.example.walklogger.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * A behavioural issue that can be ticked on a walk. Seeded defaults carry a [key] the UI localises;
 * user-added rows have none and show their [name] as typed. Hidden rows stay for walk history.
 */
@Entity(tableName = "behaviour_issues", indices = [Index("name", unique = true)])
data class BehaviourIssueEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val key: String? = null,
    @ColumnInfo(defaultValue = "0") val isHidden: Boolean = false,
)
