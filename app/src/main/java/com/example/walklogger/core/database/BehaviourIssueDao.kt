package com.example.walklogger.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.walklogger.core.database.entity.BehaviourIssueEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BehaviourIssueDao {

    /** Visible issues only; hidden ones still resolve through walk relations. */
    @Query("SELECT * FROM behaviour_issues WHERE isHidden = 0 ORDER BY id")
    fun observeBehaviourIssues(): Flow<List<BehaviourIssueEntity>>

    /** Hidden or not, so a re-added name can revive its row instead of colliding with it. */
    @Query("SELECT * FROM behaviour_issues WHERE name = :name COLLATE NOCASE LIMIT 1")
    suspend fun findByName(name: String): BehaviourIssueEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertBehaviourIssue(issue: BehaviourIssueEntity): Long

    @Query("UPDATE behaviour_issues SET isHidden = :hidden WHERE id = :id")
    suspend fun setHidden(id: Long, hidden: Boolean)
}
