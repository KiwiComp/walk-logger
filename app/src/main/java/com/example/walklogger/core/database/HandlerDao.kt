package com.example.walklogger.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.walklogger.core.database.entity.HandlerEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HandlerDao {

    @Query("SELECT name FROM handlers ORDER BY rowid")
    fun observeHandlerNames(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertHandler(handler: HandlerEntity)
}
