package com.example.walklogger.core.data

import com.example.walklogger.core.database.WalkWithBehaviourIssuesAndMedicalConcerns
import com.example.walklogger.core.database.entity.BehaviourIssueEntity
import com.example.walklogger.core.database.entity.DogEntity
import com.example.walklogger.core.database.entity.MedicalConcernEntity
import com.example.walklogger.core.database.entity.WalkEntity
import com.example.walklogger.core.model.BehaviourIssue
import com.example.walklogger.core.model.Dog
import com.example.walklogger.core.model.MedicalConcern
import com.example.walklogger.core.model.OptionalRating
import com.example.walklogger.core.model.Stool
import com.example.walklogger.core.model.Walk
import java.time.LocalDate

// Entity <-> domain model mapping. Keeps Room types out of the rest of the app.

internal fun WalkWithBehaviourIssuesAndMedicalConcerns.toModel(): Walk = walk.toModel(
    behaviourIssues = behaviourIssues.sortedBy { it.id }.map { it.toModel() },
    medicalConcerns = medicalConcerns.sortedBy { it.id }.map { it.toModel() },
)

internal fun WalkEntity.toModel(
    behaviourIssues: List<BehaviourIssue> = emptyList(),
    medicalConcerns: List<MedicalConcern> = emptyList(),
): Walk = Walk(
    id = id,
    dogId = dogId,
    handlers = handlers,
    startTime = startTime,
    endTime = endTime,
    notes = notes,
    equipment = equipment,
    rating = rating,
    rain = rain,
    temperature = temperature,
    wind = wind,
    snow = snow,
    behaviourIssues = behaviourIssues,
    medicalConcerns = medicalConcerns,
    optionalRatings = buildMap {
        reactivity?.let { put(OptionalRating.REACTIVITY, it) }
        energyLevel?.let { put(OptionalRating.ENERGY_LEVEL, it) }
        responsiveness?.let { put(OptionalRating.RESPONSIVENESS, it) }
        engagement?.let { put(OptionalRating.ENGAGEMENT, it) }
        distraction?.let { put(OptionalRating.DISTRACTION, it) }
    },
    stool = stoolPassed?.let { passed ->
        Stool(passed = passed, quantity = stoolQuantity, texture = stoolTexture, colour = stoolColour)
    },
)

internal fun Walk.toEntity(): WalkEntity = WalkEntity(
    id = id,
    dogId = dogId,
    handlers = handlers,
    startTime = startTime,
    endTime = endTime,
    notes = notes,
    equipment = equipment,
    rating = rating,
    rain = rain,
    temperature = temperature,
    wind = wind,
    snow = snow,
    reactivity = optionalRatings[OptionalRating.REACTIVITY],
    energyLevel = optionalRatings[OptionalRating.ENERGY_LEVEL],
    responsiveness = optionalRatings[OptionalRating.RESPONSIVENESS],
    engagement = optionalRatings[OptionalRating.ENGAGEMENT],
    distraction = optionalRatings[OptionalRating.DISTRACTION],
    stoolPassed = stool?.passed,
    stoolQuantity = stool?.quantity,
    stoolTexture = stool?.texture,
    stoolColour = stool?.colour,
)

internal fun BehaviourIssueEntity.toModel(): BehaviourIssue = BehaviourIssue(id = id, name = name, key = key)

internal fun MedicalConcernEntity.toModel(): MedicalConcern = MedicalConcern(id = id, name = name, key = key)

internal fun DogEntity.toModel(): Dog = Dog(
    id = id,
    name = name,
    breed = breed,
    birthDate = birthDate?.let(LocalDate::ofEpochDay),
    weightKg = weightKg,
)
