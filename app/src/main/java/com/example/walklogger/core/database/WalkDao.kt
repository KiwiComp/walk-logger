package com.example.walklogger.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.walklogger.core.database.entity.WalkBehaviourIssueEntity
import com.example.walklogger.core.database.entity.WalkEntity
import com.example.walklogger.core.database.entity.WalkMedicalConcernEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WalkDao {

    @Transaction
    @Query("SELECT * FROM walks ORDER BY startTime DESC")
    fun observeWalks(): Flow<List<WalkWithIssues>>

    @Transaction
    @Query("SELECT * FROM walks WHERE id = :id")
    fun observeWalk(id: Long): Flow<WalkWithIssues?>

    @Transaction
    @Query("SELECT * FROM walks WHERE endTime IS NULL LIMIT 1")
    fun observeActiveWalk(): Flow<WalkWithIssues?>

    @Query("SELECT * FROM walks WHERE id = :id")
    suspend fun getWalk(id: Long): WalkEntity?

    @Insert
    suspend fun insertWalk(walk: WalkEntity): Long

    /** Inserts the walk and the issues and concerns ticked on it as one unit. */
    @Transaction
    suspend fun insertWalk(walk: WalkEntity, issueIds: List<Long>, concernIds: List<Long>): Long {
        val id = insertWalk(walk)
        insertWalkIssues(issueIds.map { WalkBehaviourIssueEntity(walkId = id, issueId = it) })
        insertWalkConcerns(concernIds.map { WalkMedicalConcernEntity(walkId = id, concernId = it) })
        return id
    }

    @Update
    suspend fun updateWalk(walk: WalkEntity)

    /** Replaces the walk row and its ticked issues and concerns as one unit. */
    @Transaction
    suspend fun updateWalk(walk: WalkEntity, issueIds: List<Long>, concernIds: List<Long>) {
        updateWalk(walk)
        deleteWalkIssues(walk.id)
        insertWalkIssues(issueIds.map { WalkBehaviourIssueEntity(walkId = walk.id, issueId = it) })
        deleteWalkConcerns(walk.id)
        insertWalkConcerns(concernIds.map { WalkMedicalConcernEntity(walkId = walk.id, concernId = it) })
    }

    @Insert
    suspend fun insertWalkIssues(issues: List<WalkBehaviourIssueEntity>)

    @Query("DELETE FROM walk_behaviour_issues WHERE walkId = :walkId")
    suspend fun deleteWalkIssues(walkId: Long)

    @Insert
    suspend fun insertWalkConcerns(concerns: List<WalkMedicalConcernEntity>)

    @Query("DELETE FROM walk_medical_concerns WHERE walkId = :walkId")
    suspend fun deleteWalkConcerns(walkId: Long)

    @Query("UPDATE walks SET notes = :notes WHERE id = :id")
    suspend fun updateNotes(id: Long, notes: String)

    @Query("DELETE FROM walks WHERE id = :id")
    suspend fun deleteWalk(id: Long)
}
