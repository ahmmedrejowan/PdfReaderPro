package com.rejowan.pdfreaderpro.presentation.screens.reader.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.FitScreen
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material.icons.rounded.SwapVert
import androidx.compose.material.icons.rounded.ViewDay
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rejowan.pdfreaderpro.R
import com.rejowan.pdfreaderpro.presentation.screens.reader.ScrollMode

private val AccentPurple = Color(0xFF9575CD)
private val AccentBlue = Color(0xFF64B5F6)
private val SurfaceDark = Color(0xFF1C1C1E)

@Composable
fun ZoomControlPill(
    currentZoom: Float,
    onZoomIn: () -> Unit,
    onZoomOut: () -> Unit,
    onResetZoom: () -> Unit,
    modifier: Modifier = Modifier,
    isDarkMode: Boolean = true
) {
    val surfaceColor = if (isDarkMode) SurfaceDark.copy(alpha = 0.95f) else Color.White.copy(alpha = 0.95f)
    val contentColor = if (isDarkMode) Color.White else Color.Black

    Surface(
        modifier = modifier
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = AccentPurple.copy(alpha = 0.1f),
                spotColor = AccentPurple.copy(alpha = 0.15f)
            ),
        shape = RoundedCornerShape(20.dp),
        color = surfaceColor,
        tonalElevation = 4.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            // Zoom out
            QuickActionButton(
                icon = Icons.Rounded.Remove,
                onClick = onZoomOut,
                contentColor = contentColor
            )

            // Zoom percentage
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(onClick = onResetZoom)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${(currentZoom * 100).toInt()}%",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    ),
                    color = contentColor
                )
            }

            // Zoom in
            QuickActionButton(
                icon = Icons.Rounded.Add,
                onClick = onZoomIn,
                contentColor = contentColor
            )
        }
    }
}

@Composable
fun ScrollDirectionToggle(
    currentScrollMode: ScrollMode,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
    isDarkMode: Boolean = true
) {
    val surfaceColor = if (isDarkMode) SurfaceDark.copy(alpha = 0.95f) else Color.White.copy(alpha = 0.95f)
    val contentColor = if (isDarkMode) Color.White else Color.Black

    val icon = when (currentScrollMode) {
        ScrollMode.VERTICAL -> Icons.Rounded.SwapVert
        ScrollMode.HORIZONTAL -> Icons.Rounded.SwapHoriz
    }

    Surface(
        modifier = modifier
            .shadow(
                elevation = 12.dp,
                shape = CircleShape,
                ambientColor = AccentBlue.copy(alpha = 0.1f),
                spotColor = AccentBlue.copy(alpha = 0.15f)
            )
            .clip(CircleShape)
            .clickable(onClick = onToggle),
        shape = CircleShape,
        color = surfaceColor,
        tonalElevation = 4.dp
    ) {
        Box(
            modifier = Modifier.padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = "Toggle scroll direction",
                tint = AccentBlue,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@Composable
fun FitScreenButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isDarkMode: Boolean = true
) {
    val surfaceColor = if (isDarkMode) SurfaceDark.copy(alpha = 0.95f) else Color.White.copy(alpha = 0.95f)

    Surface(
        modifier = modifier
            .shadow(
                elevation = 12.dp,
                shape = CircleShape,
                ambientColor = AccentPurple.copy(alpha = 0.1f),
                spotColor = AccentPurple.copy(alpha = 0.15f)
            )
            .clip(CircleShape)
            .clickable(onClick = onClick),
        shape = CircleShape,
        color = surfaceColor,
        tonalElevation = 4.dp
    ) {
        Box(
            modifier = Modifier.padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.FitScreen,
                contentDescription = "Fit to screen",
                tint = AccentPurple,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@Composable
fun QuickActionsColumn(
    isVisible: Boolean,
    currentZoom: Float,
    scrollMode: ScrollMode,
    onZoomIn: () -> Unit,
    onZoomOut: () -> Unit,
    onResetZoom: () -> Unit,
    onScrollModeToggle: () -> Unit,
    modifier: Modifier = Modifier,
    isDarkMode: Boolean = true
) {
    Column(
        modifier = modifier.padding(end = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.End
    ) {
        // Zoom controls
        AnimatedVisibility(
            visible = isVisible,
            enter = slideInHorizontally(
                initialOffsetX = { it },
                animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
            ) + fadeIn(),
            exit = slideOutHorizontally(
                targetOffsetX = { it },
                animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
            ) + fadeOut()
        ) {
            ZoomControlPill(
                currentZoom = currentZoom,
                onZoomIn = onZoomIn,
                onZoomOut = onZoomOut,
                onResetZoom = onResetZoom,
                isDarkMode = isDarkMode
            )
        }

        // Scroll direction toggle
        AnimatedVisibility(
            visible = isVisible,
            enter = scaleIn(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            ) + fadeIn(),
            exit = scaleOut() + fadeOut()
        ) {
            ScrollDirectionToggle(
                currentScrollMode = scrollMode,
                onToggle = onScrollModeToggle,
                isDarkMode = isDarkMode
            )
        }
    }
}

@Composable
private fun QuickActionButton(
    icon: ImageVector,
    onClick: () -> Unit,
    contentColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(36.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = stringResource(R.string.cd_decorative),
            tint = contentColor,
            modifier = Modifier.size(20.dp)
        )
    }
}
