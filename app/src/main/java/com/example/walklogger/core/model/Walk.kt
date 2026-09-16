package com.example.walklogger.core.model

/**
 * A logged walk. Domain model, independent of how it is stored or displayed.
 * Times are epoch millis. Rating and conditions are null until the walk has been logged.
 */
data class Walk(
    val id: Long,
    val dogId: Long,
    /** Who walked the dog; at least one name. */
    val handlers: List<String>,
    val startTime: Long,
    val endTime: Long?,
    val notes: String = "",
    val equipment: Equipment? = null,
    val rating: Int? = null,
    val rain: Rain? = null,
    val temperature: Temperature? = null,
    val wind: Wind? = null,
    val snow: Snow? = null,
    val issues: List<BehaviourIssue> = emptyList(),
    val concerns: List<MedicalConcern> = emptyList(),
    /** Only the aspects the handler chose to rate; each on [RATING_RANGE], 1 least, 7 most. */
    val optionalRatings: Map<OptionalRating, Int> = emptyMap(),
) {
    init {
        require(handlers.isNotEmpty()) { "a walk needs at least one handler" }
        require(rating == null || rating in RATING_RANGE) { "rating must be in $RATING_RANGE, was $rating" }
        optionalRatings.forEach { (aspect, value) ->
            require(value in RATING_RANGE) { "$aspect rating must be in $RATING_RANGE, was $value" }
        }
    }

    val isActive: Boolean get() = endTime == null

    companion object {
        val RATING_RANGE = 1..7
    }
}

enum class Equipment { COLLAR, HARNESS }

/** The aspects of a walk that can be rated on top of the overall rating. */
enum class OptionalRating { REACTIVITY, ENERGY_LEVEL, RESPONSIVENESS, ENGAGEMENT, DISTRACTION }

enum class Rain { NONE, LIGHT, HEAVY }

enum class Temperature { COLD, COOL, NEUTRAL, WARM, HOT }

enum class Wind { STILL, BREEZE, WINDY }

enum class Snow { NONE, LIGHT, HEAVY }
