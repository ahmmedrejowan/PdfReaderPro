package com.rejowan.pdfreaderpro.presentation.screens.home.tabs

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.rejowan.pdfreaderpro.domain.model.PdfFile
import com.rejowan.pdfreaderpro.domain.model.ViewMode
import com.rejowan.pdfreaderpro.presentation.components.EmptyFilesState
import com.rejowan.pdfreaderpro.presentation.components.LoadingState
import com.rejowan.pdfreaderpro.presentation.components.PdfGridItem
import com.rejowan.pdfreaderpro.presentation.components.PdfListItem
import com.rejowan.pdfreaderpro.presentation.components.PermissionRequiredState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilesTab(
    files: List<PdfFile>,
    viewMode: ViewMode,
    isLoading: Boolean,
    hasPermission: Boolean,
    onFileClick: (PdfFile) -> Unit,
    onFileOptionsClick: (PdfFile) -> Unit,
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
            isLoading && files.isEmpty() -> {
                LoadingState()
            }
            files.isEmpty() -> {
                EmptyFilesState(onScanClick = onRefresh)
            }
            viewMode == ViewMode.LIST -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(top = 8.dp, bottom = 80.dp)
                ) {
                    itemsIndexed(
                        items = files,
                        key = { _, file -> file.id }
                    ) { index, file ->
                        PdfListItem(
                            pdfFile = file,
                            onClick = { onFileClick(file) },
                            onOptionsClick = { onFileOptionsClick(file) },
                            animationDelay = index * 30
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
                    itemsIndexed(
                        items = files,
                        key = { _, file -> file.id }
                    ) { index, file ->
                        PdfGridItem(
                            pdfFile = file,
                            onClick = { onFileClick(file) },
                            onOptionsClick = { onFileOptionsClick(file) },
                            animationDelay = index * 30
                        )
                    }
                }
            }
        }
    }
}
