package com.example.walklogger.core.data

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.walklogger.core.datastore.UserPreferencesDataSource
import com.example.walklogger.core.model.ThemeMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DefaultUserSettingsRepositoryTest {

    @get:Rule
    val folder = TemporaryFolder()

    private lateinit var dataStoreScope: CoroutineScope
    private lateinit var repository: DefaultUserSettingsRepository

    @Before
    fun setUp() {
        dataStoreScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
        val dataStore =
            PreferenceDataStoreFactory.create(scope = dataStoreScope) {
                folder.newFile("prefs.preferences_pb")
            }
        repository = DefaultUserSettingsRepository(UserPreferencesDataSource(dataStore))
    }

    @After
    fun tearDown() {
        dataStoreScope.cancel()
    }

    @Test
    fun themeMode_nothingSaved_followsTheSystem() = runBlocking {
        assertEquals(ThemeMode.System, repository.themeMode.first())
    }

    @Test
    fun setThemeMode_isReadBack() = runBlocking {
        repository.setThemeMode(ThemeMode.Dark)

        assertEquals(ThemeMode.Dark, repository.themeMode.first())
    }
}
