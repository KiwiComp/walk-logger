package com.example.walklogger.core.model

/**
 * Something that went wrong on a walk ("Pulling", "Chasing", …). Walks reference issues by [id],
 * so renaming one keeps every walk it was logged on and patterns can be counted per issue.
 * Seeded defaults carry a stable [key] the UI localises; user-added issues show [name] as typed.
 */
data class BehaviourIssue(val id: Long, val name: String, val key: String? = null) {
    init {
        require(name.isNotBlank()) { "issue name must not be blank" }
    }
}
