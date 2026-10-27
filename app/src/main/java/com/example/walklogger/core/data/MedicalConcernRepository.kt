package com.example.walklogger.core.data

import com.example.walklogger.core.model.MedicalConcern
import kotlinx.coroutines.flow.Flow

/** The medical concerns a walk can be tagged with: a seeded default set plus whatever the user adds. */
interface MedicalConcernRepository {
    /** All concerns, defaults first, then in the order they were added. */
    fun observeMedicalConcerns(): Flow<List<MedicalConcern>>

    /**
     * Adds [name] (trimmed) unless a concern with that name already exists, ignoring case.
     * Returns the stored concern, so callers can select it either way.
     */
    suspend fun addMedicalConcern(name: String): MedicalConcern
}
