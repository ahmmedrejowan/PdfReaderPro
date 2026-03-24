package com.rejowan.pdfreaderpro.presentation.screens.settings

import android.content.Intent
import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.FormatAlignLeft
import androidx.compose.material.icons.automirrored.rounded.FormatAlignRight
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.rounded.Brightness6
import androidx.compose.material.icons.rounded.BrightnessHigh
import androidx.compose.material.icons.rounded.BrightnessLow
import androidx.compose.material.icons.rounded.ColorLens
import androidx.compose.material.icons.rounded.Brightness1
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.FormatAlignCenter
import androidx.compose.material.icons.rounded.FormatAlignLeft
import androidx.compose.material.icons.rounded.FormatAlignRight
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Gavel
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.InstallMobile
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Policy
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.PhoneAndroid
import androidx.compose.material.icons.rounded.ScreenLockPortrait
import androidx.compose.material.icons.rounded.ScreenRotation
import androidx.compose.material.icons.rounded.StayCurrentLandscape
import androidx.compose.material.icons.rounded.StayCurrentPortrait
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material.icons.rounded.SwapVert
import androidx.compose.material.icons.rounded.ViewDay
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.SystemUpdate
import androidx.compose.material.icons.rounded.Work
import androidx.compose.material.icons.rounded.AspectRatio
import androidx.compose.material.icons.rounded.FitScreen
import androidx.compose.material.icons.rounded.Fullscreen
import androidx.compose.material.icons.rounded.ZoomIn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.compose.ui.res.painterResource
import com.rejowan.pdfreaderpro.BuildConfig
import com.rejowan.pdfreaderpro.R
import com.rejowan.pdfreaderpro.domain.model.QuickZoomPreset
import com.rejowan.pdfreaderpro.domain.model.ReadingTheme
import com.rejowan.pdfreaderpro.domain.model.ScreenOrientation
import com.rejowan.pdfreaderpro.domain.model.ScrollMode
import com.rejowan.pdfreaderpro.domain.model.ThemeMode
import com.rejowan.pdfreaderpro.domain.model.UpdateCheckInterval
import com.rejowan.pdfreaderpro.domain.model.UpdateState
import com.rejowan.pdfreaderpro.presentation.components.DownloadProgressSheet
import com.rejowan.pdfreaderpro.presentation.components.UpdateAvailableSheet
import com.rejowan.pdfreaderpro.util.ApkDownloadManager
import com.rejowan.licensy.LicenseContent
import com.rejowan.licensy.Licenses
import com.rejowan.licensy.compose.LicensyList
import kotlinx.coroutines.delay
import org.koin.androidx.compose.koinViewModel

// Accent colors
private val AccentPurple = Color(0xFF9575CD)
private val AccentBlue = Color(0xFF64B5F6)
private val AccentTeal = Color(0xFF4DB6AC)
private val AccentAmber = Color(0xFFFFB74D)

/**
 * Redesigned Settings content with consistent UI patterns.
 */
@Composable
fun SettingsScreenContent(
    onBackClick: (() -> Unit)?,
    paddingValues: PaddingValues = PaddingValues(0.dp),
    viewModel: SettingsViewModel = koinViewModel()
) {
    val preferences by viewModel.preferences.collectAsState()
    val updateState by viewModel.updateState.collectAsState()
    val downloadState by viewModel.downloadState.collectAsState()
    val hasPendingApk by viewModel.hasPendingApk.collectAsState()
    val pendingApkVersion by viewModel.pendingApkVersion.collectAsState()

    val context = LocalContext.current

    // Track current release for download
    var currentRelease by remember { mutableStateOf<com.rejowan.pdfreaderpro.domain.model.GithubRelease?>(null) }

    // Sheet visibility states
    var showDownloadSheet by remember { mutableStateOf(false) }

    // Auto-show download sheet when download starts
    LaunchedEffect(downloadState) {
        when (downloadState) {
            is ApkDownloadManager.DownloadState.Starting,
            is ApkDownloadManager.DownloadState.Downloading -> {
                showDownloadSheet = true
            }
            else -> {}
        }
    }

    // Refresh pending APK state on resume (in case user installed from notification)
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                viewModel.refreshPendingApkState()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Other sheet visibility states
    var showThemeModeSheet by remember { mutableStateOf(false) }
    var showScrollModeSheet by remember { mutableStateOf(false) }
    var showQuickZoomSheet by remember { mutableStateOf(false) }
    var showReadingThemeSheet by remember { mutableStateOf(false) }
    var showBrightnessSheet by remember { mutableStateOf(false) }
    var showScreenOrientationSheet by remember { mutableStateOf(false) }

    // About section sheets
    var showChangelogSheet by remember { mutableStateOf(false) }
    var showPrivacyPolicySheet by remember { mutableStateOf(false) }
    var showLicensesSheet by remember { mutableStateOf(false) }
    var showCreatorSheet by remember { mutableStateOf(false) }
    var showAppLicenseSheet by remember { mutableStateOf(false) }
    var showUpdateIntervalSheet by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .padding(top = 8.dp, bottom = 80.dp)
    ) {
        // App Header Card
        SettingsHeaderCard()

        Spacer(modifier = Modifier.height(24.dp))

        // Appearance Section
        SectionLabel(text = stringResource(R.string.appearance), delay = 0)
        Spacer(modifier = Modifier.height(8.dp))

        SettingsOptionItem(
            icon = Icons.Rounded.Palette,
            title = stringResource(R.string.theme),
            subtitle = when (preferences.themeMode) {
                ThemeMode.LIGHT -> stringResource(R.string.light)
                ThemeMode.DARK -> stringResource(R.string.dark)
                ThemeMode.SYSTEM -> stringResource(R.string.system_default)
            },
            accentColor = AccentPurple,
            onClick = { showThemeModeSheet = true },
            animationDelay = 50
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Reader Section
        SectionLabel(text = stringResource(R.string.reader_section), delay = 100)
        Spacer(modifier = Modifier.height(8.dp))

        SettingsOptionItem(
            icon = Icons.Rounded.Brightness6,
            title = stringResource(R.string.brightness),
            subtitle = if (preferences.readerBrightness < 0) stringResource(R.string.system_default) else "${(preferences.readerBrightness * 100).toInt()}%",
            accentColor = AccentAmber,
            onClick = { showBrightnessSheet = true },
            animationDelay = 150
        )

        Spacer(modifier = Modifier.height(8.dp))

        SettingsOptionItem(
            icon = Icons.Rounded.SwapVert,
            title = stringResource(R.string.scroll_mode),
            subtitle = preferences.readerScrollMode.name.lowercase().replace("_", " ").replaceFirstChar { it.uppercase() },
            accentColor = AccentPurple,
            onClick = { showScrollModeSheet = true },
            animationDelay = 200
        )

        Spacer(modifier = Modifier.height(8.dp))

        SettingsOptionItem(
            icon = Icons.Rounded.ZoomIn,
            title = stringResource(R.string.default_zoom),
            subtitle = when (preferences.readerQuickZoomPreset) {
                QuickZoomPreset.FIT_PAGE -> stringResource(R.string.fit_page)
                QuickZoomPreset.FIT_WIDTH -> stringResource(R.string.fit_width)
                QuickZoomPreset.ACTUAL_SIZE -> stringResource(R.string.actual_size)
            },
            accentColor = AccentBlue,
            onClick = { showQuickZoomSheet = true },
            animationDelay = 300
        )

        Spacer(modifier = Modifier.height(8.dp))

        SettingsOptionItem(
            icon = Icons.Rounded.ColorLens,
            title = stringResource(R.string.reading_theme),
            subtitle = preferences.readerTheme.name.lowercase().replaceFirstChar { it.uppercase() },
            accentColor = AccentAmber,
            onClick = { showReadingThemeSheet = true },
            animationDelay = 350
        )

        Spacer(modifier = Modifier.height(8.dp))

        SettingsOptionItem(
            icon = Icons.Rounded.ScreenRotation,
            title = stringResource(R.string.screen_orientation),
            subtitle = when (preferences.readerScreenOrientation) {
                ScreenOrientation.AUTO -> stringResource(R.string.orientation_auto)
                ScreenOrientation.PORTRAIT -> stringResource(R.string.orientation_portrait)
                ScreenOrientation.LANDSCAPE -> stringResource(R.string.orientation_landscape)
            },
            accentColor = AccentBlue,
            onClick = { showScreenOrientationSheet = true },
            animationDelay = 375
        )

        Spacer(modifier = Modifier.height(16.dp))

        SettingsToggleItem(
            icon = Icons.Rounded.ViewDay,
            title = stringResource(R.string.snap_to_page),
            subtitle = stringResource(R.string.snap_to_page_desc),
            accentColor = AccentTeal,
            checked = preferences.readerSnapToPages,
            onCheckedChange = { viewModel.setReaderSnapToPages(it) },
            animationDelay = 385
        )

        Spacer(modifier = Modifier.height(8.dp))

        SettingsToggleItem(
            icon = Icons.Rounded.VisibilityOff,
            title = stringResource(R.string.auto_hide_toolbar),
            subtitle = stringResource(R.string.auto_hide_toolbar_desc),
            accentColor = AccentPurple,
            checked = preferences.readerAutoHideToolbar,
            onCheckedChange = { viewModel.setReaderAutoHideToolbar(it) },
            animationDelay = 400
        )

        Spacer(modifier = Modifier.height(8.dp))

        SettingsToggleItem(
            icon = Icons.Rounded.ScreenLockPortrait,
            title = stringResource(R.string.keep_screen_on),
            subtitle = stringResource(R.string.prevent_screen_off),
            accentColor = AccentTeal,
            checked = preferences.readerKeepScreenOn,
            onCheckedChange = { viewModel.setReaderKeepScreenOn(it) },
            animationDelay = 450
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Updates Section
        SectionLabel(text = stringResource(R.string.updates), delay = 500)
        Spacer(modifier = Modifier.height(8.dp))

        SettingsOptionItem(
            icon = Icons.Rounded.SystemUpdate,
            title = stringResource(R.string.check_for_updates),
            subtitle = when (updateState) {
                is UpdateState.Idle -> stringResource(R.string.tap_to_check)
                is UpdateState.Checking -> stringResource(R.string.checking)
                is UpdateState.Available -> stringResource(R.string.update_available_version, (updateState as UpdateState.Available).release.version)
                is UpdateState.UpToDate -> stringResource(R.string.up_to_date)
                is UpdateState.Error -> stringResource(R.string.error_message, (updateState as UpdateState.Error).message)
            },
            accentColor = AccentAmber,
            onClick = { viewModel.checkForUpdates() },
            animationDelay = 550
        )

        // Show pending install option if APK is downloaded
        if (hasPendingApk) {
            Spacer(modifier = Modifier.height(8.dp))

            SettingsOptionItem(
                icon = Icons.Rounded.InstallMobile,
                title = stringResource(R.string.install_pending_update),
                subtitle = "v${pendingApkVersion ?: "?"} is ready to install",
                accentColor = Color(0xFF4CAF50),
                onClick = {
                    if (viewModel.canInstallApks()) {
                        viewModel.installPendingApk()
                    } else {
                        // Show download sheet to handle permission
                        showDownloadSheet = true
                    }
                },
                animationDelay = 560
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        SettingsOptionItem(
            icon = Icons.Rounded.Schedule,
            title = stringResource(R.string.auto_check_interval),
            subtitle = preferences.updateCheckInterval.displayName,
            accentColor = AccentBlue,
            onClick = { showUpdateIntervalSheet = true },
            animationDelay = 600
        )

        Spacer(modifier = Modifier.height(24.dp))

        // About Section
        SectionLabel(text = stringResource(R.string.about_section), delay = 650)
        Spacer(modifier = Modifier.height(8.dp))

        SettingsOptionItem(
            icon = Icons.Rounded.Info,
            title = stringResource(R.string.version_format, BuildConfig.VERSION_NAME),
            subtitle = stringResource(R.string.view_changelog),
            accentColor = AccentBlue,
            onClick = { showChangelogSheet = true },
            animationDelay = 700
        )

        Spacer(modifier = Modifier.height(8.dp))

        SettingsOptionItem(
            icon = Icons.Rounded.Policy,
            title = stringResource(R.string.privacy_policy),
            subtitle = stringResource(R.string.view_privacy_policy),
            accentColor = AccentTeal,
            onClick = { showPrivacyPolicySheet = true },
            animationDelay = 750
        )

        Spacer(modifier = Modifier.height(8.dp))

        SettingsOptionItem(
            icon = Icons.Rounded.Gavel,
            title = stringResource(R.string.open_source_licenses),
            subtitle = stringResource(R.string.view_third_party),
            accentColor = AccentPurple,
            onClick = { showLicensesSheet = true },
            animationDelay = 800
        )

        Spacer(modifier = Modifier.height(8.dp))

        SettingsOptionItem(
            icon = Icons.Rounded.Person,
            title = stringResource(R.string.creator),
            subtitle = stringResource(R.string.about_developer),
            accentColor = AccentAmber,
            onClick = { showCreatorSheet = true },
            animationDelay = 850
        )

        Spacer(modifier = Modifier.height(8.dp))

        SettingsOptionItem(
            icon = Icons.Rounded.Gavel,
            title = stringResource(R.string.app_license),
            subtitle = stringResource(R.string.gpl_full_name),
            accentColor = AccentBlue,
            onClick = { showAppLicenseSheet = true },
            animationDelay = 900
        )

        Spacer(modifier = Modifier.height(8.dp))

        SettingsOptionItem(
            icon = Icons.Rounded.Email,
            title = stringResource(R.string.contact_developer),
            subtitle = stringResource(R.string.get_in_touch),
            accentColor = AccentTeal,
            onClick = {
                val intent = Intent(Intent.ACTION_SENDTO).apply {
                    data = "mailto:kmrejowan@gmail.com".toUri()
                    putExtra(Intent.EXTRA_SUBJECT, "PDF Reader Pro Feedback")
                }
                context.startActivity(intent)
            },
            animationDelay = 950
        )

        Spacer(modifier = Modifier.height(8.dp))

        SettingsOptionItem(
            icon = Icons.Rounded.Code,
            title = stringResource(R.string.github_repository),
            subtitle = stringResource(R.string.view_source_code),
            accentColor = AccentPurple,
            onClick = {
                val intent = Intent(
                    Intent.ACTION_VIEW,
                    "https://github.com/ahmmedrejowan/PdfReaderPro".toUri()
                )
                context.startActivity(intent)
            },
            animationDelay = 1000
        )

        Spacer(modifier = Modifier.height(32.dp))
    }

    // Theme Mode picker
    if (showThemeModeSheet) {
        SettingsPickerSheet(
            title = stringResource(R.string.theme),
            subtitle = stringResource(R.string.choose_appearance),
            icon = Icons.Rounded.Palette,
            accentColor = AccentPurple,
            options = listOf(
                PickerOption(Icons.Rounded.LightMode, stringResource(R.string.light), stringResource(R.string.always_light)),
                PickerOption(Icons.Rounded.DarkMode, stringResource(R.string.dark), stringResource(R.string.always_dark)),
                PickerOption(Icons.Rounded.PhoneAndroid, stringResource(R.string.system_theme), stringResource(R.string.follow_system))
            ),
            selectedIndex = ThemeMode.entries.indexOf(preferences.themeMode),
            onSelect = { index ->
                viewModel.setThemeMode(ThemeMode.entries[index])
                showThemeModeSheet = false
            },
            onDismiss = { showThemeModeSheet = false }
        )
    }

    // Scroll Mode picker
    if (showScrollModeSheet) {
        SettingsPickerSheet(
            title = stringResource(R.string.scroll_mode),
            subtitle = stringResource(R.string.choose_scroll),
            icon = Icons.Rounded.SwapVert,
            accentColor = AccentPurple,
            options = listOf(
                PickerOption(Icons.Rounded.SwapVert, stringResource(R.string.vertical), stringResource(R.string.scroll_up_down)),
                PickerOption(Icons.Rounded.SwapHoriz, stringResource(R.string.horizontal), stringResource(R.string.scroll_left_right))
            ),
            selectedIndex = ScrollMode.entries.indexOf(preferences.readerScrollMode),
            onSelect = { index ->
                viewModel.setReaderScrollMode(ScrollMode.entries[index])
                showScrollModeSheet = false
            },
            onDismiss = { showScrollModeSheet = false }
        )
    }

    // Quick Zoom picker
    if (showQuickZoomSheet) {
        SettingsPickerSheet(
            title = stringResource(R.string.default_zoom),
            subtitle = stringResource(R.string.initial_zoom),
            icon = Icons.Rounded.ZoomIn,
            accentColor = AccentBlue,
            options = listOf(
                PickerOption(Icons.Rounded.FitScreen, stringResource(R.string.fit_page), stringResource(R.string.show_entire_page)),
                PickerOption(Icons.Rounded.Fullscreen, stringResource(R.string.fit_width), stringResource(R.string.match_width)),
                PickerOption(Icons.Rounded.AspectRatio, stringResource(R.string.actual_size), stringResource(R.string.actual_size_desc))
            ),
            selectedIndex = QuickZoomPreset.entries.indexOf(preferences.readerQuickZoomPreset),
            onSelect = { index ->
                viewModel.setReaderQuickZoomPreset(QuickZoomPreset.entries[index])
                showQuickZoomSheet = false
            },
            onDismiss = { showQuickZoomSheet = false }
        )
    }

    // Reading Theme picker
    if (showReadingThemeSheet) {
        SettingsPickerSheet(
            title = stringResource(R.string.reading_theme),
            subtitle = stringResource(R.string.background_color),
            icon = Icons.Rounded.ColorLens,
            accentColor = AccentAmber,
            options = listOf(
                PickerOption(Icons.Rounded.LightMode, stringResource(R.string.light), stringResource(R.string.white_background)),
                PickerOption(Icons.Rounded.ColorLens, stringResource(R.string.sepia), stringResource(R.string.warm_paper)),
                PickerOption(Icons.Rounded.DarkMode, stringResource(R.string.dark), stringResource(R.string.dark_gray)),
                PickerOption(Icons.Rounded.Brightness1, stringResource(R.string.black), stringResource(R.string.pure_black))
            ),
            selectedIndex = ReadingTheme.entries.indexOf(preferences.readerTheme),
            onSelect = { index ->
                viewModel.setReaderTheme(ReadingTheme.entries[index])
                showReadingThemeSheet = false
            },
            onDismiss = { showReadingThemeSheet = false }
        )
    }

    // Screen Orientation picker
    if (showScreenOrientationSheet) {
        SettingsPickerSheet(
            title = stringResource(R.string.screen_orientation),
            subtitle = stringResource(R.string.adjust_view_orientation),
            icon = Icons.Rounded.ScreenRotation,
            accentColor = AccentBlue,
            options = listOf(
                PickerOption(Icons.Rounded.ScreenRotation, stringResource(R.string.orientation_auto), stringResource(R.string.follow_system)),
                PickerOption(Icons.Rounded.StayCurrentPortrait, stringResource(R.string.orientation_portrait), stringResource(R.string.lock_portrait)),
                PickerOption(Icons.Rounded.StayCurrentLandscape, stringResource(R.string.orientation_landscape), stringResource(R.string.lock_landscape))
            ),
            selectedIndex = ScreenOrientation.entries.indexOf(preferences.readerScreenOrientation),
            onSelect = { index ->
                viewModel.setReaderScreenOrientation(ScreenOrientation.entries[index])
                showScreenOrientationSheet = false
            },
            onDismiss = { showScreenOrientationSheet = false }
        )
    }

    // Brightness picker
    if (showBrightnessSheet) {
        BrightnessSheet(
            currentBrightness = preferences.readerBrightness,
            onBrightnessChange = { viewModel.setReaderBrightness(it) },
            onDismiss = { showBrightnessSheet = false }
        )
    }

    // About section sheets
    if (showChangelogSheet) {
        AboutSheet(onDismiss = { showChangelogSheet = false }) {
            ChangelogContent()
        }
    }

    if (showPrivacyPolicySheet) {
        AboutSheet(onDismiss = { showPrivacyPolicySheet = false }) {
            PrivacyPolicyContent()
        }
    }

    if (showLicensesSheet) {
        AboutSheet(onDismiss = { showLicensesSheet = false }) {
            LicensesContent()
        }
    }

    if (showCreatorSheet) {
        AboutSheet(onDismiss = { showCreatorSheet = false }) {
            CreatorContent()
        }
    }

    if (showAppLicenseSheet) {
        AboutSheet(onDismiss = { showAppLicenseSheet = false }) {
            AppLicenseContent()
        }
    }

    // Update check interval picker
    if (showUpdateIntervalSheet) {
        SettingsPickerSheet(
            title = stringResource(R.string.update_interval),
            subtitle = stringResource(R.string.update_interval_desc),
            icon = Icons.Rounded.Schedule,
            accentColor = AccentBlue,
            options = listOf(
                PickerOption(Icons.Rounded.Schedule, stringResource(R.string.never), stringResource(R.string.manual_check_only)),
                PickerOption(Icons.Rounded.Schedule, stringResource(R.string.daily), stringResource(R.string.check_every_day)),
                PickerOption(Icons.Rounded.Schedule, stringResource(R.string.every_3_days), stringResource(R.string.check_every_3_days)),
                PickerOption(Icons.Rounded.Schedule, stringResource(R.string.weekly), stringResource(R.string.check_once_week)),
                PickerOption(Icons.Rounded.Schedule, stringResource(R.string.every_2_weeks), stringResource(R.string.check_every_2_weeks)),
                PickerOption(Icons.Rounded.Schedule, stringResource(R.string.monthly), stringResource(R.string.check_once_month))
            ),
            selectedIndex = UpdateCheckInterval.entries.indexOf(preferences.updateCheckInterval),
            onSelect = { index ->
                viewModel.setUpdateCheckInterval(UpdateCheckInterval.entries[index])
                showUpdateIntervalSheet = false
            },
            onDismiss = { showUpdateIntervalSheet = false }
        )
    }

    // Update available sheet
    if (updateState is UpdateState.Available) {
        val availableState = updateState as UpdateState.Available
        UpdateAvailableSheet(
            release = availableState.release,
            currentVersion = availableState.currentVersion,
            downloadUrl = viewModel.getApkDownloadUrl(availableState.release),
            onDismiss = { viewModel.dismissUpdateDialog() },
            onSkipVersion = { viewModel.skipVersion(availableState.release.version) },
            onDownload = {
                currentRelease = availableState.release
                viewModel.startDownload(availableState.release)
                viewModel.dismissUpdateDialog()
            }
        )
    }

    // Download progress sheet
    if (showDownloadSheet && downloadState !is ApkDownloadManager.DownloadState.Idle) {
        DownloadProgressSheet(
            downloadState = downloadState,
            versionName = currentRelease?.version ?: "",
            canInstall = viewModel.canInstallApks(),
            onDismiss = {
                showDownloadSheet = false
                // Reset state if completed/failed/cancelled
                when (downloadState) {
                    is ApkDownloadManager.DownloadState.Completed,
                    is ApkDownloadManager.DownloadState.Failed,
                    is ApkDownloadManager.DownloadState.Cancelled -> {
                        viewModel.resetDownloadState()
                    }
                    else -> {}
                }
            },
            onCancel = {
                viewModel.cancelDownload()
            },
            onInstall = {
                viewModel.installDownloadedApk()
                showDownloadSheet = false
                viewModel.resetDownloadState()
            },
            onRequestPermission = {
                viewModel.openInstallPermissionSettings()?.let { intent ->
                    context.startActivity(intent)
                }
            }
        )
    }
}

@Composable
private fun SectionLabel(
    text: String,
    delay: Int,
    modifier: Modifier = Modifier
) {
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(delay.toLong())
        isVisible = true
    }

    val scale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0.9f,
        animationSpec = tween(250, easing = FastOutSlowInEasing),
        label = "section scale"
    )

    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(200),
        label = "section alpha"
    )

    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelMedium.copy(
            fontWeight = FontWeight.SemiBold,
            letterSpacing = MaterialTheme.typography.labelMedium.letterSpacing * 1.5f
        ),
        color = MaterialTheme.colorScheme.primary.copy(alpha = alpha),
        modifier = modifier
            .scale(scale)
            .padding(start = 4.dp, bottom = 4.dp)
    )
}

@Composable
private fun SettingsHeaderCard() {
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isVisible = true
    }

    val scale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0.95f,
        animationSpec = tween(300, easing = FastOutSlowInEasing),
        label = "header scale"
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // App logo
            Image(
                painter = painterResource(id = R.drawable.img_splash_logo),
                contentDescription = stringResource(R.string.app_logo),
                modifier = Modifier.size(52.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.app_name_full),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = stringResource(R.string.version_format, BuildConfig.VERSION_NAME),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // License badge
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = AccentTeal.copy(alpha = 0.15f)
            ) {
                Text(
                    text = stringResource(R.string.license_gpl),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Medium,
                    color = AccentTeal,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun SettingsOptionItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    accentColor: Color,
    onClick: () -> Unit,
    animationDelay: Int,
    modifier: Modifier = Modifier
) {
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(animationDelay.toLong())
        isVisible = true
    }

    val scale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0.95f,
        animationSpec = tween(250, easing = FastOutSlowInEasing),
        label = "item scale"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .scale(scale)
            .clip(RoundedCornerShape(12.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = accentColor),
                onClick = onClick
            ),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = accentColor.copy(alpha = 0.12f)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = stringResource(R.string.cd_decorative),
                    modifier = Modifier
                        .padding(8.dp)
                        .size(20.dp),
                    tint = accentColor
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                contentDescription = stringResource(R.string.cd_decorative),
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
private fun SettingsToggleItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    accentColor: Color,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    animationDelay: Int,
    modifier: Modifier = Modifier
) {
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(animationDelay.toLong())
        isVisible = true
    }

    val scale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0.95f,
        animationSpec = tween(250, easing = FastOutSlowInEasing),
        label = "toggle scale"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .scale(scale)
            .clip(RoundedCornerShape(12.dp))
            .clickable { onCheckedChange(!checked) },
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = accentColor.copy(alpha = 0.12f)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = stringResource(R.string.cd_decorative),
                    modifier = Modifier
                        .padding(8.dp)
                        .size(20.dp),
                    tint = accentColor
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }

            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = accentColor,
                    uncheckedThumbColor = Color.White,
                    uncheckedTrackColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                )
            )
        }
    }
}

// ============================================================================
// PICKER SHEET - Hybrid Pattern
// ============================================================================

data class PickerOption(
    val icon: ImageVector,
    val title: String,
    val subtitle: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsPickerSheet(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    options: List<PickerOption>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    if (isLandscape) {
        PickerSideSheet(
            title = title,
            subtitle = subtitle,
            icon = icon,
            accentColor = accentColor,
            options = options,
            selectedIndex = selectedIndex,
            onSelect = onSelect,
            onDismiss = onDismiss
        )
    } else {
        PickerBottomSheet(
            title = title,
            subtitle = subtitle,
            icon = icon,
            accentColor = accentColor,
            options = options,
            selectedIndex = selectedIndex,
            onSelect = onSelect,
            onDismiss = onDismiss
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PickerBottomSheet(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    options: List<PickerOption>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        PickerContent(
            title = title,
            subtitle = subtitle,
            icon = icon,
            accentColor = accentColor,
            options = options,
            selectedIndex = selectedIndex,
            onSelect = onSelect,
            modifier = Modifier.padding(bottom = 24.dp)
        )
    }
}

@Composable
private fun PickerSideSheet(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    options: List<PickerOption>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isVisible = true
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Scrim
        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(tween(300)),
            exit = fadeOut(tween(300))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f))
                    .pointerInput(Unit) {
                        detectTapGestures { onDismiss() }
                    }
            )
        }

        // Side panel
        AnimatedVisibility(
            visible = isVisible,
            enter = slideInHorizontally(
                initialOffsetX = { it },
                animationSpec = tween(300, easing = FastOutSlowInEasing)
            ),
            exit = slideOutHorizontally(
                targetOffsetX = { it },
                animationSpec = tween(250, easing = FastOutSlowInEasing)
            ),
            modifier = Modifier.align(Alignment.CenterEnd)
        ) {
            val systemBarsPadding = WindowInsets.systemBars.asPaddingValues()

            Surface(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(320.dp),
                shape = RoundedCornerShape(topStart = 20.dp, bottomStart = 20.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                shadowElevation = 8.dp
            ) {
                PickerContent(
                    title = title,
                    subtitle = subtitle,
                    icon = icon,
                    accentColor = accentColor,
                    options = options,
                    selectedIndex = selectedIndex,
                    onSelect = onSelect,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(
                            top = systemBarsPadding.calculateTopPadding(),
                            bottom = systemBarsPadding.calculateBottomPadding()
                        )
                        .verticalScroll(rememberScrollState())
                )
            }
        }
    }
}

@Composable
private fun PickerContent(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    options: List<PickerOption>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = accentColor.copy(alpha = 0.12f)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = stringResource(R.string.cd_decorative),
                    modifier = Modifier
                        .padding(6.dp)
                        .size(16.dp),
                    tint = accentColor
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Options
        options.forEachIndexed { index, option ->
            PickerOptionItem(
                option = option,
                isSelected = index == selectedIndex,
                accentColor = accentColor,
                onClick = { onSelect(index) },
                animationDelay = 50 * (index + 1)
            )
            if (index < options.lastIndex) {
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun PickerOptionItem(
    option: PickerOption,
    isSelected: Boolean,
    accentColor: Color,
    onClick: () -> Unit,
    animationDelay: Int,
    modifier: Modifier = Modifier
) {
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(animationDelay.toLong())
        isVisible = true
    }

    val scale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0.95f,
        animationSpec = tween(250, easing = FastOutSlowInEasing),
        label = "option scale"
    )

    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) accentColor.copy(alpha = 0.12f)
        else MaterialTheme.colorScheme.surfaceContainerLow,
        animationSpec = tween(200),
        label = "option bg"
    )

    val borderColor by animateColorAsState(
        targetValue = if (isSelected) accentColor.copy(alpha = 0.5f)
        else Color.Transparent,
        animationSpec = tween(200),
        label = "option border"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .scale(scale)
            .clip(RoundedCornerShape(8.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = accentColor),
                onClick = onClick
            )
            .then(
                if (isSelected) Modifier.border(
                    width = 1.5.dp,
                    color = borderColor,
                    shape = RoundedCornerShape(8.dp)
                ) else Modifier
            ),
        shape = RoundedCornerShape(8.dp),
        color = backgroundColor
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (isSelected) accentColor.copy(alpha = 0.2f)
                else MaterialTheme.colorScheme.surfaceContainerHigh
            ) {
                Icon(
                    imageVector = option.icon,
                    contentDescription = stringResource(R.string.cd_decorative),
                    modifier = Modifier
                        .padding(6.dp)
                        .size(16.dp),
                    tint = if (isSelected) accentColor
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = option.title,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
                    ),
                    color = if (isSelected) accentColor else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = option.subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }

            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .clip(CircleShape)
                        .background(accentColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Check,
                        contentDescription = "Selected",
                        modifier = Modifier.size(12.dp),
                        tint = Color.White
                    )
                }
            }
        }
    }
}

// ============================================================================
// BRIGHTNESS SHEET - Hybrid Pattern
// ============================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BrightnessSheet(
    currentBrightness: Float,
    onBrightnessChange: (Float) -> Unit,
    onDismiss: () -> Unit
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    if (isLandscape) {
        BrightnessSideSheet(
            currentBrightness = currentBrightness,
            onBrightnessChange = onBrightnessChange,
            onDismiss = onDismiss
        )
    } else {
        BrightnessBottomSheet(
            currentBrightness = currentBrightness,
            onBrightnessChange = onBrightnessChange,
            onDismiss = onDismiss
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BrightnessBottomSheet(
    currentBrightness: Float,
    onBrightnessChange: (Float) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        BrightnessContent(
            currentBrightness = currentBrightness,
            onBrightnessChange = onBrightnessChange,
            modifier = Modifier.padding(bottom = 24.dp)
        )
    }
}

@Composable
private fun BrightnessSideSheet(
    currentBrightness: Float,
    onBrightnessChange: (Float) -> Unit,
    onDismiss: () -> Unit
) {
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isVisible = true
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(tween(300)),
            exit = fadeOut(tween(300))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f))
                    .pointerInput(Unit) {
                        detectTapGestures { onDismiss() }
                    }
            )
        }

        AnimatedVisibility(
            visible = isVisible,
            enter = slideInHorizontally(
                initialOffsetX = { it },
                animationSpec = tween(300, easing = FastOutSlowInEasing)
            ),
            exit = slideOutHorizontally(
                targetOffsetX = { it },
                animationSpec = tween(250, easing = FastOutSlowInEasing)
            ),
            modifier = Modifier.align(Alignment.CenterEnd)
        ) {
            val systemBarsPadding = WindowInsets.systemBars.asPaddingValues()

            Surface(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(320.dp),
                shape = RoundedCornerShape(topStart = 20.dp, bottomStart = 20.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                shadowElevation = 8.dp
            ) {
                BrightnessContent(
                    currentBrightness = currentBrightness,
                    onBrightnessChange = onBrightnessChange,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(
                            top = systemBarsPadding.calculateTopPadding(),
                            bottom = systemBarsPadding.calculateBottomPadding()
                        )
                        .verticalScroll(rememberScrollState())
                )
            }
        }
    }
}

@Composable
private fun BrightnessContent(
    currentBrightness: Float,
    onBrightnessChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val isSystemDefault = currentBrightness < 0
    var sliderValue by remember { mutableFloatStateOf(if (isSystemDefault) 0.5f else currentBrightness) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = AccentAmber.copy(alpha = 0.12f)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Brightness6,
                    contentDescription = stringResource(R.string.cd_decorative),
                    modifier = Modifier
                        .padding(6.dp)
                        .size(16.dp),
                    tint = AccentAmber
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column {
                Text(
                    text = stringResource(R.string.brightness),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = stringResource(R.string.adjust_brightness),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Options
        BrightnessOptionItem(
            icon = Icons.Rounded.PhoneAndroid,
            title = stringResource(R.string.system_default),
            subtitle = stringResource(R.string.follow_system_brightness),
            isSelected = isSystemDefault,
            accentColor = AccentAmber,
            onClick = { onBrightnessChange(-1f) },
            animationDelay = 50
        )

        Spacer(modifier = Modifier.height(8.dp))

        BrightnessOptionItem(
            icon = Icons.Rounded.Brightness6,
            title = stringResource(R.string.custom),
            subtitle = if (!isSystemDefault) "${(sliderValue * 100).toInt()}%" else stringResource(R.string.set_custom_level),
            isSelected = !isSystemDefault,
            accentColor = AccentAmber,
            onClick = {
                if (isSystemDefault) {
                    onBrightnessChange(sliderValue)
                }
            },
            animationDelay = 100
        )

        // Brightness slider (only when custom selected)
        AnimatedVisibility(visible = !isSystemDefault) {
            Column {
                Spacer(modifier = Modifier.height(12.dp))

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.BrightnessLow,
                            contentDescription = "Low",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )

                        Slider(
                            value = sliderValue,
                            onValueChange = {
                                sliderValue = it
                                onBrightnessChange(it)
                            },
                            valueRange = 0.1f..1f,
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 8.dp),
                            colors = SliderDefaults.colors(
                                thumbColor = AccentAmber,
                                activeTrackColor = AccentAmber,
                                inactiveTrackColor = AccentAmber.copy(alpha = 0.24f)
                            )
                        )

                        Icon(
                            imageVector = Icons.Rounded.BrightnessHigh,
                            contentDescription = "High",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BrightnessOptionItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    isSelected: Boolean,
    accentColor: Color,
    onClick: () -> Unit,
    animationDelay: Int,
    modifier: Modifier = Modifier
) {
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(animationDelay.toLong())
        isVisible = true
    }

    val scale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0.95f,
        animationSpec = tween(250, easing = FastOutSlowInEasing),
        label = "brightness option scale"
    )

    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) accentColor.copy(alpha = 0.12f)
        else MaterialTheme.colorScheme.surfaceContainerLow,
        animationSpec = tween(200),
        label = "brightness option bg"
    )

    val borderColor by animateColorAsState(
        targetValue = if (isSelected) accentColor.copy(alpha = 0.5f)
        else Color.Transparent,
        animationSpec = tween(200),
        label = "brightness option border"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .scale(scale)
            .clip(RoundedCornerShape(8.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = accentColor),
                onClick = onClick
            )
            .then(
                if (isSelected) Modifier.border(
                    width = 1.5.dp,
                    color = borderColor,
                    shape = RoundedCornerShape(8.dp)
                ) else Modifier
            ),
        shape = RoundedCornerShape(8.dp),
        color = backgroundColor
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (isSelected) accentColor.copy(alpha = 0.2f)
                else MaterialTheme.colorScheme.surfaceContainerHigh
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = stringResource(R.string.cd_decorative),
                    modifier = Modifier
                        .padding(6.dp)
                        .size(16.dp),
                    tint = if (isSelected) accentColor
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
                    ),
                    color = if (isSelected) accentColor else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }

            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .clip(CircleShape)
                        .background(accentColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Check,
                        contentDescription = "Selected",
                        modifier = Modifier.size(12.dp),
                        tint = Color.White
                    )
                }
            }
        }
    }
}

// ============================================================================
// ABOUT SECTION SHEETS
// ============================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AboutSheet(
    onDismiss: () -> Unit,
    content: @Composable () -> Unit
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    if (isLandscape) {
        // Side panel for landscape
        var isVisible by remember { mutableStateOf(false) }

        LaunchedEffect(Unit) {
            isVisible = true
        }

        Box(modifier = Modifier.fillMaxSize()) {
            // Scrim
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(tween(300)),
                exit = fadeOut(tween(300))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f))
                        .pointerInput(Unit) { detectTapGestures { onDismiss() } }
                )
            }

            // Side panel
            AnimatedVisibility(
                visible = isVisible,
                enter = slideInHorizontally(
                    initialOffsetX = { it },
                    animationSpec = tween(300, easing = FastOutSlowInEasing)
                ),
                exit = slideOutHorizontally(
                    targetOffsetX = { it },
                    animationSpec = tween(250, easing = FastOutSlowInEasing)
                ),
                modifier = Modifier.align(Alignment.CenterEnd)
            ) {
                val systemBarsPadding = WindowInsets.systemBars.asPaddingValues()

                Surface(
                    modifier = Modifier
                        .width(400.dp)
                        .fillMaxHeight()
                        .pointerInput(Unit) { detectTapGestures { /* consume */ } },
                    shape = RoundedCornerShape(topStart = 20.dp, bottomStart = 20.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 2.dp,
                    shadowElevation = 8.dp
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(
                                top = systemBarsPadding.calculateTopPadding(),
                                bottom = systemBarsPadding.calculateBottomPadding()
                            )
                    ) {
                        content()

                        // Close button
                        Surface(
                            onClick = onDismiss,
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surfaceContainerHigh,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = "Close",
                                modifier = Modifier
                                    .padding(8.dp)
                                    .size(20.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    } else {
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
            containerColor = MaterialTheme.colorScheme.surface,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.75f)
            ) {
                content()
            }
        }
    }
}

@Composable
private fun ChangelogContent() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
    ) {
        // Fixed title
        Text(
            text = stringResource(R.string.changelog),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 20.dp)
        )

        // Scrollable version list
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            ChangelogVersionItem(
                version = "2.1.1",
                date = "March 2026",
                isLatest = true,
                changes = listOf(
                    "F-Droid metadata and fastlane structure"
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            ChangelogVersionItem(
                version = "2.1.0",
                date = "March 2026",
                isLatest = false,
                changes = listOf(
                    "Horizontal page scrubber for horizontal scroll mode",
                    "Global settings for snap-to-pages preference",
                    "Global settings for screen orientation lock",
                    "Simplified view mode options"
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            ChangelogVersionItem(
                version = "2.0.0",
                date = "March 2026",
                isLatest = false,
                changes = listOf(
                    "Complete UI redesign with Material 3",
                    "New PDF viewer with PDF.js engine",
                    "Bookmarks and favorites support",
                    "Reading themes (Light, Sepia, Dark, Black)",
                    "Auto-scroll functionality",
                    "Page jump and search in documents",
                    "Table of contents with attachments",
                    "PDF tools (merge, split, compress, rotate, and more)",
                    "Customizable reader settings",
                    "Dark mode and system theme support",
                    "Folder browser with sorting options",
                    "Recent files tracking",
                    "Search across all PDFs"
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            ChangelogVersionItem(
                version = "1.0.0",
                date = "May 2024",
                isLatest = false,
                changes = listOf(
                    "Initial release",
                    "Basic PDF viewing functionality",
                    "Light and dark theme support",
                    "File browser integration"
                )
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun ChangelogVersionItem(
    version: String,
    date: String,
    isLatest: Boolean,
    changes: List<String>
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = if (isLatest) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        else MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.version_format, version),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    if (isLatest) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.primary
                        ) {
                            Text(
                                text = stringResource(R.string.latest_badge),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                Text(
                    text = date,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            changes.forEach { change ->
                Row(modifier = Modifier.padding(vertical = 4.dp)) {
                    Text(
                        text = "•",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = change,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
private fun PrivacyPolicyContent() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(bottom = 32.dp)
    ) {
        Text(
            text = stringResource(R.string.privacy_policy),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 20.dp)
        )

        // Privacy Highlights Card
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = stringResource(R.string.privacy_protected),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                PrivacyHighlightItem(stringResource(R.string.no_data_collection))
                PrivacyHighlightItem(stringResource(R.string.no_analytics))
                PrivacyHighlightItem(stringResource(R.string.optional_updates))
                PrivacyHighlightItem(stringResource(R.string.local_storage))
            }
        }

        PrivacySection(
            title = stringResource(R.string.privacy_no_collection_title),
            content = stringResource(R.string.privacy_no_collection_content)
        )

        PrivacySection(
            title = stringResource(R.string.privacy_local_storage_title),
            content = stringResource(R.string.privacy_local_storage_content)
        )

        PrivacySection(
            title = stringResource(R.string.privacy_file_access_title),
            content = stringResource(R.string.privacy_file_access_content)
        )

        PrivacySection(
            title = stringResource(R.string.privacy_password_title),
            content = stringResource(R.string.privacy_password_content)
        )

        PrivacySection(
            title = stringResource(R.string.privacy_update_title),
            content = stringResource(R.string.privacy_update_content)
        )

        Text(
            text = stringResource(R.string.last_updated),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 16.dp)
        )
    }
}

@Composable
private fun PrivacyHighlightItem(text: String) {
    Row(
        modifier = Modifier.padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Rounded.CheckCircle,
            contentDescription = stringResource(R.string.cd_decorative),
            modifier = Modifier.size(18.dp),
            tint = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@Composable
private fun PrivacySection(title: String, content: String) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 6.dp)
            )
            Text(
                text = content,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun LicensesContent() {
    val licenses = remember {
        listOf(
            LicenseContent(
                title = "Jetpack Compose",
                author = "Google",
                license = Licenses.APACHE_2_0,
                url = "https://developer.android.com/jetpack/compose"
            ),
            LicenseContent(
                title = "Koin",
                author = "Kotzilla",
                license = Licenses.APACHE_2_0,
                url = "https://insert-koin.io/"
            ),
            LicenseContent(
                title = "Room Database",
                author = "Google",
                license = Licenses.APACHE_2_0,
                url = "https://developer.android.com/training/data-storage/room"
            ),
            LicenseContent(
                title = "PDF.js",
                author = "Mozilla",
                license = Licenses.APACHE_2_0,
                url = "https://mozilla.github.io/pdf.js/"
            ),
            LicenseContent(
                title = "Material Components",
                author = "Google",
                license = Licenses.APACHE_2_0,
                url = "https://github.com/material-components/material-components-android"
            ),
            LicenseContent(
                title = "Kotlin Coroutines",
                author = "JetBrains",
                license = Licenses.APACHE_2_0,
                url = "https://github.com/Kotlin/kotlinx.coroutines"
            ),
            LicenseContent(
                title = "AndroidX Libraries",
                author = "Google",
                license = Licenses.APACHE_2_0,
                url = "https://developer.android.com/jetpack/androidx"
            ),
            LicenseContent(
                title = "DataStore",
                author = "Google",
                license = Licenses.APACHE_2_0,
                url = "https://developer.android.com/topic/libraries/architecture/datastore"
            ),
            LicenseContent(
                title = "Coil",
                author = "Coil Contributors",
                license = Licenses.APACHE_2_0,
                url = "https://coil-kt.github.io/coil/"
            ),
            LicenseContent(
                title = "Timber",
                author = "Jake Wharton",
                license = Licenses.APACHE_2_0,
                url = "https://github.com/JakeWharton/timber"
            ),
            LicenseContent(
                title = "Lottie",
                author = "Airbnb",
                license = Licenses.APACHE_2_0,
                url = "https://airbnb.io/lottie/"
            ),
            LicenseContent(
                title = "Reorderable",
                author = "Calvin Liang",
                license = Licenses.APACHE_2_0,
                url = "https://github.com/Calvin-LL/Reorderable"
            ),
            LicenseContent(
                title = "Kotlinx Serialization",
                author = "JetBrains",
                license = Licenses.APACHE_2_0,
                url = "https://github.com/Kotlin/kotlinx.serialization"
            ),
            LicenseContent(
                title = "iText",
                author = "iText Software",
                license = Licenses.AGPL_3_0,
                url = "https://itextpdf.com/"
            ),
            LicenseContent(
                title = "Bouncy Castle",
                author = "Legion of the Bouncy Castle",
                license = Licenses.MIT,
                url = "https://www.bouncycastle.org/"
            ),
            LicenseContent(
                title = "Navigation Compose",
                author = "Google",
                license = Licenses.APACHE_2_0,
                url = "https://developer.android.com/jetpack/compose/navigation"
            ),
            LicenseContent(
                title = "Ktor",
                author = "JetBrains",
                license = Licenses.APACHE_2_0,
                url = "https://ktor.io/"
            ),
            LicenseContent(
                title = "Licensy",
                author = "K M Rejowan Ahmmed",
                license = Licenses.APACHE_2_0,
                url = "https://github.com/ahmmedrejowan/Licensy"
            )
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 32.dp)
    ) {
        Text(
            text = stringResource(R.string.open_source_licenses),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 20.dp)
        )

        LicensyList(licenses = licenses)
    }
}

@Composable
private fun CreatorContent() {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(bottom = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.about_creator),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        Text(
            text = stringResource(R.string.creator_name),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Text(
            text = stringResource(R.string.creator_title),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
        )

        Text(
            text = stringResource(R.string.about_app_description),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceContainerLow
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                CreatorLinkItem(
                    icon = Icons.Rounded.Language,
                    label = "Website",
                    value = "rejowan.com",
                    accentColor = AccentBlue,
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, "https://rejowan.com".toUri())
                        context.startActivity(intent)
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                CreatorLinkItem(
                    icon = Icons.Rounded.Email,
                    label = "Email",
                    value = "kmrejowan@gmail.com",
                    accentColor = AccentAmber,
                    onClick = {
                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                            data = "mailto:kmrejowan@gmail.com".toUri()
                        }
                        context.startActivity(intent)
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                CreatorLinkItem(
                    icon = Icons.Rounded.Code,
                    label = "GitHub",
                    value = "github.com/ahmmedrejowan",
                    accentColor = AccentPurple,
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, "https://github.com/ahmmedrejowan".toUri())
                        context.startActivity(intent)
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                CreatorLinkItem(
                    icon = Icons.Rounded.Work,
                    label = "LinkedIn",
                    value = "linkedin.com/in/ahmmedrejowan",
                    accentColor = AccentTeal,
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, "https://linkedin.com/in/ahmmedrejowan".toUri())
                        context.startActivity(intent)
                    }
                )
            }
        }

    }
}

@Composable
private fun CreatorLinkItem(
    icon: ImageVector,
    label: String,
    value: String,
    accentColor: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = accentColor.copy(alpha = 0.12f)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = stringResource(R.string.cd_decorative),
                modifier = Modifier
                    .padding(8.dp)
                    .size(18.dp),
                tint = accentColor
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
            contentDescription = stringResource(R.string.cd_decorative),
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
    }
}

@Composable
private fun AppLicenseContent() {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(bottom = 32.dp)
    ) {
        Text(
            text = stringResource(R.string.gpl_full_name),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 20.dp)
        )

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceContainerLow
        ) {
            Text(
                text = stringResource(R.string.gpl_notice, java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(16.dp)
            )
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = stringResource(R.string.key_terms),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                LicenseTermItem(stringResource(R.string.gpl_freedom_use))
                LicenseTermItem(stringResource(R.string.gpl_freedom_study))
                LicenseTermItem(stringResource(R.string.gpl_freedom_distribute))
                LicenseTermItem(stringResource(R.string.gpl_freedom_modify))
                LicenseTermItem(stringResource(R.string.gpl_derivative))
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .clickable {
                    val intent = Intent(
                        Intent.ACTION_VIEW,
                        "https://www.gnu.org/licenses/gpl-3.0.en.html".toUri()
                    )
                    context.startActivity(intent)
                },
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.primary
        ) {
            Text(
                text = stringResource(R.string.view_gpl_license),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onPrimary,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )
        }
    }
}

@Composable
private fun LicenseTermItem(text: String) {
    Row(
        modifier = Modifier.padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Rounded.CheckCircle,
            contentDescription = stringResource(R.string.cd_decorative),
            modifier = Modifier.size(18.dp),
            tint = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}
