package com.example.walklogger.testing

import com.example.walklogger.core.data.MedicalConcernRepository
import com.example.walklogger.core.model.MedicalConcern
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

/** In-memory [MedicalConcernRepository] for unit tests. Ids are assigned in insertion order from 1. */
class FakeMedicalConcernRepository : MedicalConcernRepository {

    val failures = FailureSwitch()

    private val concerns = MutableStateFlow<List<MedicalConcern>>(emptyList())

    fun addMedicalConcerns(vararg names: String) = names.forEach { name ->
        concerns.update {
            it +
                MedicalConcern(id = it.size + 1L, name = name)
        }
    }

    override fun observeMedicalConcerns(): Flow<List<MedicalConcern>> = concerns

    override suspend fun addMedicalConcern(name: String): MedicalConcern {
        failures.check()
        val trimmed = name.trim()
        val existing = concerns.value.find { it.name.equals(trimmed, ignoreCase = true) }
        return existing
            ?: MedicalConcern(id = concerns.value.size + 1L, name = trimmed).also { new ->
                concerns.update {
                    it +
                        new
                }
            }
    }
}
