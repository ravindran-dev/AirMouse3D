package com.airmouse3d.utils

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import com.airmouse3d.di.SettingsStore
import com.airmouse3d.model.AppSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Persists the user-tunable sensitivity slider and exposes it as a live [Flow]. Dead zone is
 * intentionally not user-configurable (kept at [AppSettings.DEFAULT_DEAD_ZONE]) to keep the UI
 * down to just click / scroll / sensitivity.
 */
@Singleton
class SettingsDataStore @Inject constructor(
    @SettingsStore private val dataStore: DataStore<Preferences>,
) {
    private val sensitivityKey = floatPreferencesKey("sensitivity")

    val settings: Flow<AppSettings> = dataStore.data.map { prefs ->
        AppSettings(sensitivity = prefs[sensitivityKey] ?: AppSettings.DEFAULT_SENSITIVITY)
    }

    suspend fun setSensitivity(value: Float) {
        dataStore.edit { it[sensitivityKey] = value }
    }
}
