package com.rejowan.pdfreaderpro.presentation.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rejowan.pdfreaderpro.BuildConfig
import com.rejowan.pdfreaderpro.domain.model.AppPreferences
import com.rejowan.pdfreaderpro.domain.model.GithubRelease
import com.rejowan.pdfreaderpro.domain.model.PageAlignment
import com.rejowan.pdfreaderpro.domain.model.PageLayout
import com.rejowan.pdfreaderpro.domain.model.QuickZoomPreset
import com.rejowan.pdfreaderpro.domain.model.ReadingTheme
import com.rejowan.pdfreaderpro.domain.model.ScrollDirection
import com.rejowan.pdfreaderpro.domain.model.ThemeMode
import com.rejowan.pdfreaderpro.domain.model.UpdateCheckInterval
import com.rejowan.pdfreaderpro.domain.model.UpdateState
import com.rejowan.pdfreaderpro.domain.repository.PreferencesRepository
import com.rejowan.pdfreaderpro.domain.repository.UpdateRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber

class SettingsViewModel(
    private val preferencesRepository: PreferencesRepository,
    private val updateRepository: UpdateRepository
) : ViewModel() {

    companion object {
        private const val GITHUB_OWNER = "ahmmedrejowan"
        private const val GITHUB_REPO = "PdfReaderPro"
    }

    val preferences: StateFlow<AppPreferences> = preferencesRepository.preferences
        .stateIn(viewModelScope, SharingStarted.Eagerly, AppPreferences())

    private val _updateState = MutableStateFlow<UpdateState>(UpdateState.Idle)
    val updateState: StateFlow<UpdateState> = _updateState.asStateFlow()

    private val _lastCheckTime = MutableStateFlow(0L)
    val lastCheckTime: StateFlow<Long> = _lastCheckTime.asStateFlow()

    init {
        loadLastCheckTime()
        checkForUpdatesIfNeeded()
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

    fun setReaderScrollDirection(direction: ScrollDirection) {
        viewModelScope.launch {
            preferencesRepository.setReaderScrollDirection(direction)
        }
    }

    fun setReaderPageLayout(layout: PageLayout) {
        viewModelScope.launch {
            preferencesRepository.setReaderPageLayout(layout)
        }
    }

    fun setReaderPageAlignment(alignment: PageAlignment) {
        viewModelScope.launch {
            preferencesRepository.setReaderPageAlignment(alignment)
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
}
