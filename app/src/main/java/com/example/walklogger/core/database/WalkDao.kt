package com.example.walklogger.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.walklogger.core.database.entity.WalkEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WalkDao {

    @Query("SELECT * FROM walks ORDER BY startTime DESC")
    fun observeWalks(): Flow<List<WalkEntity>>

    @Query("SELECT * FROM walks WHERE id = :id")
    fun observeWalk(id: Long): Flow<WalkEntity?>

    @Query("SELECT * FROM walks WHERE endTime IS NULL LIMIT 1")
    fun observeActiveWalk(): Flow<WalkEntity?>

    @Query("SELECT * FROM walks WHERE id = :id")
    suspend fun getWalk(id: Long): WalkEntity?

    @Insert
    suspend fun insertWalk(walk: WalkEntity): Long

    @Update
    suspend fun updateWalk(walk: WalkEntity)

    @Query("UPDATE walks SET notes = :notes WHERE id = :id")
    suspend fun updateNotes(id: Long, notes: String)

    @Query("DELETE FROM walks WHERE id = :id")
    suspend fun deleteWalk(id: Long)
}
