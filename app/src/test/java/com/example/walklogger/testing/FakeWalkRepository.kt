package com.example.walklogger.testing

import com.example.walklogger.core.data.WalkRepository
import com.example.walklogger.core.model.Walk
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

/** In-memory [WalkRepository] for unit tests. Prefer fakes over mocks for repositories. */
class FakeWalkRepository : WalkRepository {

    private val walks = MutableStateFlow<List<Walk>>(emptyList())
    private var nextId = 1L

    override fun observeWalks(): Flow<List<Walk>> = walks

    override fun observeWalk(id: Long): Flow<Walk?> = walks.map { list -> list.find { it.id == id } }

    override fun observeActiveWalk(): Flow<Walk?> = walks.map { list -> list.find { it.isActive } }

    override suspend fun startWalk(): Long {
        val id = nextId++
        walks.update { it + Walk(id = id, startedAtMillis = 0, endedAtMillis = null) }
        return id
    }

    override suspend fun finishWalk(walkId: Long) {
        walks.update { list ->
            list.map { if (it.id == walkId) it.copy(endedAtMillis = 1) else it }
        }
    }

    override suspend fun deleteWalk(id: Long) {
        walks.update { list -> list.filterNot { it.id == id } }
    }
}
