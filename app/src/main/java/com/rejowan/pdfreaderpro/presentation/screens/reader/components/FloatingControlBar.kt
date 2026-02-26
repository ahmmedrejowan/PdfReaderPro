package com.rejowan.pdfreaderpro.presentation.screens.reader.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Toc
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.BookmarkBorder
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material.icons.rounded.SwapVert
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rejowan.pdfreaderpro.presentation.screens.reader.ScrollDirection

private val AccentPurple = Color(0xFF9575CD)
private val AccentBlue = Color(0xFF64B5F6)
private val SurfaceDark = Color(0xFF1C1C1E)
private val SurfaceLight = Color(0xFFF5F5F7)

@Composable
fun FloatingControlBar(
    currentZoom: Float,
    scrollDirection: ScrollDirection,
    isBookmarked: Boolean,
    onZoomIn: () -> Unit,
    onZoomOut: () -> Unit,
    onResetZoom: () -> Unit,
    onScrollDirectionToggle: () -> Unit,
    onTocClick: () -> Unit,
    onBookmarkClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier,
    isDarkMode: Boolean = true
) {
    val surfaceColor = if (isDarkMode) SurfaceDark.copy(alpha = 0.95f) else SurfaceLight.copy(alpha = 0.95f)
    val contentColor = if (isDarkMode) Color.White else Color.Black
    val dividerColor = if (isDarkMode) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.1f)

    Surface(
        modifier = modifier
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(28.dp),
                ambientColor = AccentPurple.copy(alpha = 0.1f),
                spotColor = AccentPurple.copy(alpha = 0.2f)
            ),
        shape = RoundedCornerShape(28.dp),
        color = surfaceColor,
        tonalElevation = 8.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Zoom controls
            ZoomControls(
                currentZoom = currentZoom,
                onZoomIn = onZoomIn,
                onZoomOut = onZoomOut,
                onResetZoom = onResetZoom,
                contentColor = contentColor
            )

            // Divider
            VerticalDivider(
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .size(width = 1.dp, height = 24.dp),
                color = dividerColor
            )

            // Scroll direction toggle
            ControlButton(
                icon = if (scrollDirection == ScrollDirection.VERTICAL)
                    Icons.Rounded.SwapVert else Icons.Rounded.SwapHoriz,
                onClick = onScrollDirectionToggle,
                contentColor = AccentBlue,
                contentDescription = "Toggle scroll direction"
            )

            // Divider
            VerticalDivider(
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .size(width = 1.dp, height = 24.dp),
                color = dividerColor
            )

            // Action buttons
            ControlButton(
                icon = Icons.AutoMirrored.Rounded.Toc,
                onClick = onTocClick,
                contentColor = contentColor,
                contentDescription = "Table of Contents"
            )
            ControlButton(
                icon = if (isBookmarked) Icons.Rounded.Bookmark else Icons.Rounded.BookmarkBorder,
                onClick = onBookmarkClick,
                contentColor = if (isBookmarked) AccentPurple else contentColor,
                contentDescription = "Bookmark"
            )
            ControlButton(
                icon = Icons.Rounded.Settings,
                onClick = onSettingsClick,
                contentColor = contentColor,
                contentDescription = "Settings"
            )
        }
    }
}

@Composable
private fun ZoomControls(
    currentZoom: Float,
    onZoomIn: () -> Unit,
    onZoomOut: () -> Unit,
    onResetZoom: () -> Unit,
    contentColor: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        // Zoom out
        ControlButton(
            icon = Icons.Rounded.Remove,
            onClick = onZoomOut,
            contentColor = contentColor,
            contentDescription = "Zoom out",
            size = 38
        )

        // Zoom percentage (clickable to reset)
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable(onClick = onResetZoom)
                .padding(horizontal = 8.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "${(currentZoom * 100).toInt()}%",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp
                ),
                color = contentColor
            )
        }

        // Zoom in
        ControlButton(
            icon = Icons.Rounded.Add,
            onClick = onZoomIn,
            contentColor = contentColor,
            contentDescription = "Zoom in",
            size = 38
        )
    }
}

@Composable
private fun ControlButton(
    icon: ImageVector,
    onClick: () -> Unit,
    contentColor: Color,
    contentDescription: String,
    modifier: Modifier = Modifier,
    size: Int = 42
) {
    Box(
        modifier = modifier
            .size(size.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = contentColor,
            modifier = Modifier.size((size * 0.52).dp)
        )
    }
}
