package com.example.walklogger.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.walklogger.core.database.entity.DogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DogDao {

    @Query("SELECT * FROM dogs ORDER BY id")
    fun observeDogs(): Flow<List<DogEntity>>

    @Query("UPDATE dogs SET isHidden = 1 WHERE id = :id")
    suspend fun setHidden(id: Long)

    @Insert
    suspend fun insertDog(dog: DogEntity): Long

    @Update
    suspend fun updateDog(dog: DogEntity)
}
