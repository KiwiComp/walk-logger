package com.example.walklogger.testing

import com.example.walklogger.core.data.BehaviourIssueRepository
import com.example.walklogger.core.model.BehaviourIssue
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

/** In-memory [BehaviourIssueRepository] for unit tests. Ids are assigned in insertion order from 1. */
class FakeBehaviourIssueRepository : BehaviourIssueRepository {

    val failures = FailureSwitch()

    private val issues = MutableStateFlow<List<BehaviourIssue>>(emptyList())

    fun addBehaviourIssues(vararg names: String) = names.forEach { name -> issues.update { it + BehaviourIssue(id = it.size + 1L, name = name) } }

    override fun observeBehaviourIssues(): Flow<List<BehaviourIssue>> = issues

    override suspend fun addBehaviourIssue(name: String): BehaviourIssue {
        failures.check()
        val trimmed = name.trim()
        val existing = issues.value.find { it.name.equals(trimmed, ignoreCase = true) }
        return existing ?: BehaviourIssue(id = issues.value.size + 1L, name = trimmed).also { new -> issues.update { it + new } }
    }
}
