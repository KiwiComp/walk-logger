package com.example.walklogger.core.data

import com.example.walklogger.core.model.Equipment
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

    /** Creates a new in-progress walk starting now and returns its id. */
    suspend fun startWalk(dogId: Long, handlers: List<String>, equipment: Equipment): Long

    /**
     * Stores a walk that already happened, so it must have an end time.
     * [Walk.id] is ignored; the assigned id is returned.
     */
    suspend fun logWalk(walk: Walk): Long

    /** Replaces the stored walk with [Walk.id]; like [logWalk], it must have an end time. */
    suspend fun updateWalk(walk: Walk)
    suspend fun finishWalk(walkId: Long)
    suspend fun deleteWalk(id: Long)
}
