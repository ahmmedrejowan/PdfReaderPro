package com.rejowan.pdfreaderpro.presentation.screens.reader.components

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
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.rounded.BrightnessHigh
import androidx.compose.material.icons.rounded.BrightnessLow
import androidx.compose.material.icons.rounded.BrightnessMedium
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rejowan.pdfreaderpro.presentation.screens.reader.ReadingTheme
import kotlinx.coroutines.delay

// Design colors
private val AccentAmber = Color(0xFFFFB74D)
private val AccentPurple = Color(0xFF9575CD)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DisplaySheet(
    brightness: Float,
    keepScreenOn: Boolean,
    currentTheme: ReadingTheme,
    onBrightnessChange: (Float) -> Unit,
    onKeepScreenOnChange: (Boolean) -> Unit,
    onThemeChange: (ReadingTheme) -> Unit,
    onDismiss: () -> Unit
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    if (isLandscape) {
        // Side sheet for landscape mode
        DisplaySideSheet(
            brightness = brightness,
            keepScreenOn = keepScreenOn,
            currentTheme = currentTheme,
            onBrightnessChange = onBrightnessChange,
            onKeepScreenOnChange = onKeepScreenOnChange,
            onThemeChange = onThemeChange,
            onDismiss = onDismiss
        )
    } else {
        // Bottom sheet for portrait mode
        DisplayBottomSheet(
            brightness = brightness,
            keepScreenOn = keepScreenOn,
            currentTheme = currentTheme,
            onBrightnessChange = onBrightnessChange,
            onKeepScreenOnChange = onKeepScreenOnChange,
            onThemeChange = onThemeChange,
            onDismiss = onDismiss
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DisplayBottomSheet(
    brightness: Float,
    keepScreenOn: Boolean,
    currentTheme: ReadingTheme,
    onBrightnessChange: (Float) -> Unit,
    onKeepScreenOnChange: (Boolean) -> Unit,
    onThemeChange: (ReadingTheme) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        DisplaySheetContent(
            brightness = brightness,
            keepScreenOn = keepScreenOn,
            currentTheme = currentTheme,
            onBrightnessChange = onBrightnessChange,
            onKeepScreenOnChange = onKeepScreenOnChange,
            onThemeChange = onThemeChange,
            showCloseButton = false,
            onDismiss = onDismiss,
            modifier = Modifier.padding(bottom = 32.dp)
        )
    }
}

@Composable
private fun DisplaySideSheet(
    brightness: Float,
    keepScreenOn: Boolean,
    currentTheme: ReadingTheme,
    onBrightnessChange: (Float) -> Unit,
    onKeepScreenOnChange: (Boolean) -> Unit,
    onThemeChange: (ReadingTheme) -> Unit,
    onDismiss: () -> Unit
) {
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isVisible = true
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Scrim (semi-transparent background)
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

        // Side sheet sliding from right
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
                shape = RoundedCornerShape(topStart = 24.dp, bottomStart = 24.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                shadowElevation = 8.dp
            ) {
                DisplaySheetContent(
                    brightness = brightness,
                    keepScreenOn = keepScreenOn,
                    currentTheme = currentTheme,
                    onBrightnessChange = onBrightnessChange,
                    onKeepScreenOnChange = onKeepScreenOnChange,
                    onThemeChange = onThemeChange,
                    showCloseButton = true,
                    onDismiss = onDismiss,
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
private fun DisplaySheetContent(
    brightness: Float,
    keepScreenOn: Boolean,
    currentTheme: ReadingTheme,
    onBrightnessChange: (Float) -> Unit,
    onKeepScreenOnChange: (Boolean) -> Unit,
    onThemeChange: (ReadingTheme) -> Unit,
    showCloseButton: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp)
    ) {
        // Header with optional close button for side sheet
        DisplaySheetHeader(
            showCloseButton = showCloseButton,
            onDismiss = onDismiss
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Reading Theme Section
        SectionLabel(text = "Reading Theme", delay = 0)

        Spacer(modifier = Modifier.height(12.dp))

        ThemeSelector(
            currentTheme = currentTheme,
            onThemeChange = onThemeChange,
            animationDelay = 50
        )

        Spacer(modifier = Modifier.height(20.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        Spacer(modifier = Modifier.height(20.dp))

        // Brightness Section
        SectionLabel(text = "Brightness", delay = 150)

        Spacer(modifier = Modifier.height(12.dp))

        BrightnessSlider(
            brightness = brightness,
            onBrightnessChange = onBrightnessChange,
            animationDelay = 200
        )

        Spacer(modifier = Modifier.height(20.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        Spacer(modifier = Modifier.height(20.dp))

        // Keep Screen On Toggle
        KeepScreenOnRow(
            keepScreenOn = keepScreenOn,
            onKeepScreenOnChange = onKeepScreenOnChange,
            animationDelay = 300
        )
    }
}

@Composable
private fun DisplaySheetHeader(
    showCloseButton: Boolean = false,
    onDismiss: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = AccentAmber.copy(alpha = 0.12f)
        ) {
            Icon(
                imageVector = Icons.Rounded.Palette,
                contentDescription = null,
                modifier = Modifier
                    .padding(10.dp)
                    .size(22.dp),
                tint = AccentAmber
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Display Settings",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Theme, brightness & screen options",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }

        if (showCloseButton) {
            IconButton(onClick = onDismiss) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = "Close",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
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
        text = text,
        style = MaterialTheme.typography.labelMedium.copy(
            fontWeight = FontWeight.SemiBold
        ),
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f * alpha),
        modifier = modifier.padding(start = 4.dp)
    )
}

@Composable
private fun ThemeSelector(
    currentTheme: ReadingTheme,
    onThemeChange: (ReadingTheme) -> Unit,
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
        label = "theme selector scale"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .scale(scale),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ThemeOption(
            theme = ReadingTheme.LIGHT,
            label = "Light",
            backgroundColor = Color(0xFFFAFAFA),
            textColor = Color(0xFF212121),
            isSelected = currentTheme == ReadingTheme.LIGHT,
            onClick = { onThemeChange(ReadingTheme.LIGHT) },
            modifier = Modifier.weight(1f)
        )

        ThemeOption(
            theme = ReadingTheme.DARK,
            label = "Dark",
            backgroundColor = Color(0xFF1E1E1E),
            textColor = Color(0xFFE0E0E0),
            isSelected = currentTheme == ReadingTheme.DARK,
            onClick = { onThemeChange(ReadingTheme.DARK) },
            modifier = Modifier.weight(1f)
        )

        ThemeOption(
            theme = ReadingTheme.SEPIA,
            label = "Sepia",
            backgroundColor = Color(0xFFF5E6D3),
            textColor = Color(0xFF5D4037),
            isSelected = currentTheme == ReadingTheme.SEPIA,
            onClick = { onThemeChange(ReadingTheme.SEPIA) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun ThemeOption(
    theme: ReadingTheme,
    label: String,
    backgroundColor: Color,
    textColor: Color,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) AccentAmber else MaterialTheme.colorScheme.outlineVariant,
        animationSpec = tween(200),
        label = "theme border"
    )

    val borderWidth by animateFloatAsState(
        targetValue = if (isSelected) 2.5f else 1f,
        animationSpec = tween(200),
        label = "theme border width"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = AccentAmber),
                onClick = onClick
            )
    ) {
        // Theme preview box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(70.dp)
                .clip(RoundedCornerShape(12.dp))
                .border(
                    width = borderWidth.dp,
                    color = borderColor,
                    shape = RoundedCornerShape(12.dp)
                )
                .background(backgroundColor),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Sample text lines to preview theme
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(textColor.copy(alpha = 0.8f))
                )
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .width(32.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(textColor.copy(alpha = 0.5f))
                )
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .width(36.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(textColor.copy(alpha = 0.6f))
                )
            }

            // Checkmark for selected theme
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(AccentAmber),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
            ),
            color = if (isSelected) AccentAmber else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun BrightnessSlider(
    brightness: Float,
    onBrightnessChange: (Float) -> Unit,
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
        label = "brightness scale"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .scale(scale),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Rounded.BrightnessLow,
                contentDescription = "Low brightness",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(22.dp)
            )

            Slider(
                value = brightness,
                onValueChange = onBrightnessChange,
                valueRange = 0f..1f,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp),
                colors = SliderDefaults.colors(
                    thumbColor = AccentAmber,
                    activeTrackColor = AccentAmber,
                    inactiveTrackColor = AccentAmber.copy(alpha = 0.24f)
                )
            )

            Icon(
                imageVector = Icons.Rounded.BrightnessHigh,
                contentDescription = "High brightness",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@Composable
private fun KeepScreenOnRow(
    keepScreenOn: Boolean,
    onKeepScreenOnChange: (Boolean) -> Unit,
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
        label = "keep screen scale"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .scale(scale),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = AccentPurple.copy(alpha = 0.12f),
                modifier = Modifier.size(40.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.BrightnessMedium,
                        contentDescription = null,
                        tint = AccentPurple,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Keep Screen On",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Prevent screen from turning off while reading",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }

            Switch(
                checked = keepScreenOn,
                onCheckedChange = onKeepScreenOnChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = AccentPurple,
                    checkedTrackColor = AccentPurple.copy(alpha = 0.5f)
                )
            )
        }
    }
}
