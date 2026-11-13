package com.example.walklogger.core.data

import com.example.walklogger.core.model.Equipment
import com.example.walklogger.core.model.Stool
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

    /** Replaces the notes of the walk with [walkId]; used while a walk is still in progress. */
    suspend fun updateNotes(walkId: Long, notes: String)

    /** Replaces the stool answer of the walk with [walkId]; used while a walk is still in progress. */
    suspend fun updateStool(walkId: Long, stool: Stool?)

    /** Ends the walk with [walkId] now, storing its last [notes] and [stool] in the same transaction. */
    suspend fun endWalk(walkId: Long, notes: String, stool: Stool?)
    suspend fun deleteWalk(id: Long)
}
