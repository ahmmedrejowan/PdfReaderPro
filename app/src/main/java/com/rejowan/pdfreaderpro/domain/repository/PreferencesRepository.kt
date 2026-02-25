package com.rejowan.pdfreaderpro.domain.repository

import com.rejowan.pdfreaderpro.domain.model.AccentColor
import com.rejowan.pdfreaderpro.domain.model.AppPreferences
import com.rejowan.pdfreaderpro.domain.model.ScrollDirection
import com.rejowan.pdfreaderpro.domain.model.SortOption
import com.rejowan.pdfreaderpro.domain.model.ThemeMode
import com.rejowan.pdfreaderpro.domain.model.ViewMode
import kotlinx.coroutines.flow.Flow

interface PreferencesRepository {
    val preferences: Flow<AppPreferences>

    suspend fun setFirstLaunch(isFirst: Boolean)
    suspend fun setOnboardingCompleted(completed: Boolean)
    suspend fun setThemeMode(mode: ThemeMode)
    suspend fun setAccentColor(color: AccentColor)
    suspend fun setDefaultViewMode(mode: ViewMode)
    suspend fun setDefaultSortOption(option: SortOption)
    suspend fun setKeepScreenOn(enabled: Boolean)
    suspend fun setRememberPasswords(enabled: Boolean)
    suspend fun setDefaultScrollDirection(direction: ScrollDirection)
}
