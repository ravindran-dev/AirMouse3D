package com.airmouse3d.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.airmouse3d.utils.Constants
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(
    name = Constants.DATASTORE_SETTINGS_NAME,
)
private val Context.connectionDataStore: DataStore<Preferences> by preferencesDataStore(
    name = Constants.DATASTORE_CONNECTION_NAME,
)

@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {

    @Provides
    @Singleton
    @SettingsStore
    fun provideSettingsDataStore(@ApplicationContext context: Context): DataStore<Preferences> =
        context.settingsDataStore

    @Provides
    @Singleton
    @ConnectionStore
    fun provideConnectionDataStore(@ApplicationContext context: Context): DataStore<Preferences> =
        context.connectionDataStore
}
