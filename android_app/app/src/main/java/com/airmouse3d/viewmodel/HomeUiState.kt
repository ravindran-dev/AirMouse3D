package com.airmouse3d.viewmodel

import com.airmouse3d.model.AppError
import com.airmouse3d.model.AppSettings
import com.airmouse3d.model.PcAddress
import com.airmouse3d.model.SensorStatus
import com.airmouse3d.model.TrackingState

/** Everything the Home screen needs to render, assembled by [HomeViewModel]. */
data class HomeUiState(
    val pcAddress: PcAddress? = null,
    val trackingState: TrackingState = TrackingState.STOPPED,
    val isOnline: Boolean = false,
    val isReachable: Boolean = false,
    val sensorStatus: SensorStatus = SensorStatus.UNKNOWN,
    val settings: AppSettings = AppSettings(),
    val error: AppError? = null,
) {
    val isPaired: Boolean get() = pcAddress != null
    val isRunning: Boolean get() = trackingState == TrackingState.RUNNING || trackingState == TrackingState.STARTING
}
