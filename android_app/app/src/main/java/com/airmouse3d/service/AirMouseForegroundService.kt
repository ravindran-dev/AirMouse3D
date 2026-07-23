package com.airmouse3d.service

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.airmouse3d.model.AppError
import com.airmouse3d.model.AppSettings
import com.airmouse3d.model.ClickType
import com.airmouse3d.model.MotionSample
import com.airmouse3d.model.SensorStatus
import com.airmouse3d.model.TrackingState
import com.airmouse3d.repository.AirMouseStateRepository
import com.airmouse3d.repository.ConnectionRepository
import com.airmouse3d.repository.SettingsRepository
import com.airmouse3d.sensor.GestureInputBus
import com.airmouse3d.sensor.SensorFusionManager
import com.airmouse3d.utils.Constants
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Foreground service that streams filtered gyroscope motion plus touch-driven scroll/click
 * straight to the paired PC over a direct LAN UDP socket, at [Constants.MOTION_UPLOAD_INTERVAL_MS]
 * cadence. Unlike the old Firebase-backed design, there is no session/pairing state to
 * re-establish on a hiccup: UDP send is fire-and-forget every tick regardless of whether the
 * PC is currently answering, and [ConnectionRepository.isReachable] (ack-based) self-heals the
 * moment the PC starts responding again -- nothing here needs to notice or react to that.
 */
@AndroidEntryPoint
class AirMouseForegroundService : LifecycleService() {

    @Inject lateinit var connectionRepository: ConnectionRepository
    @Inject lateinit var settingsRepository: SettingsRepository
    @Inject lateinit var sensorFusionManager: SensorFusionManager
    @Inject lateinit var gestureInputBus: GestureInputBus
    @Inject lateinit var stateRepository: AirMouseStateRepository
    @Inject lateinit var notificationHelper: NotificationHelper

    private var trackingJob: Job? = null

    @Volatile private var latestSettings = AppSettings()
    @Volatile private var latestDx = 0.0
    @Volatile private var latestDy = 0.0

    override fun onCreate() {
        super.onCreate()
        notificationHelper.ensureChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        when (intent?.action) {
            ACTION_START -> handleStart()
            ACTION_STOP -> handleStop()
        }
        return START_NOT_STICKY
    }

    private fun handleStart() {
        if (trackingJob?.isActive == true) return

        startForeground(Constants.NOTIFICATION_ID, notificationHelper.buildNotification())
        stateRepository.updateTrackingState(TrackingState.STARTING)

        trackingJob = lifecycleScope.launch {
            try {
                runTrackingSession()
            } finally {
                withContext(NonCancellable) {
                    stateRepository.updateTrackingState(TrackingState.STOPPED)
                    stateRepository.updateMotion(MotionSample.IDLE)
                }
            }
        }
    }

    private fun handleStop() {
        stateRepository.updateTrackingState(TrackingState.STOPPING)
        trackingJob?.cancel()
        trackingJob = null
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private suspend fun runTrackingSession() {
        if (!sensorFusionManager.isAvailable) {
            stateRepository.updateSensorStatus(SensorStatus.UNAVAILABLE)
            stateRepository.reportError(AppError.SensorsUnavailable)
            return
        }
        stateRepository.updateSensorStatus(SensorStatus.AVAILABLE)

        val pcAddress = connectionRepository.ensureConnected()
        if (pcAddress == null) {
            stateRepository.reportError(AppError.NotPaired)
            return
        }
        stateRepository.reportError(null)
        stateRepository.updateTrackingState(TrackingState.RUNNING)

        // A nested coroutineScope ties every child below to this function's own cancellation,
        // which in turn is `trackingJob` -- so Stop cancels all of it in one shot.
        coroutineScope {
            launch {
                settingsRepository.settings.collect { latestSettings = it }
            }
            launch {
                sensorFusionManager.motionUpdates { latestSettings }.collect { motion ->
                    latestDx = motion.dx
                    latestDy = motion.dy
                }
            }
            launch {
                while (true) {
                    val clickType = gestureInputBus.drainClick()
                    val sample = MotionSample(
                        dx = latestDx,
                        dy = latestDy,
                        scroll = gestureInputBus.drainScroll(),
                        click = clickType != ClickType.NONE,
                        clickType = clickType.name,
                        timestamp = System.currentTimeMillis(),
                    )

                    connectionRepository.sendMotion(sample)
                    stateRepository.updateMotion(sample)

                    delay(Constants.MOTION_UPLOAD_INTERVAL_MS)
                }
            }
        }
    }

    companion object {
        const val ACTION_START = "com.airmouse3d.action.START_TRACKING"
        const val ACTION_STOP = "com.airmouse3d.action.STOP_TRACKING"

        fun start(context: Context) {
            val intent = Intent(context, AirMouseForegroundService::class.java).setAction(ACTION_START)
            ContextCompat.startForegroundService(context, intent)
        }

        fun stop(context: Context) {
            val intent = Intent(context, AirMouseForegroundService::class.java).setAction(ACTION_STOP)
            context.startService(intent)
        }
    }
}
