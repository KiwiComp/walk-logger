package com.example.walklogger.feature.logwalk.preview

import com.example.walklogger.feature.logwalk.WeatherStatus
import com.example.walklogger.core.model.Place
import com.example.walklogger.core.model.WeatherCondition
import com.example.walklogger.core.model.WalkWeather
import com.example.walklogger.core.model.BehaviourIssue
import com.example.walklogger.core.model.Equipment
import com.example.walklogger.core.model.MedicalConcern
import com.example.walklogger.core.model.OptionalRating
import com.example.walklogger.core.model.Stool
import com.example.walklogger.core.model.StoolColour
import com.example.walklogger.core.model.StoolQuantity
import com.example.walklogger.core.model.StoolTexture
import com.example.walklogger.feature.logwalk.WalkFormActions
import com.example.walklogger.feature.logwalk.WalkFormState
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

    val behaviourIssues = listOf(
        BehaviourIssue(1, "Pulling", key = "pulling"),
        BehaviourIssue(2, "Barking", key = "barking"),
        BehaviourIssue(3, "Lunging", key = "lunging"),
        BehaviourIssue(4, "Dog reactivity", key = "dog_reactivity"),
        BehaviourIssue(5, "Digging"),
    )

    val medicalConcerns = listOf(
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
        form = WalkFormState(
            equipment = Equipment.COLLAR,
            temperatureInput = "-2",
            condition = WeatherCondition.LIGHT_SNOW,
            weatherStatus = WeatherStatus.TOO_OLD,
            isWeatherExpanded = true,
            behaviourIssues = behaviourIssues,
            selectedBehaviourIssueIds = setOf(1L, 4L),
            isBehaviourIssuesExpanded = true,
            medicalConcerns = medicalConcerns,
            selectedMedicalConcernIds = setOf(4L),
            isMedicalConcernsExpanded = true,
        ),
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
        form = WalkFormState(
            rating = 6,
            stool = Stool(passed = true, quantity = StoolQuantity.NORMAL, texture = StoolTexture.FIRM, colour = StoolColour.MEDIUM_BROWN),
            equipment = Equipment.HARNESS,
            fetchedWeather = WalkWeather(
                condition = WeatherCondition.PARTLY_CLOUDY,
                temperatureCelsius = 7.5,
                rainMm = 0.0,
                snowfallCm = 0.0,
                windSpeedMs = 3.4,
                windGustsMs = 7.9,
            ),
            temperatureInput = "7,5",
            condition = WeatherCondition.PARTLY_CLOUDY,
            hasLocationPermission = true,
            hasWeatherTimes = true,
            isWeatherCurrent = true,
            isWeatherExpanded = true,
            behaviourIssues = behaviourIssues,
            medicalConcerns = medicalConcerns,
            notes = "Barked at the bin lorry, otherwise steady. Off-lead in the field for ten minutes.",
        ),
        isLoading = false,
    )

    val formActions = object : WalkFormActions {
        override fun setRating(rating: Int) = Unit
        override fun setOptionalRating(aspect: OptionalRating, value: Int?) = Unit
        override fun toggleOptionalRatings() = Unit
        override fun clearOptionalRatings() = Unit
        override fun selectStoolPassed(passed: Boolean) = Unit
        override fun foldStool() = Unit
        override fun expandStool() = Unit
        override fun selectStoolQuantity(value: StoolQuantity) = Unit
        override fun selectStoolTexture(value: StoolTexture) = Unit
        override fun selectStoolColour(value: StoolColour?) = Unit
        override fun clearStool() = Unit
        override fun selectEquipment(value: Equipment) = Unit
        override fun setTemperature(text: String) = Unit
        override fun selectCondition(value: WeatherCondition) = Unit
        override fun fetchWeather() = Unit
        override fun onLocationPermissionResult(granted: Boolean) = Unit
        override fun openPlaceSearch() = Unit
        override fun searchPlaces(query: String) = Unit
        override fun selectPlace(place: Place) = Unit
        override fun closePlaceSearch() = Unit
        override fun toggleWeatherEditing() = Unit
        override fun toggleWeather() = Unit
        override fun clearWeather() = Unit
        override fun toggleBehaviourIssue(issue: BehaviourIssue) = Unit
        override fun addBehaviourIssue(name: String) = Unit
        override fun toggleBehaviourIssuesExpanded() = Unit
        override fun clearBehaviourIssues() = Unit
        override fun toggleMedicalConcern(concern: MedicalConcern) = Unit
        override fun addMedicalConcern(name: String) = Unit
        override fun toggleMedicalConcernsExpanded() = Unit
        override fun clearMedicalConcerns() = Unit
        override fun setNotes(notes: String) = Unit
    }
}
