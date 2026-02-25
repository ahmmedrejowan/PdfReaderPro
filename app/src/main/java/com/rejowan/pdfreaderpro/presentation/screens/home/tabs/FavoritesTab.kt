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
import com.rejowan.pdfreaderpro.domain.model.PdfFile
import com.rejowan.pdfreaderpro.domain.model.ViewMode
import com.rejowan.pdfreaderpro.presentation.components.EmptyFavoritesState
import com.rejowan.pdfreaderpro.presentation.components.LoadingState
import com.rejowan.pdfreaderpro.presentation.components.PdfGridItem
import com.rejowan.pdfreaderpro.presentation.components.PdfListItem

@Composable
fun FavoritesTab(
    favorites: List<PdfFile>,
    viewMode: ViewMode,
    isLoading: Boolean,
    onFileClick: (PdfFile) -> Unit,
    onFileOptionsClick: (PdfFile) -> Unit,
    onBrowseClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        when {
            isLoading && favorites.isEmpty() -> {
                LoadingState()
            }
            favorites.isEmpty() -> {
                EmptyFavoritesState(onBrowseClick = onBrowseClick)
            }
            viewMode == ViewMode.LIST -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(
                        items = favorites,
                        key = { it.id }
                    ) { file ->
                        PdfListItem(
                            pdfFile = file,
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
                    contentPadding = PaddingValues(16.dp)
                ) {
                    items(
                        items = favorites,
                        key = { it.id }
                    ) { file ->
                        PdfGridItem(
                            pdfFile = file,
                            onClick = { onFileClick(file) },
                            onOptionsClick = { onFileOptionsClick(file) }
                        )
                    }
                }
            }
        }
    }
}
