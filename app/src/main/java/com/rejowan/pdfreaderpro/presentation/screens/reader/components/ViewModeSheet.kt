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
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material.icons.rounded.SwapVert
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material.icons.rounded.ViewDay
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rejowan.pdfreaderpro.R
import com.rejowan.pdfreaderpro.presentation.screens.reader.ScrollMode
import kotlinx.coroutines.delay

// Design colors
private val AccentPurple = Color(0xFF9575CD)
private val AccentTeal = Color(0xFF4DB6AC)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewModeSheet(
    currentScrollMode: ScrollMode,
    isSnapEnabled: Boolean,
    isAutoHideToolbar: Boolean,
    onScrollModeChange: (ScrollMode) -> Unit,
    onSnapToggle: (Boolean) -> Unit,
    onAutoHideToolbarToggle: (Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    if (isLandscape) {
        ViewModeSideSheet(
            currentScrollMode = currentScrollMode,
            isSnapEnabled = isSnapEnabled,
            isAutoHideToolbar = isAutoHideToolbar,
            onScrollModeChange = onScrollModeChange,
            onSnapToggle = onSnapToggle,
            onAutoHideToolbarToggle = onAutoHideToolbarToggle,
            onDismiss = onDismiss
        )
    } else {
        ViewModeBottomSheet(
            currentScrollMode = currentScrollMode,
            isSnapEnabled = isSnapEnabled,
            isAutoHideToolbar = isAutoHideToolbar,
            onScrollModeChange = onScrollModeChange,
            onSnapToggle = onSnapToggle,
            onAutoHideToolbarToggle = onAutoHideToolbarToggle,
            onDismiss = onDismiss
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ViewModeBottomSheet(
    currentScrollMode: ScrollMode,
    isSnapEnabled: Boolean,
    isAutoHideToolbar: Boolean,
    onScrollModeChange: (ScrollMode) -> Unit,
    onSnapToggle: (Boolean) -> Unit,
    onAutoHideToolbarToggle: (Boolean) -> Unit,
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
        ViewModeSheetContent(
            currentScrollMode = currentScrollMode,
            isSnapEnabled = isSnapEnabled,
            isAutoHideToolbar = isAutoHideToolbar,
            onScrollModeChange = onScrollModeChange,
            onSnapToggle = onSnapToggle,
            onAutoHideToolbarToggle = onAutoHideToolbarToggle,
            modifier = Modifier.padding(bottom = 24.dp)
        )
    }
}

@Composable
private fun ViewModeSideSheet(
    currentScrollMode: ScrollMode,
    isSnapEnabled: Boolean,
    isAutoHideToolbar: Boolean,
    onScrollModeChange: (ScrollMode) -> Unit,
    onSnapToggle: (Boolean) -> Unit,
    onAutoHideToolbarToggle: (Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isVisible = true
    }

    Box(modifier = Modifier.fillMaxSize()) {
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
                ViewModeSheetContent(
                    currentScrollMode = currentScrollMode,
                    isSnapEnabled = isSnapEnabled,
                    isAutoHideToolbar = isAutoHideToolbar,
                    onScrollModeChange = onScrollModeChange,
                    onSnapToggle = onSnapToggle,
                    onAutoHideToolbarToggle = onAutoHideToolbarToggle,
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
private fun ViewModeSheetContent(
    currentScrollMode: ScrollMode,
    isSnapEnabled: Boolean,
    isAutoHideToolbar: Boolean,
    onScrollModeChange: (ScrollMode) -> Unit,
    onSnapToggle: (Boolean) -> Unit,
    onAutoHideToolbarToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Header
        SheetHeader(
            icon = Icons.Rounded.ViewDay,
            title = stringResource(R.string.view_options),
            subtitle = stringResource(R.string.customize_reading_layout),
            accentColor = AccentPurple
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Scroll Mode Section (2 options in single row)
        SectionLabel(text = stringResource(R.string.scroll_mode), delay = 0)

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ModeChip(
                icon = Icons.Rounded.SwapVert,
                label = stringResource(R.string.vertical),
                isSelected = currentScrollMode == ScrollMode.VERTICAL,
                accentColor = AccentPurple,
                onClick = { onScrollModeChange(ScrollMode.VERTICAL) },
                modifier = Modifier.weight(1f),
                animationDelay = 50
            )
            ModeChip(
                icon = Icons.Rounded.SwapHoriz,
                label = stringResource(R.string.horizontal),
                isSelected = currentScrollMode == ScrollMode.HORIZONTAL,
                accentColor = AccentPurple,
                onClick = { onScrollModeChange(ScrollMode.HORIZONTAL) },
                modifier = Modifier.weight(1f),
                animationDelay = 100
            )
        }

        Spacer(modifier = Modifier.height(12.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        Spacer(modifier = Modifier.height(12.dp))

        // Toggles Section
        ToggleRow(
            icon = Icons.Rounded.GridView,
            title = stringResource(R.string.snap_to_page),
            description = stringResource(R.string.snap_to_page_desc),
            isEnabled = isSnapEnabled,
            onToggle = onSnapToggle,
            accentColor = AccentTeal,
            animationDelay = 150
        )

        Spacer(modifier = Modifier.height(10.dp))

        ToggleRow(
            icon = Icons.Rounded.Timer,
            title = stringResource(R.string.auto_hide_toolbar),
            description = stringResource(R.string.auto_hide_toolbar_desc),
            isEnabled = isAutoHideToolbar,
            onToggle = onAutoHideToolbarToggle,
            accentColor = AccentTeal,
            animationDelay = 200
        )
    }
}

@Composable
private fun SheetHeader(
    icon: ImageVector,
    title: String,
    subtitle: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = accentColor.copy(alpha = 0.12f)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier
                    .padding(6.dp)
                    .size(16.dp),
                tint = accentColor
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
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
private fun ModeChip(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    animationDelay: Int = 0
) {
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(animationDelay.toLong())
        isVisible = true
    }

    val scale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0.95f,
        animationSpec = tween(250, easing = FastOutSlowInEasing),
        label = "chip scale"
    )

    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) accentColor.copy(alpha = 0.12f)
        else MaterialTheme.colorScheme.surfaceContainerHigh,
        animationSpec = tween(200),
        label = "chip bg"
    )

    val borderColor by animateColorAsState(
        targetValue = if (isSelected) accentColor.copy(alpha = 0.5f)
        else Color.Transparent,
        animationSpec = tween(200),
        label = "chip border"
    )

    val contentColor by animateColorAsState(
        targetValue = if (isSelected) accentColor
        else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(200),
        label = "chip content"
    )

    Surface(
        modifier = modifier
            .scale(scale)
            .clip(RoundedCornerShape(14.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = accentColor),
                onClick = onClick
            )
            .then(
                if (isSelected) Modifier.border(
                    width = 1.5.dp,
                    color = borderColor,
                    shape = RoundedCornerShape(14.dp)
                ) else Modifier
            ),
        shape = RoundedCornerShape(14.dp),
        color = backgroundColor
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = contentColor
            )

            Spacer(modifier = Modifier.width(6.dp))

            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
                ),
                color = contentColor
            )

            if (isSelected) {
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(accentColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Check,
                        contentDescription = null,
                        modifier = Modifier.size(10.dp),
                        tint = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun ToggleRow(
    icon: ImageVector,
    title: String,
    description: String,
    isEnabled: Boolean,
    onToggle: (Boolean) -> Unit,
    accentColor: Color,
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
        label = "toggle scale"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .scale(scale),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = accentColor.copy(alpha = 0.12f)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(8.dp)
                        .size(18.dp),
                    tint = accentColor
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }

            Switch(
                checked = isEnabled,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = accentColor,
                    uncheckedThumbColor = Color.White,
                    uncheckedTrackColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                )
            )
        }
    }
}
