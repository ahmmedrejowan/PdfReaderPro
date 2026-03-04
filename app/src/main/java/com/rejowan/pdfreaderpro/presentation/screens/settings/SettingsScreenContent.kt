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
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.rounded.Brightness6
import androidx.compose.material.icons.rounded.BrightnessHigh
import androidx.compose.material.icons.rounded.BrightnessLow
import androidx.compose.material.icons.rounded.ColorLens
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.FormatAlignCenter
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Gavel
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Policy
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.PhoneAndroid
import androidx.compose.material.icons.rounded.ScreenLockPortrait
import androidx.compose.material.icons.rounded.SwapVert
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material.icons.rounded.ZoomIn
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.rejowan.pdfreaderpro.BuildConfig
import com.rejowan.pdfreaderpro.domain.model.PageAlignment
import com.rejowan.pdfreaderpro.domain.model.QuickZoomPreset
import com.rejowan.pdfreaderpro.domain.model.ReadingTheme
import com.rejowan.pdfreaderpro.domain.model.ScrollDirection
import com.rejowan.pdfreaderpro.domain.model.ThemeMode
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

    val context = LocalContext.current

    // Sheet visibility states
    var showThemeModeSheet by remember { mutableStateOf(false) }
    var showScrollDirectionSheet by remember { mutableStateOf(false) }
    var showPageAlignmentSheet by remember { mutableStateOf(false) }
    var showQuickZoomSheet by remember { mutableStateOf(false) }
    var showReadingThemeSheet by remember { mutableStateOf(false) }
    var showBrightnessSheet by remember { mutableStateOf(false) }

    // About section sheets
    var showChangelogSheet by remember { mutableStateOf(false) }
    var showPrivacyPolicySheet by remember { mutableStateOf(false) }
    var showLicensesSheet by remember { mutableStateOf(false) }
    var showCreatorSheet by remember { mutableStateOf(false) }
    var showAppLicenseSheet by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .padding(top = 8.dp, bottom = 80.dp)
    ) {
        // Appearance Section
        SectionLabel(text = "Appearance", delay = 0)
        Spacer(modifier = Modifier.height(8.dp))

        SettingsOptionItem(
            icon = Icons.Rounded.Palette,
            title = "Theme",
            subtitle = when (preferences.themeMode) {
                ThemeMode.LIGHT -> "Light"
                ThemeMode.DARK -> "Dark"
                ThemeMode.SYSTEM -> "System Default"
            },
            accentColor = AccentPurple,
            onClick = { showThemeModeSheet = true },
            animationDelay = 50
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Reader Section
        SectionLabel(text = "Reader", delay = 100)
        Spacer(modifier = Modifier.height(8.dp))

        SettingsOptionItem(
            icon = Icons.Rounded.Brightness6,
            title = "Brightness",
            subtitle = if (preferences.readerBrightness < 0) "System Default" else "${(preferences.readerBrightness * 100).toInt()}%",
            accentColor = AccentAmber,
            onClick = { showBrightnessSheet = true },
            animationDelay = 150
        )

        Spacer(modifier = Modifier.height(8.dp))

        SettingsOptionItem(
            icon = Icons.Rounded.SwapVert,
            title = "Scroll Direction",
            subtitle = preferences.readerScrollDirection.name.lowercase().replaceFirstChar { it.uppercase() },
            accentColor = AccentPurple,
            onClick = { showScrollDirectionSheet = true },
            animationDelay = 200
        )

        Spacer(modifier = Modifier.height(8.dp))

        SettingsOptionItem(
            icon = Icons.Rounded.FormatAlignCenter,
            title = "Page Alignment",
            subtitle = preferences.readerPageAlignment.name.lowercase().replaceFirstChar { it.uppercase() },
            accentColor = AccentTeal,
            onClick = { showPageAlignmentSheet = true },
            animationDelay = 250
        )

        Spacer(modifier = Modifier.height(8.dp))

        SettingsOptionItem(
            icon = Icons.Rounded.ZoomIn,
            title = "Default Zoom",
            subtitle = when (preferences.readerQuickZoomPreset) {
                QuickZoomPreset.FIT_PAGE -> "Fit Page"
                QuickZoomPreset.FIT_WIDTH -> "Fit Width"
                QuickZoomPreset.ACTUAL_SIZE -> "100%"
            },
            accentColor = AccentBlue,
            onClick = { showQuickZoomSheet = true },
            animationDelay = 300
        )

        Spacer(modifier = Modifier.height(8.dp))

        SettingsOptionItem(
            icon = Icons.Rounded.ColorLens,
            title = "Reading Theme",
            subtitle = preferences.readerTheme.name.lowercase().replaceFirstChar { it.uppercase() },
            accentColor = AccentAmber,
            onClick = { showReadingThemeSheet = true },
            animationDelay = 350
        )

        Spacer(modifier = Modifier.height(16.dp))

        SettingsToggleItem(
            icon = Icons.Rounded.VisibilityOff,
            title = "Auto-Hide Toolbar",
            subtitle = "Hide toolbar automatically while reading",
            accentColor = AccentPurple,
            checked = preferences.readerAutoHideToolbar,
            onCheckedChange = { viewModel.setReaderAutoHideToolbar(it) },
            animationDelay = 400
        )

        Spacer(modifier = Modifier.height(8.dp))

        SettingsToggleItem(
            icon = Icons.Rounded.ScreenLockPortrait,
            title = "Keep Screen On",
            subtitle = "Prevent screen from turning off",
            accentColor = AccentTeal,
            checked = preferences.readerKeepScreenOn,
            onCheckedChange = { viewModel.setReaderKeepScreenOn(it) },
            animationDelay = 450
        )

        Spacer(modifier = Modifier.height(24.dp))

        // About Section
        SectionLabel(text = "About", delay = 500)
        Spacer(modifier = Modifier.height(8.dp))

        SettingsOptionItem(
            icon = Icons.Rounded.Info,
            title = "Version ${BuildConfig.VERSION_NAME}",
            subtitle = "View changelog",
            accentColor = AccentBlue,
            onClick = { showChangelogSheet = true },
            animationDelay = 550
        )

        Spacer(modifier = Modifier.height(8.dp))

        SettingsOptionItem(
            icon = Icons.Rounded.Policy,
            title = "Privacy Policy",
            subtitle = "View our privacy policy",
            accentColor = AccentTeal,
            onClick = { showPrivacyPolicySheet = true },
            animationDelay = 600
        )

        Spacer(modifier = Modifier.height(8.dp))

        SettingsOptionItem(
            icon = Icons.Rounded.Code,
            title = "Open Source Licenses",
            subtitle = "View third-party libraries",
            accentColor = AccentPurple,
            onClick = { showLicensesSheet = true },
            animationDelay = 650
        )

        Spacer(modifier = Modifier.height(8.dp))

        SettingsOptionItem(
            icon = Icons.Rounded.Person,
            title = "Creator",
            subtitle = "About the developer",
            accentColor = AccentAmber,
            onClick = { showCreatorSheet = true },
            animationDelay = 700
        )

        Spacer(modifier = Modifier.height(8.dp))

        SettingsOptionItem(
            icon = Icons.Rounded.Gavel,
            title = "App License",
            subtitle = "GNU General Public License v3.0",
            accentColor = AccentBlue,
            onClick = { showAppLicenseSheet = true },
            animationDelay = 750
        )

        Spacer(modifier = Modifier.height(8.dp))

        SettingsOptionItem(
            icon = Icons.Rounded.Email,
            title = "Contact",
            subtitle = "Get in touch with the developer",
            accentColor = AccentTeal,
            onClick = {
                val intent = Intent(Intent.ACTION_SENDTO).apply {
                    data = "mailto:kmrejowan@gmail.com".toUri()
                    putExtra(Intent.EXTRA_SUBJECT, "PDF Reader Pro Feedback")
                }
                context.startActivity(intent)
            },
            animationDelay = 800
        )

        Spacer(modifier = Modifier.height(8.dp))

        SettingsOptionItem(
            icon = Icons.Rounded.History,
            title = "GitHub Repository",
            subtitle = "View source code",
            accentColor = AccentPurple,
            onClick = {
                val intent = Intent(
                    Intent.ACTION_VIEW,
                    "https://github.com/ahmmedrejowan/PdfReaderPro".toUri()
                )
                context.startActivity(intent)
            },
            animationDelay = 850
        )

        Spacer(modifier = Modifier.height(32.dp))
    }

    // Theme Mode picker
    if (showThemeModeSheet) {
        SettingsPickerSheet(
            title = "Theme",
            subtitle = "Choose app appearance",
            icon = Icons.Rounded.Palette,
            accentColor = AccentPurple,
            options = listOf(
                PickerOption(Icons.Rounded.LightMode, "Light", "Always use light theme"),
                PickerOption(Icons.Rounded.DarkMode, "Dark", "Always use dark theme"),
                PickerOption(Icons.Rounded.PhoneAndroid, "System", "Follow system setting")
            ),
            selectedIndex = ThemeMode.entries.indexOf(preferences.themeMode),
            onSelect = { index ->
                viewModel.setThemeMode(ThemeMode.entries[index])
                showThemeModeSheet = false
            },
            onDismiss = { showThemeModeSheet = false }
        )
    }

    // Scroll Direction picker
    if (showScrollDirectionSheet) {
        SettingsPickerSheet(
            title = "Scroll Direction",
            subtitle = "Choose how pages scroll",
            icon = Icons.Rounded.SwapVert,
            accentColor = AccentPurple,
            options = listOf(
                PickerOption(Icons.Rounded.SwapVert, "Vertical", "Scroll up and down"),
                PickerOption(Icons.Rounded.SwapVert, "Horizontal", "Scroll left and right")
            ),
            selectedIndex = ScrollDirection.entries.indexOf(preferences.readerScrollDirection),
            onSelect = { index ->
                viewModel.setReaderScrollDirection(ScrollDirection.entries[index])
                showScrollDirectionSheet = false
            },
            onDismiss = { showScrollDirectionSheet = false }
        )
    }

    // Page Alignment picker
    if (showPageAlignmentSheet) {
        SettingsPickerSheet(
            title = "Page Alignment",
            subtitle = "Position pages on screen",
            icon = Icons.Rounded.FormatAlignCenter,
            accentColor = AccentTeal,
            options = listOf(
                PickerOption(Icons.Rounded.FormatAlignCenter, "Left", "Align pages to left"),
                PickerOption(Icons.Rounded.FormatAlignCenter, "Center", "Center pages"),
                PickerOption(Icons.Rounded.FormatAlignCenter, "Right", "Align pages to right")
            ),
            selectedIndex = PageAlignment.entries.indexOf(preferences.readerPageAlignment),
            onSelect = { index ->
                viewModel.setReaderPageAlignment(PageAlignment.entries[index])
                showPageAlignmentSheet = false
            },
            onDismiss = { showPageAlignmentSheet = false }
        )
    }

    // Quick Zoom picker
    if (showQuickZoomSheet) {
        SettingsPickerSheet(
            title = "Default Zoom",
            subtitle = "Initial zoom when opening PDFs",
            icon = Icons.Rounded.ZoomIn,
            accentColor = AccentBlue,
            options = listOf(
                PickerOption(Icons.Rounded.ZoomIn, "Fit Page", "Show entire page"),
                PickerOption(Icons.Rounded.ZoomIn, "Fit Width", "Match page width to screen"),
                PickerOption(Icons.Rounded.ZoomIn, "100%", "Actual size")
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
            title = "Reading Theme",
            subtitle = "Background color while reading",
            icon = Icons.Rounded.ColorLens,
            accentColor = AccentAmber,
            options = listOf(
                PickerOption(Icons.Rounded.LightMode, "Light", "White background"),
                PickerOption(Icons.Rounded.ColorLens, "Sepia", "Warm paper tone"),
                PickerOption(Icons.Rounded.DarkMode, "Dark", "Dark gray background"),
                PickerOption(Icons.Rounded.DarkMode, "Black", "Pure black (AMOLED)")
            ),
            selectedIndex = ReadingTheme.entries.indexOf(preferences.readerTheme),
            onSelect = { index ->
                viewModel.setReaderTheme(ReadingTheme.entries[index])
                showReadingThemeSheet = false
            },
            onDismiss = { showReadingThemeSheet = false }
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
        modifier = modifier.padding(start = 4.dp, bottom = 4.dp)
    )
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
                    contentDescription = null,
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
                contentDescription = null,
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
                    contentDescription = null,
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

@Composable
private fun SettingsInfoItem(
    icon: ImageVector,
    title: String,
    value: String,
    accentColor: Color,
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
        label = "info scale"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .scale(scale),
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
                    contentDescription = null,
                    modifier = Modifier
                        .padding(8.dp)
                        .size(20.dp),
                    tint = accentColor
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )

            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
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
                    contentDescription = null,
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
                    contentDescription = null,
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
                    contentDescription = null,
                    modifier = Modifier
                        .padding(6.dp)
                        .size(16.dp),
                    tint = AccentAmber
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column {
                Text(
                    text = "Brightness",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Adjust screen brightness",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Options
        BrightnessOptionItem(
            icon = Icons.Rounded.PhoneAndroid,
            title = "System Default",
            subtitle = "Follow system brightness",
            isSelected = isSystemDefault,
            accentColor = AccentAmber,
            onClick = { onBrightnessChange(-1f) },
            animationDelay = 50
        )

        Spacer(modifier = Modifier.height(8.dp))

        BrightnessOptionItem(
            icon = Icons.Rounded.Brightness6,
            title = "Custom",
            subtitle = if (!isSystemDefault) "${(sliderValue * 100).toInt()}%" else "Set custom level",
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
                    contentDescription = null,
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
                .pointerInput(Unit) { detectTapGestures { onDismiss() } }
        ) {
            AnimatedVisibility(
                visible = true,
                enter = slideInHorizontally(
                    initialOffsetX = { it },
                    animationSpec = tween(300, easing = FastOutSlowInEasing)
                ) + fadeIn(animationSpec = tween(200)),
                exit = slideOutHorizontally(
                    targetOffsetX = { it },
                    animationSpec = tween(300, easing = FastOutSlowInEasing)
                ) + fadeOut(animationSpec = tween(200)),
                modifier = Modifier.align(Alignment.CenterEnd)
            ) {
                Surface(
                    modifier = Modifier
                        .width(400.dp)
                        .fillMaxHeight()
                        .pointerInput(Unit) { detectTapGestures { /* consume */ } },
                    shape = RoundedCornerShape(topStart = 20.dp, bottomStart = 20.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 2.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(
                                top = WindowInsets.systemBars.asPaddingValues().calculateTopPadding(),
                                bottom = WindowInsets.systemBars.asPaddingValues().calculateBottomPadding()
                            )
                    ) {
                        content()
                    }
                }
            }
        }
    } else {
        // Bottom sheet for portrait
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            content()
        }
    }
}

@Composable
private fun ChangelogContent() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(bottom = 32.dp)
    ) {
        Text(
            text = "Changelog",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 20.dp)
        )

        ChangelogVersionItem(
            version = BuildConfig.VERSION_NAME,
            date = "2026-03-01",
            changes = listOf(
                "Complete UI redesign with Material 3",
                "New PDF viewer with PDF.js engine",
                "Bookmarks and favorites support",
                "Reading themes (Light, Sepia, Dark, Black)",
                "Auto-scroll functionality",
                "Page jump and search",
                "Table of contents with attachments",
                "Customizable reader settings",
                "Dark mode and system theme support"
            )
        )
    }
}

@Composable
private fun ChangelogVersionItem(
    version: String,
    date: String,
    changes: List<String>
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Version $version",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
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
            text = "Privacy Policy",
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
                    text = "Your Privacy is Protected",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                PrivacyHighlightItem("No internet connection required")
                PrivacyHighlightItem("No data collection or sharing")
                PrivacyHighlightItem("No analytics or tracking")
                PrivacyHighlightItem("100% offline operation")
            }
        }

        PrivacySection(
            title = "No Data Collection",
            content = "PDF Reader Pro does not collect, store, transmit, or share any personal data. The app operates completely offline and does not require an internet connection. There are no analytics, tracking, or telemetry of any kind."
        )

        PrivacySection(
            title = "Local Data Storage",
            content = "All bookmarks, favorites, and app preferences are stored exclusively on your device. This data never leaves your device and is not accessible to anyone except you. You have complete control and can delete this data at any time."
        )

        PrivacySection(
            title = "File Access",
            content = "PDF Reader Pro requires storage permission to read PDF files from your device. This access is used solely to display your documents and is never used to collect or transmit any information."
        )

        PrivacySection(
            title = "Password Storage",
            content = "If you choose to save passwords for protected PDFs, they are stored locally using encrypted storage. Passwords never leave your device."
        )

        Text(
            text = "Last updated: March 1, 2026",
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
        Text(
            text = "✓",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.padding(end = 10.dp)
        )
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
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(bottom = 32.dp)
    ) {
        Text(
            text = "Open Source Licenses",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 20.dp)
        )

        LicenseItem(
            name = "Jetpack Compose",
            author = "Google",
            license = "Apache License 2.0",
            url = "https://developer.android.com/jetpack/compose",
            context = context
        )

        LicenseItem(
            name = "Koin",
            author = "Kotzilla",
            license = "Apache License 2.0",
            url = "https://insert-koin.io/",
            context = context
        )

        LicenseItem(
            name = "Room Database",
            author = "Google",
            license = "Apache License 2.0",
            url = "https://developer.android.com/training/data-storage/room",
            context = context
        )

        LicenseItem(
            name = "PDF.js",
            author = "Mozilla",
            license = "Apache License 2.0",
            url = "https://mozilla.github.io/pdf.js/",
            context = context
        )

        LicenseItem(
            name = "Material Components",
            author = "Google",
            license = "Apache License 2.0",
            url = "https://github.com/material-components/material-components-android",
            context = context
        )

        LicenseItem(
            name = "Kotlin Coroutines",
            author = "JetBrains",
            license = "Apache License 2.0",
            url = "https://github.com/Kotlin/kotlinx.coroutines",
            context = context
        )

        LicenseItem(
            name = "AndroidX Libraries",
            author = "Google",
            license = "Apache License 2.0",
            url = "https://developer.android.com/jetpack/androidx",
            context = context
        )

        LicenseItem(
            name = "DataStore",
            author = "Google",
            license = "Apache License 2.0",
            url = "https://developer.android.com/topic/libraries/architecture/datastore",
            context = context
        )
    }
}

@Composable
private fun LicenseItem(
    name: String,
    author: String,
    license: String,
    url: String,
    context: android.content.Context
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable {
                val intent = Intent(Intent.ACTION_VIEW, url.toUri())
                context.startActivity(intent)
            },
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = author,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp)
            )
            Text(
                text = license,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
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
            text = "About the Creator",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        Text(
            text = "K M Rejowan Ahmmed",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Text(
            text = "Senior Android Developer",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
        )

        Text(
            text = "PDF Reader Pro was created to provide a free, open-source, and privacy-focused PDF reading experience. With features like bookmarks, favorites, reading themes, and customizable settings, it aims to be a complete PDF solution for Android users.",
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
                    icon = "🌐",
                    label = "Website",
                    value = "rejowan.com",
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, "https://rejowan.com".toUri())
                        context.startActivity(intent)
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                CreatorLinkItem(
                    icon = "📧",
                    label = "Email",
                    value = "kmrejowan@gmail.com",
                    onClick = {
                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                            data = "mailto:kmrejowan@gmail.com".toUri()
                        }
                        context.startActivity(intent)
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                CreatorLinkItem(
                    icon = "💼",
                    label = "GitHub",
                    value = "github.com/ahmmedrejowan",
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, "https://github.com/ahmmedrejowan".toUri())
                        context.startActivity(intent)
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                CreatorLinkItem(
                    icon = "🔗",
                    label = "LinkedIn",
                    value = "linkedin.com/in/ahmmedrejowan",
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, "https://linkedin.com/in/ahmmedrejowan".toUri())
                        context.startActivity(intent)
                    }
                )
            }
        }

        Row(
            modifier = Modifier.padding(top = 24.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Made with ❤️ by ",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "K M Rejowan Ahmmed",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun CreatorLinkItem(
    icon: String,
    label: String,
    value: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = icon,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(end = 16.dp)
        )
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
            contentDescription = null,
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
            text = "GNU General Public License v3.0",
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
                text = """
PDF Reader Pro - Open Source PDF Viewer
Copyright (C) 2026 K M Rejowan Ahmmed

This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
                """.trimIndent(),
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
                    text = "Key Terms",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                LicenseTermItem("✓ Freedom to use the software for any purpose")
                LicenseTermItem("✓ Freedom to study and modify the source code")
                LicenseTermItem("✓ Freedom to distribute copies")
                LicenseTermItem("✓ Freedom to distribute modified versions")
                LicenseTermItem("✓ Derivative works must be open source under GPL v3.0")
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
                text = "View Full GPL v3.0 License",
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
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onPrimaryContainer,
        modifier = Modifier.padding(vertical = 4.dp)
    )
}
