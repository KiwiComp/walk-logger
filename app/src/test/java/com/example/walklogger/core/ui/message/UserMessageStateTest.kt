package com.example.walklogger.core.ui.message

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class UserMessageStateTest {

    private val state = UserMessageState()

    @Test
    fun `a shown message is cleared`() {
        state.show(1)
        state.shown(1)
        assertNull(state.message.value)
    }

    @Test
    fun `a newer message survives the older one being shown`() {
        state.show(1)
        state.show(2)
        state.shown(1)
        assertEquals(2, state.message.value)
    }
}
