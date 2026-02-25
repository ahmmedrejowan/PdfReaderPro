package com.rejowan.pdfreaderpro.presentation.screens.reader.components

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.rejowan.pdfreaderpro.data.pdf.ColorMode
import com.rejowan.pdfreaderpro.data.pdf.SearchResult
import com.rejowan.pdfreaderpro.presentation.screens.reader.ReaderViewModel

@Composable
fun PdfPageView(
    pageIndex: Int,
    viewModel: ReaderViewModel,
    colorMode: ColorMode,
    zoom: Float,
    searchResults: List<SearchResult>,
    modifier: Modifier = Modifier
) {
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(pageIndex, colorMode) {
        isLoading = true
        viewModel.renderThumbnail(pageIndex) { renderedBitmap ->
            bitmap = renderedBitmap
            isLoading = false
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                when (colorMode) {
                    ColorMode.NORMAL -> MaterialTheme.colorScheme.surface
                    ColorMode.DARK -> MaterialTheme.colorScheme.inverseSurface
                    ColorMode.SEPIA -> MaterialTheme.colorScheme.surfaceVariant
                    ColorMode.INVERTED -> MaterialTheme.colorScheme.inverseSurface
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp)
            )
        } else {
            bitmap?.let { bmp ->
                Image(
                    bitmap = bmp.asImageBitmap(),
                    contentDescription = "Page ${pageIndex + 1}",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            scaleX = zoom
                            scaleY = zoom
                        }
                )
            }
        }

        // Search result highlights would be drawn here
        // This is a simplified version - full implementation would use Canvas
    }
}
