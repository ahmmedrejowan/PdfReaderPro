package com.rejowan.pdfreaderpro.presentation.components

import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.rejowan.pdfreaderpro.domain.model.ViewMode

@Composable
fun ViewModeToggle(
    currentMode: ViewMode,
    onModeChange: (ViewMode) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier) {
        IconButton(onClick = { onModeChange(ViewMode.LIST) }) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ViewList,
                contentDescription = "List view",
                tint = if (currentMode == ViewMode.LIST) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }

        IconButton(onClick = { onModeChange(ViewMode.GRID) }) {
            Icon(
                imageVector = Icons.Default.GridView,
                contentDescription = "Grid view",
                tint = if (currentMode == ViewMode.GRID) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }
    }
}
