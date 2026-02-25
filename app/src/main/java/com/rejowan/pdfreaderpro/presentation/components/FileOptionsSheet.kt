package com.rejowan.pdfreaderpro.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.rejowan.pdfreaderpro.domain.model.PdfFile
import com.rejowan.pdfreaderpro.util.FormattingUtils

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
    onDeleteClick: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier.padding(bottom = 32.dp)
        ) {
            // File header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                PdfThumbnail(size = 48.dp)
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = pdfFile.displayName,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "${FormattingUtils.formattedFileSize(pdfFile.size)} • ${pdfFile.pageCount} pages",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(horizontal = 24.dp))

            Spacer(modifier = Modifier.height(8.dp))

            // Options
            FileOptionItem(
                icon = if (isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                label = if (isFavorite) "Remove from Favorites" else "Add to Favorites",
                onClick = {
                    onFavoriteClick()
                    onDismiss()
                }
            )

            FileOptionItem(
                icon = Icons.Default.Share,
                label = "Share",
                onClick = {
                    onShareClick()
                    onDismiss()
                }
            )

            FileOptionItem(
                icon = Icons.Default.Edit,
                label = "Rename",
                onClick = {
                    onRenameClick()
                    onDismiss()
                }
            )

            FileOptionItem(
                icon = Icons.Default.Info,
                label = "File Info",
                onClick = {
                    onInfoClick()
                    onDismiss()
                }
            )

            FileOptionItem(
                icon = Icons.Default.Delete,
                label = "Delete",
                onClick = {
                    onDeleteClick()
                    onDismiss()
                },
                isDestructive = true
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun FileOptionItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    isDestructive: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isDestructive) {
                MaterialTheme.colorScheme.error
            } else {
                MaterialTheme.colorScheme.onSurface
            }
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = if (isDestructive) {
                MaterialTheme.colorScheme.error
            } else {
                MaterialTheme.colorScheme.onSurface
            }
        )
    }
}
