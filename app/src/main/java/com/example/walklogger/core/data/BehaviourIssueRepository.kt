package com.example.walklogger.core.data

import com.example.walklogger.core.model.BehaviourIssue
import kotlinx.coroutines.flow.Flow

/** The behavioural issues a walk can be tagged with: a seeded default set plus whatever the user adds. */
interface BehaviourIssueRepository {
    /** All issues, defaults first, then in the order they were added. */
    fun observeBehaviourIssues(): Flow<List<BehaviourIssue>>

    /**
     * Adds [name] (trimmed) unless an issue with that name already exists, ignoring case.
     * Returns the stored issue, so callers can select it either way.
     */
    suspend fun addBehaviourIssue(name: String): BehaviourIssue
}
