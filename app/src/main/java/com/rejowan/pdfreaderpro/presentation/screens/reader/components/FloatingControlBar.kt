package com.rejowan.pdfreaderpro.presentation.screens.reader.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.BookmarkBorder
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.ViewDay
import androidx.compose.material.icons.rounded.ZoomIn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

// Design system colors
private val AccentPurple = Color(0xFF9575CD)
private val AccentBlue = Color(0xFF64B5F6)
private val AccentTeal = Color(0xFF4DB6AC)
private val AccentAmber = Color(0xFFFFB74D)

@Composable
fun FloatingControlBar(
    isBookmarked: Boolean,
    onTocClick: () -> Unit,
    onViewClick: () -> Unit,
    onZoomClick: () -> Unit,
    onDisplayClick: () -> Unit,
    onBookmarkClick: () -> Unit,
    onMoreClick: () -> Unit,
    modifier: Modifier = Modifier,
    isDarkMode: Boolean = true
) {
    val surfaceColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.95f)
    val barContentColor = MaterialTheme.colorScheme.onSurface

    Surface(
        modifier = modifier
            .shadow(
                elevation = 20.dp,
                shape = RoundedCornerShape(32.dp),
                ambientColor = AccentPurple.copy(alpha = 0.15f),
                spotColor = AccentPurple.copy(alpha = 0.25f)
            ),
        shape = RoundedCornerShape(32.dp),
        color = surfaceColor,
        tonalElevation = 8.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Table of Contents
            ControlBarButton(
                icon = Icons.AutoMirrored.Rounded.MenuBook,
                contentDescription = "Table of Contents",
                onClick = onTocClick,
                tint = AccentPurple,
                isDarkMode = isDarkMode
            )

            // View Mode
            ControlBarButton(
                icon = Icons.Rounded.ViewDay,
                contentDescription = "View Options",
                onClick = onViewClick,
                tint = AccentBlue,
                isDarkMode = isDarkMode
            )

            // Zoom
            ControlBarButton(
                icon = Icons.Rounded.ZoomIn,
                contentDescription = "Zoom",
                onClick = onZoomClick,
                tint = AccentTeal,
                isDarkMode = isDarkMode
            )

            // Display Settings (Theme, Brightness)
            ControlBarButton(
                icon = Icons.Rounded.Palette,
                contentDescription = "Display Settings",
                onClick = onDisplayClick,
                tint = AccentAmber,
                isDarkMode = isDarkMode
            )

            // Bookmark
            BookmarkButton(
                isBookmarked = isBookmarked,
                onClick = onBookmarkClick,
                isDarkMode = isDarkMode
            )

            // More Options
            ControlBarButton(
                icon = Icons.Rounded.MoreHoriz,
                contentDescription = "More Options",
                onClick = onMoreClick,
                tint = barContentColor.copy(alpha = 0.7f),
                isDarkMode = isDarkMode
            )
        }
    }
}

@Composable
private fun ControlBarButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    tint: Color,
    isDarkMode: Boolean,
    modifier: Modifier = Modifier
) {
    val backgroundColor = tint.copy(alpha = 0.12f)

    Box(
        modifier = modifier
            .size(46.dp)
            .clip(CircleShape)
            .background(backgroundColor)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = tint, bounded = true),
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(22.dp)
        )
    }
}

@Composable
private fun BookmarkButton(
    isBookmarked: Boolean,
    onClick: () -> Unit,
    isDarkMode: Boolean,
    modifier: Modifier = Modifier
) {
    val accentRed = Color(0xFFEF5350)
    val tint = if (isBookmarked) accentRed else if (isDarkMode) Color.White.copy(alpha = 0.6f) else Color.Black.copy(alpha = 0.5f)
    val backgroundColor = if (isBookmarked) accentRed.copy(alpha = 0.12f) else Color.Transparent

    Box(
        modifier = modifier
            .size(46.dp)
            .clip(CircleShape)
            .background(backgroundColor)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = accentRed, bounded = true),
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = if (isBookmarked) Icons.Rounded.Bookmark else Icons.Rounded.BookmarkBorder,
            contentDescription = if (isBookmarked) "Remove Bookmark" else "Add Bookmark",
            tint = tint,
            modifier = Modifier.size(22.dp)
        )
    }
}
