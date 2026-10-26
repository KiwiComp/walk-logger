package com.example.walklogger.feature.logwalk.preview

import com.example.walklogger.core.model.BehaviourIssue
import com.example.walklogger.core.model.Equipment
import com.example.walklogger.core.model.MedicalConcern
import com.example.walklogger.core.model.Rain
import com.example.walklogger.core.model.Snow
import com.example.walklogger.core.model.Stool
import com.example.walklogger.core.model.StoolColour
import com.example.walklogger.core.model.StoolQuantity
import com.example.walklogger.core.model.StoolTexture
import com.example.walklogger.core.model.Temperature
import com.example.walklogger.core.model.Wind
import com.example.walklogger.feature.logwalk.activewalk.ActiveWalkUiState
import com.example.walklogger.feature.logwalk.finishwalk.FinishWalkUiState
import com.example.walklogger.feature.logwalk.newwalk.NewWalkUiState
import com.example.walklogger.feature.logwalk.pastwalk.LogPastWalkUiState
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime


/** Fixed content for `@Preview`s only; never reaches the running app. */
internal object LogWalkPreviewData {
    val handlers = listOf("Anna", "Jonas", "Elin")

    val issues = listOf(
        BehaviourIssue(1, "Pulling", key = "pulling"),
        BehaviourIssue(2, "Barking", key = "barking"),
        BehaviourIssue(3, "Lunging", key = "lunging"),
        BehaviourIssue(4, "Dog reactivity", key = "dog_reactivity"),
        BehaviourIssue(5, "Digging"),
    )

    val concerns = listOf(
        MedicalConcern(1, "Diarrhoea", key = "diarrhoea"),
        MedicalConcern(2, "Vomit", key = "vomit"),
        MedicalConcern(3, "Scooting", key = "scooting"),
        MedicalConcern(4, "Limping", key = "limping"),
        MedicalConcern(5, "Coughing"),
    )

    val newWalk = NewWalkUiState(
        date = LocalDate.of(2026, 9, 12),
        dogName = "Sixten",
        handlers = handlers,
        selectedHandlers = setOf("Anna", "Jonas"),
        equipment = Equipment.HARNESS,
    )

    val logPastWalk = LogPastWalkUiState(
        date = LocalDate.of(2026, 9, 12),
        now = LocalDateTime.of(2026, 9, 14, 20, 0),
        startTime = LocalTime.of(17, 40),
        endTime = LocalTime.of(18, 5),
        dogName = "Sixten",
        handlers = handlers,
        selectedHandlers = setOf("Jonas"),
        equipment = Equipment.COLLAR,
        rain = Rain.LIGHT,
        temperature = Temperature.COLD,
        wind = Wind.WINDY,
        snow = Snow.LIGHT,
        issues = issues,
        selectedIssueIds = setOf(1L, 4L),
        isBehaviourExpanded = true,
        concerns = concerns,
        selectedConcernIds = setOf(4L),
        isMedicalExpanded = true,
    )

    val activeWalk = ActiveWalkUiState(
        dogName = "Sixten",
        handlers = listOf("Anna"),
        equipment = Equipment.HARNESS,
        startTime = LocalTime.of(7, 5),
        elapsed = Duration.ofMinutes(34).plusSeconds(12),
        note = "Barked at the bin lorry",
        stool = Stool(passed = true, quantity = StoolQuantity.NORMAL, texture = StoolTexture.FIRM),
        isStoolExpanded = true,
    )

    val finishWalk = FinishWalkUiState(
        now = LocalDateTime.of(2026, 9, 12, 7, 39),
        date = LocalDate.of(2026, 9, 12),
        startTime = LocalTime.of(7, 5),
        endTime = LocalTime.of(7, 39),
        dogName = "Sixten",
        handlers = listOf("Anna"),
        rating = 6,
        stool = Stool(passed = true, quantity = StoolQuantity.NORMAL, texture = StoolTexture.FIRM, colour = StoolColour.MEDIUM_BROWN),
        equipment = Equipment.HARNESS,
        rain = Rain.NONE,
        temperature = Temperature.COOL,
        wind = Wind.BREEZE,
        snow = Snow.NONE,
        issues = issues,
        concerns = concerns,
        notes = "Barked at the bin lorry, otherwise steady. Off-lead in the field for ten minutes.",
        isLoading = false,
    )
}
