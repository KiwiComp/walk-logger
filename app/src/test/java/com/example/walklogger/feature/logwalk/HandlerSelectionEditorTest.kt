package com.example.walklogger.feature.logwalk

import com.example.walklogger.R
import com.example.walklogger.testing.FakeHandlerRepository
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HandlerSelectionEditorTest {

    private val handlerRepository = FakeHandlerRepository()
    private val shown = mutableListOf<Int>()
    private val logged = mutableListOf<String>()

    private fun runEditorTest(body: suspend TestScope.(HandlerSelectionEditor) -> Unit) =
        runTest(UnconfinedTestDispatcher()) {
            body(
                HandlerSelectionEditor(backgroundScope, handlerRepository) { _, message, log ->
                    shown += message
                    logged += log
                },
            )
        }

    @Test
    fun `the first stored handler starts selected`() = runEditorTest { editor ->
        handlerRepository.addHandlers("Anna", "Jonas")
        editor.preselectFirst()
        assertEquals(setOf("Anna"), editor.state.value.selected)
    }

    @Test
    fun `a prefilled selection is kept and listed in stored order`() = runEditorTest { editor ->
        handlerRepository.addHandlers("Anna", "Jonas", "Elin")
        editor.prefill(listOf("Elin", "Anna"))
        editor.preselectFirst()
        assertEquals(listOf("Anna", "Elin"), editor.state.value.selectedInOrder)
    }

    @Test
    fun `handlers toggle, and an added one is stored and selected`() = runEditorTest { editor ->
        handlerRepository.addHandlers("Anna")
        editor.toggle("Anna")
        assertEquals(setOf("Anna"), editor.state.value.selected)
        editor.toggle("Anna")
        assertTrue(editor.state.value.selected.isEmpty())
        editor.add(" Kim ")
        assertEquals(listOf("Anna", "Kim"), editor.state.value.handlers)
        assertEquals(setOf("Kim"), editor.state.value.selected)
    }

    @Test
    fun `a failed add says so and selects nothing`() = runEditorTest { editor ->
        handlerRepository.failures.failWrites = true
        editor.add("Kim")
        assertTrue(editor.state.value.selected.isEmpty())
        assertEquals(listOf(R.string.error_add_name), shown)
        assertEquals(listOf("Failed to add handler"), logged)
    }
}
