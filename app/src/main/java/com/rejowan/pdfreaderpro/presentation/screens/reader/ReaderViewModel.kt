package com.rejowan.pdfreaderpro.presentation.screens.reader

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rejowan.pdfreaderpro.data.local.PasswordStorage
import com.rejowan.pdfreaderpro.data.local.database.dao.BookmarkDao
import com.rejowan.pdfreaderpro.data.local.database.entity.BookmarkEntity
import com.rejowan.pdfreaderpro.domain.repository.FavoriteRepository
import com.rejowan.pdfreaderpro.domain.repository.RecentRepository
import com.rejowan.pdfreaderpro.presentation.components.pdf.PdfViewer
import com.rejowan.pdfreaderpro.presentation.components.pdf.addListener
import com.rejowan.pdfreaderpro.presentation.screens.reader.components.AttachmentItem
import com.rejowan.pdfreaderpro.presentation.screens.reader.components.OutlineItem
import com.rejowan.pdfreaderpro.presentation.screens.reader.components.PdfInfo
import android.os.Environment
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File

class ReaderViewModel(
    private val recentRepository: RecentRepository,
    private val favoriteRepository: FavoriteRepository,
    private val bookmarkDao: BookmarkDao,
    private val applicationContext: Context,
    savedStateHandle: SavedStateHandle,
    private val passwordStorage: PasswordStorage = PasswordStorage(applicationContext)
) : ViewModel() {

    val pdfPath: String = savedStateHandle.get<String>("path") ?: ""
    private val initialPage: Int = savedStateHandle.get<Int>("initialPage") ?: 0

    private val _state = MutableStateFlow(ReaderState(documentPath = pdfPath))
    val state: StateFlow<ReaderState> = _state.asStateFlow()

    private val _events = Channel<ReaderEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    private var pdfViewer: PdfViewer? = null
    private var storedLastPage: Int? = null
    private var isFirstOpen: Boolean = true

    init {
        // Set document title from file name
        val file = File(pdfPath)
        _state.update { it.copy(documentTitle = file.nameWithoutExtension) }

        // Load last read page from recent history
        viewModelScope.launch {
            storedLastPage = recentRepository.getLastPage(pdfPath)
            isFirstOpen = storedLastPage == null
        }

        // Observe bookmarks for this PDF
        bookmarkDao.getBookmarksForPdf(pdfPath)
            .onEach { bookmarks ->
                _state.update { state ->
                    val isCurrentBookmarked = bookmarks.any { it.pageNumber == state.currentPage }
                    state.copy(
                        bookmarks = bookmarks,
                        isCurrentPageBookmarked = isCurrentBookmarked
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    fun setPdfViewer(viewer: PdfViewer) {
        pdfViewer = viewer
        setupPdfViewerListeners(viewer)
    }

    private fun setupPdfViewerListeners(viewer: PdfViewer) {
        viewer.addListener(
            onPageLoadStart = {
                _state.update { it.copy(isLoading = true, error = null) }
            },
            onPageLoadSuccess = { pagesCount ->
                _state.update {
                    it.copy(
                        isLoading = false,
                        totalPages = pagesCount,
                        error = null
                    )
                }

                // Determine which page to start on
                val targetPage = when {
                    // If explicitly passed a page (e.g., from recent list), use it
                    initialPage > 0 && initialPage < pagesCount -> initialPage
                    // If we have a stored last page from history, use it
                    storedLastPage != null && storedLastPage!! > 0 && storedLastPage!! < pagesCount -> storedLastPage!!
                    // First time opening - scroll to top to show padding
                    else -> null
                }

                if (targetPage != null) {
                    viewer.goToPage(targetPage + 1) // Library uses 1-based indexing
                } else {
                    // First time opening - scroll to absolute top to show the padding
                    viewer.scrollTo(0)
                }

                // Add to recent files
                viewModelScope.launch {
                    addToRecent()
                }
            },
            onPageLoadFailed = { exception ->
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = exception.message ?: "Failed to load PDF"
                    )
                }
            },
            onPageChange = { pageNumber ->
                // Library uses 1-based indexing, our state uses 0-based
                val page = pageNumber - 1
                _state.update { state ->
                    val isBookmarked = state.bookmarks.any { it.pageNumber == page }
                    state.copy(currentPage = page, isCurrentPageBookmarked = isBookmarked)
                }
                // Save last read page to database
                viewModelScope.launch {
                    recentRepository.updateLastPage(pdfPath, page)
                }
            },
            onScaleChange = { scale ->
                _state.update { it.copy(zoom = scale) }
            },
            onPasswordDialogChange = { isOpen ->
                _state.update { it.copy(isPasswordRequired = isOpen) }
            },
            onSingleClick = {
                onAction(ReaderAction.ToggleToolbar)
            },
            onDoubleClick = {
                // Toggle zoom between PAGE_FIT and 2x
                viewer.let { v ->
                    if (v.currentPageScale > 1.5f) {
                        v.zoomTo(PdfViewer.Zoom.PAGE_FIT)
                    } else {
                        v.scalePageTo(2f)
                    }
                }
            },
            onLoadOutline = { sidebarItems ->
                // Convert SideBarTreeItem to OutlineItem
                val outlineItems = flattenOutline(sidebarItems, 0)
                _state.update { it.copy(outline = outlineItems) }
            },
            onFindMatchStart = {
                _state.update { it.copy(isSearching = true) }
            },
            onFindMatchChange = { current, total ->
                _state.update {
                    it.copy(
                        searchResultCount = total,
                        currentSearchIndex = current
                    )
                }
            },
            onFindMatchComplete = { found ->
                _state.update { it.copy(isSearching = false) }
            },
            onScrollModeChange = { scrollMode ->
                val direction = when (scrollMode) {
                    PdfViewer.PageScrollMode.HORIZONTAL -> ScrollDirection.HORIZONTAL
                    else -> ScrollDirection.VERTICAL
                }
                _state.update { it.copy(scrollDirection = direction) }
            },
            onAutoScrollEnd = {
                _state.update {
                    it.copy(
                        isAutoScrollActive = false,
                        isAutoScrollPaused = false
                    )
                }
                viewModelScope.launch {
                    _events.send(ReaderEvent.ShowMessage("Reached end of document"))
                }
            },
            onLoadAttachments = { sidebarItems ->
                val attachmentItems = sidebarItems.map { item ->
                    AttachmentItem(
                        title = item.title ?: "Unknown",
                        id = item.id,
                        dest = item.dest
                    )
                }
                _state.update { it.copy(attachments = attachmentItems) }
            },
            onDownload = { fileBytes, fileName, _ ->
                viewModelScope.launch {
                    saveAttachmentFile(fileBytes, fileName)
                }
            }
        )
    }

    private fun flattenOutline(
        items: List<com.rejowan.pdfreaderpro.presentation.components.pdf.model.SideBarTreeItem>,
        level: Int
    ): List<OutlineItem> {
        val result = mutableListOf<OutlineItem>()
        for (item in items) {
            result.add(
                OutlineItem(
                    title = item.title ?: "",
                    page = item.page,
                    level = level,
                    id = item.id,
                    dest = item.dest
                )
            )
            // Recursively add children
            result.addAll(flattenOutline(item.children, level + 1))
        }
        return result
    }

    /**
     * Navigate to an outline item using its page number.
     */
    fun navigateToOutlineItem(item: OutlineItem) {
        // page is 0-based, goToPage expects 1-based
        _state.update { it.copy(currentPage = item.page) }
        pdfViewer?.goToPage(item.page + 1)
    }

    private suspend fun addToRecent() {
        val file = File(pdfPath)
        recentRepository.addOrUpdateRecent(
            path = pdfPath,
            name = _state.value.documentTitle ?: file.name,
            size = file.length(),
            totalPages = _state.value.totalPages,
            currentPage = _state.value.currentPage
        )
    }

    fun onAction(action: ReaderAction) {
        when (action) {
            is ReaderAction.GoToPage -> {
                // Our state uses 0-based, library uses 1-based
                _state.update { it.copy(currentPage = action.page) }
                pdfViewer?.goToPage(action.page + 1)
            }
            is ReaderAction.NextPage -> {
                pdfViewer?.goToNextPage()
            }
            is ReaderAction.PreviousPage -> {
                pdfViewer?.goToPreviousPage()
            }

            is ReaderAction.SetZoom -> {
                _state.update { it.copy(zoom = action.zoom.coerceIn(it.minZoom, it.maxZoom)) }
                pdfViewer?.scalePageTo(action.zoom)
            }
            is ReaderAction.ZoomIn -> {
                pdfViewer?.zoomIn()
            }
            is ReaderAction.ZoomOut -> {
                pdfViewer?.zoomOut()
            }
            is ReaderAction.ResetZoom -> {
                pdfViewer?.zoomTo(PdfViewer.Zoom.PAGE_FIT)
            }
            is ReaderAction.ZoomFitPage -> {
                pdfViewer?.zoomTo(PdfViewer.Zoom.PAGE_FIT)
            }
            is ReaderAction.ZoomFitWidth -> {
                pdfViewer?.zoomTo(PdfViewer.Zoom.PAGE_WIDTH)
            }
            is ReaderAction.ZoomActualSize -> {
                pdfViewer?.zoomTo(PdfViewer.Zoom.ACTUAL_SIZE)
            }

            is ReaderAction.ToggleToolbar -> {
                // If auto-scroll is active, toggle pause instead of toolbar
                if (_state.value.isAutoScrollActive) {
                    onAction(ReaderAction.ToggleAutoScrollPause)
                } else {
                    _state.update {
                        // If in full screen, exit full screen mode and show toolbar
                        if (it.isFullScreen) {
                            it.copy(isFullScreen = false, isToolbarVisible = true)
                        } else {
                            it.copy(isToolbarVisible = !it.isToolbarVisible)
                        }
                    }
                }
            }
            is ReaderAction.ToggleControlBarExpanded -> _state.update { it.copy(isControlBarExpanded = !it.isControlBarExpanded) }
            is ReaderAction.ToggleFullScreen -> _state.update { it.copy(isFullScreen = !it.isFullScreen, isToolbarVisible = it.isFullScreen) }
            is ReaderAction.ToggleQuickActions -> _state.update { it.copy(showQuickActions = !it.showQuickActions) }
            is ReaderAction.ShowPageJumpDialog -> _state.update { it.copy(isPageJumpDialogVisible = true) }
            is ReaderAction.HidePageJumpDialog -> _state.update { it.copy(isPageJumpDialogVisible = false) }
            is ReaderAction.ShowTableOfContents -> _state.update { it.copy(isTableOfContentsVisible = true) }
            is ReaderAction.HideTableOfContents -> _state.update { it.copy(isTableOfContentsVisible = false) }
            is ReaderAction.ShowPageThumbnails -> _state.update { it.copy(isPageThumbnailsVisible = true) }
            is ReaderAction.HidePageThumbnails -> _state.update { it.copy(isPageThumbnailsVisible = false) }
            is ReaderAction.ShowSettingsPanel -> _state.update { it.copy(isSettingsPanelVisible = true) }
            is ReaderAction.HideSettingsPanel -> _state.update { it.copy(isSettingsPanelVisible = false) }

            // Bottom bar sheets
            is ReaderAction.ShowViewModeSheet -> _state.update { it.copy(isViewModeSheetVisible = true) }
            is ReaderAction.HideViewModeSheet -> _state.update { it.copy(isViewModeSheetVisible = false) }
            is ReaderAction.ShowZoomSheet -> _state.update { it.copy(isZoomSheetVisible = true) }
            is ReaderAction.HideZoomSheet -> _state.update { it.copy(isZoomSheetVisible = false) }
            is ReaderAction.ShowDisplaySheet -> _state.update { it.copy(isDisplaySheetVisible = true) }
            is ReaderAction.HideDisplaySheet -> _state.update { it.copy(isDisplaySheetVisible = false) }
            is ReaderAction.ShowBookmarksSheet -> _state.update { it.copy(isBookmarksSheetVisible = true) }
            is ReaderAction.HideBookmarksSheet -> _state.update { it.copy(isBookmarksSheetVisible = false) }
            is ReaderAction.ShowMoreOptionsSheet -> _state.update { it.copy(isMoreOptionsSheetVisible = true) }
            is ReaderAction.HideMoreOptionsSheet -> _state.update { it.copy(isMoreOptionsSheetVisible = false) }

            is ReaderAction.SetBrightness -> _state.update { it.copy(brightness = action.brightness) }
            is ReaderAction.SetScrollDirection -> {
                _state.update { it.copy(scrollDirection = action.direction) }
                val scrollMode = when (action.direction) {
                    ScrollDirection.VERTICAL -> PdfViewer.PageScrollMode.VERTICAL
                    ScrollDirection.HORIZONTAL -> PdfViewer.PageScrollMode.HORIZONTAL
                }
                pdfViewer?.pageScrollMode = scrollMode
            }
            is ReaderAction.SetSpreadMode -> {
                _state.update { it.copy(spreadMode = action.mode) }
                val spreadMode = when (action.mode) {
                    SpreadMode.NONE -> PdfViewer.PageSpreadMode.NONE
                    SpreadMode.ODD -> PdfViewer.PageSpreadMode.ODD
                    SpreadMode.EVEN -> PdfViewer.PageSpreadMode.EVEN
                }
                pdfViewer?.pageSpreadMode = spreadMode
            }
            is ReaderAction.SetSnapEnabled -> {
                _state.update { it.copy(isSnapEnabled = action.enabled) }
                pdfViewer?.snapPage = action.enabled
            }
            is ReaderAction.SetKeepScreenOn -> _state.update { it.copy(keepScreenOn = action.enabled) }
            is ReaderAction.SetScreenOrientation -> _state.update { it.copy(screenOrientation = action.orientation) }
            is ReaderAction.SetReadingTheme -> {
                _state.update { it.copy(readingTheme = action.theme) }
                val themeName = when (action.theme) {
                    ReadingTheme.LIGHT -> "light"
                    ReadingTheme.DARK -> "dark"
                    ReadingTheme.SEPIA -> "sepia"
                }
                pdfViewer?.ui?.setReadingTheme(themeName)
            }

            is ReaderAction.Search -> {
                _state.update { it.copy(searchQuery = action.query, isSearching = true) }
                if (action.query.isNotBlank()) {
                    pdfViewer?.findController?.startFind(action.query)
                } else {
                    pdfViewer?.findController?.stopFind()
                    _state.update { it.copy(isSearching = false, searchResultCount = 0, currentSearchIndex = 0) }
                }
            }
            is ReaderAction.NextSearchResult -> {
                pdfViewer?.findController?.findNext()
            }
            is ReaderAction.PreviousSearchResult -> {
                pdfViewer?.findController?.findPrevious()
            }
            is ReaderAction.ClearSearch -> {
                pdfViewer?.findController?.stopFind()
                _state.update { it.copy(searchQuery = "", isSearching = false, searchResultCount = 0, currentSearchIndex = 0) }
            }
            is ReaderAction.ToggleSearch -> _state.update { it.copy(isSearchActive = !it.isSearchActive) }

            is ReaderAction.SubmitPassword -> submitPassword(action.password, action.remember)

            is ReaderAction.ToggleFavorite -> toggleFavorite()
            is ReaderAction.ShareDocument -> viewModelScope.launch { _events.send(ReaderEvent.ShareDocument) }
            is ReaderAction.CloseDocument -> viewModelScope.launch { _events.send(ReaderEvent.DocumentClosed) }

            is ReaderAction.ShowInfoDialog -> _state.update { it.copy(isInfoDialogVisible = true) }
            is ReaderAction.HideInfoDialog -> _state.update { it.copy(isInfoDialogVisible = false) }
            is ReaderAction.ShowDeleteDialog -> _state.update { it.copy(isDeleteDialogVisible = true) }
            is ReaderAction.HideDeleteDialog -> _state.update { it.copy(isDeleteDialogVisible = false) }
            is ReaderAction.ConfirmDelete -> deleteDocument()

            is ReaderAction.ToggleRotationLock -> _state.update { it.copy(isRotationLocked = !it.isRotationLocked) }

            // Page rotation
            is ReaderAction.RotateClockwise -> {
                pdfViewer?.rotateClockWise()
                _state.update { it.copy(pageRotation = (it.pageRotation + 90) % 360) }
            }
            is ReaderAction.RotateCounterClockwise -> {
                pdfViewer?.rotateCounterClockWise()
                _state.update { it.copy(pageRotation = (it.pageRotation - 90 + 360) % 360) }
            }

            // Bookmark current page
            is ReaderAction.TogglePageBookmark -> {
                viewModelScope.launch {
                    val currentPage = _state.value.currentPage
                    val isCurrentlyBookmarked = _state.value.isCurrentPageBookmarked

                    if (isCurrentlyBookmarked) {
                        // Remove bookmark
                        bookmarkDao.deleteByPage(pdfPath, currentPage)
                    } else {
                        // Add bookmark
                        val bookmark = BookmarkEntity(
                            pdfPath = pdfPath,
                            pageNumber = currentPage,
                            title = "Page ${currentPage + 1}"
                        )
                        bookmarkDao.insert(bookmark)
                    }
                    // State will be updated automatically by the Flow observer
                }
            }

            is ReaderAction.DeleteBookmark -> {
                viewModelScope.launch {
                    bookmarkDao.delete(action.bookmark)
                }
            }

            is ReaderAction.GoToBookmark -> {
                val page = action.bookmark.pageNumber
                _state.update { state ->
                    val isBookmarked = state.bookmarks.any { it.pageNumber == page }
                    state.copy(
                        currentPage = page,
                        isCurrentPageBookmarked = isBookmarked,
                        isBookmarksSheetVisible = false
                    )
                }
                pdfViewer?.goToPage(page + 1)
            }

            // Auto-scroll actions
            is ReaderAction.ShowAutoScrollSheet -> _state.update { it.copy(isAutoScrollSheetVisible = true) }
            is ReaderAction.HideAutoScrollSheet -> _state.update { it.copy(isAutoScrollSheetVisible = false) }

            is ReaderAction.StartAutoScroll -> {
                _state.update {
                    it.copy(
                        isAutoScrollActive = true,
                        isAutoScrollPaused = false,
                        autoScrollSpeed = action.speed,
                        isAutoScrollSheetVisible = false,
                        isToolbarVisible = false
                    )
                }
                pdfViewer?.ui?.autoScroll?.start(action.speed)
            }

            is ReaderAction.StopAutoScroll -> {
                _state.update {
                    it.copy(
                        isAutoScrollActive = false,
                        isAutoScrollPaused = false
                    )
                }
                pdfViewer?.ui?.autoScroll?.stop()
            }

            is ReaderAction.ToggleAutoScrollPause -> {
                val isPaused = _state.value.isAutoScrollPaused
                _state.update { it.copy(isAutoScrollPaused = !isPaused) }
                if (isPaused) {
                    pdfViewer?.ui?.autoScroll?.resume()
                } else {
                    pdfViewer?.ui?.autoScroll?.pause()
                }
            }

            is ReaderAction.SetAutoScrollSpeed -> {
                _state.update { it.copy(autoScrollSpeed = action.speed) }
                pdfViewer?.ui?.autoScroll?.setSpeed(action.speed)
            }

            is ReaderAction.DownloadAttachment -> {
                viewModelScope.launch {
                    pdfViewer?.ui?.performSidebarTreeItemClick(action.attachment.id)
                }
            }
        }
    }

    private fun submitPassword(password: String, remember: Boolean) {
        viewModelScope.launch {
            if (remember) {
                passwordStorage.savePassword(pdfPath, password)
            }
            // Submit password to the library's password dialog
            pdfViewer?.ui?.passwordDialog?.submitPassword(password)
        }
    }

    private fun toggleFavorite() {
        viewModelScope.launch {
            val file = File(pdfPath)
            val pdfFile = com.rejowan.pdfreaderpro.domain.model.PdfFile(
                id = pdfPath.hashCode().toLong(),
                name = _state.value.documentTitle ?: file.name,
                path = pdfPath,
                uri = android.net.Uri.fromFile(file),
                size = file.length(),
                dateModified = file.lastModified(),
                dateAdded = file.lastModified(),
                pageCount = _state.value.totalPages,
                parentFolder = file.parent ?: ""
            )
            favoriteRepository.toggleFavorite(pdfFile)
        }
    }

    private fun deleteDocument() {
        viewModelScope.launch {
            _state.update { it.copy(isDeleteDialogVisible = false) }
            try {
                val file = File(pdfPath)
                if (file.exists() && file.delete()) {
                    recentRepository.removeRecent(pdfPath)
                    favoriteRepository.removeFavorite(pdfPath)
                    passwordStorage.removePassword(pdfPath)
                    _events.send(ReaderEvent.DocumentDeleted)
                } else {
                    _events.send(ReaderEvent.Error("Failed to delete file"))
                }
            } catch (e: Exception) {
                _events.send(ReaderEvent.Error("Error: ${e.message}"))
            }
        }
    }

    private suspend fun saveAttachmentFile(fileBytes: ByteArray, fileName: String?) {
        try {
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val finalFileName = fileName ?: "attachment_${System.currentTimeMillis()}"
            val file = File(downloadsDir, finalFileName)

            file.writeBytes(fileBytes)
            _events.send(ReaderEvent.ShowMessage("Saved to Downloads: $finalFileName"))
        } catch (e: Exception) {
            _events.send(ReaderEvent.Error("Failed to save attachment: ${e.message}"))
        }
    }

    fun printDocument() {
        try {
            val fileName = _state.value.documentTitle ?: File(pdfPath).nameWithoutExtension
            pdfViewer?.printFile(fileName)
        } catch (e: Exception) {
            viewModelScope.launch {
                _events.send(ReaderEvent.Error("Print error: ${e.message}"))
            }
        }
    }

    fun getPdfInfo(): PdfInfo {
        val file = File(pdfPath)
        val properties = pdfViewer?.properties
        return PdfInfo(
            title = properties?.title?.takeIf { it.isNotBlank() } ?: _state.value.documentTitle,
            author = properties?.author?.takeIf { it.isNotBlank() },
            subject = properties?.subject?.takeIf { it.isNotBlank() },
            creator = properties?.creator?.takeIf { it.isNotBlank() },
            producer = properties?.producer?.takeIf { it.isNotBlank() },
            creationDate = properties?.creationDate?.takeIf { it.isNotBlank() && it != "null" },
            keywords = properties?.keywords?.takeIf { it.isNotBlank() },
            language = properties?.language?.takeIf { it.isNotBlank() },
            pdfVersion = properties?.pdfFormatVersion?.takeIf { it.isNotBlank() },
            path = pdfPath,
            pageCount = _state.value.totalPages,
            fileSize = properties?.fileSize ?: file.length(),
            lastModified = file.lastModified(),
            isLinearized = properties?.isLinearized ?: false,
            isEncrypted = !properties?.encryptFilterName.isNullOrBlank(),
            encryptionType = properties?.encryptFilterName?.takeIf { it.isNotBlank() },
            hasForms = properties?.isAcroFormPresent ?: false,
            hasSignatures = properties?.isSignaturesPresent ?: false,
            hasXfa = properties?.isXFAPresent ?: false
        )
    }

    suspend fun isFavorite(): Boolean = favoriteRepository.isFavorite(pdfPath)
}
