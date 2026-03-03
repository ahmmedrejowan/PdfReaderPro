package com.rejowan.pdfreaderpro.presentation.screens.home.tabs

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.rejowan.pdfreaderpro.domain.model.PdfFile
import com.rejowan.pdfreaderpro.domain.model.ViewMode
import com.rejowan.pdfreaderpro.presentation.components.EmptyFavoritesState
import com.rejowan.pdfreaderpro.presentation.components.LoadingState
import com.rejowan.pdfreaderpro.presentation.components.PdfGridItem
import com.rejowan.pdfreaderpro.presentation.components.PdfListItem
import com.rejowan.pdfreaderpro.presentation.components.PermissionRequiredState

@Composable
fun FavoritesTab(
    favorites: List<PdfFile>,
    viewMode: ViewMode,
    isLoading: Boolean,
    hasPermission: Boolean,
    onFileClick: (PdfFile) -> Unit,
    onFileOptionsClick: (PdfFile) -> Unit,
    onBrowseClick: () -> Unit,
    onGrantPermissionClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        when {
            !hasPermission -> {
                PermissionRequiredState(onGrantClick = onGrantPermissionClick)
            }
            isLoading && favorites.isEmpty() -> {
                LoadingState()
            }
            favorites.isEmpty() -> {
                EmptyFavoritesState(onBrowseClick = onBrowseClick)
            }
            viewMode == ViewMode.LIST -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(top = 8.dp, bottom = 80.dp)
                ) {
                    itemsIndexed(
                        items = favorites,
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
                        items = favorites,
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
