package com.rejowan.pdfreaderpro.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.HistoryToggleOff
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.rejowan.pdfreaderpro.domain.model.PdfFile
import com.rejowan.pdfreaderpro.util.FormattingUtils
import kotlinx.coroutines.delay

// Accent colors
private val SoftAmber = Color(0xFFFFB74D)
private val SoftBlue = Color(0xFF64B5F6)
private val SoftPurple = Color(0xFF9575CD)
private val SoftTeal = Color(0xFF4DB6AC)
private val SoftOrange = Color(0xFFFF8A65)
private val SoftRed = Color(0xFFEF5350)

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
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp)
                .padding(bottom = 32.dp)
        ) {
            // File header card
            FileHeaderCard(pdfFile = pdfFile)

            Spacer(modifier = Modifier.height(20.dp))

            // Quick actions label
            Text(
                text = "Quick Actions",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier.padding(start = 4.dp, bottom = 12.dp)
            )

            // Action options
            FileActionCard(
                icon = if (isFavorite) Icons.Outlined.Star else Icons.Outlined.StarBorder,
                label = if (isFavorite) "Remove from Favorites" else "Add to Favorites",
                description = if (isFavorite) "Remove from your starred collection" else "Save to your favorites for quick access",
                accentColor = SoftAmber,
                animationDelay = 0,
                onClick = {
                    onFavoriteClick()
                    onDismiss()
                }
            )

            // Remove from Recents option (only shown when callback is provided)
            if (onRemoveFromRecentsClick != null) {
                Spacer(modifier = Modifier.height(8.dp))

                FileActionCard(
                    icon = Icons.Outlined.HistoryToggleOff,
                    label = "Remove from Recents",
                    description = "Clear from recent history",
                    accentColor = SoftOrange,
                    animationDelay = 15,
                    onClick = {
                        onRemoveFromRecentsClick()
                        onDismiss()
                    }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            FileActionCard(
                icon = Icons.Outlined.Share,
                label = "Share",
                description = "Send this PDF to other apps",
                accentColor = SoftBlue,
                animationDelay = 30,
                onClick = {
                    onShareClick()
                    onDismiss()
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            FileActionCard(
                icon = Icons.Outlined.Edit,
                label = "Rename",
                description = "Change the file name",
                accentColor = SoftPurple,
                animationDelay = 60,
                onClick = {
                    onRenameClick()
                    onDismiss()
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            FileActionCard(
                icon = Icons.Outlined.Info,
                label = "File Info",
                description = "View file details and properties",
                accentColor = SoftTeal,
                animationDelay = 90,
                onClick = {
                    onInfoClick()
                    onDismiss()
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Destructive action with different styling
            FileActionCard(
                icon = Icons.Outlined.Delete,
                label = "Delete",
                description = "Permanently remove this file",
                accentColor = SoftRed,
                animationDelay = 120,
                isDestructive = true,
                onClick = {
                    onDeleteClick()
                    onDismiss()
                }
            )
        }
    }
}

@Composable
private fun FileHeaderCard(
    pdfFile: PdfFile,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // PDF icon/thumbnail
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Description,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(12.dp)
                        .size(28.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = pdfFile.displayName,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FileMetaChip(
                        text = FormattingUtils.formattedFileSize(pdfFile.size)
                    )
                    FileMetaChip(
                        text = "${pdfFile.pageCount} pages"
                    )
                }
            }
        }
    }
}

@Composable
private fun FileMetaChip(
    text: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(6.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.7f)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
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
            .clip(RoundedCornerShape(14.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = effectiveColor),
                onClick = onClick
            ),
        shape = RoundedCornerShape(14.dp),
        color = if (isDestructive) {
            MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
        } else {
            MaterialTheme.colorScheme.surfaceContainerLow
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = effectiveColor.copy(alpha = 0.12f)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(10.dp)
                        .size(20.dp),
                    tint = effectiveColor
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleSmall.copy(
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
