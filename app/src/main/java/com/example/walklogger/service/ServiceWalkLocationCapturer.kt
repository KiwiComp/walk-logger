package com.example.walklogger.service

import android.content.Context
import androidx.core.content.ContextCompat
import com.example.walklogger.core.data.LocationRepository
import com.example.walklogger.core.data.WalkLocationCapturer
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/** Must be called while the app is visible; Android refuses to start a location service from the background. */
class ServiceWalkLocationCapturer @Inject constructor(
    @ApplicationContext private val context: Context,
    private val locationRepository: LocationRepository,
) : WalkLocationCapturer {

    override fun capture(walkId: Long) {
        if (!locationRepository.hasPermission()) return
        // Refused when the app has just left the foreground; the weather fetch then looks the position up itself.
        try {
            ContextCompat.startForegroundService(
                context,
                WalkLocationService.intent(context, walkId),
            )
        } catch (refused: IllegalStateException) {
        }
    }
}
