package com.example.walklogger.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.walklogger.core.database.entity.MedicalConcernEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicalConcernDao {

    /** Visible concerns only; hidden ones still resolve through walk relations. */
    @Query("SELECT * FROM medical_concerns WHERE isHidden = 0 ORDER BY id")
    fun observeConcerns(): Flow<List<MedicalConcernEntity>>

    /** Hidden or not, so a re-added name can revive its row instead of colliding with it. */
    @Query("SELECT * FROM medical_concerns WHERE name = :name COLLATE NOCASE LIMIT 1")
    suspend fun findByName(name: String): MedicalConcernEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertConcern(concern: MedicalConcernEntity): Long

    @Query("UPDATE medical_concerns SET isHidden = :hidden WHERE id = :id")
    suspend fun setHidden(id: Long, hidden: Boolean)
}
