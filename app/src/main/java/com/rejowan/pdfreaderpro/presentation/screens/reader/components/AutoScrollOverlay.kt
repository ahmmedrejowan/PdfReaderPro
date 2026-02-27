package com.rejowan.pdfreaderpro.presentation.screens.reader.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun AutoScrollOverlay(
    isVisible: Boolean,
    isPaused: Boolean,
    currentSpeed: Float,
    onTogglePause: () -> Unit,
    onStop: () -> Unit,
    onSpeedChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val surfaceColor = MaterialTheme.colorScheme.surfaceContainerHigh
    val iconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)

    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(
            initialOffsetY = { it },
            animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
        ) + fadeIn(),
        exit = slideOutVertically(
            targetOffsetY = { it },
            animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
        ) + fadeOut(),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(14.dp))
                .background(surfaceColor)
        ) {
            Row(
                modifier = Modifier.padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Decrease speed
                OverlayIconButton(
                    icon = Icons.Rounded.Remove,
                    contentDescription = "Slower",
                    iconColor = iconColor,
                    onClick = {
                        val newSpeed = (currentSpeed - 10f).coerceAtLeast(10f)
                        onSpeedChange(newSpeed)
                    }
                )

                // Play/Pause button
                OverlayIconButton(
                    icon = if (isPaused) Icons.Rounded.PlayArrow else Icons.Rounded.Pause,
                    contentDescription = if (isPaused) "Resume" else "Pause",
                    iconColor = iconColor,
                    onClick = onTogglePause
                )

                // Increase speed
                OverlayIconButton(
                    icon = Icons.Rounded.Add,
                    contentDescription = "Faster",
                    iconColor = iconColor,
                    onClick = {
                        val newSpeed = (currentSpeed + 10f).coerceAtMost(200f)
                        onSpeedChange(newSpeed)
                    }
                )

                // Stop button
                OverlayIconButton(
                    icon = Icons.Rounded.Close,
                    contentDescription = "Stop",
                    iconColor = iconColor,
                    onClick = onStop
                )
            }
        }
    }
}

@Composable
private fun OverlayIconButton(
    icon: ImageVector,
    contentDescription: String,
    iconColor: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(RoundedCornerShape(10.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true),
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier.size(18.dp),
            tint = iconColor
        )
    }
}
