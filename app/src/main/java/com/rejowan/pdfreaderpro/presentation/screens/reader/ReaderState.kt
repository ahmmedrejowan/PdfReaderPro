package com.rejowan.pdfreaderpro.presentation.screens.reader

import com.rejowan.pdfreaderpro.data.local.database.entity.BookmarkEntity
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
    val isRemoveFavoriteDialogVisible: Boolean = false
) {
    val pageLabel: String
        get() = "${currentPage + 1} / $totalPages"
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
sealed class ReaderEvent {
    data class ShowMessage(val message: String) : ReaderEvent()
    data class NavigateToPage(val page: Int) : ReaderEvent()
    data object DocumentClosed : ReaderEvent()
    data object DocumentDeleted : ReaderEvent()
    data object ShareDocument : ReaderEvent()
    data object SaveDocumentPicker : ReaderEvent()
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

    // Zoom
    data class SetZoom(val zoom: Float) : ReaderAction()
    data object ZoomIn : ReaderAction()
    data object ZoomOut : ReaderAction()
    data object ResetZoom : ReaderAction()
    data object ZoomFitPage : ReaderAction()
    data object ZoomFitWidth : ReaderAction()
    data object ZoomActualSize : ReaderAction()

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
}
