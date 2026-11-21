package com.example.walklogger.core.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.example.walklogger.core.model.Coordinates
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
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

    private companion object {
        /** Weather is per area and hour, so a position from the last half hour is good enough and returns at once. */
        val MAX_LOCATION_AGE_MILLIS = TimeUnit.MINUTES.toMillis(30)

        /** Without it a fresh fix can take up to 30 s before falling back to the last known location. */
        val MAX_WAIT_MILLIS = TimeUnit.SECONDS.toMillis(10)
    }
}
