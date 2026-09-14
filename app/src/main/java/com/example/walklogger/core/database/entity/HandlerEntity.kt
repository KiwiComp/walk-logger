package com.example.walklogger.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** A person who walks the dog. Just a name; walks store their handlers by name. */
@Entity(tableName = "handlers")
data class HandlerEntity(
    @PrimaryKey val name: String,
)
