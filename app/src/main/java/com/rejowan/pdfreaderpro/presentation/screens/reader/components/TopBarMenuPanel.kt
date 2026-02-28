package com.rejowan.pdfreaderpro.presentation.screens.reader.components

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Print
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

// Design colors
private val AccentBlue = Color(0xFF64B5F6)
private val AccentPink = Color(0xFFF48FB1)
private val AccentAmber = Color(0xFFFFB74D)
private val AccentTeal = Color(0xFF4DB6AC)
private val AccentPurple = Color(0xFF9575CD)
private val AccentRed = Color(0xFFEF5350)

@Composable
fun TopBarMenuPanel(
    isVisible: Boolean,
    isFavorite: Boolean,
    onInfoClick: () -> Unit,
    onShareClick: () -> Unit,
    onPrintClick: () -> Unit,
    onOpenWithClick: () -> Unit,
    onSaveClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onDismiss: () -> Unit
) {
    if (!isVisible) return

    var isAnimatedVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isAnimatedVisible = true
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Scrim
        AnimatedVisibility(
            visible = isAnimatedVisible,
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

        // Slide panel from right
        AnimatedVisibility(
            visible = isAnimatedVisible,
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
            val configuration = LocalConfiguration.current
            val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
            val panelWidth = if (isLandscape) 320.dp else 280.dp

            Surface(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(panelWidth),
                shape = RoundedCornerShape(topStart = 20.dp, bottomStart = 20.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                shadowElevation = 8.dp
            ) {
                TopBarMenuContent(
                    isFavorite = isFavorite,
                    onInfoClick = onInfoClick,
                    onShareClick = onShareClick,
                    onPrintClick = onPrintClick,
                    onOpenWithClick = onOpenWithClick,
                    onSaveClick = onSaveClick,
                    onFavoriteClick = onFavoriteClick,
                    onDeleteClick = onDeleteClick,
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
private fun TopBarMenuContent(
    isFavorite: Boolean,
    onInfoClick: () -> Unit,
    onShareClick: () -> Unit,
    onPrintClick: () -> Unit,
    onOpenWithClick: () -> Unit,
    onSaveClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 6.dp)
    ) {
        // Header
        MenuHeader()

        Spacer(modifier = Modifier.height(12.dp))

        // Document Actions Section
        SectionLabel(text = "Document Actions", animationDelay = 0)

        Spacer(modifier = Modifier.height(6.dp))

        MenuOptionItem(
            icon = Icons.Outlined.Info,
            title = "Document Info",
            subtitle = "View file details and metadata",
            accentColor = AccentBlue,
            onClick = {
                onDismiss()
                onInfoClick()
            },
            animationDelay = 50
        )

        Spacer(modifier = Modifier.height(6.dp))

        MenuOptionItem(
            icon = Icons.Outlined.Share,
            title = "Share",
            subtitle = "Share this document",
            accentColor = AccentPink,
            onClick = {
                onDismiss()
                onShareClick()
            },
            animationDelay = 100
        )

        Spacer(modifier = Modifier.height(6.dp))

        MenuOptionItem(
            icon = Icons.Outlined.Print,
            title = "Print",
            subtitle = "Print this document",
            accentColor = AccentAmber,
            onClick = {
                onDismiss()
                onPrintClick()
            },
            animationDelay = 150
        )

        Spacer(modifier = Modifier.height(6.dp))

        MenuOptionItem(
            icon = Icons.AutoMirrored.Outlined.OpenInNew,
            title = "Open With",
            subtitle = "Open in another app",
            accentColor = AccentTeal,
            onClick = {
                onDismiss()
                onOpenWithClick()
            },
            animationDelay = 200
        )

        Spacer(modifier = Modifier.height(6.dp))

        MenuOptionItem(
            icon = Icons.Outlined.Save,
            title = "Save As",
            subtitle = "Save a copy to your chosen location",
            accentColor = AccentPurple,
            onClick = {
                onDismiss()
                onSaveClick()
            },
            animationDelay = 250
        )

        Spacer(modifier = Modifier.height(10.dp))
        HorizontalDivider(
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(10.dp))

        // File Management Section
        SectionLabel(text = "File Management", animationDelay = 300)

        Spacer(modifier = Modifier.height(6.dp))

        MenuOptionItem(
            icon = if (isFavorite) Icons.Rounded.Favorite else Icons.Outlined.FavoriteBorder,
            title = if (isFavorite) "Remove from Favourites" else "Add to Favourites",
            subtitle = if (isFavorite) "Remove from your favourites list" else "Add to your favourites list",
            accentColor = AccentPink,
            onClick = {
                onDismiss()
                onFavoriteClick()
            },
            animationDelay = 350
        )

        Spacer(modifier = Modifier.height(6.dp))

        MenuOptionItem(
            icon = Icons.Outlined.Delete,
            title = "Delete",
            subtitle = "Permanently delete this file",
            accentColor = AccentRed,
            isDestructive = true,
            onClick = {
                onDismiss()
                onDeleteClick()
            },
            animationDelay = 400
        )

        Spacer(modifier = Modifier.height(12.dp))
    }
}

@Composable
private fun MenuHeader(
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Document Options",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.SemiBold
            ),
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "Share, print, and manage",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
    }
}

@Composable
private fun SectionLabel(
    text: String,
    animationDelay: Int,
    modifier: Modifier = Modifier
) {
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(animationDelay.toLong())
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
            fontWeight = FontWeight.SemiBold
        ),
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f * alpha),
        modifier = modifier.padding(start = 2.dp)
    )
}

@Composable
private fun MenuOptionItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    accentColor: Color,
    onClick: () -> Unit,
    animationDelay: Int,
    isDestructive: Boolean = false,
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
        label = "option scale"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .scale(scale)
            .clip(RoundedCornerShape(10.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = accentColor),
                onClick = onClick
            ),
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = accentColor.copy(alpha = 0.12f)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(7.dp)
                        .size(16.dp),
                    tint = accentColor
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Text
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = if (isDestructive) accentColor else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isDestructive)
                        accentColor.copy(alpha = 0.7f)
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }

            // Arrow
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = if (isDestructive)
                    accentColor.copy(alpha = 0.5f)
                else
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
            )
        }
    }
}
