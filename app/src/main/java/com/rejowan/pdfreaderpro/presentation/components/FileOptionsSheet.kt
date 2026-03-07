package com.rejowan.pdfreaderpro.presentation.components

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.HistoryToggleOff
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.StarBorder
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.rejowan.pdfreaderpro.R
import com.rejowan.pdfreaderpro.domain.model.PdfFile
import com.rejowan.pdfreaderpro.util.FormattingUtils
import kotlinx.coroutines.delay

// Accent colors matching UI guide
private val AccentAmber = Color(0xFFFFB74D)
private val AccentBlue = Color(0xFF64B5F6)
private val AccentPurple = Color(0xFF9575CD)
private val AccentTeal = Color(0xFF4DB6AC)
private val AccentOrange = Color(0xFFFF8A65)
private val AccentRed = Color(0xFFEF5350)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileOptionsSheet(
    pdfFile: PdfFile,
    isFavorite: Boolean,
    onDismiss: () -> Unit,
    onFavoriteClick: () -> Unit,
    onShareClick: () -> Unit,
    onRenameClick: () -> Unit,
    onInfoClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onRemoveFromRecentsClick: (() -> Unit)? = null
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    if (isLandscape) {
        FileOptionsSideSheet(
            pdfFile = pdfFile,
            isFavorite = isFavorite,
            onDismiss = onDismiss,
            onFavoriteClick = onFavoriteClick,
            onShareClick = onShareClick,
            onRenameClick = onRenameClick,
            onInfoClick = onInfoClick,
            onDeleteClick = onDeleteClick,
            onRemoveFromRecentsClick = onRemoveFromRecentsClick
        )
    } else {
        FileOptionsBottomSheet(
            pdfFile = pdfFile,
            isFavorite = isFavorite,
            onDismiss = onDismiss,
            onFavoriteClick = onFavoriteClick,
            onShareClick = onShareClick,
            onRenameClick = onRenameClick,
            onInfoClick = onInfoClick,
            onDeleteClick = onDeleteClick,
            onRemoveFromRecentsClick = onRemoveFromRecentsClick
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FileOptionsBottomSheet(
    pdfFile: PdfFile,
    isFavorite: Boolean,
    onDismiss: () -> Unit,
    onFavoriteClick: () -> Unit,
    onShareClick: () -> Unit,
    onRenameClick: () -> Unit,
    onInfoClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onRemoveFromRecentsClick: (() -> Unit)?
) {
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        FileOptionsContent(
            pdfFile = pdfFile,
            isFavorite = isFavorite,
            onDismiss = onDismiss,
            onFavoriteClick = onFavoriteClick,
            onShareClick = onShareClick,
            onRenameClick = onRenameClick,
            onInfoClick = onInfoClick,
            onDeleteClick = onDeleteClick,
            onRemoveFromRecentsClick = onRemoveFromRecentsClick,
            modifier = Modifier.padding(bottom = 24.dp)
        )
    }
}

@Composable
private fun FileOptionsSideSheet(
    pdfFile: PdfFile,
    isFavorite: Boolean,
    onDismiss: () -> Unit,
    onFavoriteClick: () -> Unit,
    onShareClick: () -> Unit,
    onRenameClick: () -> Unit,
    onInfoClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onRemoveFromRecentsClick: (() -> Unit)?
) {
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isVisible = true
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Scrim
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

        // Side panel
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
                FileOptionsContent(
                    pdfFile = pdfFile,
                    isFavorite = isFavorite,
                    onDismiss = onDismiss,
                    onFavoriteClick = onFavoriteClick,
                    onShareClick = onShareClick,
                    onRenameClick = onRenameClick,
                    onInfoClick = onInfoClick,
                    onDeleteClick = onDeleteClick,
                    onRemoveFromRecentsClick = onRemoveFromRecentsClick,
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
private fun FileOptionsContent(
    pdfFile: PdfFile,
    isFavorite: Boolean,
    onDismiss: () -> Unit,
    onFavoriteClick: () -> Unit,
    onShareClick: () -> Unit,
    onRenameClick: () -> Unit,
    onInfoClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onRemoveFromRecentsClick: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Header
        FileOptionsHeader(pdfFile = pdfFile)

        Spacer(modifier = Modifier.height(12.dp))

        // Quick actions label
        SectionLabel(text = stringResource(R.string.quick_actions))

        Spacer(modifier = Modifier.height(6.dp))

        // Action options
        FileActionCard(
            icon = if (isFavorite) Icons.Outlined.Star else Icons.Outlined.StarBorder,
            label = if (isFavorite) stringResource(R.string.remove_from_favorites_action) else stringResource(R.string.add_to_favorites),
            description = if (isFavorite) stringResource(R.string.remove_favorites_desc) else stringResource(R.string.add_favorites_desc),
            accentColor = AccentAmber,
            animationDelay = 0,
            onClick = {
                onFavoriteClick()
                onDismiss()
            }
        )

        // Remove from Recents option (only shown when callback is provided)
        if (onRemoveFromRecentsClick != null) {
            Spacer(modifier = Modifier.height(4.dp))

            FileActionCard(
                icon = Icons.Outlined.HistoryToggleOff,
                label = stringResource(R.string.remove_from_recents),
                description = stringResource(R.string.remove_recents_desc),
                accentColor = AccentOrange,
                animationDelay = 50,
                onClick = {
                    onRemoveFromRecentsClick()
                    onDismiss()
                }
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        FileActionCard(
            icon = Icons.Outlined.Share,
            label = stringResource(R.string.share_pdf),
            description = stringResource(R.string.share_pdf_desc),
            accentColor = AccentBlue,
            animationDelay = 100,
            onClick = {
                onShareClick()
                onDismiss()
            }
        )

        Spacer(modifier = Modifier.height(4.dp))

        FileActionCard(
            icon = Icons.Outlined.Edit,
            label = stringResource(R.string.rename_action),
            description = stringResource(R.string.rename_desc),
            accentColor = AccentPurple,
            animationDelay = 150,
            onClick = {
                onRenameClick()
                onDismiss()
            }
        )

        Spacer(modifier = Modifier.height(4.dp))

        FileActionCard(
            icon = Icons.Outlined.Info,
            label = stringResource(R.string.file_info),
            description = stringResource(R.string.file_info_desc),
            accentColor = AccentTeal,
            animationDelay = 200,
            onClick = {
                onInfoClick()
                onDismiss()
            }
        )

        Spacer(modifier = Modifier.height(10.dp))
        HorizontalDivider(
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(10.dp))

        // Destructive action with different styling
        FileActionCard(
            icon = Icons.Outlined.Delete,
            label = stringResource(R.string.delete_action),
            description = stringResource(R.string.delete_desc),
            accentColor = AccentRed,
            animationDelay = 250,
            isDestructive = true,
            onClick = {
                onDeleteClick()
                onDismiss()
            }
        )
    }
}

@Composable
private fun FileOptionsHeader(
    pdfFile: PdfFile,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // PDF icon
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        ) {
            Icon(
                imageVector = Icons.Outlined.Description,
                contentDescription = stringResource(R.string.cd_file_options),
                modifier = Modifier
                    .padding(6.dp)
                    .size(16.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = pdfFile.displayName,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = FormattingUtils.formattedFileSize(pdfFile.size),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
                Text(
                    text = "•",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
                Text(
                    text = stringResource(R.string.pages_count, pdfFile.pageCount),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
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
private fun FileActionCard(
    icon: ImageVector,
    label: String,
    description: String,
    accentColor: Color,
    animationDelay: Int,
    onClick: () -> Unit,
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
        label = "action scale"
    )

    val effectiveColor = if (isDestructive) {
        MaterialTheme.colorScheme.error.copy(alpha = 0.9f)
    } else {
        accentColor
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .scale(scale)
            .clip(RoundedCornerShape(8.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = effectiveColor),
                onClick = onClick
            ),
        shape = RoundedCornerShape(8.dp),
        color = if (isDestructive) {
            MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
        } else {
            MaterialTheme.colorScheme.surfaceContainerLow
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = effectiveColor.copy(alpha = 0.12f)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = stringResource(R.string.cd_decorative),
                    modifier = Modifier
                        .padding(6.dp)
                        .size(16.dp),
                    tint = effectiveColor
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = if (isDestructive) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
        }
    }
}
