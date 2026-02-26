package com.bhuvaneshw.pdf.compose

import android.content.Context
import android.widget.FrameLayout
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.viewinterop.AndroidView
import com.bhuvaneshw.pdf.PdfViewer

/**
 * A composable for displaying PDF documents.
 *
 * This composable is a wrapper around the [com.bhuvaneshw.pdf.PdfViewer] view, which is built
 * upon `WebView` and leverages Mozilla's [PDF.js](https://mozilla.github.io/pdf.js/)
 * library to render PDF files. It provides a comprehensive set of features for pdf viewing and interaction,
 * including navigation, zooming, text searching, and editing capabilities like highlighting.
 *
 * ### Loading a PDF
 *
 * You can load a PDF from various sources by setting the `source` property of the `PdfState`.
 * The `source` can be one of the [PdfSource] types:
 * - `PdfSource.Asset("my_document.pdf")`
 * - `PdfSource.Uri(uri)` (e.g., from a file picker)
 * - `PdfSource.File(file)`
 * - `PdfSource.Url("https://example.com/document.pdf")`
 *
 * Example usage:
 * ```kotlin
 * @Composable
 * fun MyPdfScreen() {
 *     val pdfState = rememberPdfState(
 *         source = PdfSource.Asset("sample.pdf")
 *     )
 *
 *     PdfViewer(
 *         pdfState = pdfState,
 *         modifier = Modifier.fillMaxSize()
 *     )
 * }
 * ```
 *
 * @param pdfState The state of the PDF viewer.
 * @param modifier The modifier to be applied to the composable.
 * @param containerColor The background color of the PDF viewer.
 * @param factory A function that creates a [com.bhuvaneshw.pdf.PdfViewer].
 * @param onCreateViewer A callback that is invoked when the [com.bhuvaneshw.pdf.PdfViewer] is created.
 * @param onReady A callback that is invoked when the PDF viewer is ready to load a document.
 * [DefaultOnReadyCallback] is the default callback.
 *
 * @see com.bhuvaneshw.pdf.PdfViewer
 * @see PdfState
 * @see PdfSource
 * @see OnReadyCallback
 */
@Composable
fun PdfViewer(
    pdfState: PdfState,
    modifier: Modifier = Modifier,
    containerColor: Color? = null,
    factory: (context: Context) -> PdfViewer = { PdfViewer(context = it) },
    onCreateViewer: (PdfViewer.() -> Unit)? = null,
    onReady: OnReadyCallback = DefaultOnReadyCallback(),
) {
    LaunchedEffect(pdfState.source) {
        pdfState.pdfViewer?.run {
            if (isInitialized)
                load(source = pdfState.source)
        }
    }

    AndroidView(
        factory = { context ->
            factory(context).also {
                if (!it.isInEditMode) {
                    it.highlightEditorColors = pdfState.highlightEditorColors.map { colorPair ->
                        colorPair.first to colorPair.second.toArgb()
                    }
                    pdfState.setPdfViewerTo(it)
                    onCreateViewer?.invoke(it)
                    it.onReady {
                        it.editor.highlightColor = pdfState.defaultHighlightColor.toArgb()
                        onReady.onReady(this) { load(pdfState.source) }
                    }
                } else pdfState.loadingState = PdfLoadingState.Finished(3)

                it.layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
                )
                containerColor?.toArgb()?.let { color ->
                    it.setContainerBackgroundColor(color)
                }
            }
        },
        onRelease = {
            it.clearAllListeners()
            pdfState.clearPdfViewer()
        },
        onReset = {
            it.clearAllListeners()
        },
        update = {
            containerColor?.toArgb()?.let { color ->
                it.setContainerBackgroundColor(color)
            }
        },
        modifier = modifier
    )
}
