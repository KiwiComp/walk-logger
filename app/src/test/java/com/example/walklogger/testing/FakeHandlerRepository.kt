package com.example.walklogger.testing

import com.example.walklogger.core.data.HandlerRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

/** In-memory [HandlerRepository] for unit tests. */
class FakeHandlerRepository : HandlerRepository {

    val failures = FailureSwitch()

    private val handlers = MutableStateFlow<List<String>>(emptyList())

    fun addHandlers(vararg names: String) = handlers.update { it + names }

    override fun observeHandlers(): Flow<List<String>> = handlers

    override suspend fun addHandler(name: String): String {
        failures.check()
        val trimmed = name.trim()
        val existing = handlers.value.find { it.equals(trimmed, ignoreCase = true) }
        return existing ?: trimmed.also { new -> handlers.update { it + new } }
    }
}
