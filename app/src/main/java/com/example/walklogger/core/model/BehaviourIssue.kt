package com.example.walklogger.core.model

/**
 * Something that went wrong on a walk ("Pulling", "Chasing", …). Walks reference issues by [id],
 * so renaming one keeps every walk it was logged on and patterns can be counted per issue.
 */
data class BehaviourIssue(
    val id: Long,
    val name: String,
) {
    init {
        require(name.isNotBlank()) { "issue name must not be blank" }
    }
}
