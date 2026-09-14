package com.example.walklogger.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.walklogger.core.database.entity.DogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DogDao {

    @Query("SELECT * FROM dogs ORDER BY id")
    fun observeDogs(): Flow<List<DogEntity>>

    @Insert
    suspend fun insertDog(dog: DogEntity): Long
}
