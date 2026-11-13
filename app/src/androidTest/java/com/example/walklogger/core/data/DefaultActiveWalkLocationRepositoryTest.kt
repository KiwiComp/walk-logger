package com.example.walklogger.core.data

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.walklogger.core.datastore.UserPreferencesDataSource
import com.example.walklogger.core.model.Coordinates
import com.example.walklogger.core.model.WalkLocation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DefaultActiveWalkLocationRepositoryTest {

    @get:Rule
    val folder = TemporaryFolder()

    private lateinit var dataStoreScope: CoroutineScope
    private lateinit var repository: DefaultActiveWalkLocationRepository
    private val location = WalkLocation(walkId = 4, Coordinates(59.33, 18.07))

    @Before
    fun setUp() {
        dataStoreScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
        val dataStore =
            PreferenceDataStoreFactory.create(scope = dataStoreScope) {
                folder.newFile("prefs.preferences_pb")
            }
        repository = DefaultActiveWalkLocationRepository(UserPreferencesDataSource(dataStore))
    }

    @After
    fun tearDown() {
        dataStoreScope.cancel()
    }

    @Test
    fun clearFor_theStoredWalk_removesThePosition() = runBlocking {
        repository.save(location)

        repository.clearFor(4)

        assertNull(repository.activeWalkLocation.first())
    }

    @Test
    fun clearFor_anotherWalk_keepsThePosition() = runBlocking {
        repository.save(location)

        repository.clearFor(9)

        assertEquals(location, repository.activeWalkLocation.first())
    }
}
