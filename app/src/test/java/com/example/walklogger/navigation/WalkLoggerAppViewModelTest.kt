package com.example.walklogger.navigation

import app.cash.turbine.test
import com.example.walklogger.core.model.Walk
import com.example.walklogger.testing.FakeWalkRepository
import com.example.walklogger.testing.MainDispatcherRule
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

class WalkLoggerAppViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val walkRepository = FakeWalkRepository()
    private val viewModel by lazy { WalkLoggerAppViewModel(walkRepository) }

    @Test
    fun `nothing to resume without an active walk`() = runTest {
        walkRepository.addWalks(
            Walk(id = 1, dogId = 1, handlers = listOf("Anna"), startTime = 0, endTime = 1),
        )
        viewModel.walkToResume.test {
            assertNull(expectMostRecentItem())
        }
    }

    @Test
    fun `an active walk is offered once and cleared after resuming`() = runTest {
        walkRepository.addWalks(
            Walk(id = 7, dogId = 1, handlers = listOf("Anna"), startTime = 0, endTime = null),
        )
        viewModel.walkToResume.test {
            assertEquals(7L, expectMostRecentItem())
            viewModel.onWalkResumed()
            assertNull(expectMostRecentItem())
        }
    }
}
