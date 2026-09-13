package com.example.walklogger.core.model

/**
 * A logged walk. Domain model, independent of how it is stored or displayed.
 */
data class Walk(
    val id: Long,
    val startedAtMillis: Long,
    val endedAtMillis: Long?,
) {
    val isActive: Boolean get() = endedAtMillis == null
}
