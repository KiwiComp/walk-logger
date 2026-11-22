package com.example.walklogger.core.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Looper
import androidx.core.content.ContextCompat
import com.example.walklogger.core.model.Coordinates
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FusedLocationProvider @Inject constructor(@ApplicationContext private val context: Context) :
    LocationProvider {

    private val client = LocationServices.getFusedLocationProviderClient(context)

    override fun hasPermission(): Boolean = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION,
    ) == PackageManager.PERMISSION_GRANTED

    /** High accuracy may turn on GPS, so a position is found without Wi-Fi or cell positioning. */
    override suspend fun currentLocation(): Coordinates? {
        // Checked inline, not through hasPermission, so lint's MissingPermission check sees it.
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION,
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return null
        }
        val cancellation = CancellationTokenSource()
        val request = CurrentLocationRequest.Builder()
            .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
            .setMaxUpdateAgeMillis(MAX_LOCATION_AGE_MILLIS)
            .setDurationMillis(MAX_WAIT_MILLIS)
            .build()
        val location = client.getCurrentLocation(request, cancellation.token).await(cancellation)
            ?: client.lastLocation.await()
        return location?.let { Coordinates(it.latitude, it.longitude) }
    }

    override fun locationUpdates(): Flow<Coordinates> = callbackFlow {
        // Checked inline, not through hasPermission, so lint's MissingPermission check sees it.
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION,
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            close()
            return@callbackFlow
        }
        val request = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            UPDATE_INTERVAL_MILLIS,
        )
            .setMinUpdateDistanceMeters(MIN_UPDATE_DISTANCE_METERS)
            .build()
        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.locations.forEach { trySend(Coordinates(it.latitude, it.longitude)) }
            }
        }
        client.requestLocationUpdates(request, callback, Looper.getMainLooper())
            .addOnFailureListener { close(it) }
        awaitClose { client.removeLocationUpdates(callback) }
    }

    private companion object {
        /** Weather is per area and hour, so a position from the last half hour is good enough and returns at once. */
        val MAX_LOCATION_AGE_MILLIS = TimeUnit.MINUTES.toMillis(30)

        /** Without it a fresh fix can take up to 30 s before falling back to the last known location. */
        val MAX_WAIT_MILLIS = TimeUnit.SECONDS.toMillis(10)

        /** A route point at most every 5 s and only after 10 m, so standing still adds no points. */
        val UPDATE_INTERVAL_MILLIS = TimeUnit.SECONDS.toMillis(5)
        const val MIN_UPDATE_DISTANCE_METERS = 10f
    }
}
