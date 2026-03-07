package com.rejowan.pdfreaderpro.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

// DataStore instances
private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "settings"
)

private val Context.readerSettingsDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "reader_settings"
)

val dataStoreModule = module {
    // Settings DataStore
    single { androidContext().settingsDataStore }

    // Reader Settings DataStore (separate store for reader-specific preferences)
    single(qualifier = org.koin.core.qualifier.named("readerSettings")) {
        androidContext().readerSettingsDataStore
    }

    // DataStore managers will be added here
    // single { SettingsDataStore(get()) }
    // single { ReaderSettingsDataStore(get(named("readerSettings"))) }
}
