package com.rejowan.pdfreaderpro.presentation.screens.settings

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
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.LightMode
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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

    // Sheet visibility states
    var showThemeModeSheet by remember { mutableStateOf(false) }
    var showScrollDirectionSheet by remember { mutableStateOf(false) }
    var showPageAlignmentSheet by remember { mutableStateOf(false) }
    var showQuickZoomSheet by remember { mutableStateOf(false) }
    var showReadingThemeSheet by remember { mutableStateOf(false) }
    var showBrightnessSheet by remember { mutableStateOf(false) }

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

        SettingsInfoItem(
            icon = Icons.Rounded.Info,
            title = "App Version",
            value = "2.0.0",
            accentColor = AccentBlue,
            animationDelay = 550
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
