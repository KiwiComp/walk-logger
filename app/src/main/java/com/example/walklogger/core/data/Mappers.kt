package com.example.walklogger.core.data

import com.example.walklogger.core.database.entity.WalkEntity
import com.example.walklogger.core.model.Walk

// Entity <-> domain model mapping. Keeps Room types out of the rest of the app.

internal fun WalkEntity.toModel(): Walk = Walk(
    id = id,
    startedAtMillis = startedAtMillis,
    endedAtMillis = endedAtMillis,
)
