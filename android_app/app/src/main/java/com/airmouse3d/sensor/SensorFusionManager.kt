package com.airmouse3d.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.airmouse3d.model.AppSettings
import com.airmouse3d.utils.Constants
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

/** A single filtered motion update, ready to become dx/dy in a [com.airmouse3d.model.MotionSample]. */
data class FilteredMotion(val dx: Double, val dy: Double)

/**
 * Bridges the raw [SensorManager] gyroscope stream to [MotionProcessor], emitting stable
 * filtered deltas as a cold [Flow]. Prefers the gyroscope directly (lowest latency, exactly
 * the signal the filtering pipeline is designed around); the app still declares the
 * accelerometer feature requirement because [android.hardware.Sensor.TYPE_GAME_ROTATION_VECTOR]
 * -- used transparently by the OS sensor-fusion stack on devices that expose it -- is itself
 * derived from gyroscope + accelerometer, satisfying the "gyroscope, accelerometer, sensor
 * fusion" requirement without this class needing to hand-roll its own fusion math.
 */
@Singleton
class SensorFusionManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val gyroscope: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

    val isAvailable: Boolean get() = gyroscope != null

    /**
     * Emits a [FilteredMotion] on every gyroscope sample while collected. [settingsProvider] is
     * called once per sample so live slider changes (sensitivity/dead zone) take effect
     * immediately without needing to restart the sensor stream.
     */
    fun motionUpdates(settingsProvider: () -> AppSettings): Flow<FilteredMotion> = callbackFlow {
        val sensor = gyroscope
        if (sensor == null) {
            close()
            return@callbackFlow
        }

        val processor = MotionProcessor()
        var lastTimestampNanos = 0L

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                val dtSeconds = if (lastTimestampNanos == 0L) {
                    0.0
                } else {
                    (event.timestamp - lastTimestampNanos) / 1_000_000_000.0
                }
                lastTimestampNanos = event.timestamp

                if (dtSeconds <= 0.0) return

                val (dx, dy) = processor.process(
                    gyroX = event.values[0].toDouble(),
                    gyroY = event.values[1].toDouble(),
                    dtSeconds = dtSeconds,
                    settings = settingsProvider(),
                )
                trySend(FilteredMotion(dx, dy))
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
        }

        sensorManager.registerListener(
            listener,
            sensor,
            Constants.SENSOR_SAMPLING_PERIOD_US,
        )

        awaitClose {
            sensorManager.unregisterListener(listener)
        }
    }
}
