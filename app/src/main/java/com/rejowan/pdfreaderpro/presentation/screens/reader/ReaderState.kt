package com.rejowan.pdfreaderpro.presentation.screens.reader

import com.rejowan.pdfreaderpro.data.local.database.entity.BookmarkEntity
import com.rejowan.pdfreaderpro.domain.model.Highlight
import com.rejowan.pdfreaderpro.presentation.components.pdf.model.SelectionAnchor
import com.rejowan.pdfreaderpro.presentation.components.pdf.model.TappedHighlight
import com.rejowan.pdfreaderpro.presentation.components.pdf.model.TextSelection
import com.rejowan.pdfreaderpro.presentation.screens.reader.components.AttachmentItem
import com.rejowan.pdfreaderpro.presentation.screens.reader.components.OutlineItem

/**
 * Complete state for the PDF Reader screen.
 */
data class ReaderState(
    // Loading and error states
    val isLoading: Boolean = true,
    val error: String? = null,

    // Document info
    val documentPath: String = "",
    val documentTitle: String? = null,
    val totalPages: Int = 0,

    // Navigation
    val currentPage: Int = 0,

    // Zoom and scroll
    val zoom: Float = 1f,
    val minZoom: Float = 0.5f,
    val maxZoom: Float = 5f,
    val doubleTapZoom: Float = 2f,
    val scrollMode: ScrollMode = ScrollMode.VERTICAL,
    val isSnapEnabled: Boolean = false,

    // UI visibility
    val isToolbarVisible: Boolean = true,
    val isControlBarExpanded: Boolean = false,
    val isFullScreen: Boolean = false,
    val showQuickActions: Boolean = true,

    // Reader settings
    val brightness: Float = 1f,
    val keepScreenOn: Boolean = true,

    // Search
    val isSearchActive: Boolean = false,
    val searchQuery: String = "",
    val isSearching: Boolean = false,
    val searchResultCount: Int = 0,
    // 1-based: 1 = first match, searchResultCount = last match. 0 means no selection.
    val currentSearchIndex: Int = 0,

    // Table of Contents & Attachments
    val isTableOfContentsVisible: Boolean = false,
    val outline: List<OutlineItem> = emptyList(),
    val attachments: List<AttachmentItem> = emptyList(),

    // Page thumbnails
    val isPageThumbnailsVisible: Boolean = false,

    // Password
    val isPasswordRequired: Boolean = false,
    val isPasswordError: Boolean = false,
    val passwordSubmitted: Boolean = false,

    /** True once this document has been opened with a password. */
    val isPasswordProtected: Boolean = false,

    /** Set while a decrypted copy is being written. */
    val isSavingDecryptedCopy: Boolean = false,

    /** The app's own signature sheet is showing. */
    val isSignatureSheetVisible: Boolean = false,

    /** Signatures the user has kept for reuse. */
    val savedSignatures: List<SavedSignatureUi> = emptyList(),

    /**
     * How many signatures are placed but not written to a copy yet. Drives the
     * save prompt, which stays out of the way until there is something to save.
     */
    val placedSignatures: Int = 0,

    /** Set while the signed copy is being written out. */
    val isSavingSignedCopy: Boolean = false,

    /**
     * Whether the signatures can be written back into the document itself.
     *
     * False when the reader is showing a cache copy, which happens for anything
     * opened through a content uri. Writing there would succeed and change
     * nothing the user can see, so the option is not offered.
     */
    val canSaveInPlace: Boolean = false,

    /** Confirmation before signatures are written into the document itself. */
    val isSignSaveConfirmVisible: Boolean = false,

    /**
     * Progress of the print render, 0..1, or null when no print job is running.
     * Preparing a print job rasterises every page and can take the better part of
     * a minute, so it needs to be visible rather than silent.
     */
    val printProgress: Float? = null,

    // Page jump dialog
    val isPageJumpDialogVisible: Boolean = false,

    // Settings panel
    val isSettingsPanelVisible: Boolean = false,

    // Bottom bar sheets
    val isViewModeSheetVisible: Boolean = false,
    val isZoomSheetVisible: Boolean = false,
    val isDisplaySheetVisible: Boolean = false,
    val isBookmarksSheetVisible: Boolean = false,
    val isMoreOptionsSheetVisible: Boolean = false,

    // Screen orientation
    val screenOrientation: ScreenOrientation = ScreenOrientation.AUTO,

    // Reading theme
    val readingTheme: ReadingTheme = ReadingTheme.LIGHT,

    // Auto-hide toolbar
    val autoHideToolbar: Boolean = false,

    // Show the page scrubber on scroll while the toolbar is hidden
    val scrubberOnScroll: Boolean = false,

    // Tap the sides of the page to move between pages
    val tapToTurnPage: Boolean = false,

    // PDF info dialog
    val isInfoDialogVisible: Boolean = false,

    // Delete confirmation dialog
    val isDeleteDialogVisible: Boolean = false,

    // Rotation lock
    val isRotationLocked: Boolean = false,

    // Page rotation (0, 90, 180, 270)
    val pageRotation: Int = 0,

    // Current page bookmark status
    val isCurrentPageBookmarked: Boolean = false,

    // Bookmarks for current PDF
    val bookmarks: List<BookmarkEntity> = emptyList(),

    // Auto-scroll
    val isAutoScrollActive: Boolean = false,
    val isAutoScrollPaused: Boolean = false,
    val autoScrollSpeed: Float = 50f, // pixels per second
    val isAutoScrollSheetVisible: Boolean = false,

    // Favorite status
    val isFavorite: Boolean = false,

    // Top bar menu
    val isTopBarMenuVisible: Boolean = false,

    // Remove favourite confirmation
    val isRemoveFavoriteDialogVisible: Boolean = false,

    // Highlights the app owns, from its database, in reading order
    val highlights: List<Highlight> = emptyList(),

    /**
     * Highlights the PDF file itself carries, read only. Either baked by us earlier
     * or added by another application.
     */
    val documentHighlights: List<Highlight> = emptyList(),

    // The live text selection, non-null while the user has text selected
    val pendingSelection: TextSelection? = null,

    /**
     * Snapshot taken when the user chooses to highlight.
     *
     * Held separately from [pendingSelection] because dismissing the selection
     * action mode clears the underlying selection, which would otherwise pull the
     * text out from under the colour picker before a colour is chosen.
     */
    val capturedSelection: TextSelection? = null,

    // Colour picker, shown for a new highlight or when editing an existing one
    val isHighlightPickerVisible: Boolean = false,

    // Set when the picker is editing an existing highlight rather than creating one
    val editingHighlightId: Long? = null,

    /** Where the highlight being edited sits, so its bar anchors to it. */
    val editingAnchor: SelectionAnchor? = null,

    val isHighlightsSheetVisible: Boolean = false,

    /** Pre-filled search for the highlights panel, set when opening it from search. */
    val highlightsSheetQuery: String = "",

    /**
     * Index into [highlights] of the highlight currently being stepped through,
     * or -1 when none is.
     */
    val currentHighlightIndex: Int = -1,

    /** The prev/next strip, shown after jumping to a highlight. */
    val isHighlightNavVisible: Boolean = false,

    /** Confirmation for writing highlights into a copy of the PDF. */
    val isBakeHighlightsDialogVisible: Boolean = false,

    val isBakingHighlights: Boolean = false,

    /**
     * Freezes horizontal panning at the page's current position. Stored per
     * document, not globally, and only meaningful in vertical scroll mode.
     */
    val lockHorizontalScroll: Boolean = false
) {
    val pageLabel: String
        get() = "${currentPage + 1} / $totalPages"

    /** The highlight the colour picker is currently editing, if any. */
    val editingHighlight: Highlight?
        get() = editingHighlightId?.let { id -> highlights.firstOrNull { it.id == id } }

    /**
     * Whether the horizontal lock can be used right now.
     *
     * Horizontal scroll mode needs that axis to move between pages, so the toggle
     * is shown disabled rather than hidden, which would leave people hunting for a
     * setting that silently vanished.
     */
    val canLockHorizontalScroll: Boolean
        get() = scrollMode == ScrollMode.VERTICAL

    /**
     * Everything the panel, navigation and search work from: the app's own
     * highlights and the document's own, in one reading order.
     */
    val allHighlights: List<Highlight>
        get() = (highlights + documentHighlights)
            .sortedWith(compareBy({ it.pageNumber }, { it.sortIndex }, { it.createdAt }))

    /** 1-based position for the navigation strip, e.g. "3 / 12". */
    val highlightPositionLabel: String
        get() = "${currentHighlightIndex + 1} / ${allHighlights.size}"

    /**
     * Highlights whose text matches the current search.
     *
     * Lets the search bar show how many of the hits the user has already marked.
     * Matched here rather than against the viewer's own results because PDF.js
     * reports match counts, not match positions, so there is nothing to intersect
     * a stored quad against.
     */
    val highlightsMatchingSearch: List<Highlight>
        get() = if (searchQuery.isBlank()) {
            emptyList()
        } else {
            allHighlights.filter { it.text.contains(searchQuery, ignoreCase = true) }
        }
}

/**
 * Scroll mode for PDF viewer.
 */
enum class ScrollMode {
    VERTICAL,
    HORIZONTAL
}

/**
 * Screen orientation options.
 */
enum class ScreenOrientation {
    AUTO,
    PORTRAIT,
    LANDSCAPE
}

/**
 * Reading theme options.
 */
enum class ReadingTheme {
    LIGHT,
    DARK,
    SEPIA,
    BLACK  // AMOLED black
}

/**
 * Reader events for one-time actions.
 */
/** A stored signature, as the reader needs it. */
data class SavedSignatureUi(
    val id: String,
    val filePath: String
)

sealed class ReaderEvent {
    data class ShowMessage(val message: String) : ReaderEvent()
    data class NavigateToPage(val page: Int) : ReaderEvent()
    data object DocumentClosed : ReaderEvent()
    data object DocumentDeleted : ReaderEvent()
    data object ShareDocument : ReaderEvent()
    data object SaveDocumentPicker : ReaderEvent()
    data object BakeHighlightsPicker : ReaderEvent()
    data object SaveDecryptedCopyPicker : ReaderEvent()
    data object SaveSignedCopyPicker : ReaderEvent()
    data object FavoriteAdded : ReaderEvent()
    data class Error(val message: String) : ReaderEvent()
}

/**
 * Actions that can be performed on the reader.
 */
sealed class ReaderAction {
    // Navigation
    data class GoToPage(val page: Int) : ReaderAction()
    data object NextPage : ReaderAction()
    data object PreviousPage : ReaderAction()

    // A single tap on the page: turn the page when tap-to-turn is enabled and the tap
    // lands in an edge zone, otherwise toggle the toolbar. Coordinates and dimensions are
    // in CSS pixels relative to the viewer container's top-left.
    data class TapToTurnOrToggle(val x: Float, val y: Float, val width: Float, val height: Float) : ReaderAction()

    // Zoom
    data class SetZoom(val zoom: Float) : ReaderAction()
    data object ZoomIn : ReaderAction()
    data object ZoomOut : ReaderAction()
    data object ResetZoom : ReaderAction()
    data object ZoomFitPage : ReaderAction()
    data object ZoomFitWidth : ReaderAction()
    data object ZoomActualSize : ReaderAction()
    data class SetDoubleTapZoom(val zoom: Float) : ReaderAction()

    // UI
    data object ToggleToolbar : ReaderAction()
    data object ToggleControlBarExpanded : ReaderAction()
    data object ToggleFullScreen : ReaderAction()
    data object ToggleQuickActions : ReaderAction()
    data object ShowPageJumpDialog : ReaderAction()
    data object HidePageJumpDialog : ReaderAction()
    data object ShowTableOfContents : ReaderAction()
    data object HideTableOfContents : ReaderAction()
    data object ShowPageThumbnails : ReaderAction()
    data object HidePageThumbnails : ReaderAction()
    data object ShowSettingsPanel : ReaderAction()
    data object HideSettingsPanel : ReaderAction()

    // Bottom bar sheets
    data object ShowViewModeSheet : ReaderAction()
    data object HideViewModeSheet : ReaderAction()
    data object ShowZoomSheet : ReaderAction()
    data object HideZoomSheet : ReaderAction()
    data object ShowDisplaySheet : ReaderAction()
    data object HideDisplaySheet : ReaderAction()
    data object ShowBookmarksSheet : ReaderAction()
    data object HideBookmarksSheet : ReaderAction()
    data object ShowMoreOptionsSheet : ReaderAction()
    data object HideMoreOptionsSheet : ReaderAction()

    // Reading settings
    data class SetBrightness(val brightness: Float) : ReaderAction()
    data class SetScrollMode(val mode: ScrollMode) : ReaderAction()
    data class SetSnapEnabled(val enabled: Boolean) : ReaderAction()
    data class SetKeepScreenOn(val enabled: Boolean) : ReaderAction()
    data class SetScreenOrientation(val orientation: ScreenOrientation) : ReaderAction()
    data class SetReadingTheme(val theme: ReadingTheme) : ReaderAction()
    data class SetAutoHideToolbar(val enabled: Boolean) : ReaderAction()
    data class SetScrubberOnScroll(val enabled: Boolean) : ReaderAction()
    data class SetTapToTurnPage(val enabled: Boolean) : ReaderAction()
    data class SetLockHorizontalScroll(val enabled: Boolean) : ReaderAction()
    data class OpenLink(val url: String) : ReaderAction()

    // Search
    data class Search(val query: String) : ReaderAction()
    data object NextSearchResult : ReaderAction()
    data object PreviousSearchResult : ReaderAction()
    data object ClearSearch : ReaderAction()
    data object ToggleSearch : ReaderAction()

    // Password
    data class SubmitPassword(val password: String, val remember: Boolean) : ReaderAction()

    // Document actions
    data object ToggleFavorite : ReaderAction()
    data object ShareDocument : ReaderAction()
    data object PrintDocument : ReaderAction()
    data object OpenWithExternal : ReaderAction()
    data object SaveDocument : ReaderAction()
    data object CloseDocument : ReaderAction()

    // Top bar menu
    data object ShowTopBarMenu : ReaderAction()
    data object HideTopBarMenu : ReaderAction()

    // PDF info dialog
    data object ShowInfoDialog : ReaderAction()
    data object HideInfoDialog : ReaderAction()

    // Delete dialog
    data object ShowDeleteDialog : ReaderAction()
    data object HideDeleteDialog : ReaderAction()
    data object ConfirmDelete : ReaderAction()

    // Rotation lock
    data object ToggleRotationLock : ReaderAction()

    // Page rotation
    data object RotateClockwise : ReaderAction()
    data object RotateCounterClockwise : ReaderAction()

    // Bookmark current page
    data object TogglePageBookmark : ReaderAction()
    data class DeleteBookmark(val bookmark: BookmarkEntity) : ReaderAction()
    data class GoToBookmark(val bookmark: BookmarkEntity) : ReaderAction()

    // Auto-scroll
    data object ShowAutoScrollSheet : ReaderAction()
    data object HideAutoScrollSheet : ReaderAction()
    data class StartAutoScroll(val speed: Float) : ReaderAction()
    data object StopAutoScroll : ReaderAction()
    data object ToggleAutoScrollPause : ReaderAction()
    data class SetAutoScrollSpeed(val speed: Float) : ReaderAction()

    // Attachments
    data class OpenAttachment(val attachment: AttachmentItem) : ReaderAction()
    data class DownloadAttachment(val attachment: AttachmentItem) : ReaderAction()

    // Favourite actions
    data object ShowRemoveFavoriteDialog : ReaderAction()
    data object HideRemoveFavoriteDialog : ReaderAction()
    data object ConfirmRemoveFavorite : ReaderAction()
    data object AddToFavorite : ReaderAction()

    // Save with picker
    data object SaveDocumentWithPicker : ReaderAction()

    // Highlights
    data class TextSelectionChanged(val selection: TextSelection?) : ReaderAction()
    /** Opens the colour picker for the current selection. */
    data object StartHighlight : ReaderAction()
    /** Highlights the current selection straight away, from the selection action bar. */
    data class HighlightSelection(val color: Int) : ReaderAction()
    /** Creates a highlight from the current selection, or recolours the one being edited. */
    data class ApplyHighlightColor(val color: Int) : ReaderAction()
    data class HighlightTapped(val highlight: TappedHighlight) : ReaderAction()
    data class DeleteHighlight(val highlightId: Long) : ReaderAction()
    data class SetHighlightLabel(val highlightId: Long, val label: String?) : ReaderAction()
    data object DismissHighlightPicker : ReaderAction()
    data class GoToHighlight(val highlightId: Long) : ReaderAction()
    data class ShowHighlightsSheet(val query: String = "") : ReaderAction()
    data object HideHighlightsSheet : ReaderAction()
    data object NextHighlight : ReaderAction()
    data object PreviousHighlight : ReaderAction()
    data object HideHighlightNav : ReaderAction()
    data object ShowBakeHighlightsDialog : ReaderAction()
    data object HideBakeHighlightsDialog : ReaderAction()
    data object ConfirmBakeHighlights : ReaderAction()

    /** Write a copy of a password protected document with the encryption removed. */
    data object SaveDecryptedCopy : ReaderAction()

    // Signing
    /** Open the app's signature sheet. */
    data object StartSigning : ReaderAction()

    data object HideSignatureSheet : ReaderAction()

    /** Place a signature the user kept earlier. */
    data class PlaceSavedSignature(val id: String) : ReaderAction()

    data class DeleteSavedSignature(val id: String) : ReaderAction()

    /** Throw away placements that have not been written to a copy. */
    data object DiscardSignatures : ReaderAction()

    /** Write a copy carrying the placed signatures. */
    data object SaveSignedCopy : ReaderAction()

    /** Ask before writing the placed signatures into the document itself. */
    data object ConfirmSaveSignedInPlace : ReaderAction()

    data object DismissSignSaveConfirm : ReaderAction()

    /** Write the placed signatures back into the document itself. */
    data object SaveSignedInPlace : ReaderAction()
}
