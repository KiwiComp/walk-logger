package com.example.walklogger.core.ui.message

import com.example.walklogger.testing.FakeErrorReporter
import java.io.IOException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Test

class UserMessageStateTest {

    private val errorReporter = FakeErrorReporter()
    private val state = UserMessageState(errorReporter)
    private val error = IOException()

    @Test
    fun `an error is reported with its log message and shown`() {
        state.showAndReportError(error, 1, "Failed")
        val (reported, message) = errorReporter.reported.single()
        assertSame(error, reported)
        assertEquals("Failed", message)
        assertEquals(1, state.message.value)
    }

    @Test
    fun `a shown message is cleared`() {
        state.showAndReportError(error, 1, "Failed")
        state.shown(1)
        assertNull(state.message.value)
    }

    @Test
    fun `a newer message survives the older one being shown`() {
        state.showAndReportError(error, 1, "Failed")
        state.showAndReportError(error, 2, "Failed")
        state.shown(1)
        assertEquals(2, state.message.value)
    }
}
