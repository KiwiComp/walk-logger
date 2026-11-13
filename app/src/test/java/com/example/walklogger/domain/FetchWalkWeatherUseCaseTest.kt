package com.example.walklogger.domain

import com.example.walklogger.core.model.Coordinates
import com.example.walklogger.core.model.Place
import com.example.walklogger.core.model.WalkLocation
import com.example.walklogger.testing.FakeActiveWalkLocationRepository
import com.example.walklogger.testing.FakeLocationRepository
import com.example.walklogger.testing.FakeWeatherRepository
import java.time.Duration
import java.time.Instant
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FetchWalkWeatherUseCaseTest {

    private val now = Instant.parse("2026-11-09T12:00:00Z")
    private val locationRepository = FakeLocationRepository()
    private val weatherRepository = FakeWeatherRepository()
    private val activeWalkLocationRepository = FakeActiveWalkLocationRepository()
    private val useCase =
        FetchWalkWeatherUseCase(
            locationRepository,
            weatherRepository,
            activeWalkLocationRepository,
        )
    private val uppsala = Place("Uppsala", null, Coordinates(59.86, 17.64))

    private fun walkEndingAgo(ago: Duration): Pair<Long, Long> {
        val end = now.minus(ago)
        return end.minus(Duration.ofMinutes(40)).toEpochMilli() to end.toEpochMilli()
    }

    @Test
    fun `fetches for the device location at the middle of the walk`() = runTest {
        val (start, end) = walkEndingAgo(Duration.ofHours(1))
        assertEquals(WeatherFetchResult.Fetched(weatherRepository.weather), useCase(start, end))
        val (coordinates, time) = weatherRepository.requests.single()
        assertEquals(locationRepository.location, coordinates)
        assertEquals(Instant.ofEpochMilli(start + 20 * 60_000), time)
    }

    @Test
    fun `falls back to the saved place without permission or a fix`() = runTest {
        locationRepository.savePlace(uppsala)
        val (start, end) = walkEndingAgo(Duration.ofHours(1))
        locationRepository.isGranted = false
        useCase(start, end)
        locationRepository.isGranted = true
        locationRepository.location = null
        useCase(start, end)
        assertEquals(
            listOf(uppsala.coordinates, uppsala.coordinates),
            weatherRepository.requests.map {
                it.first
            },
        )
    }

    @Test
    fun `uses the position stored when the walk started, even without permission`() = runTest {
        activeWalkLocationRepository.save(WalkLocation(walkId = 4, uppsala.coordinates))
        locationRepository.isGranted = false
        val (start, end) = walkEndingAgo(Duration.ofHours(1))
        assertTrue(useCase(start, end, walkId = 4) is WeatherFetchResult.Fetched)
        assertEquals(uppsala.coordinates, weatherRepository.requests.single().first)
    }

    @Test
    fun `ignores a position stored for another walk`() = runTest {
        activeWalkLocationRepository.save(WalkLocation(walkId = 4, uppsala.coordinates))
        val (start, end) = walkEndingAgo(Duration.ofHours(1))
        useCase(start, end, walkId = 5)
        useCase(start, end)
        assertEquals(
            listOf(locationRepository.location, locationRepository.location),
            weatherRepository.requests.map {
                it.first
            },
        )
    }

    @Test
    fun `without location or place there is nothing to fetch for`() = runTest {
        locationRepository.isGranted = false
        val (start, end) = walkEndingAgo(Duration.ofHours(1))
        assertEquals(WeatherFetchResult.NoLocation, useCase(start, end))
        assertTrue(weatherRepository.requests.isEmpty())
    }

    @Test
    fun `walks the weather source no longer reaches are too old`() = runTest {
        weatherRepository.oldestAvailable = now.minus(Duration.ofDays(92))
        val (oldStart, oldEnd) = walkEndingAgo(Duration.ofDays(93))
        assertEquals(WeatherFetchResult.TooOld, useCase(oldStart, oldEnd))
        assertTrue(weatherRepository.requests.isEmpty())
        val (start, end) = walkEndingAgo(Duration.ofDays(91))
        assertTrue(useCase(start, end) is WeatherFetchResult.Fetched)
    }

    @Test
    fun `a failed request is reported as failed`() = runTest {
        weatherRepository.fail = true
        val (start, end) = walkEndingAgo(Duration.ofHours(1))
        assertEquals(WeatherFetchResult.Failed, useCase(start, end))
    }
}
