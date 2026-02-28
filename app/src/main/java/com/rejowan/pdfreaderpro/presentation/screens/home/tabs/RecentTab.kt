package com.rejowan.pdfreaderpro.presentation.screens.home.tabs

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.rejowan.pdfreaderpro.domain.model.RecentFile
import com.rejowan.pdfreaderpro.domain.model.ViewMode
import com.rejowan.pdfreaderpro.presentation.components.EmptyRecentState
import com.rejowan.pdfreaderpro.presentation.components.LoadingState
import com.rejowan.pdfreaderpro.presentation.components.PermissionRequiredState
import com.rejowan.pdfreaderpro.presentation.components.RecentGridItem
import com.rejowan.pdfreaderpro.presentation.components.RecentListItem

@Composable
fun RecentTab(
    recentFiles: List<RecentFile>,
    viewMode: ViewMode,
    isLoading: Boolean,
    hasPermission: Boolean,
    onFileClick: (RecentFile) -> Unit,
    onFileOptionsClick: (RecentFile) -> Unit,
    onBrowseClick: () -> Unit,
    onGrantPermissionClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        when {
            !hasPermission -> {
                PermissionRequiredState(onGrantClick = onGrantPermissionClick)
            }
            isLoading && recentFiles.isEmpty() -> {
                LoadingState()
            }
            recentFiles.isEmpty() -> {
                EmptyRecentState(onBrowseClick = onBrowseClick)
            }
            viewMode == ViewMode.LIST -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(top = 8.dp, bottom = 80.dp)
                ) {
                    items(
                        items = recentFiles,
                        key = { it.id }
                    ) { file ->
                        RecentListItem(
                            recentFile = file,
                            onClick = { onFileClick(file) },
                            onOptionsClick = { onFileOptionsClick(file) }
                        )
                    }
                }
            }
            else -> {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 80.dp)
                ) {
                    items(
                        items = recentFiles,
                        key = { it.id }
                    ) { file ->
                        RecentGridItem(
                            recentFile = file,
                            onClick = { onFileClick(file) },
                            onOptionsClick = { onFileOptionsClick(file) }
                        )
                    }
                }
            }
        }
    }
}
