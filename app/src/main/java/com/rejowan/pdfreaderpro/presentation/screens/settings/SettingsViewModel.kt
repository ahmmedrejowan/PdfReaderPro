package com.rejowan.pdfreaderpro.presentation.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rejowan.pdfreaderpro.domain.model.AppPreferences
import com.rejowan.pdfreaderpro.domain.model.PageAlignment
import com.rejowan.pdfreaderpro.domain.model.PageLayout
import com.rejowan.pdfreaderpro.domain.model.QuickZoomPreset
import com.rejowan.pdfreaderpro.domain.model.ReadingTheme
import com.rejowan.pdfreaderpro.domain.model.ScrollDirection
import com.rejowan.pdfreaderpro.domain.model.ThemeMode
import com.rejowan.pdfreaderpro.domain.repository.PreferencesRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {

    val preferences: StateFlow<AppPreferences> = preferencesRepository.preferences
        .stateIn(viewModelScope, SharingStarted.Eagerly, AppPreferences())

    // App settings
    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            preferencesRepository.setThemeMode(mode)
        }
    }

    // Reader settings
    fun setReaderBrightness(brightness: Float) {
        viewModelScope.launch {
            preferencesRepository.setReaderBrightness(brightness)
        }
    }

    fun setReaderScrollDirection(direction: ScrollDirection) {
        viewModelScope.launch {
            preferencesRepository.setReaderScrollDirection(direction)
        }
    }

    fun setReaderPageLayout(layout: PageLayout) {
        viewModelScope.launch {
            preferencesRepository.setReaderPageLayout(layout)
        }
    }

    fun setReaderPageAlignment(alignment: PageAlignment) {
        viewModelScope.launch {
            preferencesRepository.setReaderPageAlignment(alignment)
        }
    }

    fun setReaderAutoHideToolbar(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setReaderAutoHideToolbar(enabled)
        }
    }

    fun setReaderQuickZoomPreset(preset: QuickZoomPreset) {
        viewModelScope.launch {
            preferencesRepository.setReaderQuickZoomPreset(preset)
        }
    }

    fun setReaderKeepScreenOn(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setReaderKeepScreenOn(enabled)
        }
    }

    fun setReaderTheme(theme: ReadingTheme) {
        viewModelScope.launch {
            preferencesRepository.setReaderTheme(theme)
        }
    }
}
