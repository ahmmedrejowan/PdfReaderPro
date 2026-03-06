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
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rejowan.pdfreaderpro.R
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
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
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
            modifier = Modifier.padding(bottom = 24.dp)
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
                shape = RoundedCornerShape(topStart = 20.dp, bottomStart = 20.dp),
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
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Header
        DisplaySheetHeader()

        Spacer(modifier = Modifier.height(12.dp))

        // Reading Theme Section
        SectionLabel(text = stringResource(R.string.reading_theme), delay = 0)

        Spacer(modifier = Modifier.height(6.dp))

        ThemeSelector(
            currentTheme = currentTheme,
            onThemeChange = onThemeChange,
            animationDelay = 50
        )

        Spacer(modifier = Modifier.height(12.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        Spacer(modifier = Modifier.height(12.dp))

        // Brightness Section
        SectionLabel(text = stringResource(R.string.brightness), delay = 150)

        Spacer(modifier = Modifier.height(12.dp))

        BrightnessSlider(
            brightness = brightness,
            onBrightnessChange = onBrightnessChange,
            animationDelay = 200
        )

        Spacer(modifier = Modifier.height(12.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        Spacer(modifier = Modifier.height(12.dp))

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
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = AccentAmber.copy(alpha = 0.12f)
        ) {
            Icon(
                imageVector = Icons.Rounded.Palette,
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
                text = stringResource(R.string.display_settings),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = stringResource(R.string.theme_brightness_options),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
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
        style = MaterialTheme.typography.labelSmall.copy(
            fontWeight = FontWeight.Medium
        ),
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f * alpha),
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

    Column(
        modifier = modifier
            .fillMaxWidth()
            .scale(scale),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // First row: Light and Dark
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ThemeOption(
                theme = ReadingTheme.LIGHT,
                label = stringResource(R.string.light_theme),
                backgroundColor = Color(0xFFFAFAFA),
                textColor = Color(0xFF212121),
                isSelected = currentTheme == ReadingTheme.LIGHT,
                onClick = { onThemeChange(ReadingTheme.LIGHT) },
                modifier = Modifier.weight(1f)
            )

            ThemeOption(
                theme = ReadingTheme.DARK,
                label = stringResource(R.string.dark_theme),
                backgroundColor = Color(0xFF1E1E1E),
                textColor = Color(0xFFE0E0E0),
                isSelected = currentTheme == ReadingTheme.DARK,
                onClick = { onThemeChange(ReadingTheme.DARK) },
                modifier = Modifier.weight(1f)
            )
        }

        // Second row: Sepia and Black (AMOLED)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ThemeOption(
                theme = ReadingTheme.SEPIA,
                label = stringResource(R.string.sepia_theme),
                backgroundColor = Color(0xFFF5E6D3),
                textColor = Color(0xFF5D4037),
                isSelected = currentTheme == ReadingTheme.SEPIA,
                onClick = { onThemeChange(ReadingTheme.SEPIA) },
                modifier = Modifier.weight(1f)
            )

            ThemeOption(
                theme = ReadingTheme.BLACK,
                label = stringResource(R.string.black_theme),
                backgroundColor = Color(0xFF000000),
                textColor = Color(0xFFB0B0B0),
                isSelected = currentTheme == ReadingTheme.BLACK,
                onClick = { onThemeChange(ReadingTheme.BLACK) },
                modifier = Modifier.weight(1f)
            )
        }
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
            .clip(RoundedCornerShape(8.dp))
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
                .height(52.dp)
                .clip(RoundedCornerShape(8.dp))
                .border(
                    width = borderWidth.dp,
                    color = borderColor,
                    shape = RoundedCornerShape(8.dp)
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
                        .width(32.dp)
                        .height(3.dp)
                        .clip(RoundedCornerShape(1.5.dp))
                        .background(textColor.copy(alpha = 0.8f))
                )
                Spacer(modifier = Modifier.height(3.dp))
                Box(
                    modifier = Modifier
                        .width(24.dp)
                        .height(3.dp)
                        .clip(RoundedCornerShape(1.5.dp))
                        .background(textColor.copy(alpha = 0.5f))
                )
                Spacer(modifier = Modifier.height(3.dp))
                Box(
                    modifier = Modifier
                        .width(28.dp)
                        .height(3.dp)
                        .clip(RoundedCornerShape(1.5.dp))
                        .background(textColor.copy(alpha = 0.6f))
                )
            }

            // Checkmark for selected theme
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(AccentAmber),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(10.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
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
                contentDescription = stringResource(R.string.low_brightness),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )

            Slider(
                value = brightness,
                onValueChange = onBrightnessChange,
                valueRange = 0f..1f,
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
                contentDescription = stringResource(R.string.high_brightness),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
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
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = AccentPurple.copy(alpha = 0.12f),
                modifier = Modifier.size(32.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.BrightnessMedium,
                        contentDescription = null,
                        tint = AccentPurple,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.keep_screen_on),
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = stringResource(R.string.prevent_screen_off),
                    style = MaterialTheme.typography.labelSmall,
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
