package com.example.walklogger.core.data

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.walklogger.core.database.WalkLoggerDatabase
import com.example.walklogger.core.database.entity.WalkEntity
import com.example.walklogger.core.model.Coordinates
import com.example.walklogger.core.model.RoutePoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DefaultWalkRouteRepositoryTest {

    private lateinit var database: WalkLoggerDatabase
    private lateinit var repository: DefaultWalkRouteRepository

    private val first = RoutePoint(time = 1_000, coordinates = Coordinates(59.3293, 18.0686))
    private val second = RoutePoint(time = 6_000, coordinates = Coordinates(59.3294, 18.0688))

    @Before
    fun setUp() = runBlocking {
        database =
            Room.inMemoryDatabaseBuilder(
                ApplicationProvider.getApplicationContext(),
                WalkLoggerDatabase::class.java,
            ).build()
        repository = DefaultWalkRouteRepository(database.routePointDao(), Dispatchers.IO)
        listOf(WALK, OTHER_WALK).forEach {
            database.walkDao().insertWalk(
                WalkEntity(id = it, dogId = 1, handlers = listOf("Anna"), startTime = 0),
            )
        }
    }

    @After
    fun tearDown() = database.close()

    @Test
    fun observeRoute_returnsOnlyThatWalksPointsOldestFirst() = runBlocking {
        repository.addPoint(WALK, second)
        repository.addPoint(OTHER_WALK, first)
        repository.addPoint(WALK, first)

        assertEquals(listOf(first, second), repository.observeRoute(WALK).first())
    }

    @Test
    fun observeRoutes_returnsOnlyTheAskedWalksPointsOldestFirst() = runBlocking {
        repository.addPoint(WALK, second)
        repository.addPoint(OTHER_WALK, first)
        repository.addPoint(WALK, first)

        assertEquals(
            mapOf(WALK to listOf(first, second)),
            repository.observeRoutes(listOf(WALK)).first(),
        )
    }

    @Test
    fun deletingTheWalk_removesItsRoute() = runBlocking {
        repository.addPoint(WALK, first)

        database.walkDao().deleteWalk(WALK)

        assertTrue(repository.observeRoute(WALK).first().isEmpty())
    }

    private companion object {
        const val WALK = 1L
        const val OTHER_WALK = 2L
    }
}
