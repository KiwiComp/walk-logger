package com.example.walklogger.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class StoolEditsTest {

    private val described = Stool(passed = true, quantity = StoolQuantity.LARGE, texture = StoolTexture.SOFT, colour = StoolColour.GREY)

    @Test
    fun `yes starts an empty description`() {
        assertEquals(Stool(passed = true), null.withPassed(true))
        assertEquals(Stool(passed = true), Stool(passed = false).withPassed(true))
    }

    @Test
    fun `yes again keeps the description`() {
        assertEquals(described, described.withPassed(true))
    }

    @Test
    fun `no drops the description`() {
        assertEquals(Stool(passed = false), described.withPassed(false))
        assertEquals(Stool(passed = false), null.withPassed(false))
    }

    @Test
    fun `description rows apply after yes`() {
        val stool = Stool(passed = true)
            .withQuantity(StoolQuantity.SMALL)
            .withTexture(StoolTexture.FIRM)
            .withColour(StoolColour.DARK_BROWN)
        assertEquals(Stool(true, StoolQuantity.SMALL, StoolTexture.FIRM, StoolColour.DARK_BROWN), stool)
        assertEquals(Stool(true, StoolQuantity.SMALL, StoolTexture.FIRM), stool.withColour(null))
    }

    @Test
    fun `description rows are ignored before yes`() {
        assertNull(null.withQuantity(StoolQuantity.SMALL))
        assertNull(null.withTexture(StoolTexture.FIRM))
        assertEquals(Stool(passed = false), Stool(passed = false).withColour(StoolColour.GREY))
    }
}
