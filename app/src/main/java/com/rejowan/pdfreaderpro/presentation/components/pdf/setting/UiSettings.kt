package com.bhuvaneshw.pdf.setting

import android.webkit.WebView
import com.bhuvaneshw.pdf.PdfEditorModeApi
import com.bhuvaneshw.pdf.PdfException
import com.bhuvaneshw.pdf.js.PdfFindBar
import com.bhuvaneshw.pdf.js.PdfSideBar
import com.bhuvaneshw.pdf.js.call
import com.bhuvaneshw.pdf.js.callDirectly
import com.bhuvaneshw.pdf.js.decode
import com.bhuvaneshw.pdf.js.encode
import com.bhuvaneshw.pdf.js.evaluate
import com.bhuvaneshw.pdf.js.invoke
import com.bhuvaneshw.pdf.js.set
import com.bhuvaneshw.pdf.js.toJsString
import com.bhuvaneshw.pdf.js.with

/**
 * Manages UI settings for the PDF viewer.
 */
class UiSettings internal constructor(private val webView: WebView) {

    /**
     * Settings for the left section of the main toolbar.
     */
    val toolbarLeft = ToolbarLeft()

    /**
     * Settings for the middle section of the main toolbar.
     */
    val toolbarMiddle = ToolbarMiddle()

    /**
     * Settings for the right section of the main toolbar.
     */
    val toolbarRight = ToolbarRight()

    /**
     * Settings for the secondary toolbar.
     */
    val toolBarSecondary = ToolbarSecondary()

    /**
     * Interacts with the password dialog.
     */
    val passwordDialog = PasswordDialog()

    /**
     * Interacts with the print dialog.
     */
    val printDialog = PrintDialog()

    /**
     * Shows or hides the entire toolbar.
     */
    var toolbarEnabled: Boolean = false
        set(value) {
            field = value
            webView callDirectly "setToolbarEnabled"(value)
        }

    /**
     * Opens or closes the sidebar.
     */
    var isSideBarOpen: Boolean = false
        set(value) {
            field = value
            webView with PdfSideBar call if (value) "open"() else "close"()
            webView with PdfSideBar set "sidebarContainer.style.display"((if (value) "" else "none").toJsString())
        }

    /**
     * Opens or closes the find bar.
     */
    var isFindBarOpen: Boolean = false
        set(value) {
            field = value
            webView with PdfFindBar call if (value) "open"() else "close"()
        }

    /**
     * Shows or hides the viewer's scrollbar.
     */
    var viewerScrollbar: Boolean = true
        set(value) {
            field = value
            webView.isVerticalScrollBarEnabled = value
            webView.isHorizontalScrollBarEnabled = value
            webView callDirectly "setViewerScrollbar"(value)
        }

    /**
     * Manages pages of the PDF.
     */
    val pages = Pages()

    /**
     * Performs a click on a tree item in the sidebar.
     *
     * @param itemId The ID of the tree item to click.
     * @return `true` if the click was handled, `false` otherwise.
     * @see com.bhuvaneshw.pdf.model.SideBarTreeItem
     */
    suspend fun performSidebarTreeItemClick(itemId: String): Boolean {
        val result = webView evaluate "performTreeItemClick(`$itemId`)"
        return result == "true"
    }

    /**
     * Settings for the editor mode UI.
     */
    @PdfEditorModeApi
    inner class EditorMode internal constructor() {
        /**
         * Enables or disables the editor mode buttons.
         */
        var enabled: Boolean = false
            set(value) {
                field = value
                webView callDirectly "setEditorModeButtonsEnabled"(value)
            }

        /**
         * Shows or hides the editor highlight button.
         */
        var editorHighlightButton: Boolean = true
            set(value) {
                field = value
                webView callDirectly "setEditorHighlightButtonEnabled"(value)
            }

        /**
         * Shows or hides the editor free text button.
         */
        var editorFreeTextButton: Boolean = true
            set(value) {
                field = value
                webView callDirectly "setEditorFreeTextButtonEnabled"(value)
            }

        /**
         * Shows or hides the editor stamp button.
         */
        var editorStampButton: Boolean = true
            set(value) {
                field = value
                webView callDirectly "setEditorStampButtonEnabled"(value)
            }

        /**
         * Shows or hides the editor ink button.
         */
        var editorInkButton: Boolean = true
            set(value) {
                field = value
                webView callDirectly "setEditorInkButtonEnabled"(value)
            }
    }

    /**
     * Settings for the middle section of the main toolbar.
     */
    inner class ToolbarMiddle internal constructor() {
        /**
         * Shows or hides the middle section of the main toolbar.
         */
        var enabled: Boolean = true
            set(value) {
                field = value
                webView callDirectly "setToolbarViewerMiddleEnabled"(value)
            }

        /**
         * Shows or hides the zoom in button.
         */
        var zoomInButton: Boolean = true
            set(value) {
                field = value
                webView callDirectly "setZoomInButtonEnabled"(value)
            }

        /**
         * Shows or hides the zoom out button.
         */
        var zoomOutButton: Boolean = true
            set(value) {
                field = value
                webView callDirectly "setZoomOutButtonEnabled"(value)
            }

        /**
         * Shows or hides the zoom scale selection container.
         */
        var zoomScaleSelectContainer: Boolean = true
            set(value) {
                field = value
                webView callDirectly "setZoomScaleSelectContainerEnabled"(value)
            }
    }

    /**
     * Settings for the left section of the main toolbar.
     */
    inner class ToolbarLeft internal constructor() {
        /**
         * Shows or hides the left section of the main toolbar.
         */
        var enabled: Boolean = true
            set(value) {
                field = value
                webView callDirectly "setToolbarViewerLeftEnabled"(value)
            }

        /**
         * Shows or hides the sidebar toggle button.
         */
        var sidebarToggleButton: Boolean = true
            set(value) {
                field = value
                webView callDirectly "setSidebarToggleButtonEnabled"(value)
            }

        /**
         * Shows or hides the find button.
         */
        var findButton: Boolean = true
            set(value) {
                field = value
                webView callDirectly "setViewFindButtonEnabled"(value)
            }

        /**
         * Shows or hides the page number container.
         */
        var pageNumberContainer: Boolean = true
            set(value) {
                field = value
                webView callDirectly "setPageNumberContainerEnabled"(value)
            }
    }

    /**
     * Settings for the right section of the main toolbar.
     */
    inner class ToolbarRight internal constructor() {
        /**
         * Shows or hides the right section of the main toolbar.
         */
        var enabled: Boolean = true
            set(value) {
                field = value
                webView callDirectly "setToolbarViewerRightEnabled"(value)
            }

        /**
         * Editor mode settings.
         */
        @OptIn(PdfEditorModeApi::class)
        val editorMode = EditorMode()

        /**
         * Shows or hides the secondary toolbar toggle button.
         */
        var secondaryToolbarToggleButton: Boolean = true
            set(value) {
                field = value
                webView callDirectly "setSecondaryToolbarToggleButtonEnabled"(value)
            }
    }

    /**
     * Settings for the secondary toolbar.
     */
    inner class ToolbarSecondary internal constructor() {
        /**
         * Shows or hides the secondary print button.
         */
        var secondaryPrint: Boolean = true
            set(value) {
                field = value
                webView callDirectly "setSecondaryPrintEnabled"(value)
            }

        /**
         * Shows or hides the secondary download button.
         */
        var secondaryDownload: Boolean = true
            set(value) {
                field = value
                webView callDirectly "setSecondaryDownloadEnabled"(value)
            }

        /**
         * Shows or hides the presentation mode button.
         */
        var presentationMode: Boolean = true
            set(value) {
                field = value
                webView callDirectly "setPresentationModeEnabled"(value)
            }

        /**
         * Shows or hides the "go to first page" button.
         */
        var goToFirstPage: Boolean = true
            set(value) {
                field = value
                webView callDirectly "setGoToFirstPageEnabled"(value)
            }

        /**
         * Shows or hides the "go to last page" button.
         */
        var goToLastPage: Boolean = true
            set(value) {
                field = value
                webView callDirectly "setGoToLastPageEnabled"(value)
            }

        /**
         * Shows or hides the page rotate clockwise button.
         */
        var pageRotateClockwise: Boolean = true
            set(value) {
                field = value
                webView callDirectly "setPageRotateCwEnabled"(value)
            }

        /**
         * Shows or hides the page rotate counter-clockwise button.
         */
        var pageRotateCounterClockwise: Boolean = true
            set(value) {
                field = value
                webView callDirectly "setPageRotateCcwEnabled"(value)
            }

        /**
         * Shows or hides the cursor select tool button.
         */
        var cursorSelectTool: Boolean = true
            set(value) {
                field = value
                webView callDirectly "setCursorSelectToolEnabled"(value)
            }

        /**
         * Shows or hides the cursor hand tool button.
         */
        var cursorHandTool: Boolean = true
            set(value) {
                field = value
                webView callDirectly "setCursorHandToolEnabled"(value)
            }

        /**
         * Shows or hides the scroll page-wise button.
         */
        var scrollPageWise: Boolean = true
            set(value) {
                field = value
                webView callDirectly "setScrollPageEnabled"(value)
            }

        /**
         * Shows or hides the vertical scroll button.
         */
        var scrollVertical: Boolean = true
            set(value) {
                field = value
                webView callDirectly "setScrollVerticalEnabled"(value)
            }

        /**
         * Shows or hides the horizontal scroll button.
         */
        var scrollHorizontal: Boolean = true
            set(value) {
                field = value
                webView callDirectly "setScrollHorizontalEnabled"(value)
            }

        /**
         * Shows or hides the wrapped scroll button.
         */
        var scrollWrapped: Boolean = true
            set(value) {
                field = value
                webView callDirectly "setScrollWrappedEnabled"(value)
            }

        /**
         * Shows or hides the "no spread" button.
         */
        var spreadNone: Boolean = true
            set(value) {
                field = value
                webView callDirectly "setSpreadNoneEnabled"(value)
            }

        /**
         * Shows or hides the "odd spread" button.
         */
        var spreadOdd: Boolean = true
            set(value) {
                field = value
                webView callDirectly "setSpreadOddEnabled"(value)
            }

        /**
         * Shows or hides the "even spread" button.
         */
        var spreadEven: Boolean = true
            set(value) {
                field = value
                webView callDirectly "setSpreadEvenEnabled"(value)
            }

        /**
         * Shows or hides the document properties button.
         */
        var documentProperties: Boolean = true
            set(value) {
                field = value
                webView callDirectly "setDocumentPropertiesEnabled"(value)
            }
    }

    /**
     * Interacts with the password dialog.
     */
    inner class PasswordDialog internal constructor() {
        /**
         * Gets the label text of the password dialog.
         *
         * @param callback The callback to receive the label text.
         */
        fun getLabelText(callback: (String?) -> Unit) {
            webView callDirectly "getLabelText"(callback = callback)
        }

        /**
         * Submits the password to the dialog.
         *
         * @param password The password to submit.
         */
        fun submitPassword(password: String) {
            webView callDirectly "JWI.onPasswordDialogChange"(false)
            webView callDirectly "submitPassword"(password.toJsString())
        }

        /**
         * Cancels the password dialog.
         */
        fun cancel() {
            webView callDirectly "cancelPasswordDialog"()
        }
    }

    /**
     * Interacts with the print dialog.
     */
    inner class PrintDialog internal constructor() {
        /**
         * Cancels the print dialog.
         */
        fun cancel() {
            webView callDirectly "cancelPrinting"()
        }
    }

    /**
     * Manages pages of the PDF.
     */
    inner class Pages internal constructor() {
        /**
         * Gets the page with the given number.
         *
         * @param pageNumber The page number to get.
         * @return The [Page] with the given number.
         * @throws PdfException If the page number is invalid.
         */
        suspend operator fun get(pageNumber: Int): Page {
            val count = webView evaluate "PDFViewerApplication.pagesCount"
            val pagesCount = count?.toInt() ?: throw PdfException("Unable to get pages count")
            if (pageNumber !in 1..pagesCount) throw PdfException("Invalid page number")

            return Page(pageNumber)
        }
    }

    /**
     * Represents a single page of the PDF.
     *
     * @property pageNumber The page number.
     */
    inner class Page internal constructor(val pageNumber: Int) {

        /**
         * Gets the inner text of the page.
         *
         *  @return The inner text of the page.
         */
        suspend fun innerText(): String? {
            val encodedResult = webView evaluate "getInnerTextOfPage($pageNumber)".encode()
            return encodedResult?.decode()
        }

        /**
         * Gets the inner HTML of the page.
         *
         * @return The inner HTML of the page.
         */
        suspend fun innerHtml(): String? {
            val encodedResult = webView evaluate "getInnerHtmlOfPage($pageNumber)".encode()
            return encodedResult?.decode()
        }

        suspend fun getRenderingState(): PageRenderingState {
            val state =
                (webView evaluate "PDFViewerApplication.pdfViewer.getPageView(${pageNumber - 1}).renderingState;")
                    ?: return PageRenderingState.UNKNOWN

            return try {
                PageRenderingState.entries[state.toInt()]
            } catch (_: Exception) {
                PageRenderingState.UNKNOWN
            }
        }
    }

    /**
     * Represents the rendering state of a page.
     */
    enum class PageRenderingState {
        /**
         * The page is not yet rendered.
         */
        INITIAL,

        /**
         * The page is currently rendering.
         */
        RUNNING,

        /**
         * The page rendering is paused.
         */
        PAUSED,

        /**
         * The page is finished rendering.
         */
        FINISHED,

        /**
         * The rendering state is unknown.
         */
        UNKNOWN
    }
}
