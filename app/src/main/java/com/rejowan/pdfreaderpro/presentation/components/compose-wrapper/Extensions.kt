@file:Suppress("unused")

package com.bhuvaneshw.pdf.compose

import android.net.Uri
import android.webkit.RenderProcessGoneDetail
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import com.bhuvaneshw.pdf.PdfDocumentProperties
import com.bhuvaneshw.pdf.PdfEditor
import com.bhuvaneshw.pdf.PdfListener
import com.bhuvaneshw.pdf.PdfViewer
import com.bhuvaneshw.pdf.WebViewError
import com.bhuvaneshw.pdf.model.SideBarTreeItem
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * A flow that emits the current loading state of the PDF document.
 *
 * @return A flow of [PdfLoadingState].
 * @see PdfListener.onPageLoadStart
 * @see PdfListener.onProgressChange
 * @see PdfListener.onPageLoadSuccess
 * @see PdfListener.onPageLoadFailed
 */
fun PdfState.loadingStateFlow(): Flow<PdfLoadingState> = flowIt { emit ->
    object : PdfListener {
        override fun onPageLoadStart() {
            emit(PdfLoadingState.Initializing)
        }

        override fun onProgressChange(progress: Float) {
            emit(PdfLoadingState.Loading(progress))
        }

        override fun onPageLoadSuccess(pagesCount: Int) {
            emit(PdfLoadingState.Finished(pagesCount))
        }

        override fun onPageLoadFailed(exception: Exception) {
            emit(PdfLoadingState.Error(exception))
        }
    }
}

/**
 * A flow that emits any errors that occur in the WebView.
 *
 * @return A flow of [WebViewError].
 * @see PdfListener.onReceivedError
 */
fun PdfState.webViewErrorFlow(): Flow<WebViewError> = flowIt { emit ->
    object : PdfListener {
        override fun onReceivedError(error: WebViewError) {
            emit(error)
        }
    }
}

/**
 * A flow that emits the current page number of the PDF document.
 *
 * @return A flow of the current page number.
 * @see PdfListener.onPageChange
 */
fun PdfState.pageNumberFlow(): Flow<Int> = flowIt { emit ->
    object : PdfListener {
        override fun onPageChange(pageNumber: Int) {
            emit(pageNumber)
        }
    }
}

/**
 * A flow that emits the page number when a page is rendered.
 *
 * @return A flow of the rendered page number.
 * @see PdfListener.onPageRendered
 */
fun PdfState.pageRenderedFlow(): Flow<Int> = flowIt { emit ->
    object : PdfListener {
        override fun onPageRendered(pageNumber: Int) {
            emit(pageNumber)
        }
    }
}

/**
 * A flow that emits the current scale of the PDF document.
 *
 * @return A flow of the current scale.
 * @see PdfListener.onScaleChange
 */
fun PdfState.scaleFlow(): Flow<Float> = flowIt { emit ->
    object : PdfListener {
        override fun onScaleChange(scale: Float) {
            emit(scale)
        }
    }
}

/**
 * A flow that emits the PDF document to be saved as a byte array.
 *
 * This function corresponds to the legacy save behavior, where only the raw PDF bytes
 * were emitted without any filename or MIME type information. It is now deprecated in
 * favor of [downloadFlow], which provides a richer data structure for downloads.
 *
 * @return A flow of the PDF as a byte array.
 * @see PdfListener.onSavePdf
 * @deprecated Use [downloadFlow] instead, which emits the file bytes along with filename and MIME type.
 */
@Deprecated("This flow is deprecated. Use downloadFlow() instead.", ReplaceWith("downloadFlow()"))
fun PdfState.savePdfFlow(): Flow<ByteArray> = flowIt { emit ->
    object : PdfListener {
        @Suppress("OVERRIDE_DEPRECATION")
        override fun onSavePdf(pdfAsBytes: ByteArray) {
            emit(pdfAsBytes)
        }
    }
}

/**
 * A flow that emits information about a file download triggered from the PDF viewer.
 *
 * @return A flow emitting a [Triple] containing `(fileBytes, fileName, mimeType)`.
 * @see PdfListener.onDownload
 */
fun PdfState.downloadFlow(): Flow<Triple<ByteArray, String?, String?>> = flowIt { emit ->
    object : PdfListener {
        override fun onDownload(fileBytes: ByteArray, fileName: String?, mimeType: String?) {
            emit(Triple(fileBytes, fileName, mimeType))
        }
    }
}

/**
 * A flow that emits the state of the find-in-page functionality.
 *
 * @return A flow of [MatchState].
 * @see PdfListener.onFindMatchStart
 * @see PdfListener.onFindMatchChange
 * @see PdfListener.onFindMatchComplete
 */
fun PdfState.matchStateFlow(): Flow<MatchState> = flowIt { emit ->
    object : PdfListener {
        private var current = 0
        private var total = 0

        override fun onFindMatchStart() {
            emit(MatchState.Started())
        }

        override fun onFindMatchChange(current: Int, total: Int) {
            this.current = current
            this.total = total
            emit(MatchState.Progress(current, total))
        }

        override fun onFindMatchComplete(found: Boolean) {
            emit(MatchState.Completed(found, current, total))
        }
    }
}

/**
 * A flow that emits the current scroll state of the PDF document.
 *
 * @return A flow of [ScrollState].
 * @see PdfListener.onScrollChange
 */
fun PdfState.scrollStateFlow(): Flow<ScrollState> = flowIt { emit ->
    object : PdfListener {
        override fun onScrollChange(
            currentOffset: Int,
            totalOffset: Int,
            isHorizontalScroll: Boolean
        ) {
            emit(ScrollState(currentOffset, totalOffset, isHorizontalScroll))
        }
    }
}

/**
 * A flow that emits the properties of the PDF document.
 *
 * @return A flow of [PdfDocumentProperties].
 * @see PdfListener.onLoadProperties
 */
fun PdfState.propertiesFlow(): Flow<PdfDocumentProperties> = flowIt { emit ->
    object : PdfListener {
        override fun onLoadProperties(properties: PdfDocumentProperties) {
            emit(properties)
        }
    }
}

/**
 * A flow that emits whether the password dialog is open.
 *
 * @return A flow of whether the password dialog is open.
 * @see PdfListener.onPasswordDialogChange
 */
fun PdfState.passwordDialogFlow(): Flow<Boolean> = flowIt { emit ->
    object : PdfListener {
        override fun onPasswordDialogChange(isOpen: Boolean) {
            emit(isOpen)
        }
    }
}

/**
 * A flow that emits the current scroll mode of the PDF document.
 *
 * @return A flow of [PdfViewer.PageScrollMode].
 * @see PdfListener.onScrollModeChange
 */
fun PdfState.scrollModeFlow(): Flow<PdfViewer.PageScrollMode> = flowIt { emit ->
    object : PdfListener {
        override fun onScrollModeChange(scrollMode: PdfViewer.PageScrollMode) {
            emit(scrollMode)
        }
    }
}

/**
 * A flow that emits the current spread mode of the PDF document.
 *
 * @return A flow of [PdfViewer.PageSpreadMode].
 * @see PdfListener.onSpreadModeChange
 */
fun PdfState.spreadModeFlow(): Flow<PdfViewer.PageSpreadMode> = flowIt { emit ->
    object : PdfListener {
        override fun onSpreadModeChange(spreadMode: PdfViewer.PageSpreadMode) {
            emit(spreadMode)
        }
    }
}

/**
 * A flow that emits the current rotation of the PDF document.
 *
 * @return A flow of [PdfViewer.PageRotation].
 * @see PdfListener.onRotationChange
 */
fun PdfState.rotationFlow(): Flow<PdfViewer.PageRotation> = flowIt { emit ->
    object : PdfListener {
        override fun onRotationChange(rotation: PdfViewer.PageRotation) {
            emit(rotation)
        }
    }
}

/**
 * A flow that emits when a single click occurs on the PDF document.
 *
 * @return A flow of [Unit].
 * @see PdfListener.onSingleClick
 */
fun PdfState.singleClickFlow(): Flow<Unit> = flowIt { emit ->
    object : PdfListener {
        override fun onSingleClick() {
            emit(Unit)
        }
    }
}

/**
 * A flow that emits when a double click occurs on the PDF document.
 *
 * @return A flow of [Unit].
 * @see PdfListener.onDoubleClick
 */
fun PdfState.doubleClickFlow(): Flow<Unit> = flowIt { emit ->
    object : PdfListener {
        override fun onDoubleClick() {
            emit(Unit)
        }
    }
}

/**
 * A flow that emits when a long click occurs on the PDF document.
 *
 * @return A flow of [Unit].
 * @see PdfListener.onLongClick
 */
fun PdfState.longClickFlow(): Flow<Unit> = flowIt { emit ->
    object : PdfListener {
        override fun onLongClick() {
            emit(Unit)
        }
    }
}

/**
 * A flow that emits the URL of a clicked link in the PDF document.
 *
 * @return A flow of the clicked link URL.
 * @see PdfListener.onLinkClick
 */
fun PdfState.linkClickFlow(): Flow<String> = flowIt { emit ->
    object : PdfListener {
        override fun onLinkClick(link: String) {
            emit(link)
        }
    }
}

/**
 * A flow that emits whether page snapping is enabled.
 *
 * @return A flow of whether page snapping is enabled.
 * @see PdfListener.onSnapChange
 */
fun PdfState.snapFlow(): Flow<Boolean> = flowIt { emit ->
    object : PdfListener {
        override fun onSnapChange(snapPage: Boolean) {
            emit(snapPage)
        }
    }
}

/**
 * A flow that emits the requested and applied single-page arrangement.
 *
 * @return A flow of a pair of booleans, where the first is the requested arrangement and the second is the applied arrangement.
 * @see PdfListener.onSinglePageArrangementChange
 */
fun PdfState.singlePageArrangementFlow(): Flow<Pair<Boolean, Boolean>> = flowIt { emit ->
    object : PdfListener {
        override fun onSinglePageArrangementChange(
            requestedArrangement: Boolean,
            appliedArrangement: Boolean
        ) {
            emit(requestedArrangement to appliedArrangement)
        }
    }
}

/**
 * A flow that emits the current editor highlight color.
 *
 * @return A flow of the editor highlight color.
 * @see PdfListener.onEditorHighlightColorChange
 */
fun PdfState.editorHighlightColorFlow(): Flow<Int> = flowIt { emit ->
    object : PdfListener {
        override fun onEditorHighlightColorChange(highlightColor: Int) {
            emit(highlightColor)
        }
    }
}

/**
 * A flow that emits whether all highlights are shown in the editor.
 *
 * @return A flow of whether all highlights are shown.
 * @see PdfListener.onEditorShowAllHighlightsChange
 */
fun PdfState.editorShowAllHighlightsFlow(): Flow<Boolean> = flowIt { emit ->
    object : PdfListener {
        override fun onEditorShowAllHighlightsChange(showAll: Boolean) {
            emit(showAll)
        }
    }
}

/**
 * A flow that emits the current editor highlight thickness.
 *
 * @return A flow of the editor highlight thickness.
 * @see PdfListener.onEditorHighlightThicknessChange
 */
fun PdfState.editorHighlightThicknessFlow(): Flow<Int> = flowIt { emit ->
    object : PdfListener {
        override fun onEditorHighlightThicknessChange(thickness: Int) {
            emit(thickness)
        }
    }
}

/**
 * A flow that emits the current editor free-font color.
 *
 * @return A flow of the editor free-font color.
 * @see PdfListener.onEditorFreeFontColorChange
 */
fun PdfState.editorFreeFontColorFlow(): Flow<Int> = flowIt { emit ->
    object : PdfListener {
        override fun onEditorFreeFontColorChange(fontColor: Int) {
            emit(fontColor)
        }
    }
}

/**
 * A flow that emits the current editor free-font size.
 *
 * @return A flow of the editor free-font size.
 * @see PdfListener.onEditorFreeFontSizeChange
 */
fun PdfState.editorFreeFontSizeFlow(): Flow<Int> = flowIt { emit ->
    object : PdfListener {
        override fun onEditorFreeFontSizeChange(fontSize: Int) {
            emit(fontSize)
        }
    }
}

/**
 * A flow that emits the current editor ink color.
 *
 * @return A flow of the editor ink color.
 * @see PdfListener.onEditorInkColorChange
 */
fun PdfState.editorInkColorFlow(): Flow<Int> = flowIt { emit ->
    object : PdfListener {
        override fun onEditorInkColorChange(color: Int) {
            emit(color)
        }
    }
}

/**
 * A flow that emits the current editor ink thickness.
 *
 * @return A flow of the editor ink thickness.
 * @see PdfListener.onEditorInkThicknessChange
 */
fun PdfState.editorInkThicknessFlow(): Flow<Int> = flowIt { emit ->
    object : PdfListener {
        override fun onEditorInkThicknessChange(thickness: Int) {
            emit(thickness)
        }
    }
}

/**
 * A flow that emits the current editor ink opacity.
 *
 * @return A flow of the editor ink opacity.
 * @see PdfListener.onEditorInkOpacityChange
 */
fun PdfState.editorInkOpacityFlow(): Flow<Int> = flowIt { emit ->
    object : PdfListener {
        override fun onEditorInkOpacityChange(opacity: Int) {
            emit(opacity)
        }
    }
}

/**
 * A flow that emits when the render process has gone.
 *
 * @param handled A function that returns whether the event was handled.
 * @return A flow of the [RenderProcessGoneDetail].
 * @see PdfListener.onRenderProcessGone
 */
fun PdfState.renderProcessGoneFlow(handled: () -> Boolean = { true }): Flow<RenderProcessGoneDetail?> =
    flowIt { emit ->
        object : PdfListener {
            override fun onRenderProcessGone(detail: RenderProcessGoneDetail?): Boolean {
                emit(detail)
                return handled()
            }
        }
    }

/**
 * A flow that emits the current state of the print process.
 *
 * @return A flow of [PdfPrintState].
 * @see PdfListener.onPrintProcessStart
 * @see PdfListener.onPrintProcessProgress
 * @see PdfListener.onPrintProcessEnd
 * @see PdfListener.onPrintCancelled
 */
fun PdfState.printStateFlow(): Flow<PdfPrintState> = flowIt { emit ->
    object : PdfListener {
        override fun onPrintProcessStart() {
            emit(PdfPrintState.Starting)
        }

        override fun onPrintProcessProgress(progress: Float) {
            emit(PdfPrintState.Loading(progress))
        }

        override fun onPrintProcessEnd() {
            emit(PdfPrintState.Completed)
        }

        override fun onPrintCancelled() {
            emit(PdfPrintState.Cancelled)
        }
    }
}

/**
 * A flow that emits messages from the editor.
 *
 * @return A flow of editor messages.
 * @see PdfListener.onShowEditorMessage
 */
fun PdfState.editorMessageFlow(): Flow<String> = flowIt { emit ->
    object : PdfListener {
        override fun onShowEditorMessage(message: String) {
            emit(message)
        }
    }
}

/**
 * A flow that emits events from the annotation editor.
 *
 * @return A flow of [PdfEditor.AnnotationEventType].
 * @see PdfListener.onAnnotationEditor
 */
fun PdfState.annotationEditorFlow(): Flow<PdfEditor.AnnotationEventType> = flowIt { emit ->
    object : PdfListener {
        override fun onAnnotationEditor(type: PdfEditor.AnnotationEventType) {
            emit(type)
        }
    }
}

/**
 * A flow that emits the current state of the editor mode.
 *
 * @return A flow of [PdfEditor.EditorModeState].
 * @see PdfListener.onEditorModeStateChange
 */
fun PdfState.editorModeStateFlow(): Flow<PdfEditor.EditorModeState> = flowIt { emit ->
    object : PdfListener {
        override fun onEditorModeStateChange(state: PdfEditor.EditorModeState) {
            emit(state)
        }
    }
}

/**
 * A flow that emits the current scale limits of the PDF document.
 *
 * @return A flow of a triple containing the minimum, maximum, and default page scale.
 * @see PdfListener.onScaleLimitChange
 */
fun PdfState.scaleLimitFlow(): Flow<Triple<Float, Float, Float>> = flowIt { emit ->
    object : PdfListener {
        override fun onScaleLimitChange(
            minPageScale: Float,
            maxPageScale: Float,
            defaultPageScale: Float
        ) {
            emit(Triple(minPageScale, maxPageScale, defaultPageScale))
        }
    }
}

/**
 * A flow that emits the actual scale limits of the PDF document.
 *
 * @return A flow of a triple containing the minimum, maximum, and default page scale.
 * @see PdfListener.onActualScaleLimitChange
 */
fun PdfState.actualScaleLimitFlow(): Flow<Triple<Float, Float, Float>> = flowIt { emit ->
    object : PdfListener {
        override fun onActualScaleLimitChange(
            minPageScale: Float,
            maxPageScale: Float,
            defaultPageScale: Float
        ) {
            emit(Triple(minPageScale, maxPageScale, defaultPageScale))
        }
    }
}

/**
 * A flow that emits the requested and applied page alignment modes.
 *
 * @return A flow of a pair of [PdfViewer.PageAlignMode], where the first is the requested mode and the second is the applied mode.
 * @see PdfListener.onAlignModeChange
 */
fun PdfState.alignModeFlow(): Flow<Pair<PdfViewer.PageAlignMode, PdfViewer.PageAlignMode>> =
    flowIt { emit ->
        object : PdfListener {
            override fun onAlignModeChange(
                requestedMode: PdfViewer.PageAlignMode,
                appliedMode: PdfViewer.PageAlignMode
            ) {
                emit(requestedMode to appliedMode)
            }
        }
    }

/**
 * A flow that emits the requested and applied scroll speed limits.
 *
 * @return A flow of a pair of [PdfViewer.ScrollSpeedLimit], where the first is the requested limit and the second is the applied limit.
 * @see PdfListener.onScrollSpeedLimitChange
 */
fun PdfState.scrollSpeedLimitFlow(): Flow<Pair<PdfViewer.ScrollSpeedLimit, PdfViewer.ScrollSpeedLimit>> =
    flowIt { emit ->
        object : PdfListener {
            override fun onScrollSpeedLimitChange(
                requestedLimit: PdfViewer.ScrollSpeedLimit,
                appliedLimit: PdfViewer.ScrollSpeedLimit
            ) {
                emit(requestedLimit to appliedLimit)
            }
        }
    }

/**
 * A flow that emits when the file chooser should be shown.
 *
 * @param handled A function that returns whether the event was handled.
 * @return A flow of a pair containing the file path callback and the file chooser parameters.
 * @see PdfListener.onShowFileChooser
 */
fun PdfState.showFileChooserFlow(handled: () -> Boolean): Flow<Pair<ValueCallback<Array<out Uri?>?>?, WebChromeClient.FileChooserParams?>> =
    flowIt { emit ->
        object : PdfListener {
            override fun onShowFileChooser(
                filePathCallback: ValueCallback<Array<out Uri?>?>?,
                fileChooserParams: WebChromeClient.FileChooserParams?
            ): Boolean {
                emit(filePathCallback to fileChooserParams)
                return handled()
            }
        }
    }

/**
 * A flow that emits the document outline.
 *
 * @return A flow of the document outline.
 * @see PdfListener.onLoadOutline
 */
fun PdfState.outlineFlow(): Flow<List<SideBarTreeItem>> = flowIt { emit ->
    object : PdfListener {
        override fun onLoadOutline(outline: List<SideBarTreeItem>) {
            emit(outline)
        }
    }
}

/**
 * A flow that emits the document attachments.
 *
 * @return A flow of the document attachments.
 * @see PdfListener.onLoadAttachments
 */
fun PdfState.attachmentsFlow(): Flow<List<SideBarTreeItem>> = flowIt { emit ->
    object : PdfListener {
        override fun onLoadAttachments(attachments: List<SideBarTreeItem>) {
            emit(attachments)
        }
    }
}

internal fun PdfViewer.load(source: PdfSource) {
    when (source) {
        is PdfSource.Asset -> loadFromAsset(source.assetPath)
        is PdfSource.ContentUri -> loadFromContentUri(source.contentUri)
        is PdfSource.File -> loadFromFile(source.file)
        is PdfSource.Plain -> load(source.source)
        is PdfSource.Url -> loadFromUrl(source.url)
    }
}

private inline fun <T> PdfState.flowIt(
    crossinline createListener: ((T) -> Unit) -> PdfListener
): Flow<T> = callbackFlow {
    val listener = createListener { value -> trySend(value).isSuccess }
    pdfViewer?.addListener(listener)
        ?: onReady.add {
            it.addListener(listener)
        }

    awaitClose { pdfViewer?.removeListener(listener) }
}
