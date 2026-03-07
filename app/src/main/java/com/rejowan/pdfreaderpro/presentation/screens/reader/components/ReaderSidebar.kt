package com.rejowan.pdfreaderpro.presentation.screens.reader.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brightness6
import androidx.compose.material.icons.filled.ScreenLockRotation
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.rejowan.pdfreaderpro.R
import kotlin.math.roundToInt

private val AccentPurple = Color(0xFF9575CD)
private val AccentBlue = Color(0xFF64B5F6)
private val SurfaceDark = Color(0xFF1C1C1E)
private val SurfaceLight = Color(0xFFF8F8FA)

@Composable
fun ReaderSidebar(
    isOpen: Boolean,
    currentPage: Int,
    totalPages: Int,
    brightness: Float,
    keepScreenOn: Boolean,
    isRotationLocked: Boolean,
    onBrightnessChange: (Float) -> Unit,
    onKeepScreenOnChange: (Boolean) -> Unit,
    onRotationLockChange: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    isDarkMode: Boolean = isSystemInDarkTheme()
) {
    val density = LocalDensity.current
    val sidebarWidth = 280.dp
    val sidebarWidthPx = with(density) { sidebarWidth.toPx() }

    // Drag state
    var dragOffset by remember { mutableFloatStateOf(0f) }
    val openProgress = if (isOpen) {
        1f - (dragOffset / sidebarWidthPx).coerceIn(0f, 1f)
    } else {
        0f
    }

    // Animate the offset
    val animatedOffset by animateDpAsState(
        targetValue = if (isOpen) 0.dp else sidebarWidth,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "sidebar offset"
    )

    // Scrim alpha
    val scrimAlpha by animateFloatAsState(
        targetValue = if (isOpen) 0.5f else 0f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "scrim alpha"
    )

    if (isOpen || scrimAlpha > 0f) {
        Box(modifier = modifier.fillMaxSize()) {
            // Scrim
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = scrimAlpha))
                    .pointerInput(Unit) {
                        detectHorizontalDragGestures(
                            onDragEnd = {
                                if (dragOffset > sidebarWidthPx * 0.3f) {
                                    onDismiss()
                                }
                                dragOffset = 0f
                            },
                            onHorizontalDrag = { _, dragAmount ->
                                if (dragAmount > 0) {
                                    dragOffset = (dragOffset + dragAmount).coerceAtLeast(0f)
                                }
                            }
                        )
                    }
                    .let { mod ->
                        if (scrimAlpha > 0.01f) {
                            mod.pointerInput(Unit) {
                                awaitPointerEventScope {
                                    while (true) {
                                        val event = awaitPointerEvent()
                                        if (event.changes.any { it.pressed }) {
                                            onDismiss()
                                        }
                                    }
                                }
                            }
                        } else mod
                    }
            )

            // Sidebar
            SidebarContent(
                currentPage = currentPage,
                totalPages = totalPages,
                brightness = brightness,
                keepScreenOn = keepScreenOn,
                isRotationLocked = isRotationLocked,
                onBrightnessChange = onBrightnessChange,
                onKeepScreenOnChange = onKeepScreenOnChange,
                onRotationLockChange = onRotationLockChange,
                onDrag = { dragAmount ->
                    if (dragAmount > 0) {
                        dragOffset = (dragOffset + dragAmount).coerceAtLeast(0f)
                    }
                },
                onDragEnd = {
                    if (dragOffset > sidebarWidthPx * 0.3f) {
                        onDismiss()
                    }
                    dragOffset = 0f
                },
                isDarkMode = isDarkMode,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .width(sidebarWidth)
                    .fillMaxHeight()
                    .offset { IntOffset((animatedOffset.toPx() + dragOffset).roundToInt(), 0) }
            )
        }
    }
}

@Composable
private fun SidebarContent(
    currentPage: Int,
    totalPages: Int,
    brightness: Float,
    keepScreenOn: Boolean,
    isRotationLocked: Boolean,
    onBrightnessChange: (Float) -> Unit,
    onKeepScreenOnChange: (Boolean) -> Unit,
    onRotationLockChange: () -> Unit,
    onDrag: (Float) -> Unit,
    onDragEnd: () -> Unit,
    isDarkMode: Boolean,
    modifier: Modifier = Modifier
) {
    val surfaceColor = if (isDarkMode) SurfaceDark else SurfaceLight
    val contentColor = if (isDarkMode) Color.White else Color.Black
    val subtleColor = if (isDarkMode) Color.White.copy(alpha = 0.6f) else Color.Black.copy(alpha = 0.6f)

    val progress = if (totalPages > 0) currentPage.toFloat() / totalPages else 0f

    Surface(
        modifier = modifier,
        color = surfaceColor,
        shape = RoundedCornerShape(topStart = 24.dp, bottomStart = 24.dp),
        shadowElevation = 16.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 16.dp)
        ) {
            // Drag handle
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .pointerInput(Unit) {
                        detectHorizontalDragGestures(
                            onDragEnd = { onDragEnd() },
                            onHorizontalDrag = { _, dragAmount -> onDrag(dragAmount) }
                        )
                    }
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Drag indicator
                    Box(
                        modifier = Modifier
                            .width(4.dp)
                            .height(40.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(AccentPurple.copy(alpha = 0.5f))
                    )

                    Column {
                        Text(
                            text = stringResource(R.string.reading),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = contentColor
                        )
                        Text(
                            text = stringResource(R.string.settings_label),
                            style = MaterialTheme.typography.bodySmall,
                            color = subtleColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Progress section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.progress),
                        style = MaterialTheme.typography.labelMedium,
                        color = subtleColor
                    )
                    Text(
                        text = "${(progress * 100).toInt()}%",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = AccentPurple
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = AccentPurple,
                    trackColor = AccentPurple.copy(alpha = 0.15f),
                    strokeCap = StrokeCap.Round
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(R.string.page_current, currentPage),
                        style = MaterialTheme.typography.labelSmall,
                        color = subtleColor
                    )
                    Text(
                        text = stringResource(R.string.of_total, totalPages),
                        style = MaterialTheme.typography.labelSmall,
                        color = subtleColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 20.dp),
                color = subtleColor.copy(alpha = 0.2f)
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Brightness control
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = AccentBlue.copy(alpha = 0.15f),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.WbSunny,
                                contentDescription = stringResource(R.string.cd_decorative),
                                modifier = Modifier.size(18.dp),
                                tint = AccentBlue
                            )
                        }
                    }
                    Text(
                        text = stringResource(R.string.brightness),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        color = contentColor
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.LightMode,
                        contentDescription = stringResource(R.string.cd_decorative),
                        modifier = Modifier.size(16.dp),
                        tint = subtleColor
                    )
                    Slider(
                        value = brightness,
                        onValueChange = onBrightnessChange,
                        valueRange = 0.1f..1f,
                        modifier = Modifier.weight(1f),
                        colors = SliderDefaults.colors(
                            thumbColor = AccentBlue,
                            activeTrackColor = AccentBlue,
                            inactiveTrackColor = AccentBlue.copy(alpha = 0.2f)
                        )
                    )
                    Icon(
                        imageVector = Icons.Filled.Brightness6,
                        contentDescription = stringResource(R.string.cd_decorative),
                        modifier = Modifier.size(18.dp),
                        tint = subtleColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Keep screen on toggle
            SettingToggleItem(
                title = stringResource(R.string.keep_screen_on),
                subtitle = stringResource(R.string.prevent_screen_timeout),
                icon = Icons.Outlined.WbSunny,
                isChecked = keepScreenOn,
                onCheckedChange = onKeepScreenOnChange,
                accentColor = AccentPurple,
                contentColor = contentColor,
                subtleColor = subtleColor
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Rotation lock toggle
            SettingToggleItem(
                title = stringResource(R.string.lock_rotation),
                subtitle = if (isRotationLocked) stringResource(R.string.rotation_locked) else stringResource(R.string.auto_rotate),
                icon = if (isRotationLocked) Icons.Filled.ScreenLockRotation else Icons.Filled.ScreenRotation,
                isChecked = isRotationLocked,
                onCheckedChange = { onRotationLockChange() },
                accentColor = AccentPurple,
                contentColor = contentColor,
                subtleColor = subtleColor
            )

            Spacer(modifier = Modifier.weight(1f))

            // Swipe hint
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.swipe_to_close),
                    style = MaterialTheme.typography.labelSmall,
                    color = subtleColor.copy(alpha = 0.5f)
                )
            }
        }
    }
}

@Composable
private fun SettingToggleItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    accentColor: Color,
    contentColor: Color,
    subtleColor: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = CircleShape,
            color = accentColor.copy(alpha = 0.15f),
            modifier = Modifier.size(36.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = stringResource(R.string.cd_decorative),
                    modifier = Modifier.size(18.dp),
                    tint = accentColor
                )
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = contentColor
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = subtleColor
            )
        }
        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = accentColor,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = subtleColor.copy(alpha = 0.3f)
            )
        )
    }
}
