package com.airmouse3d.repository

import com.airmouse3d.model.AppSettings
import com.airmouse3d.utils.SettingsDataStore
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    private val settingsDataStore: SettingsDataStore,
) : SettingsRepository {

    override val settings: Flow<AppSettings> = settingsDataStore.settings

    override suspend fun setSensitivity(value: Float) = settingsDataStore.setSensitivity(value)
}
