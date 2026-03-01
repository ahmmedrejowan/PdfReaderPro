package com.rejowan.pdfreaderpro.presentation.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brightness6
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.FormatAlignCenter
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.ScreenLockPortrait
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.ui.draw.clip
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.rejowan.pdfreaderpro.domain.model.PageAlignment
import com.rejowan.pdfreaderpro.domain.model.QuickZoomPreset
import com.rejowan.pdfreaderpro.domain.model.ReadingTheme
import com.rejowan.pdfreaderpro.domain.model.ScrollDirection
import com.rejowan.pdfreaderpro.domain.model.ThemeMode
import org.koin.androidx.compose.koinViewModel

/**
 * Reusable Settings content that can be embedded in bottom navigation.
 * Use this for bottom nav settings tab, and SettingsScreen for standalone navigation.
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
            .padding(bottom = 80.dp)
    ) {
        SettingsSection(title = "Appearance")

        SettingsItem(
            icon = Icons.Default.DarkMode,
            title = "Theme",
            subtitle = when (preferences.themeMode) {
                ThemeMode.LIGHT -> "Light"
                ThemeMode.DARK -> "Dark"
                ThemeMode.SYSTEM -> "System Default"
            },
            onClick = { showThemeModeSheet = true }
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        SettingsSection(title = "Reader")

        SettingsItem(
            icon = Icons.Default.Brightness6,
            title = "Brightness",
            subtitle = if (preferences.readerBrightness < 0) "System Default" else "${(preferences.readerBrightness * 100).toInt()}%",
            onClick = { showBrightnessSheet = true }
        )

        SettingsItem(
            icon = Icons.Default.SwapVert,
            title = "Scroll Direction",
            subtitle = preferences.readerScrollDirection.name.lowercase().replaceFirstChar { it.uppercase() },
            onClick = { showScrollDirectionSheet = true }
        )

        SettingsItem(
            icon = Icons.Default.FormatAlignCenter,
            title = "Page Alignment",
            subtitle = preferences.readerPageAlignment.name.lowercase().replaceFirstChar { it.uppercase() },
            onClick = { showPageAlignmentSheet = true }
        )

        SettingsItem(
            icon = Icons.Default.ZoomIn,
            title = "Default Zoom",
            subtitle = when (preferences.readerQuickZoomPreset) {
                QuickZoomPreset.FIT_PAGE -> "Fit Page"
                QuickZoomPreset.FIT_WIDTH -> "Fit Width"
                QuickZoomPreset.ACTUAL_SIZE -> "100%"
            },
            onClick = { showQuickZoomSheet = true }
        )

        SettingsItem(
            icon = Icons.Default.ColorLens,
            title = "Reading Theme",
            subtitle = preferences.readerTheme.name.lowercase().replaceFirstChar { it.uppercase() },
            onClick = { showReadingThemeSheet = true }
        )

        SettingsSwitchItem(
            icon = Icons.Default.VisibilityOff,
            title = "Auto-Hide Toolbar",
            subtitle = "Hide toolbar automatically while reading",
            checked = preferences.readerAutoHideToolbar,
            onCheckedChange = { viewModel.setReaderAutoHideToolbar(it) }
        )

        SettingsSwitchItem(
            icon = Icons.Default.ScreenLockPortrait,
            title = "Keep Screen On",
            subtitle = "Prevent screen from turning off while reading",
            checked = preferences.readerKeepScreenOn,
            onCheckedChange = { viewModel.setReaderKeepScreenOn(it) }
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        SettingsSection(title = "About")

        SettingsItem(
            icon = Icons.Default.Info,
            title = "App Version",
            subtitle = "2.0.0",
            onClick = { }
        )

        Spacer(modifier = Modifier.height(32.dp))
    }

    // Scroll Direction picker sheet
    if (showScrollDirectionSheet) {
        SettingsPickerSheet(
            title = "Scroll Direction",
            options = ScrollDirection.entries.map { it.name.lowercase().replaceFirstChar { c -> c.uppercase() } },
            selectedIndex = ScrollDirection.entries.indexOf(preferences.readerScrollDirection),
            onSelect = { index ->
                viewModel.setReaderScrollDirection(ScrollDirection.entries[index])
                showScrollDirectionSheet = false
            },
            onDismiss = { showScrollDirectionSheet = false }
        )
    }

    // Page Alignment picker sheet
    if (showPageAlignmentSheet) {
        SettingsPickerSheet(
            title = "Page Alignment",
            options = PageAlignment.entries.map { it.name.lowercase().replaceFirstChar { c -> c.uppercase() } },
            selectedIndex = PageAlignment.entries.indexOf(preferences.readerPageAlignment),
            onSelect = { index ->
                viewModel.setReaderPageAlignment(PageAlignment.entries[index])
                showPageAlignmentSheet = false
            },
            onDismiss = { showPageAlignmentSheet = false }
        )
    }

    // Quick Zoom picker sheet
    if (showQuickZoomSheet) {
        SettingsPickerSheet(
            title = "Default Zoom",
            options = listOf("Fit Page", "Fit Width", "100%"),
            selectedIndex = QuickZoomPreset.entries.indexOf(preferences.readerQuickZoomPreset),
            onSelect = { index ->
                viewModel.setReaderQuickZoomPreset(QuickZoomPreset.entries[index])
                showQuickZoomSheet = false
            },
            onDismiss = { showQuickZoomSheet = false }
        )
    }

    // Reading Theme picker sheet
    if (showReadingThemeSheet) {
        SettingsPickerSheet(
            title = "Reading Theme",
            options = ReadingTheme.entries.map { it.name.lowercase().replaceFirstChar { c -> c.uppercase() } },
            selectedIndex = ReadingTheme.entries.indexOf(preferences.readerTheme),
            onSelect = { index ->
                viewModel.setReaderTheme(ReadingTheme.entries[index])
                showReadingThemeSheet = false
            },
            onDismiss = { showReadingThemeSheet = false }
        )
    }

    // Brightness picker sheet
    if (showBrightnessSheet) {
        BrightnessPickerSheet(
            currentBrightness = preferences.readerBrightness,
            onBrightnessChange = { viewModel.setReaderBrightness(it) },
            onDismiss = { showBrightnessSheet = false }
        )
    }

    // Theme Mode picker sheet
    if (showThemeModeSheet) {
        SettingsPickerSheet(
            title = "Theme",
            options = listOf("Light", "Dark", "System Default"),
            selectedIndex = ThemeMode.entries.indexOf(preferences.themeMode),
            onSelect = { index ->
                viewModel.setThemeMode(ThemeMode.entries[index])
                showThemeModeSheet = false
            },
            onDismiss = { showThemeModeSheet = false }
        )
    }
}

@Composable
private fun SettingsSection(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
    )
}

@Composable
private fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SettingsSwitchItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsPickerSheet(
    title: String,
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            options.forEachIndexed { index, option ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onSelect(index) }
                        .padding(vertical = 12.dp, horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = index == selectedIndex,
                        onClick = { onSelect(index) }
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = option,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BrightnessPickerSheet(
    currentBrightness: Float,
    onBrightnessChange: (Float) -> Unit,
    onDismiss: () -> Unit
) {
    val isSystemDefault = currentBrightness < 0
    var sliderValue by remember { mutableStateOf(if (isSystemDefault) 0.5f else currentBrightness) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = "Brightness",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // System Default option
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onBrightnessChange(-1f) }
                    .padding(vertical = 12.dp, horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = isSystemDefault,
                    onClick = { onBrightnessChange(-1f) }
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "System Default",
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            // Custom brightness option
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .clickable {
                        if (isSystemDefault) {
                            onBrightnessChange(sliderValue)
                        }
                    }
                    .padding(vertical = 12.dp, horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = !isSystemDefault,
                    onClick = {
                        if (isSystemDefault) {
                            onBrightnessChange(sliderValue)
                        }
                    }
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Custom",
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            // Brightness slider
            if (!isSystemDefault) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Brightness6,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
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
                            .padding(horizontal = 8.dp)
                    )
                    Text(
                        text = "${(sliderValue * 100).toInt()}%",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
