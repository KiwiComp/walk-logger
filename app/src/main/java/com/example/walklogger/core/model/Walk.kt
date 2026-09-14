package com.example.walklogger.core.model

/**
 * A logged walk. Domain model, independent of how it is stored or displayed.
 * Times are epoch millis. Rating and conditions are null until the walk has been logged.
 */
data class Walk(
    val id: Long,
    val handler: String,
    val startTime: Long,
    val endTime: Long?,
    val notes: String = "",
    val equipment: Equipment? = null,
    val rating: Int? = null,
    val rain: Rain? = null,
    val temperature: Temperature? = null,
    val wind: Wind? = null,
) {
    init {
        require(rating == null || rating in RATING_RANGE) { "rating must be in $RATING_RANGE, was $rating" }
    }

    val isActive: Boolean get() = endTime == null

    companion object {
        val RATING_RANGE = 1..7
    }
}

enum class Equipment { COLLAR, HARNESS }

enum class Rain { NONE, LIGHT, HEAVY }

enum class Temperature { COLD, COOL, NEUTRAL, WARM, HOT }

enum class Wind { STILL, BREEZE, WINDY }
