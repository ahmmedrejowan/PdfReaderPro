package com.rejowan.pdfreaderpro.presentation.screens.reader.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brightness6
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.ScreenLockRotation
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.rejowan.pdfreaderpro.data.pdf.ColorMode
import com.rejowan.pdfreaderpro.presentation.screens.reader.ScrollDirection

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsPanel(
    colorMode: ColorMode,
    brightness: Float,
    scrollDirection: ScrollDirection,
    keepScreenOn: Boolean,
    isRotationLocked: Boolean,
    onColorModeChange: (ColorMode) -> Unit,
    onBrightnessChange: (Float) -> Unit,
    onScrollDirectionChange: (ScrollDirection) -> Unit,
    onKeepScreenOnChange: (Boolean) -> Unit,
    onRotationLockChange: () -> Unit,
    onFullScreenClick: () -> Unit,
    onInfoClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Reading Settings",
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Color mode section
            Text(
                text = "Theme",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ColorModeButton(
                    mode = ColorMode.NORMAL,
                    label = "Light",
                    backgroundColor = Color.White,
                    textColor = Color.Black,
                    isSelected = colorMode == ColorMode.NORMAL,
                    onClick = { onColorModeChange(ColorMode.NORMAL) }
                )
                ColorModeButton(
                    mode = ColorMode.SEPIA,
                    label = "Sepia",
                    backgroundColor = Color(0xFFF4ECD8),
                    textColor = Color(0xFF5B4636),
                    isSelected = colorMode == ColorMode.SEPIA,
                    onClick = { onColorModeChange(ColorMode.SEPIA) }
                )
                ColorModeButton(
                    mode = ColorMode.DARK,
                    label = "Dark",
                    backgroundColor = Color(0xFF2D2D2D),
                    textColor = Color.White,
                    isSelected = colorMode == ColorMode.DARK,
                    onClick = { onColorModeChange(ColorMode.DARK) }
                )
                ColorModeButton(
                    mode = ColorMode.INVERTED,
                    label = "Black",
                    backgroundColor = Color.Black,
                    textColor = Color.White,
                    isSelected = colorMode == ColorMode.INVERTED,
                    onClick = { onColorModeChange(ColorMode.INVERTED) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))

            // Brightness section
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Brightness6,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Brightness",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.width(16.dp))
                Slider(
                    value = brightness,
                    onValueChange = onBrightnessChange,
                    valueRange = 0.1f..1f,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))

            // Scroll direction
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        onScrollDirectionChange(
                            if (scrollDirection == ScrollDirection.VERTICAL) {
                                ScrollDirection.HORIZONTAL
                            } else {
                                ScrollDirection.VERTICAL
                            }
                        )
                    }
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.SwapVert,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Scroll Direction",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = if (scrollDirection == ScrollDirection.VERTICAL) "Vertical" else "Horizontal",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Keep screen on
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Keep Screen On",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Prevent screen from turning off",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = keepScreenOn,
                    onCheckedChange = onKeepScreenOnChange
                )
            }

            // Rotation lock
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (isRotationLocked) {
                        Icons.Default.ScreenLockRotation
                    } else {
                        Icons.Default.ScreenRotation
                    },
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Lock Rotation",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = if (isRotationLocked) "Rotation locked" else "Auto-rotate enabled",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = isRotationLocked,
                    onCheckedChange = { onRotationLockChange() }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(8.dp))

            // Full screen button
            TextButton(
                onClick = {
                    onFullScreenClick()
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Fullscreen,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Full Screen Mode")
            }

            // PDF info button
            TextButton(
                onClick = onInfoClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("PDF Information")
            }

            // Delete button
            TextButton(
                onClick = onDeleteClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Delete PDF",
                    color = MaterialTheme.colorScheme.error
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ColorModeButton(
    mode: ColorMode,
    label: String,
    backgroundColor: Color,
    textColor: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(backgroundColor)
                .then(
                    if (isSelected) {
                        Modifier.border(
                            width = 2.dp,
                            color = MaterialTheme.colorScheme.primary,
                            shape = CircleShape
                        )
                    } else {
                        Modifier.border(
                            width = 1.dp,
                            color = Color.Gray,
                            shape = CircleShape
                        )
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "A",
                style = MaterialTheme.typography.titleMedium,
                color = textColor
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = if (isSelected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }
        )
    }
}
