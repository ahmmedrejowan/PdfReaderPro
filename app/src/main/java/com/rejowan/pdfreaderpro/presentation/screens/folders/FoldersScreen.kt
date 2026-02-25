package com.rejowan.pdfreaderpro.presentation.screens.folders

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.rejowan.pdfreaderpro.domain.model.PdfFolder
import com.rejowan.pdfreaderpro.presentation.components.EmptyFoldersState
import com.rejowan.pdfreaderpro.presentation.components.FolderItem
import com.rejowan.pdfreaderpro.presentation.components.LoadingState
import com.rejowan.pdfreaderpro.presentation.components.PermissionRequiredState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoldersScreen(
    folders: List<PdfFolder>,
    isLoading: Boolean,
    hasPermission: Boolean,
    onFolderClick: (PdfFolder) -> Unit,
    onRefresh: () -> Unit,
    onGrantPermissionClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    PullToRefreshBox(
        isRefreshing = isLoading,
        onRefresh = onRefresh,
        modifier = modifier.fillMaxSize()
    ) {
        when {
            !hasPermission -> {
                PermissionRequiredState(onGrantClick = onGrantPermissionClick)
            }
            isLoading && folders.isEmpty() -> {
                LoadingState()
            }
            folders.isEmpty() -> {
                EmptyFoldersState(onScanClick = onRefresh)
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
