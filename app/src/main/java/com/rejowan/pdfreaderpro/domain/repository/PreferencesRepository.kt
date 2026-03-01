package com.rejowan.pdfreaderpro.domain.repository

import com.rejowan.pdfreaderpro.domain.model.AppPreferences
import com.rejowan.pdfreaderpro.domain.model.PageAlignment
import com.rejowan.pdfreaderpro.domain.model.PageLayout
import com.rejowan.pdfreaderpro.domain.model.QuickZoomPreset
import com.rejowan.pdfreaderpro.domain.model.ReadingTheme
import com.rejowan.pdfreaderpro.domain.model.ScrollDirection
import com.rejowan.pdfreaderpro.domain.model.SortOption
import com.rejowan.pdfreaderpro.domain.model.ThemeMode
import com.rejowan.pdfreaderpro.domain.model.ViewMode
import kotlinx.coroutines.flow.Flow

interface PreferencesRepository {
    val preferences: Flow<AppPreferences>

    // App settings
    suspend fun setFirstLaunch(isFirst: Boolean)
    suspend fun setOnboardingCompleted(completed: Boolean)
    suspend fun setThemeMode(mode: ThemeMode)
    suspend fun setDefaultViewMode(mode: ViewMode)
    suspend fun setDefaultSortOption(option: SortOption)
    suspend fun setRememberPasswords(enabled: Boolean)

    // Reader settings
    suspend fun setReaderBrightness(brightness: Float)
    suspend fun setReaderScrollDirection(direction: ScrollDirection)
    suspend fun setReaderPageLayout(layout: PageLayout)
    suspend fun setReaderPageAlignment(alignment: PageAlignment)
    suspend fun setReaderAutoHideToolbar(enabled: Boolean)
    suspend fun setReaderQuickZoomPreset(preset: QuickZoomPreset)
    suspend fun setReaderKeepScreenOn(enabled: Boolean)
    suspend fun setReaderTheme(theme: ReadingTheme)
}
