package com.example.walklogger.core.model

/** Yes keeps a description already given; No drops it. */
fun Stool?.withPassed(passed: Boolean): Stool = if (passed) {
    this?.takeIf(Stool::passed)
        ?: Stool(passed = true)
} else {
    Stool(passed = false)
}

fun Stool?.withQuantity(value: StoolQuantity): Stool? = describe { copy(quantity = value) }

fun Stool?.withTexture(value: StoolTexture): Stool? = describe { copy(texture = value) }

/** A null [value] removes the colour. */
fun Stool?.withColour(value: StoolColour?): Stool? = describe { copy(colour = value) }

/** The description only applies once Yes has been picked; anything else is returned unchanged. */
private inline fun Stool?.describe(edit: Stool.() -> Stool): Stool? = if (this?.passed ==
    true
) {
    edit()
} else {
    this
}
