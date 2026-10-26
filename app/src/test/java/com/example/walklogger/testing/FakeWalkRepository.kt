package com.example.walklogger.testing

import com.example.walklogger.core.data.WalkRepository
import com.example.walklogger.core.model.Equipment
import com.example.walklogger.core.model.Stool
import com.example.walklogger.core.model.Walk
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

/** In-memory [WalkRepository] for unit tests. Prefer fakes over mocks for repositories. */
class FakeWalkRepository : WalkRepository {

    private val walks = MutableStateFlow<List<Walk>>(emptyList())
    private var nextId = 1L

    /** Seeds walks directly, bypassing [startWalk]'s fixed timestamps. */
    fun addWalks(vararg newWalks: Walk) = walks.update { it + newWalks }

    override fun observeWalks(): Flow<List<Walk>> = walks

    override fun observeWalk(id: Long): Flow<Walk?> = walks.map { list -> list.find { it.id == id } }

    override fun observeActiveWalk(): Flow<Walk?> = walks.map { list -> list.find { it.isActive } }

    override suspend fun startWalk(dogId: Long, handlers: List<String>, equipment: Equipment): Long {
        val id = nextId++
        walks.update {
            it + Walk(id = id, dogId = dogId, handlers = handlers, equipment = equipment, startTime = 0, endTime = null)
        }
        return id
    }

    override suspend fun logWalk(walk: Walk): Long {
        require(!walk.isActive) { "a logged walk needs an end time" }
        val id = nextId++
        walks.update { it + walk.copy(id = id) }
        return id
    }

    override suspend fun updateWalk(walk: Walk) {
        require(!walk.isActive) { "a logged walk needs an end time" }
        walks.update { list -> list.map { if (it.id == walk.id) walk else it } }
    }

    override suspend fun updateNotes(walkId: Long, notes: String) {
        walks.update { list -> list.map { if (it.id == walkId) it.copy(notes = notes) else it } }
    }

    override suspend fun updateStool(walkId: Long, stool: Stool?) {
        walks.update { list -> list.map { if (it.id == walkId) it.copy(stool = stool) else it } }
    }

    override suspend fun finishWalk(walkId: Long) {
        walks.update { list ->
            list.map { if (it.id == walkId) it.copy(endTime = 1) else it }
        }
    }

    override suspend fun deleteWalk(id: Long) {
        walks.update { list -> list.filterNot { it.id == id } }
    }
}
