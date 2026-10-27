package com.example.walklogger.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.walklogger.core.database.entity.WalkBehaviourIssueEntity
import com.example.walklogger.core.database.entity.WalkEntity
import com.example.walklogger.core.database.entity.WalkMedicalConcernEntity
import com.example.walklogger.core.model.StoolColour
import com.example.walklogger.core.model.StoolQuantity
import com.example.walklogger.core.model.StoolTexture
import kotlinx.coroutines.flow.Flow

@Dao
interface WalkDao {

    @Transaction
    @Query("SELECT * FROM walks ORDER BY startTime DESC")
    fun observeWalks(): Flow<List<WalkWithBehaviourIssuesAndMedicalConcerns>>

    @Transaction
    @Query("SELECT * FROM walks WHERE id = :id")
    fun observeWalk(id: Long): Flow<WalkWithBehaviourIssuesAndMedicalConcerns?>

    @Transaction
    @Query("SELECT * FROM walks WHERE endTime IS NULL LIMIT 1")
    fun observeActiveWalk(): Flow<WalkWithBehaviourIssuesAndMedicalConcerns?>

    @Query("SELECT * FROM walks WHERE id = :id")
    suspend fun getWalk(id: Long): WalkEntity?

    @Insert
    suspend fun insertWalk(walk: WalkEntity): Long

    /** Inserts the walk and the issues and concerns ticked on it as one unit. */
    @Transaction
    suspend fun insertWalk(walk: WalkEntity, behaviourIssueIds: List<Long>, medicalConcernIds: List<Long>): Long {
        val id = insertWalk(walk)
        insertWalkBehaviourIssues(behaviourIssueIds.map { WalkBehaviourIssueEntity(walkId = id, behaviourIssueId = it) })
        insertWalkMedicalConcerns(medicalConcernIds.map { WalkMedicalConcernEntity(walkId = id, medicalConcernId = it) })
        return id
    }

    @Update
    suspend fun updateWalk(walk: WalkEntity)

    /** Replaces the walk row and its ticked issues and concerns as one unit. */
    @Transaction
    suspend fun updateWalk(walk: WalkEntity, behaviourIssueIds: List<Long>, medicalConcernIds: List<Long>) {
        updateWalk(walk)
        deleteWalkBehaviourIssues(walk.id)
        insertWalkBehaviourIssues(behaviourIssueIds.map { WalkBehaviourIssueEntity(walkId = walk.id, behaviourIssueId = it) })
        deleteWalkMedicalConcerns(walk.id)
        insertWalkMedicalConcerns(medicalConcernIds.map { WalkMedicalConcernEntity(walkId = walk.id, medicalConcernId = it) })
    }

    @Insert
    suspend fun insertWalkBehaviourIssues(behaviourIssues: List<WalkBehaviourIssueEntity>)

    @Query("DELETE FROM walk_behaviour_issues WHERE walkId = :walkId")
    suspend fun deleteWalkBehaviourIssues(walkId: Long)

    @Insert
    suspend fun insertWalkMedicalConcerns(medicalConcerns: List<WalkMedicalConcernEntity>)

    @Query("DELETE FROM walk_medical_concerns WHERE walkId = :walkId")
    suspend fun deleteWalkMedicalConcerns(walkId: Long)

    @Query("UPDATE walks SET notes = :notes WHERE id = :id")
    suspend fun updateNotes(id: Long, notes: String)

    @Query(
        "UPDATE walks SET stoolPassed = :passed, stoolQuantity = :quantity, stoolTexture = :texture, " +
            "stoolColour = :colour WHERE id = :id",
    )
    suspend fun updateStool(id: Long, passed: Boolean?, quantity: StoolQuantity?, texture: StoolTexture?, colour: StoolColour?)

    @Query("DELETE FROM walks WHERE id = :id")
    suspend fun deleteWalk(id: Long)
}
