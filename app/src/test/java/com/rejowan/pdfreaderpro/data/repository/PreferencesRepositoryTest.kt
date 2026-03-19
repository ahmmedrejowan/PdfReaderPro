package com.rejowan.pdfreaderpro.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.preferencesOf
import androidx.datastore.preferences.core.stringPreferencesKey
import app.cash.turbine.test
import com.rejowan.pdfreaderpro.domain.model.PageAlignment
import com.rejowan.pdfreaderpro.domain.model.PageLayout
import com.rejowan.pdfreaderpro.domain.model.QuickZoomPreset
import com.rejowan.pdfreaderpro.domain.model.ReadingTheme
import com.rejowan.pdfreaderpro.domain.model.ScreenOrientation
import com.rejowan.pdfreaderpro.domain.model.ScrollDirection
import com.rejowan.pdfreaderpro.domain.model.SortOption
import com.rejowan.pdfreaderpro.domain.model.ThemeMode
import com.rejowan.pdfreaderpro.domain.model.ViewMode
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class PreferencesRepositoryTest {

    private lateinit var dataStore: DataStore<Preferences>
    private lateinit var repository: PreferencesRepositoryImpl

    // Preference keys matching PreferencesRepositoryImpl
    private val isFirstLaunchKey = booleanPreferencesKey("is_first_launch")
    private val onboardingCompletedKey = booleanPreferencesKey("onboarding_completed")
    private val themeModeKey = stringPreferencesKey("theme_mode")
    private val defaultViewModeKey = stringPreferencesKey("default_view_mode")
    private val defaultSortOptionKey = stringPreferencesKey("default_sort_option")
    private val rememberPasswordsKey = booleanPreferencesKey("remember_passwords")
    private val readerBrightnessKey = floatPreferencesKey("reader_brightness")
    private val readerScrollDirectionKey = stringPreferencesKey("reader_scroll_direction")
    private val readerPageLayoutKey = stringPreferencesKey("reader_page_layout")
    private val readerPageAlignmentKey = stringPreferencesKey("reader_page_alignment")
    private val readerAutoHideToolbarKey = booleanPreferencesKey("reader_auto_hide_toolbar")
    private val readerQuickZoomPresetKey = stringPreferencesKey("reader_quick_zoom_preset")
    private val readerKeepScreenOnKey = booleanPreferencesKey("reader_keep_screen_on")
    private val readerThemeKey = stringPreferencesKey("reader_theme")
    private val readerSnapToPagesKey = booleanPreferencesKey("reader_snap_to_pages")
    private val readerScreenOrientationKey = stringPreferencesKey("reader_screen_orientation")

    @Before
    fun setup() {
        dataStore = mockk(relaxed = true)
    }

    private fun createRepository(preferences: Preferences = preferencesOf()): PreferencesRepositoryImpl {
        every { dataStore.data } returns flowOf(preferences)
        return PreferencesRepositoryImpl(dataStore)
    }

    // region Default Values Tests
    @Test
    fun `preferences returns default values when empty`() = runTest {
        repository = createRepository()

        repository.preferences.test {
            val prefs = awaitItem()
            assertTrue(prefs.isFirstLaunch)
            assertFalse(prefs.hasCompletedOnboarding)
            assertEquals(ThemeMode.SYSTEM, prefs.themeMode)
            assertEquals(ViewMode.LIST, prefs.defaultViewMode)
            assertEquals(SortOption.NAME_ASC, prefs.defaultSortOption)
            assertTrue(prefs.rememberPasswords)
            assertEquals(-1f, prefs.readerBrightness)
            assertEquals(ScrollDirection.VERTICAL, prefs.readerScrollDirection)
            assertEquals(PageLayout.CONTINUOUS, prefs.readerPageLayout)
            assertEquals(PageAlignment.CENTER, prefs.readerPageAlignment)
            assertFalse(prefs.readerAutoHideToolbar)
            assertEquals(QuickZoomPreset.FIT_WIDTH, prefs.readerQuickZoomPreset)
            assertFalse(prefs.readerKeepScreenOn)
            assertEquals(ReadingTheme.LIGHT, prefs.readerTheme)
            assertFalse(prefs.readerSnapToPages)
            assertEquals(ScreenOrientation.AUTO, prefs.readerScreenOrientation)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `preferences returns stored values`() = runTest {
        val storedPrefs = preferencesOf(
            isFirstLaunchKey to false,
            onboardingCompletedKey to true,
            themeModeKey to ThemeMode.DARK.name,
            defaultViewModeKey to ViewMode.GRID.name,
            defaultSortOptionKey to SortOption.DATE_DESC.name,
            rememberPasswordsKey to false,
            readerBrightnessKey to 0.8f,
            readerScrollDirectionKey to ScrollDirection.HORIZONTAL.name,
            readerPageLayoutKey to PageLayout.SINGLE_PAGE.name,
            readerPageAlignmentKey to PageAlignment.LEFT.name,
            readerAutoHideToolbarKey to true,
            readerQuickZoomPresetKey to QuickZoomPreset.ACTUAL_SIZE.name,
            readerKeepScreenOnKey to true,
            readerThemeKey to ReadingTheme.SEPIA.name,
            readerSnapToPagesKey to true,
            readerScreenOrientationKey to ScreenOrientation.PORTRAIT.name
        )
        repository = createRepository(storedPrefs)

        repository.preferences.test {
            val prefs = awaitItem()
            assertFalse(prefs.isFirstLaunch)
            assertTrue(prefs.hasCompletedOnboarding)
            assertEquals(ThemeMode.DARK, prefs.themeMode)
            assertEquals(ViewMode.GRID, prefs.defaultViewMode)
            assertEquals(SortOption.DATE_DESC, prefs.defaultSortOption)
            assertFalse(prefs.rememberPasswords)
            assertEquals(0.8f, prefs.readerBrightness)
            assertEquals(ScrollDirection.HORIZONTAL, prefs.readerScrollDirection)
            assertEquals(PageLayout.SINGLE_PAGE, prefs.readerPageLayout)
            assertEquals(PageAlignment.LEFT, prefs.readerPageAlignment)
            assertTrue(prefs.readerAutoHideToolbar)
            assertEquals(QuickZoomPreset.ACTUAL_SIZE, prefs.readerQuickZoomPreset)
            assertTrue(prefs.readerKeepScreenOn)
            assertEquals(ReadingTheme.SEPIA, prefs.readerTheme)
            assertTrue(prefs.readerSnapToPages)
            assertEquals(ScreenOrientation.PORTRAIT, prefs.readerScreenOrientation)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `preferences handles partial stored values`() = runTest {
        val storedPrefs = preferencesOf(
            themeModeKey to ThemeMode.DARK.name,
            readerBrightnessKey to 0.5f
        )
        repository = createRepository(storedPrefs)

        repository.preferences.test {
            val prefs = awaitItem()
            assertEquals(ThemeMode.DARK, prefs.themeMode)
            assertEquals(0.5f, prefs.readerBrightness)
            // Others should be default
            assertTrue(prefs.isFirstLaunch)
            assertEquals(ViewMode.LIST, prefs.defaultViewMode)
            cancelAndIgnoreRemainingEvents()
        }
    }
    // endregion

    // region setFirstLaunch Tests
    @Test
    fun `setFirstLaunch with false does not throw`() = runTest {
        repository = createRepository()
        repository.setFirstLaunch(false)
        // If we get here without exception, test passes
    }

    @Test
    fun `setFirstLaunch with true does not throw`() = runTest {
        repository = createRepository()
        repository.setFirstLaunch(true)
    }
    // endregion

    // region setOnboardingCompleted Tests
    @Test
    fun `setOnboardingCompleted with true does not throw`() = runTest {
        repository = createRepository()
        repository.setOnboardingCompleted(true)
    }

    @Test
    fun `setOnboardingCompleted with false does not throw`() = runTest {
        repository = createRepository()
        repository.setOnboardingCompleted(false)
    }
    // endregion

    // region setThemeMode Tests
    @Test
    fun `setThemeMode with LIGHT does not throw`() = runTest {
        repository = createRepository()
        repository.setThemeMode(ThemeMode.LIGHT)
    }

    @Test
    fun `setThemeMode with DARK does not throw`() = runTest {
        repository = createRepository()
        repository.setThemeMode(ThemeMode.DARK)
    }

    @Test
    fun `setThemeMode with SYSTEM does not throw`() = runTest {
        repository = createRepository()
        repository.setThemeMode(ThemeMode.SYSTEM)
    }
    // endregion

    // region setDefaultViewMode Tests
    @Test
    fun `setDefaultViewMode with LIST does not throw`() = runTest {
        repository = createRepository()
        repository.setDefaultViewMode(ViewMode.LIST)
    }

    @Test
    fun `setDefaultViewMode with GRID does not throw`() = runTest {
        repository = createRepository()
        repository.setDefaultViewMode(ViewMode.GRID)
    }
    // endregion

    // region setDefaultSortOption Tests
    @Test
    fun `setDefaultSortOption with NAME_ASC does not throw`() = runTest {
        repository = createRepository()
        repository.setDefaultSortOption(SortOption.NAME_ASC)
    }

    @Test
    fun `setDefaultSortOption with NAME_DESC does not throw`() = runTest {
        repository = createRepository()
        repository.setDefaultSortOption(SortOption.NAME_DESC)
    }

    @Test
    fun `setDefaultSortOption with DATE_ASC does not throw`() = runTest {
        repository = createRepository()
        repository.setDefaultSortOption(SortOption.DATE_ASC)
    }

    @Test
    fun `setDefaultSortOption with DATE_DESC does not throw`() = runTest {
        repository = createRepository()
        repository.setDefaultSortOption(SortOption.DATE_DESC)
    }

    @Test
    fun `setDefaultSortOption with SIZE_ASC does not throw`() = runTest {
        repository = createRepository()
        repository.setDefaultSortOption(SortOption.SIZE_ASC)
    }

    @Test
    fun `setDefaultSortOption with SIZE_DESC does not throw`() = runTest {
        repository = createRepository()
        repository.setDefaultSortOption(SortOption.SIZE_DESC)
    }
    // endregion

    // region setRememberPasswords Tests
    @Test
    fun `setRememberPasswords with true does not throw`() = runTest {
        repository = createRepository()
        repository.setRememberPasswords(true)
    }

    @Test
    fun `setRememberPasswords with false does not throw`() = runTest {
        repository = createRepository()
        repository.setRememberPasswords(false)
    }
    // endregion

    // region setReaderBrightness Tests
    @Test
    fun `setReaderBrightness with zero does not throw`() = runTest {
        repository = createRepository()
        repository.setReaderBrightness(0f)
    }

    @Test
    fun `setReaderBrightness with max does not throw`() = runTest {
        repository = createRepository()
        repository.setReaderBrightness(1f)
    }

    @Test
    fun `setReaderBrightness with system default does not throw`() = runTest {
        repository = createRepository()
        repository.setReaderBrightness(-1f)
    }

    @Test
    fun `setReaderBrightness with mid value does not throw`() = runTest {
        repository = createRepository()
        repository.setReaderBrightness(0.5f)
    }
    // endregion

    // region setReaderScrollDirection Tests
    @Test
    fun `setReaderScrollDirection with VERTICAL does not throw`() = runTest {
        repository = createRepository()
        repository.setReaderScrollDirection(ScrollDirection.VERTICAL)
    }

    @Test
    fun `setReaderScrollDirection with HORIZONTAL does not throw`() = runTest {
        repository = createRepository()
        repository.setReaderScrollDirection(ScrollDirection.HORIZONTAL)
    }
    // endregion

    // region setReaderPageLayout Tests
    @Test
    fun `setReaderPageLayout with CONTINUOUS does not throw`() = runTest {
        repository = createRepository()
        repository.setReaderPageLayout(PageLayout.CONTINUOUS)
    }

    @Test
    fun `setReaderPageLayout with SINGLE_PAGE does not throw`() = runTest {
        repository = createRepository()
        repository.setReaderPageLayout(PageLayout.SINGLE_PAGE)
    }
    // endregion

    // region setReaderPageAlignment Tests
    @Test
    fun `setReaderPageAlignment with CENTER does not throw`() = runTest {
        repository = createRepository()
        repository.setReaderPageAlignment(PageAlignment.CENTER)
    }

    @Test
    fun `setReaderPageAlignment with LEFT does not throw`() = runTest {
        repository = createRepository()
        repository.setReaderPageAlignment(PageAlignment.LEFT)
    }

    @Test
    fun `setReaderPageAlignment with RIGHT does not throw`() = runTest {
        repository = createRepository()
        repository.setReaderPageAlignment(PageAlignment.RIGHT)
    }
    // endregion

    // region setReaderAutoHideToolbar Tests
    @Test
    fun `setReaderAutoHideToolbar with true does not throw`() = runTest {
        repository = createRepository()
        repository.setReaderAutoHideToolbar(true)
    }

    @Test
    fun `setReaderAutoHideToolbar with false does not throw`() = runTest {
        repository = createRepository()
        repository.setReaderAutoHideToolbar(false)
    }
    // endregion

    // region setReaderQuickZoomPreset Tests
    @Test
    fun `setReaderQuickZoomPreset with FIT_PAGE does not throw`() = runTest {
        repository = createRepository()
        repository.setReaderQuickZoomPreset(QuickZoomPreset.FIT_PAGE)
    }

    @Test
    fun `setReaderQuickZoomPreset with FIT_WIDTH does not throw`() = runTest {
        repository = createRepository()
        repository.setReaderQuickZoomPreset(QuickZoomPreset.FIT_WIDTH)
    }

    @Test
    fun `setReaderQuickZoomPreset with ACTUAL_SIZE does not throw`() = runTest {
        repository = createRepository()
        repository.setReaderQuickZoomPreset(QuickZoomPreset.ACTUAL_SIZE)
    }
    // endregion

    // region setReaderKeepScreenOn Tests
    @Test
    fun `setReaderKeepScreenOn with true does not throw`() = runTest {
        repository = createRepository()
        repository.setReaderKeepScreenOn(true)
    }

    @Test
    fun `setReaderKeepScreenOn with false does not throw`() = runTest {
        repository = createRepository()
        repository.setReaderKeepScreenOn(false)
    }
    // endregion

    // region setReaderTheme Tests
    @Test
    fun `setReaderTheme with LIGHT does not throw`() = runTest {
        repository = createRepository()
        repository.setReaderTheme(ReadingTheme.LIGHT)
    }

    @Test
    fun `setReaderTheme with DARK does not throw`() = runTest {
        repository = createRepository()
        repository.setReaderTheme(ReadingTheme.DARK)
    }

    @Test
    fun `setReaderTheme with SEPIA does not throw`() = runTest {
        repository = createRepository()
        repository.setReaderTheme(ReadingTheme.SEPIA)
    }

    @Test
    fun `setReaderTheme with BLACK does not throw`() = runTest {
        repository = createRepository()
        repository.setReaderTheme(ReadingTheme.BLACK)
    }
    // endregion

    // region setReaderSnapToPages Tests
    @Test
    fun `setReaderSnapToPages with true does not throw`() = runTest {
        repository = createRepository()
        repository.setReaderSnapToPages(true)
    }

    @Test
    fun `setReaderSnapToPages with false does not throw`() = runTest {
        repository = createRepository()
        repository.setReaderSnapToPages(false)
    }
    // endregion

    // region setReaderScreenOrientation Tests
    @Test
    fun `setReaderScreenOrientation with AUTO does not throw`() = runTest {
        repository = createRepository()
        repository.setReaderScreenOrientation(ScreenOrientation.AUTO)
    }

    @Test
    fun `setReaderScreenOrientation with PORTRAIT does not throw`() = runTest {
        repository = createRepository()
        repository.setReaderScreenOrientation(ScreenOrientation.PORTRAIT)
    }

    @Test
    fun `setReaderScreenOrientation with LANDSCAPE does not throw`() = runTest {
        repository = createRepository()
        repository.setReaderScreenOrientation(ScreenOrientation.LANDSCAPE)
    }
    // endregion

    // region Enum Coverage Tests
    @Test
    fun `ThemeMode has 3 values`() {
        assertEquals(3, ThemeMode.entries.size)
    }

    @Test
    fun `ViewMode has 2 values`() {
        assertEquals(2, ViewMode.entries.size)
    }

    @Test
    fun `SortOption has 6 values`() {
        assertEquals(6, SortOption.entries.size)
    }

    @Test
    fun `ScrollDirection has 2 values`() {
        assertEquals(2, ScrollDirection.entries.size)
    }

    @Test
    fun `PageLayout has 2 values`() {
        assertEquals(2, PageLayout.entries.size)
    }

    @Test
    fun `PageAlignment has 3 values`() {
        assertEquals(3, PageAlignment.entries.size)
    }

    @Test
    fun `QuickZoomPreset has 3 values`() {
        assertEquals(3, QuickZoomPreset.entries.size)
    }

    @Test
    fun `ReadingTheme has 4 values`() {
        assertEquals(4, ReadingTheme.entries.size)
    }

    @Test
    fun `ScreenOrientation has 3 values`() {
        assertEquals(3, ScreenOrientation.entries.size)
    }
    // endregion
}
