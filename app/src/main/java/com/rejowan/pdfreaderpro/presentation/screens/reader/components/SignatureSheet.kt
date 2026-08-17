package com.rejowan.pdfreaderpro.presentation.screens.reader.components

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rejowan.pdfreaderpro.R
import com.rejowan.pdfreaderpro.presentation.screens.reader.PlacedSignatureUi
import com.rejowan.pdfreaderpro.presentation.screens.reader.SavedSignatureUi

/**
 * Everything to do with signatures, in one place.
 *
 * Deliberately the only surface for this: placing a signature used to raise a bar
 * across the document demanding to be saved or discarded, which nagged for the
 * rest of the session. Placements are stored like highlights instead, so this
 * sheet can be opened whenever the user feels like dealing with them.
 *
 * What it shows depends on the document. With nothing placed it leads with making
 * or picking a signature; once something is placed it also lists the placements
 * and offers the ways of keeping them.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignatureSheet(
    placed: List<PlacedSignatureUi>,
    saved: List<SavedSignatureUi>,
    canSaveInPlace: Boolean,
    onPlaceSaved: (String) -> Unit,
    onDeleteSaved: (String) -> Unit,
    onRemovePlaced: (Long) -> Unit,
    onGoToPlaced: (PlacedSignatureUi) -> Unit,
    onCaptured: (Bitmap, remember: Boolean) -> Unit,
    onSaveIntoFile: () -> Unit,
    onSaveAsCopy: () -> Unit,
    onRemoveAll: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var capturing by remember { mutableStateOf(saved.isEmpty() && placed.isEmpty()) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(bottom = 24.dp)) {
            Text(
                text = stringResource(R.string.sign_saved_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 8.dp)
            )

            if (capturing) {
                SignatureCapture(
                    onCaptured = { bitmap, keep -> onCaptured(bitmap, keep) },
                    onCancel = {
                        if (saved.isEmpty() && placed.isEmpty()) onDismiss() else capturing = false
                    }
                )
                return@Column
            }

            if (placed.isNotEmpty()) {
                SectionLabel(stringResource(R.string.sign_section_placed))

                placed.forEach { item ->
                    PlacedRow(
                        item = item,
                        onClick = { onGoToPlaced(item) },
                        onRemove = { onRemovePlaced(item.id) }
                    )
                }

                Spacer(Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onRemoveAll) {
                        Text(stringResource(R.string.sign_discard_all), maxLines = 1)
                    }
                    Spacer(Modifier.width(4.dp))
                    TextButton(onClick = onSaveAsCopy) {
                        Text(stringResource(R.string.sign_save_as_copy), maxLines = 1)
                    }
                }

                // Writing into the document itself is only possible when the reader
                // has the user's own file rather than a copy of it. Say so when it
                // is not on offer, rather than leaving the option to just vanish.
                if (canSaveInPlace) {
                    Button(
                        onClick = onSaveIntoFile,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        Text(stringResource(R.string.sign_save_into_file))
                    }
                } else {
                    Text(
                        text = stringResource(R.string.sign_cannot_save_into_file),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }

                Spacer(Modifier.height(12.dp))
                HorizontalDivider()
                Spacer(Modifier.height(12.dp))
            }

            SectionLabel(stringResource(R.string.sign_section_saved))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                item {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .size(width = 120.dp, height = 84.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .clickable { capturing = true }
                    ) {
                        Spacer(Modifier.height(18.dp))
                        Icon(
                            Icons.Rounded.Add,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = stringResource(R.string.sign_new),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                items(saved, key = { it.id }) { signature ->
                    SavedTile(
                        signature = signature,
                        onPlace = { onPlaceSaved(signature.id) },
                        onDelete = { onDeleteSaved(signature.id) }
                    )
                }
            }

            if (placed.isEmpty()) {
                Spacer(Modifier.height(12.dp))
                Text(
                    text = stringResource(R.string.sign_nothing_placed),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 6.dp)
    )
}

@Composable
private fun PlacedRow(
    item: PlacedSignatureUi,
    onClick: () -> Unit,
    onRemove: () -> Unit
) {
    val bitmap = remember(item.imagePath) { BitmapFactory.decodeFile(item.imagePath) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(width = 64.dp, height = 34.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
        ) {
            if (bitmap != null) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier.fillMaxWidth().padding(4.dp)
                )
            }
        }
        Spacer(Modifier.width(12.dp))
        Text(
            text = stringResource(R.string.sign_placed_on_page, item.pageIndex + 1),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
        IconButton(onClick = onRemove) {
            Icon(
                Icons.Rounded.Delete,
                contentDescription = stringResource(R.string.sign_remove_placed),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SavedTile(
    signature: SavedSignatureUi,
    onPlace: () -> Unit,
    onDelete: () -> Unit
) {
    val bitmap = remember(signature.filePath) { BitmapFactory.decodeFile(signature.filePath) }
    Box(
        modifier = Modifier
            .size(width = 140.dp, height = 84.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            .clickable(onClick = onPlace)
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(84.dp)
                    .padding(10.dp)
            )
        }
        IconButton(
            onClick = onDelete,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(28.dp)
        ) {
            Icon(
                Icons.Rounded.Delete,
                contentDescription = stringResource(R.string.sign_delete),
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
