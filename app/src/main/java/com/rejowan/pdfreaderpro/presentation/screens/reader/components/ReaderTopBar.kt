package com.rejowan.pdfreaderpro.presentation.screens.reader.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Fullscreen
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Print
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

private val AccentPurple = Color(0xFF9575CD)
private val SurfaceDark = Color(0xFF1C1C1E)
private val SurfaceLight = Color(0xFFF5F5F7)

@Composable
fun ReaderTopBar(
    title: String,
    isVisible: Boolean,
    onBackClick: () -> Unit,
    onSearchClick: () -> Unit,
    onShareClick: () -> Unit,
    onPrintClick: () -> Unit,
    onInfoClick: () -> Unit,
    onFullScreenClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier,
    isDarkMode: Boolean = true
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(
            initialOffsetY = { -it },
            animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
        ),
        exit = slideOutVertically(
            targetOffsetY = { -it },
            animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
        ),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = if (isDarkMode) {
                            listOf(
                                Color.Black.copy(alpha = 0.7f),
                                Color.Black.copy(alpha = 0.5f),
                                Color.Transparent
                            )
                        } else {
                            listOf(
                                Color.White.copy(alpha = 0.9f),
                                Color.White.copy(alpha = 0.7f),
                                Color.Transparent
                            )
                        }
                    )
                )
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(horizontal = 8.dp, vertical = 8.dp)
        ) {
            TopBarContent(
                title = title,
                onBackClick = onBackClick,
                onSearchClick = onSearchClick,
                onShareClick = onShareClick,
                onPrintClick = onPrintClick,
                onInfoClick = onInfoClick,
                onFullScreenClick = onFullScreenClick,
                onDeleteClick = onDeleteClick,
                isDarkMode = isDarkMode
            )
        }
    }
}

@Composable
private fun TopBarContent(
    title: String,
    onBackClick: () -> Unit,
    onSearchClick: () -> Unit,
    onShareClick: () -> Unit,
    onPrintClick: () -> Unit,
    onInfoClick: () -> Unit,
    onFullScreenClick: () -> Unit,
    onDeleteClick: () -> Unit,
    isDarkMode: Boolean
) {
    val surfaceColor = if (isDarkMode) SurfaceDark.copy(alpha = 0.95f) else SurfaceLight.copy(alpha = 0.95f)
    val contentColor = if (isDarkMode) Color.White else Color.Black

    var showMenu by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = AccentPurple.copy(alpha = 0.1f),
                spotColor = AccentPurple.copy(alpha = 0.15f)
            ),
        shape = RoundedCornerShape(16.dp),
        color = surfaceColor,
        tonalElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Back button
            TopBarIconButton(
                icon = Icons.AutoMirrored.Rounded.ArrowBack,
                contentDescription = "Back",
                onClick = onBackClick,
                contentColor = contentColor
            )

            // Title
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = contentColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp)
            )

            // Search button
            TopBarIconButton(
                icon = Icons.Rounded.Search,
                contentDescription = "Search",
                onClick = onSearchClick,
                contentColor = contentColor
            )

            // More button with dropdown
            Box {
                TopBarIconButton(
                    icon = Icons.Rounded.MoreVert,
                    contentDescription = "More options",
                    onClick = { showMenu = true },
                    contentColor = contentColor
                )

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Share") },
                        onClick = {
                            showMenu = false
                            onShareClick()
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Outlined.Share,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Print") },
                        onClick = {
                            showMenu = false
                            onPrintClick()
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Outlined.Print,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Full Screen") },
                        onClick = {
                            showMenu = false
                            onFullScreenClick()
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Outlined.Fullscreen,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("PDF Info") },
                        onClick = {
                            showMenu = false
                            onInfoClick()
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Outlined.Info,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    )
                    DropdownMenuItem(
                        text = {
                            Text(
                                "Delete",
                                color = MaterialTheme.colorScheme.error
                            )
                        },
                        onClick = {
                            showMenu = false
                            onDeleteClick()
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Outlined.Delete,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun TopBarIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    contentColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(44.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = contentColor,
            modifier = Modifier.size(22.dp)
        )
    }
}
