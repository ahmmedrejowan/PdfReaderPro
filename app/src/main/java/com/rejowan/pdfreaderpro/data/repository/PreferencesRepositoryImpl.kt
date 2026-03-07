package com.rejowan.pdfreaderpro.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.rejowan.pdfreaderpro.domain.model.AppPreferences
import com.rejowan.pdfreaderpro.domain.model.PageAlignment
import com.rejowan.pdfreaderpro.domain.model.PageLayout
import com.rejowan.pdfreaderpro.domain.model.QuickZoomPreset
import com.rejowan.pdfreaderpro.domain.model.ReadingTheme
import com.rejowan.pdfreaderpro.domain.model.ScrollDirection
import com.rejowan.pdfreaderpro.domain.model.SortOption
import com.rejowan.pdfreaderpro.domain.model.ThemeMode
import com.rejowan.pdfreaderpro.domain.model.UpdateCheckInterval
import com.rejowan.pdfreaderpro.domain.model.ViewMode
import com.rejowan.pdfreaderpro.domain.repository.PreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PreferencesRepositoryImpl(
    private val dataStore: DataStore<Preferences>
) : PreferencesRepository {

    private object Keys {
        // App settings
        val IS_FIRST_LAUNCH = booleanPreferencesKey("is_first_launch")
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val DEFAULT_VIEW_MODE = stringPreferencesKey("default_view_mode")
        val DEFAULT_SORT_OPTION = stringPreferencesKey("default_sort_option")
        val REMEMBER_PASSWORDS = booleanPreferencesKey("remember_passwords")
        val UPDATE_CHECK_INTERVAL = stringPreferencesKey("update_check_interval")

        // Reader settings
        val READER_BRIGHTNESS = floatPreferencesKey("reader_brightness")
        val READER_SCROLL_DIRECTION = stringPreferencesKey("reader_scroll_direction")
        val READER_PAGE_LAYOUT = stringPreferencesKey("reader_page_layout")
        val READER_PAGE_ALIGNMENT = stringPreferencesKey("reader_page_alignment")
        val READER_AUTO_HIDE_TOOLBAR = booleanPreferencesKey("reader_auto_hide_toolbar")
        val READER_QUICK_ZOOM_PRESET = stringPreferencesKey("reader_quick_zoom_preset")
        val READER_KEEP_SCREEN_ON = booleanPreferencesKey("reader_keep_screen_on")
        val READER_THEME = stringPreferencesKey("reader_theme")
    }

    override val preferences: Flow<AppPreferences> = dataStore.data.map { prefs ->
        AppPreferences(
            // App settings
            isFirstLaunch = prefs[Keys.IS_FIRST_LAUNCH] ?: true,
            hasCompletedOnboarding = prefs[Keys.ONBOARDING_COMPLETED] ?: false,
            themeMode = prefs[Keys.THEME_MODE]?.let { ThemeMode.valueOf(it) } ?: ThemeMode.SYSTEM,
            defaultViewMode = prefs[Keys.DEFAULT_VIEW_MODE]?.let { ViewMode.valueOf(it) } ?: ViewMode.LIST,
            defaultSortOption = prefs[Keys.DEFAULT_SORT_OPTION]?.let { SortOption.valueOf(it) } ?: SortOption.NAME_ASC,
            rememberPasswords = prefs[Keys.REMEMBER_PASSWORDS] ?: true,
            updateCheckInterval = prefs[Keys.UPDATE_CHECK_INTERVAL]?.let { UpdateCheckInterval.valueOf(it) } ?: UpdateCheckInterval.WEEKLY,

            // Reader settings
            readerBrightness = prefs[Keys.READER_BRIGHTNESS] ?: -1f,
            readerScrollDirection = prefs[Keys.READER_SCROLL_DIRECTION]?.let { ScrollDirection.valueOf(it) } ?: ScrollDirection.VERTICAL,
            readerPageLayout = prefs[Keys.READER_PAGE_LAYOUT]?.let { PageLayout.valueOf(it) } ?: PageLayout.CONTINUOUS,
            readerPageAlignment = prefs[Keys.READER_PAGE_ALIGNMENT]?.let { PageAlignment.valueOf(it) } ?: PageAlignment.CENTER,
            readerAutoHideToolbar = prefs[Keys.READER_AUTO_HIDE_TOOLBAR] ?: false,
            readerQuickZoomPreset = prefs[Keys.READER_QUICK_ZOOM_PRESET]?.let { QuickZoomPreset.valueOf(it) } ?: QuickZoomPreset.FIT_WIDTH,
            readerKeepScreenOn = prefs[Keys.READER_KEEP_SCREEN_ON] ?: false,
            readerTheme = prefs[Keys.READER_THEME]?.let { ReadingTheme.valueOf(it) } ?: ReadingTheme.LIGHT
        )
    }

    // App settings
    override suspend fun setFirstLaunch(isFirst: Boolean) {
        dataStore.edit { it[Keys.IS_FIRST_LAUNCH] = isFirst }
    }

    override suspend fun setOnboardingCompleted(completed: Boolean) {
        dataStore.edit { it[Keys.ONBOARDING_COMPLETED] = completed }
    }

    override suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.edit { it[Keys.THEME_MODE] = mode.name }
    }

    override suspend fun setDefaultViewMode(mode: ViewMode) {
        dataStore.edit { it[Keys.DEFAULT_VIEW_MODE] = mode.name }
    }

    override suspend fun setDefaultSortOption(option: SortOption) {
        dataStore.edit { it[Keys.DEFAULT_SORT_OPTION] = option.name }
    }

    override suspend fun setRememberPasswords(enabled: Boolean) {
        dataStore.edit { it[Keys.REMEMBER_PASSWORDS] = enabled }
    }

    override suspend fun setUpdateCheckInterval(interval: UpdateCheckInterval) {
        dataStore.edit { it[Keys.UPDATE_CHECK_INTERVAL] = interval.name }
    }

    // Reader settings
    override suspend fun setReaderBrightness(brightness: Float) {
        dataStore.edit { it[Keys.READER_BRIGHTNESS] = brightness }
    }

    override suspend fun setReaderScrollDirection(direction: ScrollDirection) {
        dataStore.edit { it[Keys.READER_SCROLL_DIRECTION] = direction.name }
    }

    override suspend fun setReaderPageLayout(layout: PageLayout) {
        dataStore.edit { it[Keys.READER_PAGE_LAYOUT] = layout.name }
    }

    override suspend fun setReaderPageAlignment(alignment: PageAlignment) {
        dataStore.edit { it[Keys.READER_PAGE_ALIGNMENT] = alignment.name }
    }

    override suspend fun setReaderAutoHideToolbar(enabled: Boolean) {
        dataStore.edit { it[Keys.READER_AUTO_HIDE_TOOLBAR] = enabled }
    }

    override suspend fun setReaderQuickZoomPreset(preset: QuickZoomPreset) {
        dataStore.edit { it[Keys.READER_QUICK_ZOOM_PRESET] = preset.name }
    }

    override suspend fun setReaderKeepScreenOn(enabled: Boolean) {
        dataStore.edit { it[Keys.READER_KEEP_SCREEN_ON] = enabled }
    }

    override suspend fun setReaderTheme(theme: ReadingTheme) {
        dataStore.edit { it[Keys.READER_THEME] = theme.name }
    }
}
