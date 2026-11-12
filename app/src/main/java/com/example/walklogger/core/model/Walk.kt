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
    val weather: WalkWeather? = null,
    val behaviourIssues: List<BehaviourIssue> = emptyList(),
    val medicalConcerns: List<MedicalConcern> = emptyList(),
    /** Only the aspects the handler chose to rate; each on [RATING_RANGE], 1 least, 7 most. */
    val optionalRatings: Map<OptionalRating, Int> = emptyMap(),
    /** Null until the handler has actively answered whether the dog passed stool. */
    val stool: Stool? = null,
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

/** Whether the dog passed stool on the walk and, if so, an optional description of it. */
data class Stool(
    val passed: Boolean,
    val quantity: StoolQuantity? = null,
    val texture: StoolTexture? = null,
    val colour: StoolColour? = null,
) {
    init {
        require(passed || listOf(quantity, texture, colour).all { it == null }) { "a stool that was not passed has no description" }
    }
}

enum class StoolQuantity { SMALL, NORMAL, LARGE }

enum class StoolTexture { HARD, FIRM, SOFT, MUSHY, LIQUID }

/** [isTypical] marks the shades of a healthy stool; every other colour is worth a closer look. */
enum class StoolColour(val isTypical: Boolean) {
    LIGHT_BROWN(isTypical = true),
    MEDIUM_BROWN(isTypical = true),
    DARK_BROWN(isTypical = true),
    BLACK(isTypical = false),
    YELLOWY_BROWN(isTypical = false),
    BRIGHT_YELLOW(isTypical = false),
    GREY(isTypical = false),
    GREEN(isTypical = false),
    RED_STREAKED(isTypical = false),
    WHITE_SPECKED(isTypical = false),
}
