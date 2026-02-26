@file:Suppress("unused")

package com.bhuvaneshw.pdf.compose

import androidx.annotation.ColorInt
import androidx.annotation.FloatRange
import androidx.annotation.IntRange
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import com.bhuvaneshw.pdf.PdfDocumentProperties
import com.bhuvaneshw.pdf.PdfListener
import com.bhuvaneshw.pdf.PdfUnstableApi
import com.bhuvaneshw.pdf.PdfViewer
import com.bhuvaneshw.pdf.WebViewError

/**
 * Creates a [PdfState] that is remembered across compositions.
 *
 * @param source The source of the PDF file.
 * @param highlightEditorColors The colors to be used for highlighting text.
 * @param defaultHighlightColor The default color to be used for highlighting text.
 * @return A [PdfState] instance.
 * @see com.bhuvaneshw.pdf.compose.PdfViewer
 */
@Composable
fun rememberPdfState(
    source: String,
    highlightEditorColors: List<Pair<String, Color>> = PdfViewerDefaults.highlightEditorColors,
    defaultHighlightColor: Color = highlightEditorColors.firstOrNull()?.second
        ?: PdfViewerDefaults.highlightEditorColors[0].second,
): PdfState = rememberPdfState(
    source = PdfSource.Plain(source),
    highlightEditorColors = highlightEditorColors,
    defaultHighlightColor = defaultHighlightColor,
)

/**
 * Creates a [PdfState] that is remembered across compositions.
 *
 * @param source The source of the PDF file.
 * @param highlightEditorColors The colors to be used for highlighting text.
 * @param defaultHighlightColor The default color to be used for highlighting text.
 * @return A [PdfState] instance.
 * @see com.bhuvaneshw.pdf.compose.PdfViewer
 * @see PdfSource
 */
@Composable
fun rememberPdfState(
    source: PdfSource,
    highlightEditorColors: List<Pair<String, Color>> = PdfViewerDefaults.highlightEditorColors,
    defaultHighlightColor: Color = highlightEditorColors.firstOrNull()?.second
        ?: PdfViewerDefaults.highlightEditorColors[0].second,
): PdfState = remember {
    PdfState(
        source = source,
        highlightEditorColors = highlightEditorColors,
        defaultHighlightColor = defaultHighlightColor
    )
}

/**
 * A state object that can be hoisted to control and observe the [PdfViewer].
 *
 * @param source The source of the PDF file.
 * @param highlightEditorColors The colors to be used for highlighting text.
 * @param defaultHighlightColor The default color to be used for highlighting text.
 * @see com.bhuvaneshw.pdf.compose.PdfViewer
 * @see com.bhuvaneshw.pdf.PdfViewer
 */
class PdfState(
    source: PdfSource,
    val highlightEditorColors: List<Pair<String, Color>> = PdfViewerDefaults.highlightEditorColors,
    val defaultHighlightColor: Color = highlightEditorColors.firstOrNull()?.second
        ?: PdfViewerDefaults.highlightEditorColors[0].second,
) {
    /**
     * The source of the PDF file.
     * @see PdfSource
     */
    var source by mutableStateOf(source); internal set

    /**
     * The underlying [PdfViewer] instance. It is `null` until the composable is composed.
     */
    var pdfViewer: PdfViewer? by mutableStateOf(null); internal set

    /**
     * The current loading state of the PDF.
     * @see PdfLoadingState
     */
    var loadingState: PdfLoadingState by mutableStateOf(PdfLoadingState.Initializing); internal set

    /**
     * The last WebView error that occurred.
     */
    var webViewError: WebViewError? by mutableStateOf(null); internal set

    /**
     * The total number of pages in the PDF.
     * @see currentPage
     */
    var pagesCount by mutableIntStateOf(0); internal set

    /**
     * The current page number of the PDF.
     * @see pagesCount
     */
    var currentPage by mutableIntStateOf(0); internal set

    /**
     * The current scale of the PDF.
     */
    var currentScale by mutableFloatStateOf(0f); internal set

    /**
     * The properties of the PDF document.
     * @see PdfDocumentProperties
     */
    var properties by mutableStateOf<PdfDocumentProperties?>(null); internal set

    /**
     * `true` if the PDF document requires a password to open.
     */
    var passwordRequired by mutableStateOf(false); internal set

    /**
     * The current scroll state of the PDF.
     * @see ScrollState
     */
    var scrollState by mutableStateOf(ScrollState()); internal set

    /**
     * The current find match state of the PDF.
     * @see MatchState
     */
    var matchState: MatchState by mutableStateOf(MatchState.Initialized()); internal set

    /**
     * The scroll mode of the PDF.
     * @see PdfViewer.PageScrollMode
     */
    var scrollMode by mutableStateOf(PdfViewer.PageScrollMode.SINGLE_PAGE); internal set

    /**
     * The spread mode of the PDF.
     * @see PdfViewer.PageSpreadMode
     */
    var spreadMode by mutableStateOf(PdfViewer.PageSpreadMode.NONE); internal set

    /**
     * The rotation of the PDF.
     * @see PdfViewer.PageRotation
     */
    var rotation by mutableStateOf(PdfViewer.PageRotation.R_0); internal set

    /**
     * The scale limits of the PDF.
     * @see ScaleLimit
     */
    var scaleLimit by mutableStateOf(ScaleLimit()); internal set

    /**
     * The actual scale limits of the PDF.
     * @see ActualScaleLimit
     */
    var actualScaleLimit by mutableStateOf(ActualScaleLimit()); internal set

    /**
     * `true` if the page should snap to the nearest page when scrolling.
     */
    var snapPage by mutableStateOf(false); internal set

    /**
     * `true` if the pages are arranged in a single page layout.
     */
    var singlePageArrangement by mutableStateOf(false); internal set

    /**
     * The alignment of the pages.
     * @see PdfViewer.PageAlignMode
     */
    var alignMode by mutableStateOf(PdfViewer.PageAlignMode.DEFAULT); internal set

    /**
     * The current print state of the PDF.
     * @see PdfPrintState
     */
    var printState: PdfPrintState by mutableStateOf(PdfPrintState.Initial); internal set

    /**
     * The scroll speed limit of the PDF.
     */
    @PdfUnstableApi
    var scrollSpeedLimit: PdfViewer.ScrollSpeedLimit by mutableStateOf(PdfViewer.ScrollSpeedLimit.None); internal set

    /**
     * The editor for interacting with the PDF.
     * @see Editor
     */
    val editor = Editor()

    /**
     * The editor for interacting with the PDF.
     * @see com.bhuvaneshw.pdf.PdfEditor
     */
    inner class Editor internal constructor() {
        /**
         * The color to be used for highlighting text.
         */
        var highlightColor by mutableStateOf(
            highlightEditorColors.firstOrNull()?.second
                ?: PdfViewerDefaults.highlightEditorColors.first().second
        )
            internal set

        /**
         * The thickness of the highlight.
         */
        var highlightThickness by mutableIntStateOf(12); internal set

        /**
         * `true` if all highlights should be shown.
         */
        var showAllHighlights by mutableStateOf(true); internal set

        /**
         * The color of the free text.
         */
        var freeFontColor by mutableStateOf(Color.Black); internal set

        /**
         * The font size of the free text.
         */
        var freeFontSize by mutableIntStateOf(10); internal set

        /**
         * The thickness of the ink.
         */
        var inkThickness by mutableIntStateOf(1); internal set

        /**
         * The color of the ink.
         */
        var inkColor by mutableStateOf(Color.Black); internal set

        /**
         * The opacity of the ink.
         */
        var inkOpacity by mutableIntStateOf(100); internal set
    }

    internal val onReady = mutableListOf<(PdfViewer) -> Unit>()

    /**
     * Clears the find results.
     */
    fun clearFind() {
        matchState = MatchState.Initialized()
    }

    internal fun setPdfViewerTo(viewer: PdfViewer) {
        if (pdfViewer == viewer) return

        this.pdfViewer = viewer
        viewer.addListener(Listener())
        viewer.onReady { this@PdfState.loadingState = PdfLoadingState.Loading(0f) }

        if (viewer.isInitialized) {
            if (viewer.pagesCount == 0)
                this@PdfState.loadingState = PdfLoadingState.Loading(0f)
            else this@PdfState.loadingState = PdfLoadingState.Finished(viewer.pagesCount)
        } else this@PdfState.loadingState = PdfLoadingState.Initializing

        pagesCount = viewer.pagesCount
        currentPage = viewer.currentPage
        currentScale = viewer.currentPageScale
        properties = viewer.properties
        passwordRequired = false
        scrollState = ScrollState()
        matchState = MatchState.Initialized()
        scrollMode = viewer.pageScrollMode
        spreadMode = viewer.pageSpreadMode
        rotation = viewer.pageRotation
        scaleLimit = ScaleLimit(
            viewer.minPageScale,
            viewer.maxPageScale,
            viewer.defaultPageScale
        )
        actualScaleLimit = ActualScaleLimit(
            viewer.actualMinPageScale,
            viewer.actualMaxPageScale,
            viewer.actualDefaultPageScale
        )
        snapPage = viewer.snapPage
        singlePageArrangement = viewer.singlePageArrangement
        alignMode = viewer.pageAlignMode

        @OptIn(PdfUnstableApi::class)
        scrollSpeedLimit = viewer.scrollSpeedLimit
        editor.highlightColor = Color(viewer.editor.highlightColor)
        editor.highlightThickness = viewer.editor.highlightThickness
        editor.showAllHighlights = viewer.editor.showAllHighlights
        editor.freeFontColor = Color(viewer.editor.freeFontColor)
        editor.freeFontSize = viewer.editor.freeFontSize
        editor.inkThickness = viewer.editor.inkThickness
        editor.inkColor = Color(viewer.editor.inkColor)
        editor.inkOpacity = viewer.editor.inkOpacity

        onReady.forEach { it(viewer) }
    }

    internal fun clearPdfViewer() {
        pdfViewer = null
    }

    inner class Listener internal constructor() : PdfListener {
        override fun onPageLoadStart() {
            this@PdfState.loadingState = PdfLoadingState.Loading(0f)
        }

        override fun onPageLoadSuccess(pagesCount: Int) {
            this@PdfState.loadingState = PdfLoadingState.Finished(pagesCount)
            this@PdfState.pagesCount = pagesCount
            this@PdfState.currentPage = 1
        }

        override fun onPageLoadFailed(exception: Exception) {
            this@PdfState.loadingState = PdfLoadingState.Error(exception)
        }

        override fun onReceivedError(error: WebViewError) {
            this@PdfState.webViewError = error
        }

        override fun onProgressChange(progress: Float) {
            this@PdfState.loadingState = PdfLoadingState.Loading(progress)
        }

        override fun onPageChange(pageNumber: Int) {
            this@PdfState.currentPage = pageNumber
        }

        override fun onScaleChange(scale: Float) {
            this@PdfState.currentScale = scale
        }

        override fun onFindMatchStart() {
            this@PdfState.matchState = MatchState.Started()
        }

        override fun onFindMatchChange(current: Int, total: Int) {
            this@PdfState.matchState = MatchState.Progress(current, total)
        }

        override fun onFindMatchComplete(found: Boolean) {
            this@PdfState.matchState =
                MatchState.Completed(found, matchState.current, matchState.total)
        }

        override fun onScrollChange(
            currentOffset: Int,
            totalOffset: Int,
            isHorizontalScroll: Boolean
        ) {
            this@PdfState.scrollState = ScrollState(currentOffset, totalOffset, isHorizontalScroll)
        }

        override fun onLoadProperties(properties: PdfDocumentProperties) {
            this@PdfState.properties = properties
        }

        override fun onPasswordDialogChange(isOpen: Boolean) {
            this@PdfState.passwordRequired = isOpen
        }

        override fun onScrollModeChange(scrollMode: PdfViewer.PageScrollMode) {
            this@PdfState.scrollMode = scrollMode
        }

        override fun onSpreadModeChange(spreadMode: PdfViewer.PageSpreadMode) {
            this@PdfState.spreadMode = spreadMode
        }

        override fun onRotationChange(rotation: PdfViewer.PageRotation) {
            this@PdfState.rotation = rotation
        }

        override fun onScaleLimitChange(
            minPageScale: Float,
            maxPageScale: Float,
            defaultPageScale: Float
        ) {
            this@PdfState.scaleLimit = ScaleLimit(minPageScale, maxPageScale, defaultPageScale)
        }

        override fun onActualScaleLimitChange(
            minPageScale: Float,
            maxPageScale: Float,
            defaultPageScale: Float
        ) {
            this@PdfState.actualScaleLimit =
                ActualScaleLimit(minPageScale, maxPageScale, defaultPageScale)
        }

        override fun onSnapChange(snapPage: Boolean) {
            this@PdfState.snapPage = snapPage
        }

        override fun onSinglePageArrangementChange(
            requestedArrangement: Boolean,
            appliedArrangement: Boolean
        ) {
            this@PdfState.singlePageArrangement = appliedArrangement
        }

        override fun onAlignModeChange(
            requestedMode: PdfViewer.PageAlignMode,
            appliedMode: PdfViewer.PageAlignMode
        ) {
            this@PdfState.alignMode = appliedMode
        }

        override fun onScrollSpeedLimitChange(
            requestedLimit: PdfViewer.ScrollSpeedLimit,
            appliedLimit: PdfViewer.ScrollSpeedLimit
        ) {
            @OptIn(PdfUnstableApi::class)
            this@PdfState.scrollSpeedLimit = appliedLimit
        }

        override fun onEditorHighlightColorChange(@ColorInt highlightColor: Int) {
            this@PdfState.editor.highlightColor = Color(highlightColor)
        }

        override fun onEditorShowAllHighlightsChange(showAll: Boolean) {
            this@PdfState.editor.showAllHighlights = showAll
        }

        override fun onEditorHighlightThicknessChange(@IntRange(from = 8, to = 24) thickness: Int) {
            this@PdfState.editor.highlightThickness = thickness
        }

        override fun onEditorFreeFontColorChange(@ColorInt fontColor: Int) {
            this@PdfState.editor.freeFontColor = Color(fontColor)
        }

        override fun onEditorFreeFontSizeChange(fontSize: Int) {
            this@PdfState.editor.freeFontSize = fontSize
        }

        override fun onEditorInkColorChange(color: Int) {
            this@PdfState.editor.inkColor = Color(color)
        }

        override fun onEditorInkThicknessChange(thickness: Int) {
            this@PdfState.editor.inkThickness = thickness
        }

        override fun onEditorInkOpacityChange(opacity: Int) {
            this@PdfState.editor.inkOpacity = opacity
        }

        override fun onPrintProcessStart() {
            this@PdfState.printState = PdfPrintState.Starting
        }

        override fun onPrintProcessProgress(progress: Float) {
            this@PdfState.printState = PdfPrintState.Loading(progress)
        }

        override fun onPrintProcessEnd() {
            this@PdfState.printState = PdfPrintState.Completed
        }

        override fun onPrintCancelled() {
            this@PdfState.printState = PdfPrintState.Cancelled
        }
    }
}

/**
 * Represents the loading state of the PDF file.
 */
sealed interface PdfLoadingState {
    /**
     * The PDF file is being initialized.
     */
    data object Initializing : PdfLoadingState

    /**
     * The PDF file is being loaded.
     *
     * @param progress The progress of the loading process, between 0.0 and 1.0.
     */
    data class Loading(@param:FloatRange(0.0, 1.0) val progress: Float) : PdfLoadingState

    /**
     * The PDF file has been loaded successfully.
     *
     * @param pagesCount The number of pages in the PDF file.
     */
    data class Finished(val pagesCount: Int) : PdfLoadingState

    /**
     * An error occurred while loading the PDF file.
     *
     * @param exception The exception that occurred.
     * @see com.bhuvaneshw.pdf.PdfException
     */
    data class Error(val exception: Exception) : PdfLoadingState {
        /**
         * The message of the exception.
         */
        val message: String? get() = exception.message
    }

    /**
     * `true` if the PDF file is being initialized or loaded.
     */
    val isLoading: Boolean get() = this is Initializing || this is Loading

    /**
     * `true` if the PDF file has been initialized.
     */
    val isInitialized: Boolean get() = this !is Initializing
}

/**
 * Represents the scroll state of the PDF viewer.
 *
 * @param currentOffset The current scroll offset.
 * @param totalOffset The total scroll offset.
 * @param isHorizontalScroll `true` if the scroll is horizontal, `false` otherwise.
 */
data class ScrollState(
    val currentOffset: Int = 0,
    val totalOffset: Int = 0,
    val isHorizontalScroll: Boolean = false,
) {
    /**
     * The ratio of the current scroll offset to the total scroll offset.
     */
    val ratio: Float get() = if (totalOffset == 0) 0f else currentOffset.toFloat() / totalOffset.toFloat()
}

/**
 * Represents the state of the find operation.
 *
 * @param current The current match index.
 * @param total The total number of matches.
 */
sealed class MatchState(val current: Int = 0, val total: Int = 0) {
    /**
     * The find operation has not been started.
     */
    class Initialized(current: Int = 0, total: Int = 0) : MatchState(current, total)

    /**
     * The find operation has been started.
     */
    class Started(current: Int = 0, total: Int = 0) : MatchState(current, total)

    /**
     * The find operation is in progress.
     */
    class Progress(current: Int, total: Int) : MatchState(current, total)

    /**
     * The find operation has been completed.
     *
     * @param found `true` if any matches were found, `false` otherwise.
     */
    class Completed(val found: Boolean, current: Int, total: Int) : MatchState(current, total)

    /**
     * `true` if the find operation is in progress.
     */
    val isLoading: Boolean get() = this is Started || this is Progress
}

/**
 * Represents the state of the print operation.
 */
sealed interface PdfPrintState {
    /**
     * The print operation is idle.
     */
    sealed interface Idle : PdfPrintState

    /**
     * The print operation has not been started.
     */
    data object Initial : Idle

    /**
     * The print operation has been cancelled.
     */
    data object Cancelled : Idle

    /**
     * The print operation has been completed.
     */
    data object Completed : Idle

    /**
     * The print operation is starting.
     */
    data object Starting : PdfPrintState

    /**
     * The print operation is in progress.
     *
     * @param progress The progress of the printing process, between 0.0 and 1.0.
     */
    data class Loading(@param:FloatRange(0.0, 1.0) val progress: Float) : PdfPrintState

    /**
     * `true` if the print operation is in progress.
     */
    val isLoading: Boolean get() = this is Loading || this is Starting
}
