package com.rejowan.pdfreaderpro.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.rejowan.pdfreaderpro.domain.model.AppPreferences
import com.rejowan.pdfreaderpro.domain.model.QuickZoomPreset
import com.rejowan.pdfreaderpro.domain.model.ReadingTheme
import com.rejowan.pdfreaderpro.domain.model.ScreenOrientation
import com.rejowan.pdfreaderpro.domain.model.ScrollMode
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
        val READER_SCROLL_MODE = stringPreferencesKey("reader_scroll_mode")
        val READER_AUTO_HIDE_TOOLBAR = booleanPreferencesKey("reader_auto_hide_toolbar")
        val READER_QUICK_ZOOM_PRESET = stringPreferencesKey("reader_quick_zoom_preset")
        val READER_DOUBLE_TAP_ZOOM = floatPreferencesKey("reader_double_tap_zoom")
        val READER_KEEP_SCREEN_ON = booleanPreferencesKey("reader_keep_screen_on")
        val READER_THEME = stringPreferencesKey("reader_theme")
        val READER_SNAP_TO_PAGES = booleanPreferencesKey("reader_snap_to_pages")
        val READER_SCREEN_ORIENTATION = stringPreferencesKey("reader_screen_orientation")
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
            readerScrollMode = prefs[Keys.READER_SCROLL_MODE]?.let { ScrollMode.valueOf(it) } ?: ScrollMode.VERTICAL,
            readerAutoHideToolbar = prefs[Keys.READER_AUTO_HIDE_TOOLBAR] ?: false,
            readerQuickZoomPreset = prefs[Keys.READER_QUICK_ZOOM_PRESET]?.let { QuickZoomPreset.valueOf(it) } ?: QuickZoomPreset.FIT_WIDTH,
            readerDoubleTapZoom = prefs[Keys.READER_DOUBLE_TAP_ZOOM] ?: 2.0f,
            readerKeepScreenOn = prefs[Keys.READER_KEEP_SCREEN_ON] ?: false,
            readerTheme = prefs[Keys.READER_THEME]?.let { ReadingTheme.valueOf(it) } ?: ReadingTheme.LIGHT,
            readerSnapToPages = prefs[Keys.READER_SNAP_TO_PAGES] ?: false,
            readerScreenOrientation = prefs[Keys.READER_SCREEN_ORIENTATION]?.let { ScreenOrientation.valueOf(it) } ?: ScreenOrientation.AUTO
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

    override suspend fun setReaderScrollMode(mode: ScrollMode) {
        dataStore.edit { it[Keys.READER_SCROLL_MODE] = mode.name }
    }

    override suspend fun setReaderAutoHideToolbar(enabled: Boolean) {
        dataStore.edit { it[Keys.READER_AUTO_HIDE_TOOLBAR] = enabled }
    }

    override suspend fun setReaderQuickZoomPreset(preset: QuickZoomPreset) {
        dataStore.edit { it[Keys.READER_QUICK_ZOOM_PRESET] = preset.name }
    }

    override suspend fun setReaderDoubleTapZoom(zoom: Float) {
        dataStore.edit { it[Keys.READER_DOUBLE_TAP_ZOOM] = zoom }
    }

    override suspend fun setReaderKeepScreenOn(enabled: Boolean) {
        dataStore.edit { it[Keys.READER_KEEP_SCREEN_ON] = enabled }
    }

    override suspend fun setReaderTheme(theme: ReadingTheme) {
        dataStore.edit { it[Keys.READER_THEME] = theme.name }
    }

    override suspend fun setReaderSnapToPages(enabled: Boolean) {
        dataStore.edit { it[Keys.READER_SNAP_TO_PAGES] = enabled }
    }

    override suspend fun setReaderScreenOrientation(orientation: ScreenOrientation) {
        dataStore.edit { it[Keys.READER_SCREEN_ORIENTATION] = orientation.name }
    }
}
