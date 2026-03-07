@file:SuppressLint("UseKtx")
@file:Suppress("unused")

package com.rejowan.pdfreaderpro.presentation.components.pdf

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.ActionMode
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.webkit.WebView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.FloatRange
import androidx.annotation.IntRange
import com.rejowan.pdfreaderpro.presentation.components.pdf.PdfViewer.Companion.defaultHighlightEditorColors
import com.rejowan.pdfreaderpro.presentation.components.pdf.js.Body
import com.rejowan.pdfreaderpro.presentation.components.pdf.js.call
import com.rejowan.pdfreaderpro.presentation.components.pdf.js.callDirectly
import com.rejowan.pdfreaderpro.presentation.components.pdf.js.invoke
import com.rejowan.pdfreaderpro.presentation.components.pdf.js.set
import com.rejowan.pdfreaderpro.presentation.components.pdf.js.setDirectly
import com.rejowan.pdfreaderpro.presentation.components.pdf.js.toJsHex
import com.rejowan.pdfreaderpro.presentation.components.pdf.js.toJsRgba
import com.rejowan.pdfreaderpro.presentation.components.pdf.js.toJsString
import com.rejowan.pdfreaderpro.presentation.components.pdf.js.with
import com.rejowan.pdfreaderpro.presentation.components.pdf.model.SideBarTreeItem
import com.rejowan.pdfreaderpro.presentation.components.pdf.print.PdfPrintAdapter
import com.rejowan.pdfreaderpro.presentation.components.pdf.resource.AssetResourceLoader
import com.rejowan.pdfreaderpro.presentation.components.pdf.resource.ContentResourceLoader
import com.rejowan.pdfreaderpro.presentation.components.pdf.resource.FileResourceLoader
import com.rejowan.pdfreaderpro.presentation.components.pdf.resource.NetworkResourceHandler
import com.rejowan.pdfreaderpro.presentation.components.pdf.resource.NetworkResourceLoader
import com.rejowan.pdfreaderpro.presentation.components.pdf.resource.PdfViewerResourceLoader
import com.rejowan.pdfreaderpro.presentation.components.pdf.resource.ResourceLoader
import com.rejowan.pdfreaderpro.presentation.components.pdf.setting.UiSettings
import com.rejowan.pdfreaderpro.R
import java.io.File
import kotlin.math.abs

/**
 * A custom `View` for displaying PDF documents.
 *
 * This view is built upon `WebView` and leverages Mozilla's [PDF.js](https://mozilla.github.io/pdf.js/)
 * library to render PDF files. It provides a comprehensive set of features for pdf viewing and interaction,
 * including navigation, zooming, text searching, and editing capabilities like highlighting.
 *
 * ### Loading a PDF
 *
 * You can load a PDF from various sources using the `load()` methods or specific `loadFrom...` methods:
 * - **Assets**: `loadFromAsset("my_document.pdf")`
 * - **Content URIs**: `loadFromContentUri(uri)` (e.g., from a file picker)
 * - **Files**: `loadFromFile(file)`
 * - **URLs**: `loadFromUrl("https://example.com/document.pdf")`
 *
 * Example usage in an Activity:
 * ```kotlin
 * class MyActivity : AppCompatActivity() {
 *     private lateinit var pdfViewer: PdfViewer
 *
 *     override fun onCreate(savedInstanceState: Bundle?) {
 *         super.onCreate(savedInstanceState)
 *         pdfViewer = PdfViewer(this)
 *         setContentView(pdfViewer)
 *
 *         pdfViewer.onReady {
 *             loadFromAsset("sample.pdf")
 *         }
 *     }
 * }
 * ```
 */
class PdfViewer @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : LinearLayout(context, attrs, defStyleAttr) {

    /**
     * A flag indicating whether the underlying PDF.js viewer has finished its initial setup and is ready to process commands.
     * It becomes `true` after the view has loaded the PDF.js library.
     * Most of the viewer's functions will throw a `PdfViewerNotInitializedException` if called before this flag is `true`.
     *
     * To safely interact with the viewer, use the [onReady] callback.
     *
     * @see onReady
     * @see PdfViewerNotInitializedException
     */
    var isInitialized = false; internal set

    /**
     * Gets the source URI of the currently loaded PDF document.
     *
     * This will be the full URI used by the view to load the document,
     * including the scheme and domain for the resource loader (e.g.,
     * `https://bhuvaneshw.github.io/pdf-viewer-resources/file/path/to/your/file.pdf`).
     *
     * It is set when one of the `load()` methods is called and is `null` before a document is loaded.
     *
     * @see createSharableUri
     */
    var currentSource: String? = null; internal set

    /**
     * Gets the current page number being displayed.
     *
     * This property reflects the 1-based index of the page currently visible to the user.
     * It is updated as the user scrolls or navigates through the document.
     *
     * The value is `1` by default when a new document is loaded and is capped by the total [pagesCount].
     * To navigate to a specific page, use the [goToPage] method.
     *
     * @see pagesCount
     * @see goToPage
     */
    var currentPage: Int = 1; internal set

    /**
     * Gets the total number of pages in the currently loaded PDF document.
     * This value is updated after a document is successfully loaded. It is `0` before any document is loaded or if loading fails.
     *
     * @see currentPage
     * @see goToPage
     */
    var pagesCount: Int = 0; internal set

    /**
     * Gets the current zoom scale of the displayed page as a floating-point number.
     *
     * A scale of `1.0` represents 100% (actual size). Values greater than `1.0` indicate magnification (zoom in),
     * while values less than `1.0` indicate minification (zoom out).
     *
     * This value is updated whenever the user zooms in or out.
     *
     * @see scalePageTo
     * @see currentPageScaleValue
     * @see minPageScale
     * @see maxPageScale
     */
    var currentPageScale: Float = 0f; internal set

    /**
     * Gets the current page scale preset value as a string.
     *
     * This represents the named zoom level currently active, such as "auto", "page-fit",
     * "page-width", or a percentage value (e.g., "150%"). It provides a more descriptive
     * representation of the zoom level compared to the raw float value in [currentPageScale].
     *
     * This value is updated whenever the zoom level changes.
     *
     * @see currentPageScale
     * @see scalePageTo
     * @see Zoom
     */
    var currentPageScaleValue: String = ""; internal set

    /**
     * Gets the metadata properties of the currently loaded PDF document.
     *
     * This property contains information extracted from the PDF file's metadata, such as
     * the author, title, creation date, and more. It is encapsulated in a [PdfDocumentProperties] object.
     *
     * The value is `null` until a document is successfully loaded and its properties have been parsed.
     * It is updated each time a new document is opened.
     *
     * @see PdfDocumentProperties
     * @see PdfListener.onLoadProperties
     */
    var properties: PdfDocumentProperties? = null; internal set

    /**
     * Gets the outline (table of contents) of the currently loaded PDF document.
     *
     * This property provides a hierarchical representation of the document's structure,
     * such as chapters, sections, and bookmarks. Each entry is represented as a [SideBarTreeItem].
     *
     * The value is `null` until the document is fully loaded and its outline has been parsed.
     * It is refreshed every time a new PDF is opened.
     *
     * @see SideBarTreeItem
     * @see PdfListener.onLoadOutline
     */
    var outline: List<SideBarTreeItem>? = null; internal set

    /**
     * Gets the list of embedded file attachments in the currently loaded PDF document.
     *
     * This property includes only the files that are explicitly embedded in the PDF as
     * attachments. These may include documents, images, or other binary files.
     *
     * The value is `null` until the document is loaded and its embedded attachments (if any)
     * have been extracted. It updates automatically whenever a new PDF is opened.
     *
     * @see SideBarTreeItem
     * @see PdfListener.onLoadAttachments
     */
    var attachments: List<SideBarTreeItem>? = null; internal set

    /**
     * Defines the list of colors available in the highlight editor's color palette.
     *
     * This property is a list of pairs, where each pair consists of:
     * - A `String` representing the CSS class name for the color (e.g., "yellow"). This is used internally by PDF.js.
     * - An `Int` representing the Android color value (`@ColorInt`).
     *
     * The colors provided here will be displayed to the user when they choose to change the highlight color.
     * The default set of colors can be found in [defaultHighlightEditorColors].
     *
     * **Note:** Any changes to this property require the `PdfViewer` to be reinitialized via the [reInitialize] method
     * for the changes to take effect or set it initially while creating the instance.
     *
     * Example of custom colors:
     * ```kotlin
     * pdfViewer.highlightEditorColors = listOf(
     *     "custom-yellow" to Color.YELLOW,
     *     "custom-cyan" to Color.CYAN
     * )
     * pdfViewer.reInitialize() // Important if viewer already initialized!
     * ```
     *
     * @see defaultHighlightEditorColors
     * @see reInitialize
     */
    var highlightEditorColors: List<Pair<String, Int>> = defaultHighlightEditorColors

    /**
     * Defines the list of allowed custom protocols for links.
     *
     * This list defines custom protocols that are permitted in addition to the default ones:
     * `"http:"`, `"https:"`, `"ftp:"`, `"mailto:"`, and `"tel:"`.
     *
     * **Note:** Any changes to this property require the `PdfViewer` to be reinitialized via the [reInitialize] method
     * for the changes to take effect or set it initially while creating the instance.
     *
     * Example of custom colors:
     * ```kotlin
     * pdfViewer.allowedCustomProtocols += "myapp:"
     * pdfViewer.reInitialize() // Important if viewer already initialized!
     * ```
     *
     * @see PdfListener.onLinkClick
     * @see reInitialize
     */
    val allowedCustomProtocols = mutableListOf<String>()

    /**
     * An adapter for handling PDF printing.
     *
     * This property holds an instance of [PdfPrintAdapter], which is required to use the printing functionality
     * of the viewer. You must set this property before calling the [printFile] method.
     *
     * The [PdfPrintAdapter] bridges the `PdfViewer` with Android's printing framework.
     *
     * Example:
     * ```kotlin
     * // In your Activity or Fragment
     * pdfViewer.pdfPrintAdapter = DefaultPdfPrintAdapter(context).also {
     *      it.defaultFileName = "filename"
     * }
     *
     * // When a print button is clicked
     * printButton.setOnClickListener {
     *     pdfViewer.printFile()
     * }
     * ```
     *
     * @see printFile
     * @see com.rejowan.pdfreaderpro.presentation.components.pdf.print.PdfPrintAdapter
     * @throws RuntimeException if [printFile] is called when this property is `null`.
     */
    var pdfPrintAdapter: PdfPrintAdapter? = null

    internal val listeners = mutableListOf<PdfListener>()
    internal val webInterface: WebInterface = WebInterface(this)
    internal val mainHandler = Handler(Looper.getMainLooper())
    internal var onReadyListeners = mutableListOf<PdfViewer.() -> Unit>()
    internal var tempBackgroundColor: Int? = null

    private val networkResourceLoader = NetworkResourceLoader(webInterface::onLoadFailed)
    internal val resourceLoaders = listOf(
        PdfViewerResourceLoader(context, webInterface::onLoadFailed),
        AssetResourceLoader(context, webInterface::onLoadFailed),
        ContentResourceLoader(context, webInterface::onLoadFailed),
        FileResourceLoader(webInterface::onLoadFailed),
        networkResourceLoader,
    )

    internal val webView: WebView = PdfJsWebView()

    /**
     * Provides access to settings for controlling the visibility of various UI elements within the PDF viewer.
     *
     * This property returns a [UiSettings] object, which allows you to programmatically show or hide
     * components like the toolbar, sidebar, and other interactive buttons.
     *
     * Example:
     * ```kotlin
     * // Hide the main toolbar
     * pdfViewer.ui.toolbarEnabled = false
     *
     * ```
     *
     * **Note:** Accessing this property before the viewer is fully initialized will result in a
     * [PdfViewerNotInitializedException]. It's recommended to modify UI settings within the `onReady`
     * callback to ensure the viewer is prepared.
     *
     * @see UiSettings
     * @see onReady
     * @throws PdfViewerNotInitializedException if the viewer is not yet initialized.
     */
    val ui = UiSettings(webView)
        get() {
            checkViewer()
            return field
        }

    /**
     * Provides access to the PDF editing functionalities.
     *
     * This controller allows you to manage and interact with various editing tools, such as the text highlighter,
     * ink annotation (freehand drawing), and free text annotations. You can use it to enable or disable
     * specific tools, configure their properties (like color and thickness), and manage annotations.
     *
     * @see PdfEditor
     */
    val editor = PdfEditor(this)

    /**
     * Provides access to the text search functionality within the PDF document.
     *
     * This controller allows you to programmatically find and highlight text, navigate through search results,
     * and configure search options like case sensitivity and finding whole words.
     *
     * Example:
     * ```kotlin
     * // Start a search for the word "Android"
     * pdfViewer.findController.find("Android")
     *
     * // Listen for find results
     * pdfViewer.addListener(object : PdfListener {
     *    override fun onFindMatchStart() {
     *    }
     *    override fun onFindMatchChange(current: Int, total: Int) {
     *    }
     *    override fun onFindMatchComplete(found: Boolean) {
     *    }
     *})
     *
     * // Navigate to the next match
     * pdfViewer.findController.findNext()
     * ```
     *
     * @see FindController
     * @throws PdfViewerNotInitializedException if accessed before the viewer is ready.
     */
    val findController = FindController(webView)
        get() {
            checkViewer()
            return field
        }

    /**
     * Controls the scrolling behavior for navigating between pages.
     *
     * This property determines how the user scrolls through the document. It can be set to one of the
     * following modes defined in the [PageScrollMode] enum:
     * - [PageScrollMode.VERTICAL]: Pages are laid out vertically, and the user scrolls up and down.
     * - [PageScrollMode.HORIZONTAL]: Pages are laid out horizontally, and the user scrolls left and right.
     * - [PageScrollMode.WRAPPED]: Pages are laid out in a grid that wraps based on the view's width, with vertical scrolling.
     * - [PageScrollMode.SINGLE_PAGE]: Only one page is visible at a time.
     *
     * Changing the scroll mode may also affect other layout properties like [pageAlignMode] and [singlePageArrangement]
     * to ensure a consistent viewing experience. For example, vertical alignment options are disabled in vertical scroll modes.
     *
     * The default value is [PageScrollMode.VERTICAL].
     *
     * @see PageScrollMode
     * @see pageAlignMode
     * @see singlePageArrangement
     * @throws PdfViewerNotInitializedException if accessed before the viewer is ready.
     */
    var pageScrollMode: PageScrollMode = PageScrollMode.VERTICAL
        set(value) {
            checkViewer()
            field = value
            webView callDirectly value.function()
            adjustAlignModeAndArrangementMode(value)
            dispatchSnapChange(snapPage, false)
        }

    /**
     * Controls how pages are displayed in a spread (side-by-side).
     *
     * This property determines the layout of pages when the viewer is wide enough
     * to show more than one page at a time. It uses the [PageSpreadMode] enum to
     * configure the display.
     *
     * - [PageSpreadMode.NONE]: Default. Pages are displayed one below the other.
     * - [PageSpreadMode.ODD]: Pages are displayed in a two-page spread, with odd-numbered pages on the left.
     * - [PageSpreadMode.EVEN]: Pages are displayed in a two-page spread, with even-numbered pages on the left.
     *
     * **Note:** Setting this to [PageSpreadMode.ODD] or [PageSpreadMode.EVEN] will automatically
     * disable [singlePageArrangement] if it is currently active, as they are mutually exclusive.
     *
     * @see PageSpreadMode
     * @see singlePageArrangement
     * @throws PdfViewerNotInitializedException if accessed before the viewer is ready.
     */
    var pageSpreadMode: PageSpreadMode = PageSpreadMode.NONE
        set(value) {
            checkViewer()
            field = value
            webView callDirectly value.function()
            if (value != PageSpreadMode.NONE && singlePageArrangement)
                singlePageArrangement = false
        }

    /**
     * Specifies the active cursor tool for interacting with the PDF content.
     *
     * This property determines the behavior of the cursor when the user clicks and drags on the document.
     * It can be set to one of the values defined in the [CursorToolMode] enum:
     *
     * - [CursorToolMode.TEXT_SELECT]: The default mode. Allows the user to select text within the document.
     * - [CursorToolMode.HAND]: Changes the cursor to a hand tool, allowing the user to pan and scroll the document by clicking and dragging.
     *
     * Changing this value will immediately update the cursor behavior in the viewer.
     *
     * @see CursorToolMode
     * @throws PdfViewerNotInitializedException if accessed before the viewer is ready.
     */
    var cursorToolMode: CursorToolMode = CursorToolMode.TEXT_SELECT
        set(value) {
            checkViewer()
            field = value
            webView callDirectly value.function()
        }

    /**
     * Gets or sets the rotation of the displayed pages.
     *
     * This property allows you to apply a rotation to all pages in the document. The rotation
     * is specified using the [PageRotation] enum, which provides options for 0, 90, 180, and 270 degrees.
     *
     * Setting this property will immediately update the view to reflect the new rotation.
     * The default value is [PageRotation.R_0] (no rotation).
     *
     * You can also use [rotateClockWise] and [rotateCounterClockWise] for convenient rotation control.
     *
     * @see PageRotation
     * @see rotateClockWise
     * @see rotateCounterClockWise
     * @throws PdfViewerNotInitializedException if accessed before the viewer is ready.
     */
    var pageRotation: PageRotation = PageRotation.R_0
        set(value) {
            checkViewer()
            field = value
            dispatchRotationChange(value)
        }

    /**
     * Sets the time threshold in milliseconds for detecting a double-click.
     *
     * A double-click is registered if two consecutive clicks occur within this time frame.
     * The default value is `300` milliseconds. Adjusting this value can help fine-tune
     * the responsiveness of double-click actions, such as zooming.
     *
     * @see longClickThreshold
     * @throws PdfViewerNotInitializedException if accessed before the viewer is ready.
     */
    var doubleClickThreshold: Long = 300
        set(value) {
            checkViewer()
            field = value
            webView setDirectly "DOUBLE_CLICK_THRESHOLD"(value)
        }

    /**
     * Gets or sets the time threshold in milliseconds for a touch to be considered a long-click.
     *
     * This value determines how long a user must press on the view before a long-click event
     * is triggered. The default value is 500 milliseconds.
     *
     * Setting a longer duration requires the user to press and hold for more time, making
     * long-clicks less frequent. A shorter duration makes them more sensitive.
     *
     * @see doubleClickThreshold
     * @throws PdfViewerNotInitializedException if accessed before the viewer is ready.
     */
    var longClickThreshold: Long = 500
        set(value) {
            checkViewer()
            field = value
            webView setDirectly "LONG_CLICK_THRESHOLD"(value)
        }

    /**
     * Gets or sets the default zoom scale for the page when a document is first loaded.
     *
     * This property determines the initial zoom level. The value can be either a direct
     * floating-point scale (e.g., `0.5f` for 50%) or a special constant from the [Zoom] enum,
     * represented by a negative float value.
     *
     * ### Scale Values:
     * - **Positive Float (`> 0`)**: A direct scale factor. For example, `1.5f` sets the default zoom to 150%.
     * - **[Zoom] Enum Constants**:
     *   - `Zoom.AUTOMATIC.floatValue` (`-1f`): Automatically determines the best scale to fit the page.
     *   - `Zoom.PAGE_FIT.floatValue` (`-2f`): Fits the entire page within the view.
     *   - `Zoom.PAGE_WIDTH.floatValue` (`-3f`): Fits the width of the page within the view.
     *   - `Zoom.ACTUAL_SIZE.floatValue` (`-4f`): Sets the default scale to 100% (actual size).
     *
     * The default value is `Zoom.AUTOMATIC.floatValue` (`-1f`).
     *
     * **Note:** When a [Zoom] constant is used, the viewer calculates the corresponding actual float scale,
     * which can be retrieved from [actualDefaultPageScale]. If the default scale is changed after a document
     * is loaded, the view will automatically adjust to the new scale.
     *
     * @see minPageScale
     * @see maxPageScale
     * @see actualDefaultPageScale
     * @see Zoom
     */
    @FloatRange(from = -4.0, to = 10.0)
    var minPageScale = 0.1f
        set(value) {
            field = value
            if (isInitialized) {
                if (value in ZOOM_SCALE_RANGE) getActualScaleFor(Zoom.entries[abs(value.toInt()) - 1]) {
                    actualMinPageScale = it ?: actualMinPageScale
                } else actualMinPageScale = value
            }
            if (field != value)
                listeners.forEach { it.onScaleLimitChange(value, maxPageScale, defaultPageScale) }
        }

    /**
     * Gets or sets the maximum zoom scale for the page.
     *
     * This property defines the lower boundary for zooming out. The value can be either a direct
     * floating-point scale (e.g., `0.5f` for 50%) or a special constant from the [Zoom] enum,
     * represented by a negative float value.
     *
     * ### Scale Values:
     * - **Positive Float (`> 0`)**: A direct scale factor. For example, `0.1f` sets the maximum zoom to 10%.
     * - **[Zoom] Enum Constants**:
     *   - `Zoom.AUTOMATIC.floatValue` (`-1f`): Automatically determines the best maximum scale.
     *   - `Zoom.PAGE_FIT.floatValue` (`-2f`): Fits the entire page within the view.
     *   - `Zoom.PAGE_WIDTH.floatValue` (`-3f`): Fits the width of the page within the view.
     *   - `Zoom.ACTUAL_SIZE.floatValue` (`-4f`): Sets the maximum scale to 100% (actual size).
     *
     * The default value is `0.1f`.
     *
     * **Note:** When a [Zoom] constant is used, the viewer calculates the corresponding actual float scale,
     * which can be retrieved from [actualMaxPageScale].
     *
     * @see minPageScale
     * @see defaultPageScale
     * @see actualMaxPageScale
     * @see Zoom
     */
    @FloatRange(from = -4.0, to = 10.0)
    var maxPageScale = 10f
        set(value) {
            if (field != value)
                listeners.forEach { it.onScaleLimitChange(minPageScale, value, defaultPageScale) }
            field = value
            if (isInitialized) {
                if (value in ZOOM_SCALE_RANGE) getActualScaleFor(Zoom.entries[abs(value.toInt()) - 1]) {
                    actualMaxPageScale = it ?: actualMaxPageScale
                } else actualMaxPageScale = value
            }
        }

    /**
     * Gets or sets the default zoom scale for the page.
     *
     * This property defines the initial zoom. The value can be either a direct
     * floating-point scale (e.g., `0.5f` for 50%) or a special constant from the [Zoom] enum,
     * represented by a negative float value.
     *
     * ### Scale Values:
     * - **Positive Float (`> 0`)**: A direct scale factor. For example, `0.1f` sets the default zoom to 10%.
     * - **[Zoom] Enum Constants**:
     *   - `Zoom.AUTOMATIC.floatValue` (`-1f`): Automatically determines the best default scale.
     *   - `Zoom.PAGE_FIT.floatValue` (`-2f`): Fits the entire page within the view.
     *   - `Zoom.PAGE_WIDTH.floatValue` (`-3f`): Fits the width of the page within the view.
     *   - `Zoom.ACTUAL_SIZE.floatValue` (`-4f`): Sets the default scale to 100% (actual size).
     *
     * The default value is `0.1f`.
     *
     * **Note:** When a [Zoom] constant is used, the viewer calculates the corresponding actual float scale,
     * which can be retrieved from [actualDefaultPageScale].
     *
     * @see minPageScale
     * @see maxPageScale
     * @see actualDefaultPageScale
     * @see Zoom
     */
    @FloatRange(from = -4.0, to = 10.0)
    var defaultPageScale = Zoom.AUTOMATIC.floatValue
        set(value) {
            if (field != value)
                listeners.forEach { it.onScaleLimitChange(minPageScale, maxPageScale, value) }
            field = value
            if (isInitialized) {
                if (value in ZOOM_SCALE_RANGE) getActualScaleFor(Zoom.entries[abs(value.toInt()) - 1]) {
                    actualDefaultPageScale = it ?: actualDefaultPageScale
                    scalePageTo(actualDefaultPageScale)
                } else {
                    actualDefaultPageScale = value
                    scalePageTo(value)
                }
            }
        }

    /**
     * Gets the actual minimum zoom scale limit as a raw float value.
     *
     * This property reflects the computed minimum scale value that the viewer will enforce,
     * derived from the value set in [minPageScale]. While [minPageScale] can be set to
     * a symbolic [Zoom] value (e.g., `Zoom.PAGE_FIT`), this property will always hold the
     * concrete floating-point scale that corresponds to it (e.g., `0.25f`).
     *
     * @see minPageScale
     */
    var actualMinPageScale = 0f
        internal set(value) {
            if (field != value)
                listeners.forEach {
                    it.onActualScaleLimitChange(value, actualMaxPageScale, actualDefaultPageScale)
                }
            field = value
            if (value > 0) webView setDirectly "MIN_SCALE"(value)
        }

    /**
     * Gets the actual maximum zoom scale limit as a raw float value.
     *
     * This property reflects the computed maximum scale value that the viewer will enforce,
     * derived from the value set in [maxPageScale]. While [maxPageScale] can be set to
     * a symbolic [Zoom] value (e.g., `Zoom.PAGE_FIT`), this property will always hold the
     * concrete floating-point scale that corresponds to it (e.g., `0.25f`).
     *
     * @see maxPageScale
     */
    var actualMaxPageScale = 0f
        internal set(value) {
            if (field != value)
                listeners.forEach {
                    it.onActualScaleLimitChange(actualMinPageScale, value, actualDefaultPageScale)
                }
            field = value
            if (value > 0) webView setDirectly "MAX_SCALE"(value)
        }

    /**
     * Gets the actual default zoom scale limit as a raw float value.
     *
     * This property reflects the computed default scale value that the viewer will enforce,
     * derived from the value set in [defaultPageScale]. While [defaultPageScale] can be set to
     * a symbolic [Zoom] value (e.g., `Zoom.PAGE_FIT`), this property will always hold the
     * concrete floating-point scale that corresponds to it (e.g., `0.25f`).
     *
     * @see defaultPageScale
     */
    var actualDefaultPageScale = 0f
        internal set(value) {
            if (field != value)
                listeners.forEach {
                    it.onActualScaleLimitChange(actualMinPageScale, actualMaxPageScale, value)
                }
            field = value
        }

    /**
     * Enables or disables page snapping behavior during scrolling.
     *
     * When set to `true`, the viewer will automatically "snap" to the nearest page as the user scrolls,
     * making it easier to land perfectly on a page boundary. This is particularly useful in
     * horizontal scrolling modes but also works for vertical and wrapped modes.
     *
     * When set to `false` (the default), scrolling is continuous and does not snap to pages.
     *
     * The snapping behavior is influenced by the current [pageScrollMode]:
     * - [PageScrollMode.HORIZONTAL]: Snaps horizontally to the start of each page.
     * - [PageScrollMode.VERTICAL] and [PageScrollMode.WRAPPED]: Snaps vertically to the top of each page.
     * - The behavior is disabled in other modes.
     *
     * @see pageScrollMode
     * @throws PdfViewerNotInitializedException if accessed before the viewer is ready.
     */
    var snapPage = false
        set(value) {
            checkViewer()
            field = value
            dispatchSnapChange(value)
        }

    /**
     * Controls the alignment of pages within the viewer.
     *
     * This property determines how a page is positioned when its size is smaller than the available
     * view area. It uses the [PageAlignMode] enum to set the alignment.
     *
     * - [PageAlignMode.DEFAULT]: No specific alignment is enforced.
     * - [PageAlignMode.CENTER_VERTICAL]: The page is centered vertically.
     * - [PageAlignMode.CENTER_HORIZONTAL]: The page is centered horizontally.
     * - [PageAlignMode.CENTER_BOTH]: The page is centered both vertically and horizontally.
     *
     * **Note:** The behavior of this property is influenced by the current [pageScrollMode] and
     * [singlePageArrangement] settings.
     * - In vertical scroll modes ([PageScrollMode.VERTICAL], [PageScrollMode.WRAPPED]), vertical alignment options are ignored.
     * - In horizontal scroll mode ([PageScrollMode.HORIZONTAL]), horizontal alignment options are ignored.
     * - These restrictions do not apply when [singlePageArrangement] is enabled or when using [PageScrollMode.SINGLE_PAGE].
     *
     * The default value is [PageAlignMode.DEFAULT].
     *
     * @see PageAlignMode
     * @see pageScrollMode
     * @see singlePageArrangement
     * @throws PdfViewerNotInitializedException if accessed before the viewer is ready.
     */
    var pageAlignMode = PageAlignMode.DEFAULT
        set(value) {
            checkViewer()
            field = dispatchPageAlignMode(value)
        }

    /**
     * Enables or disables a special layout mode where pages are arranged as if they are on a single,
     * continuous surface, allowing for smooth, uninterrupted scrolling.
     *
     * When set to `true`, this mode offers a unique viewing experience with the following constraints and behaviors:
     * - It is only effective when [pageScrollMode] is set to either [PageScrollMode.VERTICAL] or [PageScrollMode.HORIZONTAL].
     *   It will be automatically disabled for other scroll modes.
     * - It is mutually exclusive with [pageSpreadMode]. If a spread mode (e.g., [PageSpreadMode.ODD]) is active,
     *   this arrangement will not apply.
     * - This mode allows for more flexible page alignment via [pageAlignMode], as the standard restrictions
     *   for vertical and horizontal scroll modes are lifted.
     *
     * When `false` (the default), pages are rendered with standard margins and separation based on the
     * current scroll and spread settings.
     *
     * @see pageScrollMode
     * @see pageSpreadMode
     * @see pageAlignMode
     * @throws PdfViewerNotInitializedException if accessed before the viewer is ready.
     */
    var singlePageArrangement = false
        set(value) {
            checkViewer()
            field = dispatchSinglePageArrangement(value)
        }

    /**
     * Configures the scroll speed limit, which can be useful for creating "single page" scrolling effects.
     *
     * This property allows you to control how fast the user can scroll between pages, particularly when
     * [singlePageArrangement] is enabled. It uses the [ScrollSpeedLimit] sealed class to define the behavior.
     *
     * ### Modes:
     * - [ScrollSpeedLimit.None]: (Default) No speed limit is applied. Scrolling is continuous.
     * - [ScrollSpeedLimit.Fixed]: Enforces a fixed scroll speed limit and allows you to control whether flinging is permitted.
     * - [ScrollSpeedLimit.AdaptiveFling]: Enforces a speed limit but intelligently allows flinging only when a page is smaller than the view, preventing accidental page turns.
     *
     * **Note:** This feature is only effective when [singlePageArrangement] is `true`. If `singlePageArrangement` is `false`,
     * this property will automatically be reset to [ScrollSpeedLimit.None].
     *
     * @see ScrollSpeedLimit
     * @see singlePageArrangement
     * @throws PdfViewerNotInitializedException if accessed before the viewer is ready.
     */
    @PdfUnstableApi
    var scrollSpeedLimit: ScrollSpeedLimit = ScrollSpeedLimit.None
        set(value) {
            checkViewer()
            field = dispatchScrollSpeedLimit(value)
        }

    /**
     * Sets the `aria-label` attribute for the PDF viewer, which is crucial for accessibility.
     *
     * This label provides a human-readable, accessible name for the viewer that screen readers
     * (like TalkBack) can announce to users. It helps users with visual impairments understand
     * the purpose of the component.
     *
     * The default value is "Pdf Viewer". It's recommended to provide a more descriptive label if
     * multiple viewers are on the same screen or to match the context of your app's content.
     *
     * @see ariaRoleDescription
     * @throws PdfViewerNotInitializedException if accessed before the viewer is ready.
     */
    var ariaLabel: String = "Pdf Viewer"
        set(value) {
            checkViewer()
            webView callDirectly "setAriaLabel"(value.toJsString())
            field = value
        }

    /**
     * Sets a human-readable, description for the role of the PDF viewer,
     * intended for accessibility services like TalkBack.
     *
     * This description provides more context than the `aria-label`. For example, if the `aria-label`
     * is "Document Viewer", the `ariaRoleDescription` could be "PDF document". TalkBack might then
     * announce "PDF document, Document Viewer".
     *
     * If left empty, TalkBack may default to announcing the role as "Region", which can be less descriptive.
     * The default value is "Region" to provide a basic fallback.
     *
     * @see ariaLabel
     */
    var ariaRoleDescription: String = "Region"
        set(value) {
            checkViewer()
            webView callDirectly "setAriaRoleDescription"(value.toJsString())
            field = value
        }

    /**
     * A handler for customizing how PDF documents are fetched from network URLs.
     *
     * This property allows you to provide a custom implementation of [NetworkResourceHandler]
     * to control the entire network request and response process. This is useful for adding
     * custom headers (e.g., for authorization), implementing caching strategies, or handling
     * specific network conditions.
     *
     * When a PDF is loaded from a network URL, the viewer will invoke the
     * `open` method of this handler.
     *
     * @see NetworkResourceHandler
     * @see loadFromUrl
     */
    var networkResourceHandler: NetworkResourceHandler
        get() = networkResourceLoader.handler
        set(value) {
            networkResourceLoader.handler = value
        }

    /**
     * Enables or disables DOM storage APIs, such as `localStorage` and `sessionStorage`
     * (used by Pdf.js to store history).
     * @see [android.webkit.WebSettings.setDomStorageEnabled]
     */
    var domStorageEnabled: Boolean
        get() = webView.settings.domStorageEnabled
        set(value) {
            webView.settings.domStorageEnabled = value
        }

    /**
     * A wrapper for customizing the behavior of the text selection `ActionMode.Callback`.
     *
     * This property allows you to intercept and modify the standard `ActionMode.Callback`
     * that is triggered when a user selects text in the `WebView`. By default, it is set
     * to the `customSelectionModeCallback` function, which provides custom handling for
     * text highlighting features.
     *
     * You can replace this with your own implementation to achieve different behaviors,
     * such as adding custom menu items to the selection action mode bar or changing its
     * appearance.
     *
     * @see ActionMode.Callback
     */
    var actionModeCallbackWrapper: (ActionMode.Callback?) -> ActionMode.Callback =
        ::customSelectionModeCallback

    init {
        val containerBgColor = attrs?.let {
            val typedArray =
                context.obtainStyledAttributes(it, R.styleable.PdfViewer, defStyleAttr, 0)
            val color =
                typedArray.getColor(R.styleable.PdfViewer_containerBackgroundColor, COLOR_NOT_FOUND)
            typedArray.recycle()
            color
        } ?: COLOR_NOT_FOUND

        if (!isInEditMode) {
            addView(webView, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
            webView.addJavascriptInterface(webInterface, "JWI")
            loadPage()

            if (containerBgColor != COLOR_NOT_FOUND)
                setContainerBackgroundColor(containerBgColor)
        } else setPreviews(context, containerBgColor)
    }

    /**
     * Loads a PDF document from a source specified by a string.
     *
     * This is a versatile method that automatically detects the source type based on the string's prefix
     * and delegates to the appropriate `loadFrom...` method.
     *
     * The `source` string can represent one of the following:
     * - **Asset file**: Prefixed with `asset://` or `file:///android_asset/`.
     *   Example: `"asset://sample.pdf"`
     * - **Content URI**: Prefixed with `content://`. Typically used for files from a document picker.
     *   Example: `"content://com.android.providers.media.documents/document/123"`
     * - **File path**: Prefixed with `file://` or starts with `/`. Represents a direct file path.
     *   Note: Accessing files directly from internal storage is not recommended due to permission complexities.
     *   Example: `"file:///data/user/0/com.example.app/files/document.pdf"`
     * - **Network URL**: Prefixed with `http://` or `https://`.
     *   Example: `"https://example.com/document.pdf"`
     *
     * @param source The string representing the location of the PDF document.
     * @see loadFromAsset
     * @see loadFromContentUri
     * @see loadFromFile
     * @see loadFromUrl
     * @throws PdfViewerNotInitializedException if the viewer has not been initialized yet.
     */
    fun load(source: Uri) {
        load(source.toString())
    }

    /**
     * Loads a PDF document from a source specified by a string.
     *
     * This is a versatile method that automatically detects the source type based on the string's prefix
     * and delegates to the appropriate `loadFrom...` method.
     *
     * The `source` string can represent one of the following:
     * - **Asset file**: Prefixed with `asset://` or `file:///android_asset/`.
     *   Example: `"asset://sample.pdf"`
     * - **Content URI**: Prefixed with `content://`. Typically used for files from a document picker.
     *   Example: `"content://com.android.providers.media.documents/document/123"`
     * - **File path**: Prefixed with `file://` or starts with `/`. Represents a direct file path.
     *   Note: Accessing files directly from internal storage is not recommended due to permission complexities.
     *   Example: `"file:///data/user/0/com.example.app/files/document.pdf"`
     * - **Network URL**: Prefixed with `http://` or `https://`.
     *   Example: `"https://example.com/document.pdf"`
     *
     * @param source The string representing the location of the PDF document.
     * @see loadFromAsset
     * @see loadFromContentUri
     * @see loadFromFile
     * @see loadFromUrl
     * @throws PdfViewerNotInitializedException if the viewer has not been initialized yet.
     */
    fun load(source: String) {
        when {
            source.startsWith("file:///android_asset/") ->
                loadFromAsset(source.replaceFirst("file:///android_asset/", ""))

            source.startsWith("asset://") ->
                loadFromAsset(source.replaceFirst("asset://", ""))

            source.startsWith("content://") ->
                loadFromContentUri(source)

            source.startsWith("file://") ->
                loadFromFile(source.replaceFirst("file://", ""))

            source.startsWith("/") ->
                loadFromFile(source)

            source.startsWith("https://") || source.startsWith("http://") ->
                loadFromUrl(source)

            else ->
                throw IllegalArgumentException("No resource loader is available for provided source! $source")
        }
    }

    /**
     * Loads a PDF document from the application's assets folder.
     *
     * This function initiates the loading of a PDF file that is bundled with the application.
     * The viewer must be initialized before calling this method, which is best done inside
     * the [onReady] callback.
     *
     * @param assetPath The relative path to the PDF file within the `assets` directory.
     *                  For example, if the file is located at `src/main/assets/sample.pdf`,
     *                  the `assetPath` would be `"sample.pdf"`. If it's in a subdirectory like
     *                  `src/main/assets/docs/manual.pdf`, the path would be `"docs/manual.pdf"`.
     * @see onReady
     * @see load
     * @throws PdfViewerNotInitializedException if the viewer has not been initialized yet.
     */
    fun loadFromAsset(assetPath: String) {
        openUrl(urlFor(AssetResourceLoader.PATH, assetPath))
    }

    /**
     * Loads a PDF document from a `content://` URI.
     *
     * This function initiates the loading of a PDF file that is bundled with the application.
     * The viewer must be initialized before calling this method, which is best done inside
     * the [onReady] callback.
     *
     * This method is typically used to load documents provided by a `ContentProvider`,
     * such as a file selected through the Android Storage Access Framework (e.g., from a file picker).
     *
     * @param contentUri The `Uri` pointing to the PDF document. Must have the `content://` scheme.
     * @see onReady
     * @see load
     * @throws PdfViewerNotInitializedException if the viewer has not been initialized yet.
     */
    fun loadFromContentUri(contentUri: Uri) {
        loadFromContentUri(contentUri.toString())
    }

    /**
     * Loads a PDF document from a `content://` URI.
     *
     * This method is typically used to load documents provided by a `ContentProvider`,
     * such as a file selected through the Android Storage Access Framework (e.g., from a file picker).
     * The viewer must be initialized before calling this method, which is best done inside
     * the [onReady] callback.
     *
     * @param contentUri The `Uri` pointing to the PDF document. Must have the `content://` scheme.
     * @see onReady
     * @see load
     * @throws PdfViewerNotInitializedException if the viewer has not been initialized yet.
     */
    fun loadFromContentUri(contentUri: String) {
        openUrl(urlFor(ContentResourceLoader.PATH, Uri.encode(contentUri)))
    }

    /**
     * Loads a PDF document from a local file path.
     *
     * This function is suitable for loading PDFs stored in the application's internal or external
     * storage directories.
     * The viewer must be initialized before calling this method, which is best done inside
     * the [onReady] callback.
     *
     * **Note on Storage Access:** On modern Android versions (API 29+), direct file path access
     * to external storage is restricted. It is highly recommended to use the Storage Access Framework
     * and load files via a `content://` URI using [loadFromContentUri] for better compatibility
     * and user privacy. This method is best used for files within your app's sandboxed directories
     * (e.g., `context.getFilesDir()`).
     *
     * @param file The absolute path to the PDF file on the device's storage.
     *                 For example, `"/data/user/0/com.example.app/files/document.pdf"`.
     * @see loadFromContentUri
     * @see onReady
     * @see load
     * @throws PdfViewerNotInitializedException if the viewer has not been initialized yet.
     */
    fun loadFromFile(file: File) {
        loadFromFile(file.absolutePath)
    }

    /**
     * Loads a PDF document from a local file path.
     *
     * This function is suitable for loading PDFs stored in the application's internal or external
     * storage directories.
     * The viewer must be initialized before calling this method, which is best done inside
     * the [onReady] callback.
     *
     * **Note on Storage Access:** On modern Android versions (API 29+), direct file path access
     * to external storage is restricted. It is highly recommended to use the Storage Access Framework
     * and load files via a `content://` URI using [loadFromContentUri] for better compatibility
     * and user privacy. This method is best used for files within your app's sandboxed directories
     * (e.g., `context.getFilesDir()`).
     *
     * @param filePath The absolute path to the PDF file on the device's storage.
     *                 For example, `"/data/user/0/com.example.app/files/document.pdf"`.
     * @see loadFromContentUri
     * @see onReady
     * @see load
     * @throws PdfViewerNotInitializedException if the viewer has not been initialized yet.
     */
    fun loadFromFile(filePath: String) {
        openUrl(urlFor(FileResourceLoader.PATH, Uri.encode(filePath)))
    }

    /**
     * Loads a PDF document from a network URL.
     *
     * This method initiates the download and rendering of a PDF file located at the specified `url`.
     * By default, it uses a standard network request. To customize network behavior, such as adding
     * authentication headers or implementing custom caching, provide a [NetworkResourceHandler].
     * The viewer must be initialized before calling this method, which is best done inside
     * the [onReady] callback.
     *
     * @param url The complete `http://` or `https://` URL of the PDF document.
     * @see onReady
     * @see load
     * @see networkResourceHandler
     * @throws PdfViewerNotInitializedException if the viewer has not been initialized yet.
     */
    fun loadFromUrl(url: Uri) {
        loadFromUrl(url.toString())
    }

    /**
     * Loads a PDF document from a network URL.
     *
     * This method initiates the download and rendering of a PDF file located at the specified `url`.
     * By default, it uses a standard network request. To customize network behavior, such as adding
     * authentication headers or implementing custom caching, provide a [NetworkResourceHandler].
     * The viewer must be initialized before calling this method, which is best done inside
     * the [onReady] callback.
     *
     * @param url The complete `http://` or `https://` URL of the PDF document.
     * @see onReady
     * @see load
     * @see networkResourceHandler
     * @throws PdfViewerNotInitializedException if the viewer has not been initialized yet.
     */
    fun loadFromUrl(url: String) {
        openUrl(urlFor(NetworkResourceLoader.PATH, Uri.encode(url)))
    }

    private fun openUrl(url: String, originalUrl: String = url) {
        checkViewer()
        currentPage = 1
        pagesCount = 0
        currentPageScale = 0f
        currentPageScaleValue = ""
        properties = null
        currentSource = url

        listeners.forEach { it.onPageLoadStart() }
        webView callDirectly "openUrl"("{url: '$url', originalUrl: '$originalUrl'}")
    }

    /**
     * Registers a listener that will be called when the PDF viewer is fully initialized and ready to use.
     *
     * If the viewer is already initialized when this function is called, the provided `onReady`
     * block will be executed immediately.
     *
     * @param onReady A lambda with `PdfViewer` as its receiver, which will be executed
     *                when the viewer is initialized. This lambda has access to all public
     *                members and functions of the `PdfViewer` instance.
     * @see isInitialized
     * @see PdfViewerNotInitializedException
     */
    fun onReady(onReady: PdfViewer.() -> Unit) {
        onReadyListeners.add(onReady)
        if (isInitialized) onReady(this)
    }

    /**
     * Registers a listener to receive callbacks for various events occurring within the PDF viewer.
     *
     * @param listener The [PdfListener] implementation to be added.
     * @see PdfListener
     * @see removeListener
     * @see clearAllListeners
     */
    fun addListener(listener: PdfListener) {
        listeners.add(listener)
    }

    /**
     * Unregisters a listener to stop receiving callbacks about events in the `PdfViewer`.
     *
     * @param listener The [PdfListener] instance to be removed.
     * @see PdfListener
     * @see addListener
     * @see clearAllListeners
     */
    fun removeListener(listener: PdfListener) {
        listeners.remove(listener)
    }

    /**
     * Removes all registered listeners from the `PdfViewer`.
     *
     * This function clears both the [onReady] callbacks and all listeners added via [addListener].
     *
     * @see addListener
     * @see onReady
     */
    fun clearAllListeners() {
        onReadyListeners.clear()
        listeners.clear()
    }

    /**
     * Scrolls the document to a specific position based on a ratio.
     *
     * This function allows you to programmatically scroll the viewer to a relative position within
     * the entire scrollable area of the document. A ratio of `0.0` corresponds to the very beginning
     * of the document, and `1.0` corresponds to the very end.
     *
     * The direction of scrolling (horizontal or vertical) is determined automatically based on the
     * current [pageScrollMode], but can be overridden.
     *
     * @param ratio A float value between `0.0` and `1.0` representing the target scroll position.
     * @param isHorizontalScroll A boolean indicating the scroll direction. If `true`, the viewer
     *   scrolls horizontally; if `false`, it scrolls vertically. Defaults to `true` if the
     *   current [pageScrollMode] is `HORIZONTAL`, and `false` otherwise.
     * @see scrollTo
     * @see pageScrollMode
     */
    @JvmOverloads
    fun scrollToRatio(
        @FloatRange(from = 0.0, to = 1.0) ratio: Float,
        isHorizontalScroll: Boolean = pageScrollMode == PageScrollMode.HORIZONTAL
    ) {
        webView callDirectly "scrollToRatio"(ratio, isHorizontalScroll)
    }

    /**
     * Scrolls the view to a specific pixel offset.
     *
     * This method provides direct control over the scroll position of the document.
     * The behavior depends on the current [pageScrollMode]:
     * - In vertical scroll modes, it scrolls vertically.
     * - In horizontal scroll mode, it scrolls horizontally.
     *
     * @param offset The scroll position in pixels. Must be a non-negative integer.
     * @see scrollToRatio
     */
    fun scrollTo(@IntRange(from = 0) offset: Int) {
        webView callDirectly "scrollTo"(offset)
    }

    /**
     * Navigates the viewer to a specific page number.
     *
     * This method scrolls the document to bring the specified page into view.
     * The page number must be a 1-based index and fall within the valid range of
     * the document's total page count.
     *
     * @param pageNumber The 1-based page number to navigate to. Must be between 1 and [pagesCount].
     * @return `true` if the page number is valid and the navigation command was sent, `false` otherwise.
     * @see currentPage
     * @see pagesCount
     * @see goToNextPage
     * @see goToPreviousPage
     */
    fun goToPage(@IntRange(from = 1) pageNumber: Int): Boolean {
        if (pageNumber in 1..pagesCount) {
            webView set "page"(pageNumber)
            return true
        }

        return false
    }

    /**
     * Navigates to the next page of the document.
     *
     * This is a convenience method that is equivalent to calling `goToPage(currentPage + 1)`.
     * If the current page is the last page, this call will have no effect.
     *
     * @return `true` if the navigation was successful (i.e., not on the last page), `false` otherwise.
     * @see goToPage
     * @see goToPreviousPage
     * @see goToLastPage
     */
    fun goToNextPage() = goToPage(currentPage + 1)

    /**
     * Navigates to the previous page of the document.
     *
     * This is a convenience method that is equivalent to calling  `goToPage(currentPage - 1)`.
     * If the current page is the first page, this call will have no effect.
     *
     * @return `true` if the navigation was successful (i.e., not on the first page), `false` otherwise.
     * @see goToPage
     * @see goToNextPage
     * @see goToFirstPage
     */
    fun goToPreviousPage() = goToPage(currentPage - 1)

    /**
     * Navigates the view to the first page of the document.
     *
     * @see goToLastPage
     * @see goToPage
     * @see goToNextPage
     * @see goToPreviousPage
     */
    fun goToFirstPage() {
        webView callDirectly "goToFirstPage"()
    }

    /**
     * Navigates the view to the last page of the document.
     *
     * @see goToFirstPage
     * @see goToPage
     * @see goToNextPage
     * @see goToPreviousPage
     */
    fun goToLastPage() {
        webView callDirectly "goToLastPage"()
    }

    /**
     * Sets the zoom level of the current page to a specific scale.
     *
     * This method allows you to programmatically adjust the zoom. The scale can be provided in two ways:
     *
     * 1.  **Direct Scale Factor (Positive Float)**: A floating-point number representing the desired zoom level.
     *     For example, `1.5f` will set the zoom to 150%. The value will be automatically coerced to fit within
     *     the configured [actualMinPageScale] and [actualMaxPageScale] limits.
     *
     * 2.  **Preset Zoom Level ([Zoom] Enum)**: A negative float value corresponding to one of the predefined
     *     zoom behaviors from the [Zoom] enum.
     *     - `Zoom.AUTOMATIC.floatValue` (`-1f`): Automatically determines the best scale to fit the page.
     *     - `Zoom.PAGE_FIT.floatValue` (`-2f`): Fits the entire page within the view.
     *     - `Zoom.PAGE_WIDTH.floatValue` (`-3f`): Fits the width of the page within the view.
     *     - `Zoom.ACTUAL_SIZE.floatValue` (`-4f`): Sets the zoom to 100% (actual size).
     *
     * @param scale The desired zoom scale. It can be a direct float value (e.g., `1.0f` for 100%) or
     *              a negative constant from [Zoom] (e.g., `Zoom.PAGE_FIT.floatValue`). The accepted
     *              range for this parameter is from -4.0 to 10.0.
     *
     * @see zoomIn
     * @see zoomOut
     * @see zoomTo
     */
    fun scalePageTo(@FloatRange(from = -4.0, to = 10.0) scale: Float) {
        if (scale in ZOOM_SCALE_RANGE)
            zoomTo(Zoom.entries[abs(scale.toInt()) - 1])
        else {
            if (actualMaxPageScale < actualMinPageScale)
                throw RuntimeException("Max Page Scale($actualMaxPageScale) is less than Min Page Scale($actualMinPageScale)")
            webView set "pdfViewer.currentScale"(
                scale.coerceIn(actualMinPageScale, actualMaxPageScale)
            )
        }
    }

    /**
     * Increases the current zoom level by a predefined step.
     *
     * The zoom will stop once it reaches the maximum limit defined by [maxPageScale].
     *
     * @see zoomOut
     * @see scalePageTo
     * @see zoomTo
     * @see maxPageScale
     */
    fun zoomIn() {
        webView call "zoomIn"()
    }

    /**
     * Decreases the current zoom level by a predefined step.
     *
     * The zoom will stop once it reaches the minimum limit defined by [minPageScale].
     *
     * @see zoomIn
     * @see scalePageTo
     * @see zoomTo
     * @see minPageScale
     */
    fun zoomOut() {
        webView call "zoomOut"()
    }

    /**
     * Sets the zoom level of the document to one of the predefined [Zoom] presets.
     *
     * This method adjusts the view to a specific zoom level, such as fitting the page to the screen width
     * or displaying it at its actual size. The zoom operation will only be performed if the resulting
     * scale is within the currently configured [actualMinPageScale] and [actualMaxPageScale] limits.
     *
     * @param zoom The desired zoom preset, chosen from the [Zoom] enum (e.g., [Zoom.PAGE_WIDTH], [Zoom.PAGE_FIT]).
     * @see scalePageTo
     * @see zoomIn
     * @see zoomOut
     * @see Zoom
     */
    fun zoomTo(zoom: Zoom) {
        getActualScaleFor(zoom) { scale ->
            if (scale != null && scale in actualMinPageScale..actualMaxPageScale)
                webView set "pdfViewer.currentScaleValue"(zoom.value.toJsString())
        }
    }

    /**
     * Zooms the document to the maximum allowed scale.
     *
     * This convenience method scales the page to the value defined by [actualMaxPageScale].
     *
     * @see zoomToMinimum
     * @see zoomIn
     * @see scalePageTo
     * @see maxPageScale
     * @see actualMaxPageScale
     */
    fun zoomToMaximum() {
        scalePageTo(actualMaxPageScale)
    }

    /**
     * Zooms the document to its minimum allowed scale.
     *
     * This convenience method scales the page to the value defined by  [actualMinPageScale].
     *
     * @see zoomToMaximum
     * @see zoomOut
     * @see scalePageTo
     * @see minPageScale
     * @see actualMinPageScale
     */
    fun zoomToMinimum() {
        scalePageTo(actualMinPageScale)
    }

    /**
     * Checks if the current zoom level has reached its maximum allowed scale.
     *
     * @return `true` if the current zoom scale is equal to the maximum zoom scale, `false` otherwise.
     * @see isZoomInMinScale
     */
    fun isZoomInMaxScale(): Boolean {
        return currentPageScale == actualMaxPageScale
    }

    /**
     * Checks if the current zoom level has reached its minimum allowed scale.
     *
     * @return `true` if the current zoom scale is equal to the minimum zoom scale, `false` otherwise.
     * @see isZoomInMaxScale
     */
    fun isZoomInMinScale(): Boolean {
        return currentPageScale == actualMinPageScale
    }

    /**
     * Initiates the download of the currently loaded PDF file (includes edited annotations).
     *
     * Handling with `onSavePdf`:
     * ```kotlin
     * // In your Activity or Fragment, implement a PdfListener
     * pdfViewer.addListener(object : PdfListener {
     *      override fun onSavePdf(pdfAsBytes: ByteArray) {
     *          // save logic
     *      }
     * }
     */
    fun downloadFile() {
        webView callDirectly "downloadFile"()
    }

    /**
     * Initiates the printing process for the currently loaded PDF document (includes edited annotations).
     *
     * If the print process is already active, this method will do nothing.
     *
     * @param defaultFileName An optional name for the print job and the resulting file.
     *                        If provided, it will be set on the [PdfPrintAdapter.defaultFileName] property
     *                        before printing begins.
     * @see pdfPrintAdapter
     * @see com.rejowan.pdfreaderpro.presentation.components.pdf.print.PdfPrintAdapter
     * @throws RuntimeException if [pdfPrintAdapter] has not been set.
     */
    @JvmOverloads
    fun printFile(defaultFileName: String? = null) {
        pdfPrintAdapter?.also { pdfPrintAdapter ->
            if (pdfPrintAdapter.isPrinting)
                return

            defaultFileName?.let { fileName ->
                pdfPrintAdapter.defaultFileName = fileName
            }
        } ?: throw RuntimeException("PdfPrintAdapter has not been set!")

        webView callDirectly "printFile"()
    }

    /**
     * Switches the viewer to presentation mode.
     *
     * **Note:** This is an unstable API and its behavior may change in future versions.
     *
     * @see PdfUnstableApi
     */
    @PdfUnstableApi
    fun startPresentationMode() {
        webView callDirectly "startPresentationMode"()
    }

    /**
     * Rotates the document 90 degrees clockwise.
     *
     * This is a convenience method that cycles through the available rotations
     * ([PageRotation.R_0], [PageRotation.R_90], [PageRotation.R_180], [PageRotation.R_270])
     * in a clockwise direction. For example, if the current rotation is 90 degrees,
     * calling this method will change it to 180 degrees.
     *
     * @see rotateCounterClockWise
     * @see pageRotation
     * @see PageRotation
     */
    fun rotateClockWise() {
        pageRotation = PageRotation.entries.let { it[(it.indexOf(pageRotation) + 1) % it.size] }
    }

    /**
     * Rotates the document 90 degrees counter-clockwise.
     *
     * This is a convenience method that cycles through the available rotations in reverse order:
     * ([PageRotation.R_0], [PageRotation.R_90], [PageRotation.R_180], [PageRotation.R_270])
     * in a clockwise direction. For example, if the current rotation is 180 degrees,
     * calling this method will change it to 90 degrees.
     *
     * @see pageRotation
     * @see rotateClockWise
     * @see PageRotation
     */
    fun rotateCounterClockWise() {
        pageRotation = PageRotation.entries.let {
            it[(it.indexOf(pageRotation) - 1).let { index ->
                if (index < 0) index + it.size else index
            }]
        }
    }

    /**
     * Re-initializes the PDF viewer.
     *
     * This method effectively resets the viewer's state to its initial configuration,
     * forcing a complete reload of the PDF.js library and any associated resources.
     * This is useful when you need to apply configuration changes that only take effect
     * upon initialization, such as modifying the [highlightEditorColors].
     *
     * After calling this, the viewer will be in a non-initialized state ([isInitialized] will be `false`)
     * until the reload is complete. You will need to wait for the [onReady] callback to fire again
     * before interacting with the viewer or loading a new document.
     *
     * @see isInitialized
     * @see onReady
     */
    fun reInitialize() {
        isInitialized = false
        webView.reload()
    }

    /**
     * Sets the background color of the container that holds the PDF pages.
     *
     * This color is visible in the margins around the pages, especially when the page is zoomed out
     * or does not fill the entire view. It can be used to theme the viewer's backdrop to match
     * the application's design.
     *
     * If this method is called before the viewer is initialized (i.e., before the [onReady] callback),
     * the color will be stored and applied once the viewer is ready.
     *
     * @param color The `@ColorInt` value to be set as the background color.
     * @see onReady
     */
    fun setContainerBackgroundColor(@ColorInt color: Int) {
        if (!isInitialized) {
            tempBackgroundColor = color
            return
        }
        if (tempBackgroundColor != null) tempBackgroundColor = null

        webView with Body set "style.backgroundColor"(color.toJsRgba().toJsString())
    }

    /**
     * Sets the content padding for the PDF viewer container.
     * This adds padding inside the scrollable area so that PDF pages
     * have space at the top and bottom, allowing content to scroll
     * without being cut off by overlaying UI elements.
     *
     * @param top Top padding in pixels
     * @param bottom Bottom padding in pixels
     */
    fun setContentPadding(top: Int, bottom: Int) {
        if (!isInitialized) return

        val js = """
            (function() {
                var container = document.getElementById('viewerContainer');
                if (container) {
                    container.style.paddingTop = '${top}px';
                    container.style.paddingBottom = '${bottom}px';
                }
            })();
        """.trimIndent()
        webView.evaluateJavascript(js, null)
    }

    fun saveState(outState: Bundle) {
        webView.saveState(outState)
    }

    fun restoreState(inState: Bundle) {
        webView.restoreState(inState)
    }

    /**
     * Asynchronously retrieves the concrete floating-point scale value for a given symbolic [Zoom] preset.
     *
     * While symbolic zoom levels like [Zoom.PAGE_FIT] or [Zoom.PAGE_WIDTH] are convenient, their
     * actual scale factor depends on the current page dimensions and the viewer's size.
     *
     * This is useful when you need to know the exact scale before applying it, for instance, to check if it falls
     * within the `minPageScale` and `maxPageScale` limits.
     *
     * @param zoom The symbolic zoom level (e.g., `Zoom.PAGE_WIDTH`) for which to calculate the actual scale.
     * @param callback A lambda function that will be invoked with the calculated scale as a `Float`.
     *                 The value will be `null` if the scale could not be determined.
     */
    fun getActualScaleFor(zoom: Zoom, callback: (scale: Float?) -> Unit) {
        webView callDirectly "getActualScaleFor"(zoom.value.toJsString()) {
            callback(it?.toFloatOrNull())
        }
    }

    /**
     * Creates a shareable `Uri` for the currently loaded PDF document.
     *
     * This method is useful for sharing the PDF file with other applications, such as via an `Intent`.
     * It identifies the appropriate `ResourceLoader` for the current document source and uses it
     * to generate a `Uri` that can be exposed through a `FileProvider`.
     *
     * @param authority The authority of a `FileProvider` defined in your application's manifest.
     *                  This is typically in the format `com.your.package.name.fileprovider`.
     * @return A shareable `Uri` for the current PDF, or `null` if the source cannot be shared
     *         (e.g., a network URL) or if no document is currently loaded.
     * @see currentSource
     */
    fun createSharableUri(authority: String): Uri? {
        return resourceLoaders
            .firstOrNull { it.canHandle(Uri.parse(currentSource ?: return null)) }
            ?.createSharableUri(context, authority, currentSource ?: return null)
    }

    /**
     * Sets the color for the text selection highlight.
     *
     * This function allows you to customize the appearance of the highlight that appears
     * when a user selects text within the document. It is useful for matching the app's
     * theme or for providing visual feedback during text-related operations, like
     * highlighting.
     *
     * @param color The base color for the selection highlight, specified as an `@ColorInt`.
     * @param alpha The alpha (transparency) value for the selection color, ranging from
     *              0 (fully transparent) to 255 (fully opaque). The default value is `64`,
     *              which provides a semi-transparent highlight.
     *
     * @see resetTextSelectionColor
     */
    @JvmOverloads
    fun setTextSelectionColor(
        @ColorInt color: Int,
        @IntRange(0, 255) alpha: Int = 64,
    ) {
        val finalColor = Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color))
        webView callDirectly "setTextSelectionColor"(finalColor.toJsHex().toJsString())
    }

    /**
     * Resets the text selection highlight color to its default appearance.
     *
     * This function undoes any custom color set by [setTextSelectionColor], restoring
     * the system's default selection style. It is typically called
     * after a temporary color change, such as when finishing a text highlighting action.
     *
     * @see setTextSelectionColor
     */
    fun resetTextSelectionColor() {
        webView callDirectly "removeTextSelectionColor"()
    }

    /**
     * Clears any active text selection in the document.
     */
    fun removeTextSelection() {
        webView callDirectly "window.getSelection().removeAllRanges"()
    }

    override fun setLayerType(layerType: Int, paint: Paint?) {
        super.setLayerType(layerType, paint)
        webView.setLayerType(layerType, paint)
    }

    override fun startActionModeForChild(
        originalView: View?,
        callback: ActionMode.Callback?,
        type: Int
    ): ActionMode? {
        return super.startActionModeForChild(
            originalView,
            actionModeCallbackWrapper(callback),
            type
        )
    }

    override fun startActionMode(callback: ActionMode.Callback?, type: Int): ActionMode? {
        return super.startActionMode(actionModeCallbackWrapper(callback), type)
    }

    private fun customSelectionModeCallback(callback: ActionMode.Callback?): ActionMode.Callback {
        val actionModeCallback = callback ?: simpleActionModeCallback

        return object : ActionMode.Callback2(), PdfListener {
            override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?) =
                actionModeCallback.onActionItemClicked(mode, item)

            override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?) =
                actionModeCallback.onPrepareActionMode(mode, menu)

            override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
                if (editor.run { applyHighlightColorOnTextSelection && textHighlighterOn }) {
                    setTextSelectionColor(editor.highlightColor)
                    menu?.clear()
                    mode?.finish()

                    setTextSelectionColor(editor.highlightColor)
                    addListener(this)
                    return true
                }

                return actionModeCallback.onCreateActionMode(mode, menu)
            }

            override fun onDestroyActionMode(mode: ActionMode?) {
                if (editor.run { applyHighlightColorOnTextSelection && textHighlighterOn }) {
                    resetTextSelectionColor()
                    removeListener(this)
                }

                webView setDirectly "isContextMenuActive"(false)
                actionModeCallback.onDestroyActionMode(mode)
            }

            override fun onGetContentRect(
                mode: ActionMode?,
                view: View?,
                outRect: Rect?
            ) {
                if (actionModeCallback is ActionMode.Callback2)
                    actionModeCallback.onGetContentRect(mode, view, outRect)
                else super.onGetContentRect(mode, view, outRect)
            }

            override fun onEditorHighlightColorChange(highlightColor: Int) {
                setTextSelectionColor(highlightColor)
            }
        }
    }

    internal fun loadPage() {
        webView.loadUrl(PDF_VIEWER_URL)
    }

    internal fun checkViewer() {
        if (!isInitialized) throw PdfViewerNotInitializedException()
    }

    internal fun dispatchRotationChange(
        pageRotation: PageRotation,
        dispatchToListener: Boolean = true,
    ) {
        dispatch(
            dispatchToListener = dispatchToListener,
            callListener = { onRotationChange(pageRotation) }
        ) {
            webView set "pdfViewer.pagesRotation"(pageRotation.degree)
        }
    }

    internal fun dispatchSnapChange(
        snapPage: Boolean,
        dispatchToListener: Boolean = true,
    ) {
        dispatch(
            dispatchToListener = dispatchToListener,
            callListener = { onSnapChange(snapPage) }
        ) {
            if (snapPage) {
                when (pageScrollMode) {
                    PageScrollMode.HORIZONTAL -> webView callDirectly "enableHorizontalSnapBehavior"()
                    PageScrollMode.VERTICAL, PageScrollMode.WRAPPED -> webView callDirectly "enableVerticalSnapBehavior"()
                    else -> {}
                }
            } else webView callDirectly "removeSnapBehavior"()
        }
    }

    internal fun dispatchPageAlignMode(
        pageAlignMode: PageAlignMode,
        dispatchToListener: Boolean = true,
    ): PageAlignMode {
        return dispatch(
            dispatchToListener = dispatchToListener,
            callListener = { onAlignModeChange(pageAlignMode, it) }
        ) {
            val alignMode = adjustAlignMode(pageAlignMode)
            webView callDirectly "centerPage"(
                alignMode.vertical,
                alignMode.horizontal,
                singlePageArrangement
            )
            alignMode
        }
    }

    internal fun adjustAlignMode(alignMode: PageAlignMode): PageAlignMode {
        if (singlePageArrangement) return alignMode

        when (pageScrollMode) {
            PageScrollMode.VERTICAL, PageScrollMode.WRAPPED -> {
                if (alignMode == PageAlignMode.CENTER_VERTICAL || alignMode == PageAlignMode.CENTER_BOTH)
                    return PageAlignMode.DEFAULT
            }

            PageScrollMode.HORIZONTAL -> {
                if (alignMode == PageAlignMode.CENTER_HORIZONTAL || alignMode == PageAlignMode.CENTER_BOTH)
                    return PageAlignMode.DEFAULT
            }

            PageScrollMode.SINGLE_PAGE -> {}
        }

        return alignMode
    }

    internal fun dispatchSinglePageArrangement(
        singlePageArrangement: Boolean,
        dispatchToListener: Boolean = true,
    ): Boolean {
        return dispatch(
            dispatchToListener = dispatchToListener,
            callListener = { onSinglePageArrangementChange(singlePageArrangement, it) }
        ) {
            val newValue =
                if (singlePageArrangement) {
                    (pageScrollMode == PageScrollMode.VERTICAL || pageScrollMode == PageScrollMode.HORIZONTAL)
                            && pageSpreadMode == PageSpreadMode.NONE
                } else false
            webView callDirectly if (newValue) "applySinglePageArrangement"() else "removeSinglePageArrangement"()
            newValue
        }
    }

    internal fun dispatchHighlightColor(
        highlightColor: Int,
        dispatchToListener: Boolean = true,
    ) {
        dispatch(
            dispatchToListener = dispatchToListener,
            callListener = { onEditorHighlightColorChange(highlightColor) }
        ) {
            webView callDirectly "selectHighlightColor"(
                highlightColor.toJsHex(includeAlpha = false).toJsString()
            )
        }
    }

    internal fun dispatchShowAllHighlights(
        showAll: Boolean,
        dispatchToListener: Boolean = true,
    ) {
        dispatch(
            dispatchToListener = dispatchToListener,
            callListener = { onEditorShowAllHighlightsChange(showAll) }
        ) {
            webView callDirectly if (showAll) "showAllHighlights"() else "hideAllHighlights"()
        }
    }

    internal fun dispatchHighlightThickness(
        @IntRange(from = 8, to = 24) thickness: Int,
        dispatchToListener: Boolean = true,
    ) {
        dispatch(
            dispatchToListener = dispatchToListener,
            callListener = { onEditorHighlightThicknessChange(thickness) }
        ) {
            webView callDirectly "setHighlighterThickness"(thickness)
        }
    }

    internal fun dispatchFreeFontColor(
        @ColorInt fontColor: Int,
        dispatchToListener: Boolean = true,
    ) {
        dispatch(
            dispatchToListener = dispatchToListener,
            callListener = { onEditorFreeFontColorChange(fontColor) }
        ) {
            webView callDirectly "setFreeTextFontColor"(
                fontColor.toJsHex(includeAlpha = false).toJsString()
            )
        }
    }

    internal fun dispatchFreeFontSize(
        @IntRange(from = 5, to = 100) fontSize: Int,
        dispatchToListener: Boolean = true,
    ) {
        dispatch(
            dispatchToListener = dispatchToListener,
            callListener = { onEditorFreeFontSizeChange(fontSize) }
        ) {
            webView callDirectly "setFreeTextFontSize"(fontSize)
        }
    }

    internal fun dispatchInkColor(
        @ColorInt color: Int,
        dispatchToListener: Boolean = true,
    ) {
        dispatch(
            dispatchToListener = dispatchToListener,
            callListener = { onEditorInkColorChange(color) }
        ) {
            webView callDirectly "setInkColor"(color.toJsHex(includeAlpha = false).toJsString())
        }
    }

    internal fun dispatchInkThickness(
        @IntRange(from = 1, to = 20) thickness: Int,
        dispatchToListener: Boolean = true,
    ) {
        dispatch(
            dispatchToListener = dispatchToListener,
            callListener = { onEditorInkThicknessChange(thickness) }
        ) {
            webView callDirectly "setInkThickness"(thickness)
        }
    }

    internal fun dispatchInkOpacity(
        @IntRange(from = 1, to = 100) opacity: Int,
        dispatchToListener: Boolean = true,
    ) {
        dispatch(
            dispatchToListener = dispatchToListener,
            callListener = { onEditorInkOpacityChange(opacity) }
        ) {
            webView callDirectly "setInkOpacity"(opacity)
        }
    }

    internal fun dispatchScrollSpeedLimit(
        scrollSpeedLimit: ScrollSpeedLimit,
        dispatchToListener: Boolean = true,
    ): ScrollSpeedLimit {
        return dispatch(
            dispatchToListener = dispatchToListener,
            callListener = { onScrollSpeedLimitChange(scrollSpeedLimit, it) }
        ) {
            if (!singlePageArrangement) {
                webView callDirectly "removeScrollLimit"()
                return@dispatch ScrollSpeedLimit.None
            }
            webView callDirectly when (scrollSpeedLimit) {
                is ScrollSpeedLimit.AdaptiveFling -> "limitScroll"(
                    scrollSpeedLimit.limit,
                    scrollSpeedLimit.flingThreshold,
                    true,
                    true,
                )

                is ScrollSpeedLimit.Fixed -> "limitScroll"(
                    scrollSpeedLimit.limit,
                    scrollSpeedLimit.flingThreshold,
                    scrollSpeedLimit.canFling,
                    false,
                )

                ScrollSpeedLimit.None -> "removeScrollLimit"()
            }
            scrollSpeedLimit
        }
    }

    private inline fun <T> dispatch(
        dispatchToListener: Boolean = true,
        callListener: PdfListener.(result: T) -> Unit,
        block: () -> T,
    ): T {
        val result = block()
        if (dispatchToListener) listeners.forEach { callListener(it, result) }
        return result
    }

    internal fun adjustAlignModeAndArrangementMode(scrollMode: PageScrollMode) {
        if (singlePageArrangement) {
            if (scrollMode != PageScrollMode.VERTICAL && scrollMode != PageScrollMode.HORIZONTAL)
                singlePageArrangement = false
            else return
        }

        when (scrollMode) {
            PageScrollMode.VERTICAL, PageScrollMode.WRAPPED -> {
                if (pageAlignMode == PageAlignMode.CENTER_VERTICAL || pageAlignMode == PageAlignMode.CENTER_BOTH)
                    pageAlignMode = PageAlignMode.DEFAULT
            }

            PageScrollMode.HORIZONTAL -> {
                if (pageAlignMode == PageAlignMode.CENTER_HORIZONTAL || pageAlignMode == PageAlignMode.CENTER_BOTH)
                    pageAlignMode = PageAlignMode.DEFAULT
            }

            PageScrollMode.SINGLE_PAGE -> {
                pageAlignMode = pageAlignMode
            }
        }
    }

    internal fun setUpActualScaleValues(callback: () -> Unit) {
        var isMinSet = false
        var isMaxSet = false
        var isDefaultSet = false
        fun checkAndCall() {
            if (isMinSet && isMaxSet && isDefaultSet) callback()
        }

        if (minPageScale in ZOOM_SCALE_RANGE)
            getActualScaleFor(Zoom.entries[abs(minPageScale.toInt()) - 1]) {
                actualMinPageScale = it ?: actualMinPageScale
                isMinSet = true
                checkAndCall()
            }
        else {
            actualMinPageScale = minPageScale
            isMinSet = true
            checkAndCall()
        }

        if (maxPageScale in ZOOM_SCALE_RANGE)
            getActualScaleFor(Zoom.entries[abs(maxPageScale.toInt()) - 1]) {
                actualMaxPageScale = it ?: actualMaxPageScale
                isMaxSet = true
                checkAndCall()
            }
        else {
            actualMaxPageScale = maxPageScale
            isMaxSet = true
            checkAndCall()
        }

        if (defaultPageScale in ZOOM_SCALE_RANGE)
            getActualScaleFor(Zoom.entries[abs(defaultPageScale.toInt()) - 1]) {
                actualDefaultPageScale = it ?: actualDefaultPageScale
                isDefaultSet = true
                checkAndCall()
            }
        else {
            actualDefaultPageScale = defaultPageScale
            isDefaultSet = true
            checkAndCall()
        }
    }

    /**
     * Represents predefined zoom levels for scaling the PDF page.
     *
     * These presets are used to control how the document is scaled within the viewport.
     * They can be applied using `scalePageTo()`, and their behavior can be constrained
     * by setting `minPageScale` and `maxPageScale`.
     *
     * @see scalePageTo
     * @see minPageScale
     * @see maxPageScale
     * @see currentPageScale
     * @see currentPageScaleValue
     */
    enum class Zoom(internal val value: String, val floatValue: Float) {
        /**
         * Automatically adjusts the zoom level to fit the content optimally within the available space.
         */
        AUTOMATIC("auto", -1f),

        /**
         * Scales the page to fit entirely within the viewport, showing the whole page at once.
         */
        PAGE_FIT("page-fit", -2f),

        /**
         * Scales the page to match the width of the viewport.
         */
        PAGE_WIDTH("page-width", -3f),

        /**
         * Displays the page at its actual size (100% scale), without any scaling.
         */
        ACTUAL_SIZE("page-actual", -4f)
    }

    /**
     * Defines the interaction mode for the mouse cursor within the PDF viewer.
     *
     * This enum is used to switch between different cursor behaviors, allowing the user
     * to either select text or to pan and scroll the document.
     *
     * @see cursorToolMode
     */
    enum class CursorToolMode(internal val function: String) {
        /**
         * Sets the cursor to text selection mode.
         */
        TEXT_SELECT("selectCursorSelectTool"),

        /**
         * Sets the cursor to hand (panning) mode.
         */
        HAND("selectCursorHandTool")
    }

    /**
     * Specifies the scrolling behavior and layout of pages in the PDF viewer.
     *
     * This enum determines how the document's pages are arranged and how the user navigates through them.
     *
     * @see pageScrollMode
     */
    enum class PageScrollMode(internal val function: String) {
        /**
         * Arranges pages in a single vertical column.
         */
        VERTICAL("selectScrollVertical"),

        /**
         * Arranges pages in a single horizontal row.
         */
        HORIZONTAL("selectScrollHorizontal"),

        /**
         * Displays pages in a wrapped, grid-like layout.
         * Pages flow to fill the available width before starting a new row.
         */
        WRAPPED("selectScrollWrapped"),

        /**
         * Displays only one page at a time.
         */
        SINGLE_PAGE("selectScrollPage")
    }

    /**
     * Configures the display of pages in a spread, similar to an open book.
     *
     * This enum controls how pages are laid out side-by-side.
     *
     * @see pageSpreadMode
     */
    enum class PageSpreadMode(internal val function: String) {
        /**
         * Disables spreads and displays pages individually. This is the default behavior.
         */
        NONE("selectSpreadNone"),

        /**
         * Arranges pages in spreads, starting with an odd-numbered page on the right.
         * For example, page 1 will be by itself, and pages 2 and 3 will form a spread.
         */
        ODD("selectSpreadOdd"),

        /**
         * Arranges pages in spreads, starting with an even-numbered page on the left.
         * For example, pages 1 and 2 will form the first spread.
         */
        EVEN("selectSpreadEven")
    }

    /**
     * Represents the possible rotation values for a PDF page.
     *
     * This enum defines fixed degrees of rotation that can be applied to a page,
     * allowing for rotation in 90-degree increments.
     *
     * @see rotateClockWise
     * @see rotateCounterClockWise
     * @see pageRotation
     */
    enum class PageRotation(internal val degree: Int) {
        /**
         * No rotation is applied; the page is displayed in its default orientation.
         */
        R_0(0),

        /**
         * Rotates the page 90 degrees clockwise.
         */
        R_90(90),

        /**
         * Rotates the page 180 degrees.
         */
        R_180(180),

        /**
         * Rotates the page 270 degrees clockwise.
         */
        R_270(270),
    }

    /**
     * Defines the alignment of the page within the viewer's viewport.
     *
     * This enum controls how a page is positioned when there is extra space available,
     * for example, when the page is zoomed out. It can be centered vertically, horizontally, or both.
     * This mode is affected by `pageAlignMode`, `pageScrollMode` and `singlePageArrangement`.
     *
     * @see pageAlignMode
     * @see pageScrollMode
     * @see singlePageArrangement
     */
    enum class PageAlignMode(internal val vertical: Boolean, internal val horizontal: Boolean) {
        /**
         * Default alignment based on the `pageScrollMode`.
         */
        DEFAULT(false, false),

        /**
         * Centers the page vertically within the viewport.
         */
        CENTER_VERTICAL(true, false),

        /**
         * Centers the page horizontally within the viewport.
         */
        CENTER_HORIZONTAL(false, true),

        /**
         * Centers the page both vertically and horizontally within the viewport.
         */
        CENTER_BOTH(true, true),
    }

    /**
     * Controls the scrolling and flinging behavior in `SINGLE_PAGE` mode.
     *
     * This sealed class provides different strategies for limiting the scroll speed and managing
     * fling gestures, which is particularly useful when displaying pages one at a time.
     *
     * @see scrollSpeedLimit
     * @see pageScrollMode
     */
    sealed class ScrollSpeedLimit {

        /**
         * Default behavior where no scroll or fling limits are applied.
         * Scrolling is unrestricted.
         */
        data object None : ScrollSpeedLimit()

        /**
         * Applies a fixed scroll speed limit and provides explicit control over flinging.
         *
         * @param limit The maximum scroll velocity. Must be a positive value.
         * @param flingThreshold The velocity threshold required to initiate a fling gesture.
         * @param canFling Determines whether the user can fling.
         */
        data class Fixed(
            @param:FloatRange(from = 0.0, fromInclusive = false) val limit: Float = 100f,
            @param:FloatRange(from = 0.0, fromInclusive = false) val flingThreshold: Float = 0.5f,
            val canFling: Boolean = false,
        ) : ScrollSpeedLimit()

        /**
         * Applies a scroll speed limit while allowing flinging only when the page size
         * is less than its container's size.
         *
         * @param limit The maximum scroll velocity. Must be a positive value.
         * @param flingThreshold The velocity threshold required to initiate a fling gesture.
         */
        data class AdaptiveFling(
            @param:FloatRange(from = 0.0, fromInclusive = false) val limit: Float = 100f,
            @param:FloatRange(from = 0.0, fromInclusive = false) val flingThreshold: Float = 0.5f,
        ) : ScrollSpeedLimit()
    }

    companion object {
        internal const val PDF_VIEWER_URL =
            "https://${ResourceLoader.RESOURCE_DOMAIN}${PdfViewerResourceLoader.PATH}com/rejowan/mozilla/pdfjs/pdf_viewer.html"
        private const val COLOR_NOT_FOUND = 11
        private val ZOOM_SCALE_RANGE = -4f..-1f

        /**
         * Controls suppression of WebView console logs.
         *
         * IMPORTANT: Set to 'false' *only* when debugging WebView behavior,
         * as it will allow console logs to appear. Keep it 'true' in production
         * or non-debug scenarios to avoid unnecessary log noise.
         */
        var preventWebViewConsoleLog = true

        /**
         * Defines the list of default highlight colors for the editor's highlight color palette.
         *
         * @see com.rejowan.pdfreaderpro.presentation.components.pdf.PdfViewer.highlightEditorColors
         */
        val defaultHighlightEditorColors = listOf(
            "yellow" to Color.parseColor("#FFFF98"),
            "green" to Color.parseColor("#53FFBC"),
            "blue" to Color.parseColor("#80EBFF"),
            "pink" to Color.parseColor("#FFCBE6"),
            "red" to Color.parseColor("#FF4F5F"),
        )

        @Suppress("NOTHING_TO_INLINE")
        private inline fun urlFor(path: String, source: String) =
            "https://${ResourceLoader.RESOURCE_DOMAIN}$path$source"
    }

    private fun setPreviews(context: Context, containerBgColor: Int) {
        addView(
            LinearLayout(context).apply {
                orientation = VERTICAL
                if (containerBgColor == COLOR_NOT_FOUND) {
                    if (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES)
                        setBackgroundColor(Color.parseColor("#2A2A2E"))
                    else setBackgroundColor(Color.parseColor("#d4d4d7"))
                } else setBackgroundColor(containerBgColor)
                addView(createPageView(context, 1))
                addView(createPageView(context, 2))
                addView(createPageView(context, 3))
            },
            LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        )
    }

    private fun createPageView(context: Context, pageNo: Int): View {
        return TextView(context).apply {
            setBackgroundColor(Color.WHITE)
            layoutParams =
                LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, 1f).apply {
                    setMargins(24, 24, 24, 0)
                }
            gravity = Gravity.CENTER
            text = context.getString(R.string.page_current, pageNo)
            setTextColor(Color.BLACK)
        }
    }

    private val simpleActionModeCallback = object : ActionMode.Callback {
        override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?) = false
        override fun onCreateActionMode(mode: ActionMode?, menu: Menu?) = true
        override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?) = false
        override fun onDestroyActionMode(mode: ActionMode?) {}
    }
}
