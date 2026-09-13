package com.example.walklogger.core.data

import com.example.walklogger.core.model.Walk
import kotlinx.coroutines.flow.Flow

/**
 * Single source of truth for walks. The UI layer talks only to this interface,
 * never to Room directly.
 */
interface WalkRepository {
    fun observeWalks(): Flow<List<Walk>>
    fun observeWalk(id: Long): Flow<Walk?>
    fun observeActiveWalk(): Flow<Walk?>

    /** Creates a new in-progress walk and returns its id. */
    suspend fun startWalk(): Long
    suspend fun finishWalk(walkId: Long)
    suspend fun deleteWalk(id: Long)
}
