package com.rejowan.pdfreaderpro.presentation.viewmodel

import app.cash.turbine.test
import com.rejowan.pdfreaderpro.domain.model.AppPreferences
import com.rejowan.pdfreaderpro.domain.model.QuickZoomPreset
import com.rejowan.pdfreaderpro.domain.model.ReadingTheme
import com.rejowan.pdfreaderpro.domain.model.ScrollMode
import com.rejowan.pdfreaderpro.domain.model.ThemeMode
import com.rejowan.pdfreaderpro.domain.repository.PreferencesRepository
import com.rejowan.pdfreaderpro.domain.model.GithubRelease
import com.rejowan.pdfreaderpro.domain.model.ReleaseAsset
import com.rejowan.pdfreaderpro.domain.model.UpdateCheckInterval
import com.rejowan.pdfreaderpro.domain.model.UpdateState
import com.rejowan.pdfreaderpro.domain.repository.UpdateRepository
import com.rejowan.pdfreaderpro.presentation.screens.settings.SettingsViewModel
import com.rejowan.pdfreaderpro.util.ApkDownloadManager
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.verify
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
import java.io.File

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
    fun `initial state has default scroll mode`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.preferences.test {
            val prefs = awaitItem()
            assertEquals(ScrollMode.VERTICAL, prefs.readerScrollMode)
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

    // region setReaderScrollMode Tests
    @Test
    fun `setReaderScrollMode calls repository with VERTICAL`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.setReaderScrollMode(ScrollMode.VERTICAL)
        advanceUntilIdle()

        coVerify { preferencesRepository.setReaderScrollMode(ScrollMode.VERTICAL) }
    }

    @Test
    fun `setReaderScrollMode calls repository with HORIZONTAL`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.setReaderScrollMode(ScrollMode.HORIZONTAL)
        advanceUntilIdle()

        coVerify { preferencesRepository.setReaderScrollMode(ScrollMode.HORIZONTAL) }
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
            readerScrollMode = ScrollMode.HORIZONTAL
        )
        coEvery { preferencesRepository.preferences } returns flowOf(updatedPrefs)

        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.preferences.test {
            val prefs = awaitItem()
            assertEquals(ThemeMode.DARK, prefs.themeMode)
            assertEquals(0.75f, prefs.readerBrightness)
            assertEquals(ScrollMode.HORIZONTAL, prefs.readerScrollMode)
            cancelAndIgnoreRemainingEvents()
        }
    }
    // endregion

    // region Checking for an update
    private fun release(
        tag: String = "v9.9.9",
        assets: List<ReleaseAsset> = listOf(
            ReleaseAsset("app.apk", "https://example.invalid/app.apk", 1024)
        )
    ) = GithubRelease(
        tagName = tag,
        name = "Release $tag",
        body = "notes",
        publishedAt = "2026-01-01T00:00:00Z",
        htmlUrl = "https://example.invalid",
        assets = assets
    )

    @Test
    fun `a newer release is offered to the user`() = runTest {
        val found = release()
        coEvery { updateRepository.checkForUpdate(any(), any(), any()) } returns
            Result.success(found)
        coEvery { updateRepository.shouldSkipVersion(any()) } returns false
        val vm = createViewModel()

        vm.checkForUpdates()
        advanceUntilIdle()

        val state = vm.updateState.value
        assertTrue(state is UpdateState.Available)
        assertEquals(found, (state as UpdateState.Available).release)
    }

    @Test
    fun `a version the user chose to skip is not offered again`() = runTest {
        coEvery { updateRepository.checkForUpdate(any(), any(), any()) } returns
            Result.success(release())
        coEvery { updateRepository.shouldSkipVersion("9.9.9") } returns true
        val vm = createViewModel()

        vm.checkForUpdates()
        advanceUntilIdle()

        assertEquals(UpdateState.UpToDate, vm.updateState.value)
    }

    @Test
    fun `no newer release leaves the app reported as current`() = runTest {
        coEvery { updateRepository.checkForUpdate(any(), any(), any()) } returns
            Result.success(null)
        val vm = createViewModel()

        vm.checkForUpdates()
        advanceUntilIdle()

        assertEquals(UpdateState.UpToDate, vm.updateState.value)
    }

    @Test
    fun `a failed check is shown as an error, not as being up to date`() = runTest {
        // Reporting "up to date" after a failed check would hide real updates.
        coEvery { updateRepository.checkForUpdate(any(), any(), any()) } returns
            Result.failure(java.io.IOException("network unreachable"))
        val vm = createViewModel()

        vm.checkForUpdates()
        advanceUntilIdle()

        val state = vm.updateState.value
        assertTrue(state is UpdateState.Error)
        assertEquals("network unreachable", (state as UpdateState.Error).message)
    }

    @Test
    fun `the time of the check is recorded whatever the outcome`() = runTest {
        // The interval between automatic checks is measured from this, so a failed
        // check that did not record would retry on every launch.
        coEvery { updateRepository.checkForUpdate(any(), any(), any()) } returns
            Result.failure(java.io.IOException("network unreachable"))
        val vm = createViewModel()

        vm.checkForUpdates()
        advanceUntilIdle()

        coVerify { updateRepository.setLastCheckTime(any()) }
        assertTrue(vm.lastCheckTime.value > 0)
    }

    @Test
    fun `skipping a version records it and closes the prompt`() = runTest {
        val vm = createViewModel()

        vm.skipVersion("9.9.9")
        advanceUntilIdle()

        coVerify { updateRepository.skipVersion("9.9.9") }
        assertEquals(UpdateState.Idle, vm.updateState.value)
    }

    @Test
    fun `dismissing the prompt leaves nothing pending`() = runTest {
        coEvery { updateRepository.checkForUpdate(any(), any(), any()) } returns
            Result.success(release())
        coEvery { updateRepository.shouldSkipVersion(any()) } returns false
        val vm = createViewModel()
        vm.checkForUpdates()
        advanceUntilIdle()

        vm.dismissUpdateDialog()

        assertEquals(UpdateState.Idle, vm.updateState.value)
    }
    // endregion

    // region Automatic checks
    @Test
    fun `with automatic checks off, nothing is fetched on opening settings`() = runTest {
        coEvery { preferencesRepository.preferences } returns
            flowOf(AppPreferences(updateCheckInterval = UpdateCheckInterval.NEVER))
        coEvery { updateRepository.getLastCheckTime() } returns 0L

        createViewModel()
        advanceUntilIdle()

        coVerify(exactly = 0) { updateRepository.checkForUpdate(any(), any(), any()) }
    }

    @Test
    fun `a check that is not due yet is not repeated`() = runTest {
        coEvery { preferencesRepository.preferences } returns
            flowOf(AppPreferences(updateCheckInterval = UpdateCheckInterval.WEEKLY))
        coEvery { updateRepository.getLastCheckTime() } returns System.currentTimeMillis()

        createViewModel()
        advanceUntilIdle()

        coVerify(exactly = 0) { updateRepository.checkForUpdate(any(), any(), any()) }
    }

    @Test
    fun `a check that is overdue happens on opening settings`() = runTest {
        coEvery { preferencesRepository.preferences } returns
            flowOf(AppPreferences(updateCheckInterval = UpdateCheckInterval.DAILY))
        coEvery { updateRepository.getLastCheckTime() } returns
            System.currentTimeMillis() - 10L * 24 * 60 * 60 * 1000

        createViewModel()
        advanceUntilIdle()

        coVerify { updateRepository.checkForUpdate(any(), any(), any()) }
    }
    // endregion

    // region Downloading the update
    @Test
    fun `the apk is the asset offered for download`() = runTest {
        val vm = createViewModel()
        val withExtras = release(
            assets = listOf(
                ReleaseAsset("source.zip", "https://example.invalid/source.zip", 1),
                ReleaseAsset("app.apk", "https://example.invalid/app.apk", 2)
            )
        )

        assertEquals("https://example.invalid/app.apk", vm.getApkDownloadUrl(withExtras))
    }

    @Test
    fun `a release with no apk offers nothing to download`() = runTest {
        val vm = createViewModel()
        val sourceOnly = release(
            assets = listOf(ReleaseAsset("source.zip", "https://example.invalid/source.zip", 1))
        )

        assertNull(vm.getApkDownloadUrl(sourceOnly))
    }

    @Test
    fun `asking to download a release with no apk fails rather than hanging`() = runTest {
        val vm = createViewModel()

        vm.startDownload(release(assets = emptyList()))
        advanceUntilIdle()

        assertTrue(vm.downloadState.value is ApkDownloadManager.DownloadState.Failed)
    }

    @Test
    fun `download progress is passed through to the screen`() = runTest {
        every { apkDownloadManager.downloadApk(any(), any(), any()) } returns flowOf(
            ApkDownloadManager.DownloadState.Downloading(50, 512, 1024)
        )
        val vm = createViewModel()

        vm.startDownload(release())
        advanceUntilIdle()

        assertTrue(vm.downloadState.value is ApkDownloadManager.DownloadState.Downloading)
    }

    @Test
    fun `cancelling a download says so rather than leaving it running`() = runTest {
        val vm = createViewModel()

        vm.cancelDownload()

        assertEquals(ApkDownloadManager.DownloadState.Cancelled, vm.downloadState.value)
    }

    @Test
    fun `resetting puts the download back to idle`() = runTest {
        val vm = createViewModel()
        vm.cancelDownload()

        vm.resetDownloadState()

        assertEquals(ApkDownloadManager.DownloadState.Idle, vm.downloadState.value)
    }

    @Test
    fun `installing without a downloaded file reports failure`() = runTest {
        val vm = createViewModel()

        assertFalse(vm.installDownloadedApk())
    }
    // endregion

    // region Installing an update that is already downloaded
    @Test
    fun `a pending update is noticed when settings opens`() = runTest {
        coEvery { apkDownloadManager.hasPendingApk(any()) } returns true
        coEvery { apkDownloadManager.getPendingApkVersion() } returns "9.9.9"

        val vm = createViewModel()
        advanceUntilIdle()

        assertTrue(vm.hasPendingApk.value)
        assertEquals("9.9.9", vm.pendingApkVersion.value)
    }

    @Test
    fun `with nothing downloaded, no install is offered`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()

        assertFalse(vm.hasPendingApk.value)
        assertFalse(vm.installPendingApk())
    }

    @Test
    fun `installing a pending update hands the file over`() = runTest {
        val apk = File("/downloads/app-9.9.9.apk")
        every { apkDownloadManager.getPendingApk() } returns apk
        every { apkDownloadManager.installApk(apk) } returns true
        val vm = createViewModel()
        advanceUntilIdle()

        assertTrue(vm.installPendingApk())
    }

    @Test
    fun `clearing a pending update removes it and stops offering it`() = runTest {
        coEvery { apkDownloadManager.hasPendingApk(any()) } returns true
        val vm = createViewModel()
        advanceUntilIdle()
        coEvery { apkDownloadManager.hasPendingApk(any()) } returns false

        vm.clearPendingApk()
        advanceUntilIdle()

        verify { apkDownloadManager.cleanupOldDownloads() }
        assertFalse(vm.hasPendingApk.value)
    }

    @Test
    fun `the pending state is checked again when settings is returned to`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()
        coEvery { apkDownloadManager.hasPendingApk(any()) } returns true

        vm.refreshPendingApkState()
        advanceUntilIdle()

        assertTrue(vm.hasPendingApk.value)
    }

    @Test
    fun `installing follows whatever permission the system reports`() = runTest {
        every { apkDownloadManager.canInstallApks() } returns false
        val vm = createViewModel()

        assertFalse(vm.canInstallApks())
    }

    @Test
    fun `installing a specific file is passed straight through`() = runTest {
        val apk = File("/downloads/app-9.9.9.apk")
        every { apkDownloadManager.installApk(apk) } returns true
        val vm = createViewModel()

        assertTrue(vm.installApk(apk))
    }
    // endregion
}
