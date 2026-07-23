package com.airmouse3d.repository

import com.airmouse3d.model.AppError
import com.airmouse3d.model.MotionSample
import com.airmouse3d.model.SensorStatus
import com.airmouse3d.model.TrackingState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * In-memory state bus shared between [com.airmouse3d.service.AirMouseForegroundService]
 * (producer) and the UI layer (consumer). A foreground service and a Compose screen live in
 * the same process but not the same object graph reachable through Compose alone, so rather
 * than a bound-service/Binder round trip, both sides simply inject this Hilt singleton: the
 * service publishes into it on every sensor/upload tick, the ViewModel exposes it straight
 * through to the UI as [StateFlow].
 */
@Singleton
class AirMouseStateRepository @Inject constructor() {

    private val _trackingState = MutableStateFlow(TrackingState.STOPPED)
    val trackingState: StateFlow<TrackingState> = _trackingState.asStateFlow()

    private val _currentMotion = MutableStateFlow(MotionSample.IDLE)
    val currentMotion: StateFlow<MotionSample> = _currentMotion.asStateFlow()

    private val _sensorStatus = MutableStateFlow(SensorStatus.UNKNOWN)
    val sensorStatus: StateFlow<SensorStatus> = _sensorStatus.asStateFlow()

    private val _lastError = MutableStateFlow<AppError?>(null)
    val lastError: StateFlow<AppError?> = _lastError.asStateFlow()

    fun updateTrackingState(state: TrackingState) {
        _trackingState.value = state
    }

    fun updateMotion(sample: MotionSample) {
        _currentMotion.value = sample
    }

    fun updateSensorStatus(status: SensorStatus) {
        _sensorStatus.value = status
    }

    fun reportError(error: AppError?) {
        _lastError.value = error
    }
}
