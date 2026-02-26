package com.bhuvaneshw.pdf.compose

import androidx.compose.ui.graphics.Color

/**
 * Default values for [PdfViewer] composable.
 */
object PdfViewerDefaults {

    /**
     * Defines the list of default highlight colors for the editor's highlight color palette.
     *
     * @see com.bhuvaneshw.pdf.PdfViewer.defaultHighlightEditorColors
     */
    val highlightEditorColors: List<Pair<String, Color>> = listOf(
        "yellow" to Color(0xFFFFFF98),
        "green" to Color(0xFF53FFBC),
        "blue" to Color(0xFF80EBFF),
        "pink" to Color(0xFFFFCBE6),
        "red" to Color(0xFFFF4F5F),
    )
}
