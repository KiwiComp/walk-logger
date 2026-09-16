package com.example.walklogger.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.walklogger.core.database.entity.BehaviourIssueEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BehaviourIssueDao {

    @Query("SELECT * FROM behaviour_issues ORDER BY id")
    fun observeIssues(): Flow<List<BehaviourIssueEntity>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertIssue(issue: BehaviourIssueEntity): Long
}
