package com.rejowan.pdfreaderpro.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.rejowan.pdfreaderpro.domain.model.AccentColor
import com.rejowan.pdfreaderpro.domain.model.AppPreferences
import com.rejowan.pdfreaderpro.domain.model.ScrollDirection
import com.rejowan.pdfreaderpro.domain.model.SortOption
import com.rejowan.pdfreaderpro.domain.model.ThemeMode
import com.rejowan.pdfreaderpro.domain.model.ViewMode
import com.rejowan.pdfreaderpro.domain.repository.PreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PreferencesRepositoryImpl(
    private val dataStore: DataStore<Preferences>
) : PreferencesRepository {

    private object Keys {
        val IS_FIRST_LAUNCH = booleanPreferencesKey("is_first_launch")
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val ACCENT_COLOR = stringPreferencesKey("accent_color")
        val DEFAULT_VIEW_MODE = stringPreferencesKey("default_view_mode")
        val DEFAULT_SORT_OPTION = stringPreferencesKey("default_sort_option")
        val KEEP_SCREEN_ON = booleanPreferencesKey("keep_screen_on")
        val REMEMBER_PASSWORDS = booleanPreferencesKey("remember_passwords")
        val DEFAULT_SCROLL_DIRECTION = stringPreferencesKey("default_scroll_direction")
    }

    override val preferences: Flow<AppPreferences> = dataStore.data.map { prefs ->
        AppPreferences(
            isFirstLaunch = prefs[Keys.IS_FIRST_LAUNCH] ?: true,
            hasCompletedOnboarding = prefs[Keys.ONBOARDING_COMPLETED] ?: false,
            themeMode = prefs[Keys.THEME_MODE]?.let { ThemeMode.valueOf(it) } ?: ThemeMode.SYSTEM,
            accentColor = prefs[Keys.ACCENT_COLOR]?.let { AccentColor.valueOf(it) } ?: AccentColor.DEFAULT,
            defaultViewMode = prefs[Keys.DEFAULT_VIEW_MODE]?.let { ViewMode.valueOf(it) } ?: ViewMode.LIST,
            defaultSortOption = prefs[Keys.DEFAULT_SORT_OPTION]?.let { SortOption.valueOf(it) } ?: SortOption.NAME_ASC,
            keepScreenOn = prefs[Keys.KEEP_SCREEN_ON] ?: false,
            rememberPasswords = prefs[Keys.REMEMBER_PASSWORDS] ?: true,
            defaultScrollDirection = prefs[Keys.DEFAULT_SCROLL_DIRECTION]?.let { ScrollDirection.valueOf(it) } ?: ScrollDirection.VERTICAL
        )
    }

    override suspend fun setFirstLaunch(isFirst: Boolean) {
        dataStore.edit { it[Keys.IS_FIRST_LAUNCH] = isFirst }
    }

    override suspend fun setOnboardingCompleted(completed: Boolean) {
        dataStore.edit { it[Keys.ONBOARDING_COMPLETED] = completed }
    }

    override suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.edit { it[Keys.THEME_MODE] = mode.name }
    }

    override suspend fun setAccentColor(color: AccentColor) {
        dataStore.edit { it[Keys.ACCENT_COLOR] = color.name }
    }

    override suspend fun setDefaultViewMode(mode: ViewMode) {
        dataStore.edit { it[Keys.DEFAULT_VIEW_MODE] = mode.name }
    }

    override suspend fun setDefaultSortOption(option: SortOption) {
        dataStore.edit { it[Keys.DEFAULT_SORT_OPTION] = option.name }
    }

    override suspend fun setKeepScreenOn(enabled: Boolean) {
        dataStore.edit { it[Keys.KEEP_SCREEN_ON] = enabled }
    }

    override suspend fun setRememberPasswords(enabled: Boolean) {
        dataStore.edit { it[Keys.REMEMBER_PASSWORDS] = enabled }
    }

    override suspend fun setDefaultScrollDirection(direction: ScrollDirection) {
        dataStore.edit { it[Keys.DEFAULT_SCROLL_DIRECTION] = direction.name }
    }
}
