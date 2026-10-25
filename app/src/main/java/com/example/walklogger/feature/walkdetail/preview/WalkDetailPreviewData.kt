package com.example.walklogger.feature.walkdetail.preview

import com.example.walklogger.core.model.BehaviourIssue
import com.example.walklogger.core.model.Equipment
import com.example.walklogger.core.model.MedicalConcern
import com.example.walklogger.core.model.OptionalRating
import com.example.walklogger.core.model.Rain
import com.example.walklogger.core.model.Snow
import com.example.walklogger.core.model.Stool
import com.example.walklogger.core.model.StoolColour
import com.example.walklogger.core.model.StoolQuantity
import com.example.walklogger.core.model.StoolTexture
import com.example.walklogger.core.model.Temperature
import com.example.walklogger.core.model.Wind
import com.example.walklogger.feature.walkdetail.WalkDetailUiState
import java.time.LocalDate
import java.time.LocalTime


/** Fixed content for `@Preview`s only; never reaches the running app. */
internal object WalkDetailPreviewData {
    val complete = WalkDetailUiState(
        isLoading = false,
        date = LocalDate.of(2026, 9, 15),
        startTime = LocalTime.of(7, 12),
        endTime = LocalTime.of(7, 46),
        dogName = "Sixten",
        handlers = listOf("Kim"),
        equipment = Equipment.COLLAR,
        rating = 5,
        stool = Stool(passed = true, quantity = StoolQuantity.LARGE, texture = StoolTexture.SOFT, colour = StoolColour.YELLOWY_BROWN),
        optionalRatings = mapOf(
            OptionalRating.REACTIVITY to 6,
            OptionalRating.ENERGY_LEVEL to 5,
            OptionalRating.RESPONSIVENESS to 3,
            OptionalRating.ENGAGEMENT to 4,
            OptionalRating.DISTRACTION to 6,
        ),
        rain = Rain.LIGHT,
        temperature = Temperature.COOL,
        wind = Wind.WINDY,
        snow = Snow.NONE,
        issues = listOf(
            BehaviourIssue(1, "Pulling", key = "pulling"),
            BehaviourIssue(2, "Barking", key = "barking"),
            BehaviourIssue(7, "Vehicle fright", key = "vehicle_fright"),
        ),
        concerns = listOf(MedicalConcern(4, "Limping", key = "limping")),
        notes = "Wind put him on edge from the start. Barked at a passing van on Kungsgatan and pulled hard for " +
            "the first ten minutes, then settled once we reached the park. Slight limp on the left hind leg for " +
            "the last stretch — watch it tomorrow.",
    )

    val minimal = WalkDetailUiState(
        isLoading = false,
        date = LocalDate.of(2026, 9, 14),
        startTime = LocalTime.of(18, 5),
        endTime = LocalTime.of(18, 27),
        dogName = "Sixten",
        handlers = listOf("Kim"),
        equipment = Equipment.HARNESS,
        rating = 3,
        notes = "Quick round the block. Fine.",
    )
}
