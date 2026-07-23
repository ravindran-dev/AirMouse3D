package com.airmouse3d.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.airmouse3d.model.AppError
import com.airmouse3d.model.AppSettings
import com.airmouse3d.model.ClickType
import com.airmouse3d.model.PcAddress
import com.airmouse3d.model.SensorStatus
import com.airmouse3d.model.TrackingState
import com.airmouse3d.repository.AirMouseStateRepository
import com.airmouse3d.repository.ConnectionRepository
import com.airmouse3d.repository.SettingsRepository
import com.airmouse3d.sensor.GestureInputBus
import com.airmouse3d.service.AirMouseForegroundService
import com.airmouse3d.utils.NetworkMonitor
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val connectionRepository: ConnectionRepository,
    private val settingsRepository: SettingsRepository,
    private val stateRepository: AirMouseStateRepository,
    private val networkMonitor: NetworkMonitor,
    private val gestureInputBus: GestureInputBus,
) : ViewModel() {

    private data class CoreState(
        val tracking: TrackingState,
        val sensorStatus: SensorStatus,
        val error: AppError?,
        val settings: AppSettings,
    )

    private data class ConnState(
        val pcAddress: PcAddress?,
        val isReachable: Boolean,
        val isOnline: Boolean,
    )

    val uiState: StateFlow<HomeUiState> = combine(
        combine(
            stateRepository.trackingState,
            stateRepository.sensorStatus,
            stateRepository.lastError,
            settingsRepository.settings,
        ) { tracking, sensorStatus, error, settings -> CoreState(tracking, sensorStatus, error, settings) },
        combine(
            connectionRepository.pcAddress,
            connectionRepository.isReachable,
            networkMonitor.isOnline,
        ) { pcAddress, isReachable, isOnline -> ConnState(pcAddress, isReachable, isOnline) },
    ) { core, conn ->
        HomeUiState(
            pcAddress = conn.pcAddress,
            trackingState = core.tracking,
            isOnline = conn.isOnline,
            isReachable = conn.isReachable,
            sensorStatus = core.sensorStatus,
            settings = core.settings,
            error = core.error,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState(),
    )

    fun onStartClicked() {
        AirMouseForegroundService.start(context)
    }

    fun onStopClicked() {
        AirMouseForegroundService.stop(context)
    }

    fun onForgetPcClicked() {
        AirMouseForegroundService.stop(context)
        viewModelScope.launch { connectionRepository.forgetPc() }
    }

    fun onSensitivityChanged(value: Float) {
        viewModelScope.launch { settingsRepository.setSensitivity(value) }
    }

    fun onClickGesture(type: ClickType) {
        gestureInputBus.triggerClick(type)
    }

    fun onScrollDelta(delta: Double) {
        gestureInputBus.addScroll(delta)
    }
}
