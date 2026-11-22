package com.example.walklogger.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.IBinder
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.ServiceCompat
import com.example.walklogger.R
import com.example.walklogger.core.common.di.Dispatcher
import com.example.walklogger.core.common.di.WalkLoggerDispatchers
import com.example.walklogger.domain.TrackWalkLocationUseCase
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * Records the start position and route of the active walk. A foreground service started while the app
 * is visible keeps "while in use" location access with the screen locked; it stops when the walk ends.
 * It is not sticky: Android refuses to restart a location service from the background, so after the
 * process is killed tracking resumes when the active walk is shown again.
 */
@AndroidEntryPoint
class WalkLocationService : Service() {

    @Inject lateinit var trackWalkLocation: TrackWalkLocationUseCase

    @Inject
    @Dispatcher(WalkLoggerDispatchers.Default)
    lateinit var defaultDispatcher: CoroutineDispatcher

    private val scope by lazy { CoroutineScope(SupervisorJob() + defaultDispatcher) }

    private var tracking: Job? = null
    private var trackedWalkId = NO_WALK

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        try {
            ServiceCompat.startForeground(
                this,
                NOTIFICATION_ID,
                notification(),
                ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION,
            )
        } catch (revoked: SecurityException) {
            // Location permission was revoked after the service was requested.
            stopSelf(startId)
            return START_NOT_STICKY
        }
        val walkId = intent?.getLongExtra(EXTRA_WALK_ID, NO_WALK) ?: NO_WALK
        val isTracking = tracking?.isActive == true
        when {
            walkId == NO_WALK -> if (!isTracking) stopSelf(startId)

            walkId == trackedWalkId && isTracking -> Unit

            else -> {
                tracking?.cancel()
                trackedWalkId = walkId
                tracking = scope.launch {
                    trackWalkLocation(walkId)
                    // Not stopSelf(startId): a repeated start for the same walk has a later id and would keep it running.
                    stopSelf()
                }
            }
        }
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    private fun notification() = run {
        NotificationManagerCompat.from(this).createNotificationChannel(
            NotificationChannelCompat.Builder(CHANNEL_ID, NotificationManagerCompat.IMPORTANCE_LOW)
                .setName(getString(R.string.walk_location_channel))
                .build(),
        )
        NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_weather_sunny)
            .setContentTitle(getString(R.string.walk_location_notification))
            .setOngoing(true)
            .build()
    }

    companion object {
        private const val CHANNEL_ID = "walk_location"
        private const val NOTIFICATION_ID = 1
        private const val EXTRA_WALK_ID = "walk_id"
        private const val NO_WALK = -1L

        fun intent(context: Context, walkId: Long): Intent =
            Intent(context, WalkLocationService::class.java).putExtra(EXTRA_WALK_ID, walkId)
    }
}
