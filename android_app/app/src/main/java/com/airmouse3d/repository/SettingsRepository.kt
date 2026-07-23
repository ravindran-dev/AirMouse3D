package com.airmouse3d.repository

import com.airmouse3d.model.AppSettings
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val settings: Flow<AppSettings>
    suspend fun setSensitivity(value: Float)
}
