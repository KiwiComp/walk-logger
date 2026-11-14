package com.example.walklogger.core.common

import com.example.walklogger.testing.FakeErrorReporter
import java.io.IOException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ErrorReporterTest {

    private val errorReporter = FakeErrorReporter()

    @Test
    fun `an offline failure is not reported`() {
        errorReporter.reportUnlessOffline(IOException(), "Failed")
        assertTrue(errorReporter.reported.isEmpty())
    }

    @Test
    fun `any other failure is reported`() {
        errorReporter.reportUnlessOffline(IllegalStateException(), "Failed")
        assertEquals(listOf("Failed"), errorReporter.reported.map { it.second })
    }
}
