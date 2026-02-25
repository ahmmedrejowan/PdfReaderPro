package com.rejowan.pdfreaderpro.presentation.screens.reader.components

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.ui.platform.LocalDensity
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
    var renderError by remember { mutableStateOf<String?>(null) }

    val density = LocalDensity.current

    BoxWithConstraints(
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
        // Convert constraints to pixels with density
        val maxWidthPx = with(density) { maxWidth.toPx().toInt() }
        val maxHeightPx = with(density) { maxHeight.toPx().toInt() }

        LaunchedEffect(pageIndex, colorMode, maxWidthPx, maxHeightPx) {
            if (maxWidthPx > 0 && maxHeightPx > 0) {
                isLoading = true
                renderError = null
                viewModel.renderPageHighRes(
                    pageIndex = pageIndex,
                    maxWidth = maxWidthPx,
                    maxHeight = maxHeightPx,
                    colorMode = colorMode
                ) { result ->
                    result.fold(
                        onSuccess = { renderedBitmap ->
                            bitmap = renderedBitmap
                            isLoading = false
                        },
                        onFailure = { error ->
                            renderError = error.message
                            isLoading = false
                        }
                    )
                }
            }
        }

        when {
            isLoading -> {
                CircularProgressIndicator(
                    modifier = Modifier.size(48.dp)
                )
            }
            renderError != null -> {
                androidx.compose.material3.Text(
                    text = "Failed to render page",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            bitmap != null -> {
                Image(
                    bitmap = bitmap!!.asImageBitmap(),
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
    }
}
