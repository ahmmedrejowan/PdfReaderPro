package com.rejowan.pdfreaderpro.presentation.screens.reader.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
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
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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

// Accent colors matching app design
private val AccentPurple = Color(0xFF9575CD)
private val AccentBlue = Color(0xFF64B5F6)
private val SurfaceDark = Color(0xFF1C1C1E)
private val SurfaceLight = Color(0xFFF5F5F7)

@Composable
fun FloatingControlBar(
    currentPage: Int,
    totalPages: Int,
    currentZoom: Float,
    scrollDirection: ScrollDirection,
    isExpanded: Boolean,
    isBookmarked: Boolean,
    onExpandToggle: () -> Unit,
    onPageChange: (Int) -> Unit,
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
    val subtleColor = if (isDarkMode) Color.White.copy(alpha = 0.6f) else Color.Black.copy(alpha = 0.6f)

    var sliderPosition by remember(currentPage) { mutableFloatStateOf(currentPage.toFloat()) }

    val cornerRadius by animateDpAsState(
        targetValue = if (isExpanded) 24.dp else 28.dp,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "corner radius"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            modifier = Modifier
                .shadow(
                    elevation = 16.dp,
                    shape = RoundedCornerShape(cornerRadius),
                    ambientColor = AccentPurple.copy(alpha = 0.1f),
                    spotColor = AccentPurple.copy(alpha = 0.2f)
                ),
            shape = RoundedCornerShape(cornerRadius),
            color = surfaceColor,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
            ) {
                // Main control row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left: Zoom controls
                    ZoomControls(
                        currentZoom = currentZoom,
                        onZoomIn = onZoomIn,
                        onZoomOut = onZoomOut,
                        onResetZoom = onResetZoom,
                        contentColor = contentColor
                    )

                    // Center: Page indicator pill (clickable to expand)
                    PageIndicatorPill(
                        currentPage = currentPage + 1,
                        totalPages = totalPages,
                        isExpanded = isExpanded,
                        onClick = onExpandToggle,
                        accentColor = AccentPurple,
                        contentColor = contentColor
                    )

                    // Right: Action buttons
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Scroll direction toggle
                        ControlButton(
                            icon = if (scrollDirection == ScrollDirection.VERTICAL)
                                Icons.Rounded.SwapVert else Icons.Rounded.SwapHoriz,
                            onClick = onScrollDirectionToggle,
                            contentColor = AccentBlue,
                            contentDescription = "Toggle scroll direction"
                        )
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

                // Expandable slider section
                AnimatedVisibility(
                    visible = isExpanded,
                    enter = expandVertically(
                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                    ) + fadeIn(),
                    exit = shrinkVertically(
                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                    ) + fadeOut()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp, start = 8.dp, end = 8.dp)
                    ) {
                        // Page slider with gradient track
                        if (totalPages > 1) {
                            Slider(
                                value = sliderPosition,
                                onValueChange = { sliderPosition = it },
                                onValueChangeFinished = {
                                    onPageChange(sliderPosition.toInt())
                                },
                                valueRange = 0f..(totalPages - 1).toFloat(),
                                colors = SliderDefaults.colors(
                                    thumbColor = AccentPurple,
                                    activeTrackColor = AccentPurple,
                                    inactiveTrackColor = subtleColor.copy(alpha = 0.2f)
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        // Quick page navigation hints
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "1",
                                style = MaterialTheme.typography.labelSmall,
                                color = subtleColor
                            )
                            Text(
                                text = "$totalPages",
                                style = MaterialTheme.typography.labelSmall,
                                color = subtleColor
                            )
                        }
                    }
                }
            }
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
            size = 36
        )

        // Zoom percentage (clickable to reset)
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable(onClick = onResetZoom)
                .padding(horizontal = 6.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "${(currentZoom * 100).toInt()}%",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Medium,
                    fontSize = 11.sp
                ),
                color = contentColor.copy(alpha = 0.8f)
            )
        }

        // Zoom in
        ControlButton(
            icon = Icons.Rounded.Add,
            onClick = onZoomIn,
            contentColor = contentColor,
            contentDescription = "Zoom in",
            size = 36
        )
    }
}

@Composable
private fun PageIndicatorPill(
    currentPage: Int,
    totalPages: Int,
    isExpanded: Boolean,
    onClick: () -> Unit,
    accentColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier
) {
    val progress = if (totalPages > 0) currentPage.toFloat() / totalPages else 0f

    val backgroundColor by animateColorAsState(
        targetValue = if (isExpanded) accentColor.copy(alpha = 0.15f) else Color.Transparent,
        label = "pill background"
    )

    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = backgroundColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            // Progress arc indicator
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .padding(2.dp),
                contentAlignment = Alignment.Center
            ) {
                // Background circle
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.2f))
                )
                // Progress indicator
                CircularProgressIndicator(
                    progress = progress,
                    color = accentColor,
                    strokeWidth = 2.5f,
                    size = 18f
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Page numbers
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$currentPage",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    ),
                    color = contentColor
                )
                Text(
                    text = " / $totalPages",
                    style = MaterialTheme.typography.bodySmall,
                    color = contentColor.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
private fun CircularProgressIndicator(
    progress: Float,
    color: Color,
    strokeWidth: Float,
    size: Float,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "progress"
    )

    androidx.compose.foundation.Canvas(
        modifier = modifier.size(size.dp)
    ) {
        drawArc(
            color = color,
            startAngle = -90f,
            sweepAngle = 360f * animatedProgress,
            useCenter = false,
            style = androidx.compose.ui.graphics.drawscope.Stroke(
                width = strokeWidth.dp.toPx(),
                cap = androidx.compose.ui.graphics.StrokeCap.Round
            )
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
    size: Int = 40
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
            modifier = Modifier.size((size * 0.5).dp)
        )
    }
}
