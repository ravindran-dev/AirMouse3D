package com.airmouse3d.utils

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.airmouse3d.di.ConnectionStore
import com.airmouse3d.model.PcAddress
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/** Persists the PC address learned from the pairing QR code, so re-pairing isn't needed every launch. */
@Singleton
class PcAddressStore @Inject constructor(
    @ConnectionStore private val dataStore: DataStore<Preferences>,
) {
    private val hostKey = stringPreferencesKey("pc_host")
    private val portKey = intPreferencesKey("pc_port")

    val pcAddress: Flow<PcAddress?> = dataStore.data.map { prefs ->
        val host = prefs[hostKey] ?: return@map null
        val port = prefs[portKey] ?: Constants.DEFAULT_PC_UDP_PORT
        PcAddress(host, port)
    }

    suspend fun save(address: PcAddress) {
        dataStore.edit {
            it[hostKey] = address.host
            it[portKey] = address.port
        }
    }

    suspend fun clear() {
        dataStore.edit {
            it.remove(hostKey)
            it.remove(portKey)
        }
    }
}
