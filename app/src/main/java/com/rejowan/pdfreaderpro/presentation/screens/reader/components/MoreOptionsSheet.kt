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
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material.icons.rounded.Numbers
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Print
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
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
import kotlinx.coroutines.delay

// Design colors
private val AccentPurple = Color(0xFF9575CD)
private val AccentBlue = Color(0xFF64B5F6)
private val AccentTeal = Color(0xFF4DB6AC)
private val AccentAmber = Color(0xFFFFB74D)
private val AccentPink = Color(0xFFF48FB1)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreOptionsSheet(
    onBookmarksClick: () -> Unit,
    onAutoScrollClick: () -> Unit,
    onGoToPageClick: () -> Unit,
    onPrintClick: () -> Unit,
    onShareClick: () -> Unit,
    onDocumentInfoClick: () -> Unit,
    onDismiss: () -> Unit
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    if (isLandscape) {
        MoreOptionsSideSheet(
            onBookmarksClick = onBookmarksClick,
            onAutoScrollClick = onAutoScrollClick,
            onGoToPageClick = onGoToPageClick,
            onPrintClick = onPrintClick,
            onShareClick = onShareClick,
            onDocumentInfoClick = onDocumentInfoClick,
            onDismiss = onDismiss
        )
    } else {
        MoreOptionsBottomSheet(
            onBookmarksClick = onBookmarksClick,
            onAutoScrollClick = onAutoScrollClick,
            onGoToPageClick = onGoToPageClick,
            onPrintClick = onPrintClick,
            onShareClick = onShareClick,
            onDocumentInfoClick = onDocumentInfoClick,
            onDismiss = onDismiss
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MoreOptionsBottomSheet(
    onBookmarksClick: () -> Unit,
    onAutoScrollClick: () -> Unit,
    onGoToPageClick: () -> Unit,
    onPrintClick: () -> Unit,
    onShareClick: () -> Unit,
    onDocumentInfoClick: () -> Unit,
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
        MoreOptionsSheetContent(
            onBookmarksClick = onBookmarksClick,
            onAutoScrollClick = onAutoScrollClick,
            onGoToPageClick = onGoToPageClick,
            onPrintClick = onPrintClick,
            onShareClick = onShareClick,
            onDocumentInfoClick = onDocumentInfoClick,
            onDismiss = onDismiss,
            modifier = Modifier.padding(bottom = 24.dp)
        )
    }
}

@Composable
private fun MoreOptionsSideSheet(
    onBookmarksClick: () -> Unit,
    onAutoScrollClick: () -> Unit,
    onGoToPageClick: () -> Unit,
    onPrintClick: () -> Unit,
    onShareClick: () -> Unit,
    onDocumentInfoClick: () -> Unit,
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
                MoreOptionsSheetContent(
                    onBookmarksClick = onBookmarksClick,
                    onAutoScrollClick = onAutoScrollClick,
                    onGoToPageClick = onGoToPageClick,
                    onPrintClick = onPrintClick,
                    onShareClick = onShareClick,
                    onDocumentInfoClick = onDocumentInfoClick,
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
private fun MoreOptionsSheetContent(
    onBookmarksClick: () -> Unit,
    onAutoScrollClick: () -> Unit,
    onGoToPageClick: () -> Unit,
    onPrintClick: () -> Unit,
    onShareClick: () -> Unit,
    onDocumentInfoClick: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Header
        MoreOptionsHeader()

        Spacer(modifier = Modifier.height(12.dp))

        // Reading Tools Section
        SectionLabel(text = stringResource(R.string.reading_tools))

        Spacer(modifier = Modifier.height(6.dp))

        OptionItem(
            icon = Icons.Rounded.Bookmark,
            title = stringResource(R.string.bookmarks),
            subtitle = stringResource(R.string.view_manage_bookmarks),
            accentColor = AccentPurple,
            onClick = {
                onDismiss()
                onBookmarksClick()
            },
            animationDelay = 0
        )

        Spacer(modifier = Modifier.height(4.dp))

        OptionItem(
            icon = Icons.Rounded.PlayArrow,
            title = stringResource(R.string.auto_scroll),
            subtitle = stringResource(R.string.auto_scroll_desc),
            accentColor = AccentBlue,
            onClick = {
                onDismiss()
                onAutoScrollClick()
            },
            animationDelay = 50
        )

        Spacer(modifier = Modifier.height(4.dp))

        OptionItem(
            icon = Icons.Rounded.Numbers,
            title = stringResource(R.string.go_to_page),
            subtitle = stringResource(R.string.go_to_page_desc),
            accentColor = AccentTeal,
            onClick = {
                onDismiss()
                onGoToPageClick()
            },
            animationDelay = 100
        )

        Spacer(modifier = Modifier.height(10.dp))
        HorizontalDivider(
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(10.dp))

        // Document Actions Section
        SectionLabel(text = stringResource(R.string.document))

        Spacer(modifier = Modifier.height(6.dp))

        OptionItem(
            icon = Icons.Rounded.Share,
            title = stringResource(R.string.share),
            subtitle = stringResource(R.string.share_document),
            accentColor = AccentPink,
            onClick = {
                onDismiss()
                onShareClick()
            },
            animationDelay = 150
        )

        Spacer(modifier = Modifier.height(4.dp))

        OptionItem(
            icon = Icons.Rounded.Print,
            title = stringResource(R.string.print),
            subtitle = stringResource(R.string.print_document),
            accentColor = AccentAmber,
            onClick = {
                onDismiss()
                onPrintClick()
            },
            animationDelay = 200
        )

        Spacer(modifier = Modifier.height(4.dp))

        OptionItem(
            icon = Icons.Rounded.Info,
            title = stringResource(R.string.document_info),
            subtitle = stringResource(R.string.view_file_details),
            accentColor = MaterialTheme.colorScheme.primary,
            onClick = {
                onDismiss()
                onDocumentInfoClick()
            },
            animationDelay = 250
        )
    }
}

@Composable
private fun MoreOptionsHeader(
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        ) {
            Icon(
                imageVector = Icons.Rounded.MoreHoriz,
                contentDescription = null,
                modifier = Modifier
                    .padding(6.dp)
                    .size(16.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column {
            Text(
                text = stringResource(R.string.more_options),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = stringResource(R.string.additional_tools),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun SectionLabel(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall.copy(
            fontWeight = FontWeight.Medium
        ),
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
        modifier = modifier.padding(start = 4.dp)
    )
}

@Composable
private fun OptionItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    accentColor: Color,
    onClick: () -> Unit,
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
        label = "option scale"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .scale(scale)
            .clip(RoundedCornerShape(8.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = accentColor),
                onClick = onClick
            ),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .padding(horizontal = 10.dp),
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
                        .padding(6.dp)
                        .size(16.dp),
                    tint = accentColor
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Text
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }

            // Arrow
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
            )
        }
    }
}
