package com.example.walklogger

import com.example.walklogger.core.model.ThemeMode
import com.example.walklogger.testing.FakeUserSettingsRepository
import com.example.walklogger.testing.MainDispatcherRule
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class MainActivityViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `the saved theme is loaded without a collector, so the first frame can be released`() =
        runTest {
            val viewModel = MainActivityViewModel(FakeUserSettingsRepository(ThemeMode.Dark))

            assertEquals(ThemeMode.Dark, viewModel.themeMode.value)
        }
}
