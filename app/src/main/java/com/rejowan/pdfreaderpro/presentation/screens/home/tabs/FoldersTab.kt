package com.rejowan.pdfreaderpro.presentation.screens.home.tabs

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.rejowan.pdfreaderpro.domain.model.PdfFolder
import com.rejowan.pdfreaderpro.presentation.components.EmptyState
import com.rejowan.pdfreaderpro.presentation.components.FolderItem
import com.rejowan.pdfreaderpro.presentation.components.LoadingState

@Composable
fun FoldersTab(
    folders: List<PdfFolder>,
    isLoading: Boolean,
    onFolderClick: (PdfFolder) -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        when {
            isLoading && folders.isEmpty() -> {
                LoadingState()
            }
            folders.isEmpty() -> {
                EmptyState(
                    icon = Icons.Outlined.FolderOpen,
                    title = "No Folders",
                    message = "PDF folders will appear here once files are scanned.",
                    actionLabel = "Scan for PDFs",
                    onAction = onRefresh
                )
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(
                        items = folders,
                        key = { it.path }
                    ) { folder ->
                        FolderItem(
                            folder = folder,
                            onClick = { onFolderClick(folder) }
                        )
                    }
                }
            }
        }
    }
}
