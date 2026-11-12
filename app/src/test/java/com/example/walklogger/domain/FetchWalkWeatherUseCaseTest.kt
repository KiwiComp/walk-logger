package com.example.walklogger.domain

import com.example.walklogger.core.model.Coordinates
import com.example.walklogger.core.model.Place
import com.example.walklogger.core.model.WalkLocation
import com.example.walklogger.testing.FakeLocationProvider
import com.example.walklogger.testing.FakePlaceRepository
import com.example.walklogger.testing.FakeWeatherRepository
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZoneOffset
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FetchWalkWeatherUseCaseTest {

    private val now = Instant.parse("2026-11-09T12:00:00Z")
    private val locationProvider = FakeLocationProvider()
    private val placeRepository = FakePlaceRepository()
    private val weatherRepository = FakeWeatherRepository()
    private val useCase =
        FetchWalkWeatherUseCase(
            locationProvider,
            placeRepository,
            weatherRepository,
            Clock.fixed(now, ZoneOffset.UTC),
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
        assertEquals(locationProvider.location, coordinates)
        assertEquals(Instant.ofEpochMilli(start + 20 * 60_000), time)
    }

    @Test
    fun `falls back to the saved place without permission or a fix`() = runTest {
        placeRepository.savePlace(uppsala)
        val (start, end) = walkEndingAgo(Duration.ofHours(1))
        locationProvider.isGranted = false
        useCase(start, end)
        locationProvider.isGranted = true
        locationProvider.location = null
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
        placeRepository.saveActiveWalkLocation(WalkLocation(walkId = 4, uppsala.coordinates))
        locationProvider.isGranted = false
        val (start, end) = walkEndingAgo(Duration.ofHours(1))
        assertTrue(useCase(start, end, walkId = 4) is WeatherFetchResult.Fetched)
        assertEquals(uppsala.coordinates, weatherRepository.requests.single().first)
    }

    @Test
    fun `ignores a position stored for another walk`() = runTest {
        placeRepository.saveActiveWalkLocation(WalkLocation(walkId = 4, uppsala.coordinates))
        val (start, end) = walkEndingAgo(Duration.ofHours(1))
        useCase(start, end, walkId = 5)
        useCase(start, end)
        assertEquals(
            listOf(locationProvider.location, locationProvider.location),
            weatherRepository.requests.map {
                it.first
            },
        )
    }

    @Test
    fun `without location or place there is nothing to fetch for`() = runTest {
        locationProvider.isGranted = false
        val (start, end) = walkEndingAgo(Duration.ofHours(1))
        assertEquals(WeatherFetchResult.NoLocation, useCase(start, end))
        assertTrue(weatherRepository.requests.isEmpty())
    }

    @Test
    fun `walks from before the forecast's 92 days are too old`() = runTest {
        val (oldStart, oldEnd) = walkEndingAgo(Duration.ofDays(93))
        assertEquals(WeatherFetchResult.TooOld, useCase(oldStart, oldEnd))
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
