package com.rejowan.pdfreaderpro.presentation.viewmodel

import app.cash.turbine.test
import com.rejowan.pdfreaderpro.domain.model.AppPreferences
import com.rejowan.pdfreaderpro.domain.model.PageAlignment
import com.rejowan.pdfreaderpro.domain.model.PageLayout
import com.rejowan.pdfreaderpro.domain.model.QuickZoomPreset
import com.rejowan.pdfreaderpro.domain.model.ReadingTheme
import com.rejowan.pdfreaderpro.domain.model.ScrollDirection
import com.rejowan.pdfreaderpro.domain.model.ThemeMode
import com.rejowan.pdfreaderpro.domain.repository.PreferencesRepository
import com.rejowan.pdfreaderpro.domain.repository.UpdateRepository
import com.rejowan.pdfreaderpro.presentation.screens.settings.SettingsViewModel
import com.rejowan.pdfreaderpro.util.ApkDownloadManager
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var preferencesRepository: PreferencesRepository
    private lateinit var updateRepository: UpdateRepository
    private lateinit var apkDownloadManager: ApkDownloadManager
    private lateinit var viewModel: SettingsViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        preferencesRepository = mockk(relaxed = true)
        updateRepository = mockk(relaxed = true)
        apkDownloadManager = mockk(relaxed = true)

        // Setup mocks for init block
        coEvery { preferencesRepository.preferences } returns flowOf(AppPreferences())
        coEvery { updateRepository.getLastCheckTime() } returns System.currentTimeMillis() // Recent check to skip auto-check
        coEvery { updateRepository.checkForUpdate(any(), any(), any()) } returns Result.success(null)
        coEvery { apkDownloadManager.hasPendingApk(any()) } returns false
        coEvery { apkDownloadManager.getPendingApkVersion() } returns null
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): SettingsViewModel {
        return SettingsViewModel(
            preferencesRepository = preferencesRepository,
            updateRepository = updateRepository,
            apkDownloadManager = apkDownloadManager
        )
    }

    // region Initial State Tests
    @Test
    fun `initial state has default preferences`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.preferences.test {
            val prefs = awaitItem()
            assertNotNull(prefs)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `initial state has default theme mode`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.preferences.test {
            val prefs = awaitItem()
            assertEquals(ThemeMode.SYSTEM, prefs.themeMode)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `initial state has default reader brightness`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.preferences.test {
            val prefs = awaitItem()
            assertEquals(-1f, prefs.readerBrightness) // -1 = system default
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `initial state has default scroll direction`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.preferences.test {
            val prefs = awaitItem()
            assertEquals(ScrollDirection.VERTICAL, prefs.readerScrollDirection)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `initial state has default page layout`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.preferences.test {
            val prefs = awaitItem()
            assertEquals(PageLayout.CONTINUOUS, prefs.readerPageLayout)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `initial state has default page alignment`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.preferences.test {
            val prefs = awaitItem()
            assertEquals(PageAlignment.CENTER, prefs.readerPageAlignment)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `initial state has auto hide toolbar disabled`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.preferences.test {
            val prefs = awaitItem()
            assertFalse(prefs.readerAutoHideToolbar)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `initial state has keep screen on disabled`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.preferences.test {
            val prefs = awaitItem()
            assertFalse(prefs.readerKeepScreenOn)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `initial state has default reader theme`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.preferences.test {
            val prefs = awaitItem()
            assertEquals(ReadingTheme.LIGHT, prefs.readerTheme)
            cancelAndIgnoreRemainingEvents()
        }
    }
    // endregion

    // region setThemeMode Tests
    @Test
    fun `setThemeMode calls repository with LIGHT`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.setThemeMode(ThemeMode.LIGHT)
        advanceUntilIdle()

        coVerify { preferencesRepository.setThemeMode(ThemeMode.LIGHT) }
    }

    @Test
    fun `setThemeMode calls repository with DARK`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.setThemeMode(ThemeMode.DARK)
        advanceUntilIdle()

        coVerify { preferencesRepository.setThemeMode(ThemeMode.DARK) }
    }

    @Test
    fun `setThemeMode calls repository with SYSTEM`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.setThemeMode(ThemeMode.SYSTEM)
        advanceUntilIdle()

        coVerify { preferencesRepository.setThemeMode(ThemeMode.SYSTEM) }
    }
    // endregion

    // region setReaderBrightness Tests
    @Test
    fun `setReaderBrightness calls repository with value`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.setReaderBrightness(0.5f)
        advanceUntilIdle()

        coVerify { preferencesRepository.setReaderBrightness(0.5f) }
    }

    @Test
    fun `setReaderBrightness calls repository with minimum value`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.setReaderBrightness(0f)
        advanceUntilIdle()

        coVerify { preferencesRepository.setReaderBrightness(0f) }
    }

    @Test
    fun `setReaderBrightness calls repository with maximum value`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.setReaderBrightness(1f)
        advanceUntilIdle()

        coVerify { preferencesRepository.setReaderBrightness(1f) }
    }
    // endregion

    // region setReaderScrollDirection Tests
    @Test
    fun `setReaderScrollDirection calls repository with VERTICAL`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.setReaderScrollDirection(ScrollDirection.VERTICAL)
        advanceUntilIdle()

        coVerify { preferencesRepository.setReaderScrollDirection(ScrollDirection.VERTICAL) }
    }

    @Test
    fun `setReaderScrollDirection calls repository with HORIZONTAL`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.setReaderScrollDirection(ScrollDirection.HORIZONTAL)
        advanceUntilIdle()

        coVerify { preferencesRepository.setReaderScrollDirection(ScrollDirection.HORIZONTAL) }
    }
    // endregion

    // region setReaderPageLayout Tests
    @Test
    fun `setReaderPageLayout calls repository with CONTINUOUS`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.setReaderPageLayout(PageLayout.CONTINUOUS)
        advanceUntilIdle()

        coVerify { preferencesRepository.setReaderPageLayout(PageLayout.CONTINUOUS) }
    }

    @Test
    fun `setReaderPageLayout calls repository with SINGLE_PAGE`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.setReaderPageLayout(PageLayout.SINGLE_PAGE)
        advanceUntilIdle()

        coVerify { preferencesRepository.setReaderPageLayout(PageLayout.SINGLE_PAGE) }
    }
    // endregion

    // region setReaderPageAlignment Tests
    @Test
    fun `setReaderPageAlignment calls repository with CENTER`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.setReaderPageAlignment(PageAlignment.CENTER)
        advanceUntilIdle()

        coVerify { preferencesRepository.setReaderPageAlignment(PageAlignment.CENTER) }
    }

    @Test
    fun `setReaderPageAlignment calls repository with LEFT`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.setReaderPageAlignment(PageAlignment.LEFT)
        advanceUntilIdle()

        coVerify { preferencesRepository.setReaderPageAlignment(PageAlignment.LEFT) }
    }

    @Test
    fun `setReaderPageAlignment calls repository with RIGHT`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.setReaderPageAlignment(PageAlignment.RIGHT)
        advanceUntilIdle()

        coVerify { preferencesRepository.setReaderPageAlignment(PageAlignment.RIGHT) }
    }
    // endregion

    // region setReaderAutoHideToolbar Tests
    @Test
    fun `setReaderAutoHideToolbar calls repository with true`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.setReaderAutoHideToolbar(true)
        advanceUntilIdle()

        coVerify { preferencesRepository.setReaderAutoHideToolbar(true) }
    }

    @Test
    fun `setReaderAutoHideToolbar calls repository with false`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.setReaderAutoHideToolbar(false)
        advanceUntilIdle()

        coVerify { preferencesRepository.setReaderAutoHideToolbar(false) }
    }
    // endregion

    // region setReaderQuickZoomPreset Tests
    @Test
    fun `setReaderQuickZoomPreset calls repository with FIT_WIDTH`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.setReaderQuickZoomPreset(QuickZoomPreset.FIT_WIDTH)
        advanceUntilIdle()

        coVerify { preferencesRepository.setReaderQuickZoomPreset(QuickZoomPreset.FIT_WIDTH) }
    }

    @Test
    fun `setReaderQuickZoomPreset calls repository with FIT_PAGE`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.setReaderQuickZoomPreset(QuickZoomPreset.FIT_PAGE)
        advanceUntilIdle()

        coVerify { preferencesRepository.setReaderQuickZoomPreset(QuickZoomPreset.FIT_PAGE) }
    }

    @Test
    fun `setReaderQuickZoomPreset calls repository with ACTUAL_SIZE`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.setReaderQuickZoomPreset(QuickZoomPreset.ACTUAL_SIZE)
        advanceUntilIdle()

        coVerify { preferencesRepository.setReaderQuickZoomPreset(QuickZoomPreset.ACTUAL_SIZE) }
    }
    // endregion

    // region setReaderKeepScreenOn Tests
    @Test
    fun `setReaderKeepScreenOn calls repository with true`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.setReaderKeepScreenOn(true)
        advanceUntilIdle()

        coVerify { preferencesRepository.setReaderKeepScreenOn(true) }
    }

    @Test
    fun `setReaderKeepScreenOn calls repository with false`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.setReaderKeepScreenOn(false)
        advanceUntilIdle()

        coVerify { preferencesRepository.setReaderKeepScreenOn(false) }
    }
    // endregion

    // region setReaderTheme Tests
    @Test
    fun `setReaderTheme calls repository with LIGHT`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.setReaderTheme(ReadingTheme.LIGHT)
        advanceUntilIdle()

        coVerify { preferencesRepository.setReaderTheme(ReadingTheme.LIGHT) }
    }

    @Test
    fun `setReaderTheme calls repository with DARK`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.setReaderTheme(ReadingTheme.DARK)
        advanceUntilIdle()

        coVerify { preferencesRepository.setReaderTheme(ReadingTheme.DARK) }
    }

    @Test
    fun `setReaderTheme calls repository with SEPIA`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.setReaderTheme(ReadingTheme.SEPIA)
        advanceUntilIdle()

        coVerify { preferencesRepository.setReaderTheme(ReadingTheme.SEPIA) }
    }
    // endregion

    // region Preferences Flow Tests
    @Test
    fun `preferences flow emits updated values`() = runTest {
        val updatedPrefs = AppPreferences(
            themeMode = ThemeMode.DARK,
            readerBrightness = 0.75f,
            readerScrollDirection = ScrollDirection.HORIZONTAL
        )
        coEvery { preferencesRepository.preferences } returns flowOf(updatedPrefs)

        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.preferences.test {
            val prefs = awaitItem()
            assertEquals(ThemeMode.DARK, prefs.themeMode)
            assertEquals(0.75f, prefs.readerBrightness)
            assertEquals(ScrollDirection.HORIZONTAL, prefs.readerScrollDirection)
            cancelAndIgnoreRemainingEvents()
        }
    }
    // endregion
}
