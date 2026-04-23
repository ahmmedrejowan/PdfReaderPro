package com.rejowan.pdfreaderpro.presentation.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rejowan.pdfreaderpro.BuildConfig
import com.rejowan.pdfreaderpro.domain.model.AppPreferences
import com.rejowan.pdfreaderpro.domain.model.GithubRelease
import com.rejowan.pdfreaderpro.domain.model.QuickZoomPreset
import com.rejowan.pdfreaderpro.domain.model.ReadingTheme
import com.rejowan.pdfreaderpro.domain.model.ScreenOrientation
import com.rejowan.pdfreaderpro.domain.model.ScrollMode
import com.rejowan.pdfreaderpro.domain.model.ThemeMode
import com.rejowan.pdfreaderpro.domain.model.UpdateCheckInterval
import com.rejowan.pdfreaderpro.domain.model.UpdateState
import com.rejowan.pdfreaderpro.domain.repository.PreferencesRepository
import com.rejowan.pdfreaderpro.domain.repository.UpdateRepository
import com.rejowan.pdfreaderpro.util.ApkDownloadManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File

class SettingsViewModel(
    private val preferencesRepository: PreferencesRepository,
    private val updateRepository: UpdateRepository,
    private val apkDownloadManager: ApkDownloadManager
) : ViewModel() {

    companion object {
        private const val TAG = "UpdateChecker"
        private const val GITHUB_OWNER = "ahmmedrejowan"
        private const val GITHUB_REPO = "PdfReaderPro"
    }

    val preferences: StateFlow<AppPreferences> = preferencesRepository.preferences
        .stateIn(viewModelScope, SharingStarted.Eagerly, AppPreferences())

    private val _updateState = MutableStateFlow<UpdateState>(UpdateState.Idle)
    val updateState: StateFlow<UpdateState> = _updateState.asStateFlow()

    private val _downloadState = MutableStateFlow<ApkDownloadManager.DownloadState>(ApkDownloadManager.DownloadState.Idle)
    val downloadState: StateFlow<ApkDownloadManager.DownloadState> = _downloadState.asStateFlow()

    private val _lastCheckTime = MutableStateFlow(0L)
    val lastCheckTime: StateFlow<Long> = _lastCheckTime.asStateFlow()

    private var downloadJob: Job? = null
    private var pendingInstallFile: File? = null

    private val _hasPendingApk = MutableStateFlow(false)
    val hasPendingApk: StateFlow<Boolean> = _hasPendingApk.asStateFlow()

    private val _pendingApkVersion = MutableStateFlow<String?>(null)
    val pendingApkVersion: StateFlow<String?> = _pendingApkVersion.asStateFlow()

    init {
        loadLastCheckTime()
        checkForUpdatesIfNeeded()
        checkPendingApk()
    }

    private fun checkPendingApk() {
        val currentVersion = BuildConfig.VERSION_NAME
        _hasPendingApk.value = apkDownloadManager.hasPendingApk(currentVersion)
        _pendingApkVersion.value = if (_hasPendingApk.value) {
            apkDownloadManager.getPendingApkVersion()
        } else {
            null
        }
        Timber.tag(TAG).d("Pending APK: ${_hasPendingApk.value}, version: ${_pendingApkVersion.value}, current: $currentVersion")
    }

    private fun loadLastCheckTime() {
        viewModelScope.launch {
            _lastCheckTime.value = updateRepository.getLastCheckTime()
        }
    }

    /**
     * Automatically checks for updates if the configured interval has passed.
     * Called on ViewModel initialization.
     */
    private fun checkForUpdatesIfNeeded() {
        viewModelScope.launch {
            val lastCheck = updateRepository.getLastCheckTime()
            val interval = preferences.value.updateCheckInterval

            // Skip if auto-check is disabled
            if (interval == UpdateCheckInterval.NEVER) {
                Timber.d("Auto update check disabled")
                return@launch
            }

            val intervalMillis = interval.days * 24 * 60 * 60 * 1000L
            val timeSinceLastCheck = System.currentTimeMillis() - lastCheck

            if (timeSinceLastCheck >= intervalMillis) {
                Timber.d("Auto-checking for updates (last check: ${timeSinceLastCheck / 1000 / 60 / 60}h ago)")
                checkForUpdates()
            } else {
                val hoursUntilNextCheck = (intervalMillis - timeSinceLastCheck) / 1000 / 60 / 60
                Timber.d("Next auto-check in ${hoursUntilNextCheck}h")
            }
        }
    }

    fun checkForUpdates() {
        viewModelScope.launch {
            _updateState.value = UpdateState.Checking
            Timber.d("Checking for updates...")

            val currentVersion = BuildConfig.VERSION_NAME
            val result = updateRepository.checkForUpdate(
                owner = GITHUB_OWNER,
                repo = GITHUB_REPO,
                currentVersion = currentVersion
            )

            result.fold(
                onSuccess = { release ->
                    if (release != null) {
                        // Check if this version should be skipped
                        if (updateRepository.shouldSkipVersion(release.version)) {
                            Timber.d("Version ${release.version} is skipped")
                            _updateState.value = UpdateState.UpToDate
                        } else {
                            Timber.d("Update available: ${release.version}")
                            _updateState.value = UpdateState.Available(
                                release = release,
                                currentVersion = currentVersion
                            )
                        }
                    } else {
                        Timber.d("App is up to date")
                        _updateState.value = UpdateState.UpToDate
                    }
                },
                onFailure = { error ->
                    Timber.e(error, "Failed to check for updates")
                    _updateState.value = UpdateState.Error(
                        error.message ?: "Unknown error occurred"
                    )
                }
            )

            // Update last check time
            val now = System.currentTimeMillis()
            updateRepository.setLastCheckTime(now)
            _lastCheckTime.value = now
        }
    }

    fun skipVersion(version: String) {
        viewModelScope.launch {
            updateRepository.skipVersion(version)
            _updateState.value = UpdateState.Idle
        }
    }

    fun dismissUpdateDialog() {
        _updateState.value = UpdateState.Idle
    }

    fun getApkDownloadUrl(release: GithubRelease): String? {
        return release.assets.firstOrNull { it.isApk }?.downloadUrl
    }

    /**
     * Starts downloading the APK for the given release.
     */
    fun startDownload(release: GithubRelease) {
        val apkAsset = release.assets.firstOrNull { it.isApk }
        if (apkAsset == null) {
            Timber.tag(TAG).e("No APK asset found in release")
            _downloadState.value = ApkDownloadManager.DownloadState.Failed("No APK available")
            return
        }

        Timber.tag(TAG).d("Starting download: ${apkAsset.name}, version: ${release.version}")

        downloadJob?.cancel()
        downloadJob = viewModelScope.launch {
            apkDownloadManager.downloadApk(apkAsset.downloadUrl, apkAsset.name, release.version)
                .collect { state ->
                    _downloadState.value = state
                    Timber.tag(TAG).d("Download state: $state")

                    if (state is ApkDownloadManager.DownloadState.Completed) {
                        pendingInstallFile = state.file
                    }
                }
        }
    }

    /**
     * Cancels the ongoing download.
     */
    fun cancelDownload() {
        Timber.tag(TAG).d("Cancelling download")
        downloadJob?.cancel()
        downloadJob = null
        _downloadState.value = ApkDownloadManager.DownloadState.Cancelled
    }

    /**
     * Resets the download state to idle.
     */
    fun resetDownloadState() {
        _downloadState.value = ApkDownloadManager.DownloadState.Idle
        pendingInstallFile = null
    }

    /**
     * Checks if the app has permission to install APKs.
     */
    fun canInstallApks(): Boolean {
        return apkDownloadManager.canInstallApks()
    }

    /**
     * Installs the downloaded APK.
     */
    fun installDownloadedApk(): Boolean {
        val file = pendingInstallFile ?: run {
            Timber.tag(TAG).e("No pending install file")
            return false
        }
        return apkDownloadManager.installApk(file)
    }

    /**
     * Installs APK from a specific file.
     */
    fun installApk(file: File): Boolean {
        return apkDownloadManager.installApk(file)
    }

    /**
     * Installs the pending APK if one exists.
     */
    fun installPendingApk(): Boolean {
        val file = apkDownloadManager.getPendingApk() ?: run {
            Timber.tag(TAG).e("No pending APK found")
            return false
        }
        return apkDownloadManager.installApk(file)
    }

    /**
     * Clears the pending APK (deletes downloaded file).
     */
    fun clearPendingApk() {
        apkDownloadManager.cleanupOldDownloads()
        checkPendingApk()
    }

    /**
     * Refreshes pending APK state (call on resume).
     */
    fun refreshPendingApkState() {
        checkPendingApk()
    }

    /**
     * Opens the system settings to enable install from unknown sources.
     */
    fun openInstallPermissionSettings(): android.content.Intent? {
        return apkDownloadManager.getInstallPermissionIntent()
    }

    // App settings
    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            preferencesRepository.setThemeMode(mode)
        }
    }

    fun setUpdateCheckInterval(interval: UpdateCheckInterval) {
        viewModelScope.launch {
            preferencesRepository.setUpdateCheckInterval(interval)
        }
    }

    // Reader settings
    fun setReaderBrightness(brightness: Float) {
        viewModelScope.launch {
            preferencesRepository.setReaderBrightness(brightness)
        }
    }

    fun setReaderScrollMode(mode: ScrollMode) {
        viewModelScope.launch {
            preferencesRepository.setReaderScrollMode(mode)
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

    fun setReaderDoubleTapZoom(zoom: Float) {
        viewModelScope.launch {
            preferencesRepository.setReaderDoubleTapZoom(zoom)
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

    fun setReaderSnapToPages(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setReaderSnapToPages(enabled)
        }
    }

    fun setReaderScreenOrientation(orientation: ScreenOrientation) {
        viewModelScope.launch {
            preferencesRepository.setReaderScreenOrientation(orientation)
        }
    }
}
