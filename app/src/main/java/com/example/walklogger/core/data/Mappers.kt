package com.example.walklogger.core.data

import com.example.walklogger.core.database.WalkWithIssues
import com.example.walklogger.core.database.entity.BehaviourIssueEntity
import com.example.walklogger.core.database.entity.DogEntity
import com.example.walklogger.core.database.entity.WalkEntity
import com.example.walklogger.core.model.BehaviourIssue
import com.example.walklogger.core.model.Dog
import com.example.walklogger.core.model.Walk
import java.time.LocalDate

// Entity <-> domain model mapping. Keeps Room types out of the rest of the app.

internal fun WalkWithIssues.toModel(): Walk = walk.toModel(issues.sortedBy { it.id }.map { it.toModel() })

internal fun WalkEntity.toModel(issues: List<BehaviourIssue> = emptyList()): Walk = Walk(
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
    issues = issues,
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
)

internal fun BehaviourIssueEntity.toModel(): BehaviourIssue = BehaviourIssue(id = id, name = name)

internal fun DogEntity.toModel(): Dog = Dog(
    id = id,
    name = name,
    breed = breed,
    birthDate = birthDate?.let(LocalDate::ofEpochDay),
    weightKg = weightKg,
)
