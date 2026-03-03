package com.rejowan.pdfreaderpro.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.preferencesOf
import androidx.datastore.preferences.core.stringPreferencesKey
import app.cash.turbine.test
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import com.rejowan.pdfreaderpro.domain.model.PageAlignment
import com.rejowan.pdfreaderpro.domain.model.PageLayout
import com.rejowan.pdfreaderpro.domain.model.QuickZoomPreset
import com.rejowan.pdfreaderpro.domain.model.ReadingTheme
import com.rejowan.pdfreaderpro.domain.model.ScrollDirection
import com.rejowan.pdfreaderpro.domain.model.SortOption
import com.rejowan.pdfreaderpro.domain.model.ThemeMode
import com.rejowan.pdfreaderpro.domain.model.ViewMode

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
            readerThemeKey to ReadingTheme.SEPIA.name
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
            cancelAndIgnoreRemainingEvents()
        }
    }
    // endregion

    // region setFirstLaunch Tests
    @Test
    fun `setFirstLaunch updates datastore`() = runTest {
        repository = createRepository()
        val transformSlot = slot<suspend (MutablePreferences) -> Unit>()
        coEvery { dataStore.edit(capture(transformSlot)) } coAnswers {
            val mutablePrefs = mockk<MutablePreferences>(relaxed = true)
            transformSlot.captured.invoke(mutablePrefs)
            mockk()
        }

        repository.setFirstLaunch(false)

        coVerify { dataStore.edit(any()) }
    }

    @Test
    fun `setFirstLaunch with true works correctly`() = runTest {
        repository = createRepository()

        repository.setFirstLaunch(true)

        coVerify { dataStore.edit(any()) }
    }
    // endregion

    // region setOnboardingCompleted Tests
    @Test
    fun `setOnboardingCompleted updates datastore`() = runTest {
        repository = createRepository()

        repository.setOnboardingCompleted(true)

        coVerify { dataStore.edit(any()) }
    }

    @Test
    fun `setOnboardingCompleted with false works correctly`() = runTest {
        repository = createRepository()

        repository.setOnboardingCompleted(false)

        coVerify { dataStore.edit(any()) }
    }
    // endregion

    // region setThemeMode Tests
    @Test
    fun `setThemeMode with LIGHT updates datastore`() = runTest {
        repository = createRepository()

        repository.setThemeMode(ThemeMode.LIGHT)

        coVerify { dataStore.edit(any()) }
    }

    @Test
    fun `setThemeMode with DARK updates datastore`() = runTest {
        repository = createRepository()

        repository.setThemeMode(ThemeMode.DARK)

        coVerify { dataStore.edit(any()) }
    }

    @Test
    fun `setThemeMode with SYSTEM updates datastore`() = runTest {
        repository = createRepository()

        repository.setThemeMode(ThemeMode.SYSTEM)

        coVerify { dataStore.edit(any()) }
    }
    // endregion

    // region setDefaultViewMode Tests
    @Test
    fun `setDefaultViewMode with LIST updates datastore`() = runTest {
        repository = createRepository()

        repository.setDefaultViewMode(ViewMode.LIST)

        coVerify { dataStore.edit(any()) }
    }

    @Test
    fun `setDefaultViewMode with GRID updates datastore`() = runTest {
        repository = createRepository()

        repository.setDefaultViewMode(ViewMode.GRID)

        coVerify { dataStore.edit(any()) }
    }
    // endregion

    // region setDefaultSortOption Tests
    @Test
    fun `setDefaultSortOption with NAME_ASC updates datastore`() = runTest {
        repository = createRepository()

        repository.setDefaultSortOption(SortOption.NAME_ASC)

        coVerify { dataStore.edit(any()) }
    }

    @Test
    fun `setDefaultSortOption with NAME_DESC updates datastore`() = runTest {
        repository = createRepository()

        repository.setDefaultSortOption(SortOption.NAME_DESC)

        coVerify { dataStore.edit(any()) }
    }

    @Test
    fun `setDefaultSortOption with DATE_ASC updates datastore`() = runTest {
        repository = createRepository()

        repository.setDefaultSortOption(SortOption.DATE_ASC)

        coVerify { dataStore.edit(any()) }
    }

    @Test
    fun `setDefaultSortOption with DATE_DESC updates datastore`() = runTest {
        repository = createRepository()

        repository.setDefaultSortOption(SortOption.DATE_DESC)

        coVerify { dataStore.edit(any()) }
    }

    @Test
    fun `setDefaultSortOption with SIZE_ASC updates datastore`() = runTest {
        repository = createRepository()

        repository.setDefaultSortOption(SortOption.SIZE_ASC)

        coVerify { dataStore.edit(any()) }
    }

    @Test
    fun `setDefaultSortOption with SIZE_DESC updates datastore`() = runTest {
        repository = createRepository()

        repository.setDefaultSortOption(SortOption.SIZE_DESC)

        coVerify { dataStore.edit(any()) }
    }
    // endregion

    // region setRememberPasswords Tests
    @Test
    fun `setRememberPasswords with true updates datastore`() = runTest {
        repository = createRepository()

        repository.setRememberPasswords(true)

        coVerify { dataStore.edit(any()) }
    }

    @Test
    fun `setRememberPasswords with false updates datastore`() = runTest {
        repository = createRepository()

        repository.setRememberPasswords(false)

        coVerify { dataStore.edit(any()) }
    }
    // endregion

    // region setReaderBrightness Tests
    @Test
    fun `setReaderBrightness with zero updates datastore`() = runTest {
        repository = createRepository()

        repository.setReaderBrightness(0f)

        coVerify { dataStore.edit(any()) }
    }

    @Test
    fun `setReaderBrightness with max updates datastore`() = runTest {
        repository = createRepository()

        repository.setReaderBrightness(1f)

        coVerify { dataStore.edit(any()) }
    }

    @Test
    fun `setReaderBrightness with system default updates datastore`() = runTest {
        repository = createRepository()

        repository.setReaderBrightness(-1f)

        coVerify { dataStore.edit(any()) }
    }

    @Test
    fun `setReaderBrightness with mid value updates datastore`() = runTest {
        repository = createRepository()

        repository.setReaderBrightness(0.5f)

        coVerify { dataStore.edit(any()) }
    }
    // endregion

    // region setReaderScrollDirection Tests
    @Test
    fun `setReaderScrollDirection with VERTICAL updates datastore`() = runTest {
        repository = createRepository()

        repository.setReaderScrollDirection(ScrollDirection.VERTICAL)

        coVerify { dataStore.edit(any()) }
    }

    @Test
    fun `setReaderScrollDirection with HORIZONTAL updates datastore`() = runTest {
        repository = createRepository()

        repository.setReaderScrollDirection(ScrollDirection.HORIZONTAL)

        coVerify { dataStore.edit(any()) }
    }
    // endregion

    // region setReaderPageLayout Tests
    @Test
    fun `setReaderPageLayout with SINGLE_PAGE updates datastore`() = runTest {
        repository = createRepository()

        repository.setReaderPageLayout(PageLayout.SINGLE_PAGE)

        coVerify { dataStore.edit(any()) }
    }

    @Test
    fun `setReaderPageLayout with CONTINUOUS updates datastore`() = runTest {
        repository = createRepository()

        repository.setReaderPageLayout(PageLayout.CONTINUOUS)

        coVerify { dataStore.edit(any()) }
    }
    // endregion

    // region setReaderPageAlignment Tests
    @Test
    fun `setReaderPageAlignment with LEFT updates datastore`() = runTest {
        repository = createRepository()

        repository.setReaderPageAlignment(PageAlignment.LEFT)

        coVerify { dataStore.edit(any()) }
    }

    @Test
    fun `setReaderPageAlignment with CENTER updates datastore`() = runTest {
        repository = createRepository()

        repository.setReaderPageAlignment(PageAlignment.CENTER)

        coVerify { dataStore.edit(any()) }
    }

    @Test
    fun `setReaderPageAlignment with RIGHT updates datastore`() = runTest {
        repository = createRepository()

        repository.setReaderPageAlignment(PageAlignment.RIGHT)

        coVerify { dataStore.edit(any()) }
    }
    // endregion

    // region setReaderAutoHideToolbar Tests
    @Test
    fun `setReaderAutoHideToolbar with true updates datastore`() = runTest {
        repository = createRepository()

        repository.setReaderAutoHideToolbar(true)

        coVerify { dataStore.edit(any()) }
    }

    @Test
    fun `setReaderAutoHideToolbar with false updates datastore`() = runTest {
        repository = createRepository()

        repository.setReaderAutoHideToolbar(false)

        coVerify { dataStore.edit(any()) }
    }
    // endregion

    // region setReaderQuickZoomPreset Tests
    @Test
    fun `setReaderQuickZoomPreset with FIT_PAGE updates datastore`() = runTest {
        repository = createRepository()

        repository.setReaderQuickZoomPreset(QuickZoomPreset.FIT_PAGE)

        coVerify { dataStore.edit(any()) }
    }

    @Test
    fun `setReaderQuickZoomPreset with FIT_WIDTH updates datastore`() = runTest {
        repository = createRepository()

        repository.setReaderQuickZoomPreset(QuickZoomPreset.FIT_WIDTH)

        coVerify { dataStore.edit(any()) }
    }

    @Test
    fun `setReaderQuickZoomPreset with ACTUAL_SIZE updates datastore`() = runTest {
        repository = createRepository()

        repository.setReaderQuickZoomPreset(QuickZoomPreset.ACTUAL_SIZE)

        coVerify { dataStore.edit(any()) }
    }
    // endregion

    // region setReaderKeepScreenOn Tests
    @Test
    fun `setReaderKeepScreenOn with true updates datastore`() = runTest {
        repository = createRepository()

        repository.setReaderKeepScreenOn(true)

        coVerify { dataStore.edit(any()) }
    }

    @Test
    fun `setReaderKeepScreenOn with false updates datastore`() = runTest {
        repository = createRepository()

        repository.setReaderKeepScreenOn(false)

        coVerify { dataStore.edit(any()) }
    }
    // endregion

    // region setReaderTheme Tests
    @Test
    fun `setReaderTheme with LIGHT updates datastore`() = runTest {
        repository = createRepository()

        repository.setReaderTheme(ReadingTheme.LIGHT)

        coVerify { dataStore.edit(any()) }
    }

    @Test
    fun `setReaderTheme with SEPIA updates datastore`() = runTest {
        repository = createRepository()

        repository.setReaderTheme(ReadingTheme.SEPIA)

        coVerify { dataStore.edit(any()) }
    }

    @Test
    fun `setReaderTheme with DARK updates datastore`() = runTest {
        repository = createRepository()

        repository.setReaderTheme(ReadingTheme.DARK)

        coVerify { dataStore.edit(any()) }
    }

    @Test
    fun `setReaderTheme with BLACK updates datastore`() = runTest {
        repository = createRepository()

        repository.setReaderTheme(ReadingTheme.BLACK)

        coVerify { dataStore.edit(any()) }
    }
    // endregion

    // region Edge Cases
    @Test
    fun `preferences handles partial stored values`() = runTest {
        val partialPrefs = preferencesOf(
            themeModeKey to ThemeMode.DARK.name,
            readerBrightnessKey to 0.5f
        )
        repository = createRepository(partialPrefs)

        repository.preferences.test {
            val prefs = awaitItem()
            // Stored values
            assertEquals(ThemeMode.DARK, prefs.themeMode)
            assertEquals(0.5f, prefs.readerBrightness)
            // Default values for non-stored
            assertTrue(prefs.isFirstLaunch)
            assertEquals(ViewMode.LIST, prefs.defaultViewMode)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `preferences handles all theme modes`() = runTest {
        for (mode in ThemeMode.entries) {
            val prefs = preferencesOf(themeModeKey to mode.name)
            repository = createRepository(prefs)

            repository.preferences.test {
                assertEquals(mode, awaitItem().themeMode)
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Test
    fun `preferences handles all view modes`() = runTest {
        for (mode in ViewMode.entries) {
            val prefs = preferencesOf(defaultViewModeKey to mode.name)
            repository = createRepository(prefs)

            repository.preferences.test {
                assertEquals(mode, awaitItem().defaultViewMode)
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Test
    fun `preferences handles all sort options`() = runTest {
        for (option in SortOption.entries) {
            val prefs = preferencesOf(defaultSortOptionKey to option.name)
            repository = createRepository(prefs)

            repository.preferences.test {
                assertEquals(option, awaitItem().defaultSortOption)
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Test
    fun `preferences handles all scroll directions`() = runTest {
        for (direction in ScrollDirection.entries) {
            val prefs = preferencesOf(readerScrollDirectionKey to direction.name)
            repository = createRepository(prefs)

            repository.preferences.test {
                assertEquals(direction, awaitItem().readerScrollDirection)
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Test
    fun `preferences handles all page layouts`() = runTest {
        for (layout in PageLayout.entries) {
            val prefs = preferencesOf(readerPageLayoutKey to layout.name)
            repository = createRepository(prefs)

            repository.preferences.test {
                assertEquals(layout, awaitItem().readerPageLayout)
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Test
    fun `preferences handles all page alignments`() = runTest {
        for (alignment in PageAlignment.entries) {
            val prefs = preferencesOf(readerPageAlignmentKey to alignment.name)
            repository = createRepository(prefs)

            repository.preferences.test {
                assertEquals(alignment, awaitItem().readerPageAlignment)
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Test
    fun `preferences handles all quick zoom presets`() = runTest {
        for (preset in QuickZoomPreset.entries) {
            val prefs = preferencesOf(readerQuickZoomPresetKey to preset.name)
            repository = createRepository(prefs)

            repository.preferences.test {
                assertEquals(preset, awaitItem().readerQuickZoomPreset)
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Test
    fun `preferences handles all reading themes`() = runTest {
        for (theme in ReadingTheme.entries) {
            val prefs = preferencesOf(readerThemeKey to theme.name)
            repository = createRepository(prefs)

            repository.preferences.test {
                assertEquals(theme, awaitItem().readerTheme)
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Test
    fun `preferences handles brightness at boundary values`() = runTest {
        val testCases = listOf(-1f, 0f, 0.001f, 0.5f, 0.999f, 1f)
        for (brightness in testCases) {
            val prefs = preferencesOf(readerBrightnessKey to brightness)
            repository = createRepository(prefs)

            repository.preferences.test {
                assertEquals(brightness, awaitItem().readerBrightness)
                cancelAndIgnoreRemainingEvents()
            }
        }
    }
    // endregion
}
