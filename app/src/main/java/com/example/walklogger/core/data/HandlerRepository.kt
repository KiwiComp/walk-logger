package com.example.walklogger.core.data

import kotlinx.coroutines.flow.Flow

/** The people who walk the dog. Names only; nothing else is known about a handler. */
interface HandlerRepository {
    /** All handlers in the order they were added. */
    fun observeHandlers(): Flow<List<String>>

    /**
     * Adds [name] (trimmed) unless a handler with that name already exists, ignoring case.
     * Returns the stored name, so callers can select it either way.
     */
    suspend fun addHandler(name: String): String
}
