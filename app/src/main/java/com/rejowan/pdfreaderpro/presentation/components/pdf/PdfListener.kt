package com.bhuvaneshw.pdf

import android.net.Uri
import android.webkit.RenderProcessGoneDetail
import android.webkit.ValueCallback
import android.webkit.WebChromeClient.FileChooserParams
import androidx.annotation.ColorInt
import androidx.annotation.FloatRange
import androidx.annotation.IntRange
import com.bhuvaneshw.pdf.model.SideBarTreeItem

/**
 * Interface for listening to events from the PDF viewer.
 */
interface PdfListener {

    /**
     * Called when the PDF document starts loading.
     */
    fun onPageLoadStart() {}

    /**
     * Called when the PDF document has successfully loaded.
     * @param pagesCount The total number of pages in the document.
     */
    fun onPageLoadSuccess(pagesCount: Int) {}

    /**
     * Called when the PDF document fails to load.
     * @param exception The exception that caused the failure.
     */
    fun onPageLoadFailed(exception: Exception) {}

    /**
     * Called when a web view error occurs.
     * @param error The web view error.
     * @see WebViewError
     */
    fun onReceivedError(error: WebViewError) {}

    /**
     * Called to indicate the progress of page loading.
     * @param progress The progress value between 0.0 and 1.0.
     */
    fun onProgressChange(@FloatRange(0.0, 1.0) progress: Float) {}

    /**
     * Called when the current page changes.
     * @param pageNumber The new page number.
     */
    fun onPageChange(pageNumber: Int) {}

    /**
     * Called when a page is rendered..
     * @param pageNumber The page number.
     */
    fun onPageRendered(pageNumber: Int) {}

    /**
     * Called when the zoom scale of the PDF document changes.
     * @param scale The new scale factor.
     */
    fun onScaleChange(scale: Float) {}

    /**
     * Called to save the PDF document.
     * @param pdfAsBytes The byte array of the PDF document.
     */
    @Deprecated("This callback is deprecated. Use onDownload() instead.", ReplaceWith("onDownload(fileBytes, fileName, mimeType)"))
    fun onSavePdf(pdfAsBytes: ByteArray) {}

    /**
     * Called when download action is triggered.
     *
     * This callback is invoked when saving the PDF file itself as well as when saving embedded attachments.
     *
     * @param fileBytes The PDF/attachment file content as a `ByteArray`.
     * @param fileName The name of the file (e.g., "document.pdf"). This is often derived from the
     *                 source URL or content disposition headers and may be `null` if not available.
     * @param mimeType The MIME type of the file (e.g., "application/pdf"). This can be `null` if not
     *                 available.
     */
    fun onDownload(fileBytes: ByteArray, fileName: String?, mimeType: String?) {}

    /**
     * Called when a find operation starts.
     */
    fun onFindMatchStart() {}

    /**
     * Called when the current find match changes.
     * @param current The index of the current match.
     * @param total The total number of matches.
     */
    fun onFindMatchChange(current: Int, total: Int) {}

    /**
     * Called when a find operation is complete.
     * @param found `true` if any matches were found, `false` otherwise.
     */
    fun onFindMatchComplete(found: Boolean) {}

    /**
     * Called when the scroll position changes.
     * @param currentOffset The current scroll offset.
     * @param totalOffset The total scrollable offset.
     * @param isHorizontalScroll `true` if the scroll is horizontal, `false` otherwise.
     */
    fun onScrollChange(currentOffset: Int, totalOffset: Int, isHorizontalScroll: Boolean) {}

    /**
     * Called when the PDF document properties are loaded.
     * @param properties The properties of the PDF document.
     * @see PdfDocumentProperties
     */
    fun onLoadProperties(properties: PdfDocumentProperties) {}

    /**
     * Called when the state of the password dialog changes.
     * @param isOpen `true` if the dialog is open, `false` otherwise.
     */
    fun onPasswordDialogChange(isOpen: Boolean) {}

    /**
     * Called when the page scroll mode changes.
     * @param scrollMode The new page scroll mode.
     * @see PdfViewer.PageScrollMode
     */
    fun onScrollModeChange(scrollMode: PdfViewer.PageScrollMode) {}

    /**
     * Called when the page spread mode changes.
     * @param spreadMode The new page spread mode.
     * @see PdfViewer.PageSpreadMode
     */
    fun onSpreadModeChange(spreadMode: PdfViewer.PageSpreadMode) {}

    /**
     * Called when the page rotation changes.
     * @param rotation The new page rotation.
     * @see PdfViewer.PageRotation
     */
    fun onRotationChange(rotation: PdfViewer.PageRotation) {}

    /**
     * Called on a single tap on the view.
     */
    fun onSingleClick() {}

    /**
     * Called on a double tap on the view.
     */
    fun onDoubleClick() {}

    /**
     * Called on a long press on the view.
     */
    fun onLongClick() {}

    /**
     * Called when a link is clicked.
     * @param link The URL of the link.
     */
    fun onLinkClick(link: String) {}

    /**
     * Called when the snap-to-page setting changes.
     * @param snapPage `true` if snapping is enabled, `false` otherwise.
     */
    fun onSnapChange(snapPage: Boolean) {}

    /**
     * Called when the single page arrangement changes.
     * @param requestedArrangement The requested single page arrangement.
     * @param appliedArrangement The applied single page arrangement.
     */
    fun onSinglePageArrangementChange(requestedArrangement: Boolean, appliedArrangement: Boolean) {}

    /**
     * Called when the editor's highlight color changes.
     * @param highlightColor The new highlight color.
     */
    fun onEditorHighlightColorChange(@ColorInt highlightColor: Int) {}

    /**
     * Called when the editor's "show all highlights" setting changes.
     * @param showAll `true` to show all highlights, `false` otherwise.
     */
    fun onEditorShowAllHighlightsChange(showAll: Boolean) {}

    /**
     * Called when the editor's highlight thickness changes.
     * @param thickness The new highlight thickness, in range 8-24.
     */
    fun onEditorHighlightThicknessChange(@IntRange(from = 8, to = 24) thickness: Int) {}

    /**
     * Called when the editor's free font color changes.
     * @param fontColor The new free font color.
     */
    fun onEditorFreeFontColorChange(@ColorInt fontColor: Int) {}

    /**
     * Called when the editor's free font size changes.
     * @param fontSize The new free font size, in range 5-100.
     */
    fun onEditorFreeFontSizeChange(@IntRange(from = 5, to = 100) fontSize: Int) {}

    /**
     * Called when the editor's ink color changes.
     * @param color The new ink color.
     */
    fun onEditorInkColorChange(@ColorInt color: Int) {}

    /**
     * Called when the editor's ink thickness changes.
     * @param thickness The new ink thickness, in range 1-20.
     */
    fun onEditorInkThicknessChange(@IntRange(from = 1, to = 20) thickness: Int) {}

    /**
     * Called when the editor's ink opacity changes.
     * @param opacity The new ink opacity, in range 1-100.
     */
    fun onEditorInkOpacityChange(@IntRange(from = 1, to = 100) opacity: Int) {}

    /**
     * Called when the renderer process has gone.
     * @param detail The details of why the renderer process is gone.
     * @return `true` if the host application handled the situation, `false` otherwise.
     * @see RenderProcessGoneDetail
     */
    fun onRenderProcessGone(detail: RenderProcessGoneDetail?): Boolean = false

    /**
     * Called when the print process starts.
     */
    fun onPrintProcessStart() {}

    /**
     * Called to indicate the progress of the print process.
     * @param progress The progress of the print process, from 0.0 to 1.0.
     */
    fun onPrintProcessProgress(@FloatRange(0.0, 1.0) progress: Float) {}

    /**
     * Called when the print process ends.
     */
    fun onPrintProcessEnd() {}

    /**
     * Called when the print process is cancelled.
     */
    fun onPrintCancelled() {}

    /**
     * Called to show a message from the editor. Like 'Highlight removed'
     * @param message The message to show.
     */
    fun onShowEditorMessage(message: String) {}

    /**
     * Called for an annotation editor event.
     * @param type The type of the annotation editor event.
     * @see PdfEditor.AnnotationEventType
     */
    fun onAnnotationEditor(type: PdfEditor.AnnotationEventType) {}

    /**
     * Called when the editor mode state changes.
     * @param state The new editor mode state.
     * @see PdfEditor.EditorModeState
     */
    fun onEditorModeStateChange(state: PdfEditor.EditorModeState) {}

    /**
     * Called when the scale limits change.
     * @param minPageScale The minimum page scale, in range -4.0 to 10.0.
     * @param maxPageScale The maximum page scale, in range -4.0 to 10.0.
     * @param defaultPageScale The default page scale, in range -4.0 to 10.0.
     */
    fun onScaleLimitChange(
        @FloatRange(-4.0, 10.0) minPageScale: Float,
        @FloatRange(-4.0, 10.0) maxPageScale: Float,
        @FloatRange(-4.0, 10.0) defaultPageScale: Float
    ) {
    }

    /**
     * Called when the actual scale limits change.
     * @param minPageScale The minimum page scale, in range 0.0 to 10.0.
     * @param maxPageScale The maximum page scale, in range 0.0 to 10.0.
     * @param defaultPageScale The default page scale, in range 0.0 to 10.0.
     */
    fun onActualScaleLimitChange(
        @FloatRange(0.0, 10.0) minPageScale: Float,
        @FloatRange(0.0, 10.0) maxPageScale: Float,
        @FloatRange(0.0, 10.0) defaultPageScale: Float
    ) {
    }

    /**
     * Called when the page alignment mode changes.
     * @param requestedMode The requested page alignment mode.
     * @param appliedMode The applied page alignment mode.
     * @see PdfViewer.PageAlignMode
     */
    fun onAlignModeChange(
        requestedMode: PdfViewer.PageAlignMode,
        appliedMode: PdfViewer.PageAlignMode
    ) {
    }

    /**
     * Called when the scroll speed limit changes.
     * @param requestedLimit The requested scroll speed limit.
     * @param appliedLimit The applied scroll speed limit.
     * @see PdfViewer.ScrollSpeedLimit
     */
    fun onScrollSpeedLimitChange(
        requestedLimit: PdfViewer.ScrollSpeedLimit,
        appliedLimit: PdfViewer.ScrollSpeedLimit
    ) {
    }

    /**
     * Called to show a file chooser.
     * @param filePathCallback The callback to handle the file path.
     * @param fileChooserParams The parameters for the file chooser.
     * @return `true` if the file chooser was shown, `false` otherwise.
     * @see FileChooserParams
     */
    fun onShowFileChooser(
        filePathCallback: ValueCallback<Array<out Uri?>?>?,
        fileChooserParams: FileChooserParams?
    ): Boolean = false

    /**
     * Called when the document outline is loaded.
     * @param outline The list of outline items.
     * @see SideBarTreeItem
     */
    fun onLoadOutline(outline: List<SideBarTreeItem>) {}

    /**
     * Called when the document attachments are loaded.
     * @param attachments The list of attachment items.
     * @see SideBarTreeItem
     */
    fun onLoadAttachments(attachments: List<SideBarTreeItem>) {}

}
