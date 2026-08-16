package com.rejowan.pdfreaderpro.presentation.screens.reader

import android.app.Application
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rejowan.pdfreaderpro.R
import com.rejowan.pdfreaderpro.data.local.PasswordStorage
import com.rejowan.pdfreaderpro.data.local.database.dao.AnnotationDao
import com.rejowan.pdfreaderpro.data.local.database.dao.BookmarkDao
import com.rejowan.pdfreaderpro.data.local.database.entity.BookmarkEntity
import com.rejowan.pdfreaderpro.data.local.database.dao.FilePreferenceDao
import com.rejowan.pdfreaderpro.data.local.database.entity.FilePreferenceEntity
import com.rejowan.pdfreaderpro.data.mapper.toEntity
import com.rejowan.pdfreaderpro.data.mapper.toRendered
import com.rejowan.pdfreaderpro.data.mapper.toHighlight
import com.rejowan.pdfreaderpro.domain.model.Highlight
import com.rejowan.pdfreaderpro.domain.model.HighlightQuad
import com.rejowan.pdfreaderpro.domain.model.QuickZoomPreset
import com.rejowan.pdfreaderpro.domain.model.ReadingTheme as DomainReadingTheme
import com.rejowan.pdfreaderpro.domain.model.ScreenOrientation as DomainScreenOrientation
import com.rejowan.pdfreaderpro.domain.model.ScrollMode as DomainScrollMode
import com.rejowan.pdfreaderpro.domain.repository.FavoriteRepository
import com.rejowan.pdfreaderpro.domain.repository.PreferencesRepository
import com.rejowan.pdfreaderpro.domain.repository.RecentRepository
import kotlinx.coroutines.flow.first
import com.rejowan.pdfreaderpro.presentation.components.pdf.PdfViewer
import com.rejowan.pdfreaderpro.presentation.components.pdf.addListener
import com.rejowan.pdfreaderpro.presentation.components.pdf.model.PdfQuad
import com.rejowan.pdfreaderpro.presentation.components.pdf.model.RenderedHighlight
import kotlinx.coroutines.delay
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
import timber.log.Timber
import com.rejowan.pdfreaderpro.util.HighlightBaker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.io.File

class ReaderViewModel(
    private val recentRepository: RecentRepository,
    private val favoriteRepository: FavoriteRepository,
    private val preferencesRepository: PreferencesRepository,
    private val bookmarkDao: BookmarkDao,
    private val annotationDao: AnnotationDao,
    private val filePreferenceDao: FilePreferenceDao,
    private val pdfToolsRepository: com.rejowan.pdfreaderpro.domain.repository.PdfToolsRepository,
    private val applicationContext: Application,
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
    private var pendingAttachmentAction: AttachmentAction? = null
    private var triedStoredPassword: Boolean = false
    private var awaitingStoredPasswordResult: Boolean = false

    /**
     * The password this document was opened with, kept only for the lifetime of the
     * reader so a decrypted copy can be written without asking for it again. Never
     * persisted from here; [passwordStorage] owns that decision.
     */
    private var documentPassword: String? = null

    private enum class AttachmentAction { OPEN, DOWNLOAD }

    init {
        // Set document title from file name
        val file = File(pdfPath)
        _state.update { it.copy(documentTitle = file.nameWithoutExtension) }

        // Load last read page from recent history
        viewModelScope.launch {
            storedLastPage = recentRepository.getLastPage(pdfPath)
            isFirstOpen = storedLastPage == null
        }

        // Load global reader settings from preferences
        viewModelScope.launch {
            val prefs = preferencesRepository.preferences.first()
            _state.update { state ->
                state.copy(
                    brightness = prefs.readerBrightness,
                    scrollMode = mapDomainScrollMode(prefs.readerScrollMode),
                    readingTheme = mapDomainReadingTheme(prefs.readerTheme),
                    autoHideToolbar = prefs.readerAutoHideToolbar,
                    scrubberOnScroll = prefs.readerScrubberOnScroll,
                    tapToTurnPage = prefs.readerTapToTurnPage,
                    keepScreenOn = prefs.readerKeepScreenOn,
                    isSnapEnabled = prefs.readerSnapToPages,
                    screenOrientation = mapDomainScreenOrientation(prefs.readerScreenOrientation),
                    doubleTapZoom = prefs.readerDoubleTapZoom
                )
            }
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

        // Observe highlights for this PDF and keep the viewer's overlay in step
        annotationDao.getHighlightsForPdf(pdfPath)
            .onEach { entities ->
                val highlights = entities.map { it.toHighlight() }
                _state.update { state ->
                    state.copy(
                        highlights = highlights,
                        // Deleting a highlight shrinks the list under the navigation
                        // strip, so keep the index inside it.
                        currentHighlightIndex = state.currentHighlightIndex
                            .coerceAtMost(highlights.size + state.documentHighlights.size - 1),
                        isHighlightNavVisible = state.isHighlightNavVisible &&
                            (highlights.isNotEmpty() || state.documentHighlights.isNotEmpty())
                    )
                }
                renderHighlights(highlights)
            }
            .launchIn(viewModelScope)

        // Observe this document's own settings, which are separate from the global ones
        filePreferenceDao.observe(pdfPath)
            .onEach { preference ->
                val locked = preference?.lockHorizontalScroll == true
                _state.update { it.copy(lockHorizontalScroll = locked) }
                applyHorizontalScrollLock(locked)
            }
            .launchIn(viewModelScope)

        // Load favorite state
        viewModelScope.launch {
            val isFav = favoriteRepository.isFavorite(pdfPath)
            _state.update { it.copy(isFavorite = isFav) }
        }
    }

    /**
     * Closes the highlight bar when the page moves under it.
     *
     * Only closes an edit bar. A bar opened for a live selection is left alone,
     * since the selection itself survives and the system moves its handles with it.
     */
    private fun dismissHighlightBarOnViewChange() {
        if (!_state.value.isHighlightPickerVisible) return
        if (_state.value.editingHighlightId == null) return

        _state.update {
            it.copy(
                isHighlightPickerVisible = false,
                editingHighlightId = null,
                editingAnchor = null
            )
        }
    }

    /**
     * Pushes the horizontal lock to the viewer.
     *
     * A no-op until the viewer is attached, so [setPdfViewer] applies it again once
     * it is.
     */
    private fun applyHorizontalScrollLock(locked: Boolean) {
        pdfViewer?.setHorizontalScrollLock(locked)
    }

    /**
     * Pushes the current highlights to the viewer.
     *
     * A no-op until the viewer is attached. [setPdfViewer] renders again once it is,
     * so highlights loaded before the viewer was ready are not lost.
     */
    private fun renderHighlights(highlights: List<Highlight>) {
        val viewer = pdfViewer ?: return
        viewer.setHighlights(highlights.map { it.toRendered(HIGHLIGHT_FILL_ALPHA) })
    }

    /**
     * Creates a highlight from the current selection, or recolours the one the
     * picker is editing.
     */
    private fun applyHighlightColor(color: Int) {
        val state = _state.value
        val editingId = state.editingHighlightId

        viewModelScope.launch {
            if (editingId != null) {
                val existing = annotationDao.getById(editingId)
                if (existing != null) {
                    annotationDao.update(
                        existing.copy(color = color, updatedAt = System.currentTimeMillis())
                    )
                }
            } else {
                val selection = state.capturedSelection ?: return@launch
                // Page numbers arrive 1-based from the viewer and are stored 0-based,
                // matching bookmarks.
                val pageNumber = selection.pageNumber - 1
                val nextSortIndex =
                    (annotationDao.getMaxSortIndexForPage(pdfPath, pageNumber) ?: -1) + 1

                annotationDao.insert(
                    Highlight(
                        pdfPath = pdfPath,
                        pageNumber = pageNumber,
                        text = selection.text,
                        quads = selection.quads.map { HighlightQuad(it.x, it.y, it.w, it.h) },
                        color = color,
                        sortIndex = nextSortIndex
                    ).toEntity()
                )

                // The selection has served its purpose, and leaving it up would keep
                // the system selection handles over the new highlight.
                pdfViewer?.removeTextSelection()
            }

            _state.update {
                it.copy(
                    isHighlightPickerVisible = false,
                    editingHighlightId = null,
                    editingAnchor = null,
                    capturedSelection = null,
                    pendingSelection = null
                )
            }
        }
    }

    /** Jumps to a highlight's page, then pulses it once the page has rendered. */
    private fun goToHighlight(highlightId: Long) {
        val highlights = _state.value.allHighlights
        val index = highlights.indexOfFirst { it.id == highlightId }
        if (index < 0) return

        _state.update {
            it.copy(
                isHighlightsSheetVisible = false,
                currentHighlightIndex = index,
                isHighlightNavVisible = true
            )
        }

        scrollToHighlight(highlights[index])
    }

    /**
     * Moves to the next or previous highlight, wrapping at both ends.
     *
     * Order comes straight from [ReaderState.highlights], which the DAO returns by
     * page then sortIndex, so this and the panel can never disagree.
     */
    private fun stepHighlight(forward: Boolean) {
        val highlights = _state.value.allHighlights
        if (highlights.isEmpty()) return

        val current = _state.value.currentHighlightIndex
        val next = when {
            // Nothing focused yet: start at either end depending on direction.
            current < 0 -> if (forward) 0 else highlights.lastIndex
            forward -> (current + 1) % highlights.size
            else -> (current - 1 + highlights.size) % highlights.size
        }

        _state.update { it.copy(currentHighlightIndex = next, isHighlightNavVisible = true) }
        scrollToHighlight(highlights[next])
    }

    private fun scrollToHighlight(highlight: Highlight) {
        val viewer = pdfViewer ?: return

        // Viewer pages are 1-based.
        viewer.goToPage(highlight.pageNumber + 1)

        viewModelScope.launch {
            // Neither path can run until the page has rendered, and goToPage does not
            // render synchronously.
            delay(HIGHLIGHT_SCROLL_DELAY_MS)

            if (highlight.isEditable) {
                viewer.scrollToHighlight(highlight.id)
            } else {
                // Painted by the viewer's annotation layer rather than our overlay, so
                // there is no element of ours to find. Scroll to the quad instead,
                // otherwise a jump lands on the page and leaves the highlight off
                // screen. No pulse, for the same reason.
                highlight.quads.firstOrNull()?.let { quad ->
                    viewer.scrollToPageQuad(
                        pageNumber = highlight.pageNumber + 1,
                        quad = PdfQuad(quad.x, quad.y, quad.w, quad.h)
                    )
                }
            }
        }
    }


    // Mapping functions from domain models to reader state models
    private fun mapDomainScrollMode(mode: DomainScrollMode): ScrollMode {
        return when (mode) {
            DomainScrollMode.VERTICAL -> ScrollMode.VERTICAL
            DomainScrollMode.HORIZONTAL -> ScrollMode.HORIZONTAL
        }
    }

    private fun mapDomainReadingTheme(theme: DomainReadingTheme): ReadingTheme {
        return when (theme) {
            DomainReadingTheme.LIGHT -> ReadingTheme.LIGHT
            DomainReadingTheme.SEPIA -> ReadingTheme.SEPIA
            DomainReadingTheme.DARK -> ReadingTheme.DARK
            DomainReadingTheme.BLACK -> ReadingTheme.BLACK
        }
    }

    private fun mapDomainScreenOrientation(orientation: DomainScreenOrientation): ScreenOrientation {
        return when (orientation) {
            DomainScreenOrientation.AUTO -> ScreenOrientation.AUTO
            DomainScreenOrientation.PORTRAIT -> ScreenOrientation.PORTRAIT
            DomainScreenOrientation.LANDSCAPE -> ScreenOrientation.LANDSCAPE
        }
    }

    fun setPdfViewer(viewer: PdfViewer) {
        pdfViewer = viewer
        setupPdfViewerListeners(viewer)
        // Both may have loaded from the database before the viewer attached.
        renderHighlights(_state.value.highlights)
        applyHorizontalScrollLock(_state.value.lockHorizontalScroll)
    }

    /**
     * Drop the viewer reference when the view leaves. This ViewModel outlives the
     * Activity across configuration changes, and [pdfViewer] is a WebView-backed
     * View, so holding it past that point keeps the old Activity alive.
     */
    fun clearPdfViewer() {
        pdfViewer = null
    }

    override fun onCleared() {
        super.onCleared()
        pdfViewer = null
    }

    private fun applyInitialSettings(viewer: PdfViewer) {
        viewModelScope.launch {
            try {
                val prefs = preferencesRepository.preferences.first()

                // Apply scroll mode
                val scrollMode = when (prefs.readerScrollMode) {
                    DomainScrollMode.VERTICAL -> PdfViewer.PageScrollMode.VERTICAL
                    DomainScrollMode.HORIZONTAL -> PdfViewer.PageScrollMode.HORIZONTAL
                }
                viewer.pageScrollMode = scrollMode

                // Apply reading theme
                val themeName = when (prefs.readerTheme) {
                    DomainReadingTheme.LIGHT -> "light"
                    DomainReadingTheme.SEPIA -> "sepia"
                    DomainReadingTheme.DARK -> "dark"
                    DomainReadingTheme.BLACK -> "black"
                }
                viewer.ui.setReadingTheme(themeName)

                // Apply snap to pages
                viewer.snapPage = prefs.readerSnapToPages
            } catch (e: Exception) {
                // Viewer not yet initialized, settings will be applied when ready
            }
        }
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
                        error = null,
                        passwordSubmitted = false
                    )
                }

                // Push these again now the document exists. Both the viewer attaching
                // and the database emitting happen before the document has loaded, so
                // anything sent earlier landed on an empty viewer and was lost.
                renderHighlights(_state.value.highlights)
                applyHorizontalScrollLock(_state.value.lockHorizontalScroll)
                // The file's own highlights only become readable once it is open.
                viewer.loadDocumentHighlights()

                // Determine which page to start on
                val lastPage = storedLastPage // Capture for smart cast
                val targetPage = when {
                    // If explicitly passed a page (e.g., from recent list), use it
                    initialPage > 0 && initialPage < pagesCount -> initialPage
                    // If we have a stored last page from history, use it
                    lastPage != null && lastPage > 0 && lastPage < pagesCount -> lastPage
                    // First time opening - scroll to top to show padding
                    else -> null
                }

                if (targetPage != null) {
                    viewer.goToPage(targetPage + 1) // Library uses 1-based indexing
                } else {
                    // First time opening - scroll to absolute top to show the padding
                    viewer.scrollTo(0)
                }

                // Add to recent files and apply settings
                viewModelScope.launch {
                    addToRecent()

                    // Apply initial settings (scroll direction, reading theme)
                    applyInitialSettings(viewer)

                    // Apply quick zoom preset from settings
                    val prefs = preferencesRepository.preferences.first()
                    when (prefs.readerQuickZoomPreset) {
                        QuickZoomPreset.FIT_PAGE -> viewer.zoomTo(PdfViewer.Zoom.PAGE_FIT)
                        QuickZoomPreset.FIT_WIDTH -> viewer.zoomTo(PdfViewer.Zoom.PAGE_WIDTH)
                        QuickZoomPreset.ACTUAL_SIZE -> viewer.zoomTo(PdfViewer.Zoom.ACTUAL_SIZE)
                    }
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
            // Preparing a print job rasterises every page, which takes tens of
            // seconds on a longer document. Without this the reader looked frozen
            // and people assumed printing was unsupported.
            onPrintProcessStart = {
                _state.update { it.copy(printProgress = 0f) }
            },
            onPrintProcessProgress = { progress ->
                _state.update { it.copy(printProgress = progress.coerceIn(0f, 1f)) }
            },
            onPrintProcessEnd = {
                _state.update { it.copy(printProgress = null) }
            },
            onPrintCancelled = {
                _state.update { it.copy(printProgress = null) }
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
                dismissHighlightBarOnViewChange()
            },
            onScrollChange = { _, _, _ ->
                // The bar is anchored to a position in the viewer, so once the page
                // moves underneath it, it is pointing at nothing.
                dismissHighlightBarOnViewChange()
            },
            onDocumentHighlightsLoaded = { loaded ->
                _state.update { state ->
                    state.copy(
                        documentHighlights = loaded.map { it.toHighlight(pdfPath) },
                        // The merged list just grew or shrank, so keep the strip's
                        // index inside it.
                        currentHighlightIndex = state.currentHighlightIndex
                            .coerceAtMost(state.highlights.size + loaded.size - 1)
                    )
                }
            },
            onTextSelectionChange = { selection ->
                onAction(ReaderAction.TextSelectionChanged(selection))
            },
            onHighlightTapped = { highlight ->
                onAction(ReaderAction.HighlightTapped(highlight))
            },
            onPasswordDialogChange = { isOpen ->
                when {
                    isOpen && !triedStoredPassword -> {
                        triedStoredPassword = true
                        viewModelScope.launch {
                            val rememberEnabled = preferencesRepository.preferences.first().rememberPasswords
                            val stored = if (rememberEnabled) passwordStorage.getPassword(pdfPath) else null
                            if (stored != null) {
                                awaitingStoredPasswordResult = true
                                documentPassword = stored
                                _state.update {
                                    it.copy(passwordSubmitted = true, isPasswordProtected = true)
                                }
                                pdfViewer?.ui?.passwordDialog?.submitPassword(stored)
                            } else {
                                _state.update {
                                    it.copy(isPasswordRequired = true, isPasswordProtected = true)
                                }
                            }
                        }
                    }
                    isOpen && awaitingStoredPasswordResult -> {
                        // Silent auto-submit failed — stored password is stale.
                        awaitingStoredPasswordResult = false
                        documentPassword = null
                        viewModelScope.launch { passwordStorage.removePassword(pdfPath) }
                        _state.update { it.copy(isPasswordRequired = true, isPasswordError = true, passwordSubmitted = false) }
                    }
                    isOpen && _state.value.passwordSubmitted -> {
                        documentPassword = null
                        _state.update { it.copy(isPasswordRequired = true, isPasswordError = true) }
                    }
                    isOpen -> {
                        _state.update { it.copy(isPasswordRequired = true) }
                    }
                    else -> {
                        _state.update { it.copy(isPasswordRequired = false, isPasswordError = false) }
                    }
                }
            },
            onSingleClick = { x, y, width, height ->
                onAction(ReaderAction.TapToTurnOrToggle(x, y, width, height))
            },
            onDoubleClick = { x, y ->
                viewer.let { v ->
                    val target = _state.value.doubleTapZoom
                    if (v.currentPageScale >= target - 0.05f) {
                        v.getActualScaleFor(PdfViewer.Zoom.PAGE_FIT) { fitScale ->
                            if (fitScale != null) v.scalePageToAt(fitScale, x, y)
                            else v.zoomTo(PdfViewer.Zoom.PAGE_FIT)
                        }
                    } else {
                        v.scalePageToAt(target, x, y)
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
            onScrollModeChange = { pdfScrollMode ->
                val scrollMode = when (pdfScrollMode) {
                    PdfViewer.PageScrollMode.VERTICAL -> ScrollMode.VERTICAL
                    PdfViewer.PageScrollMode.HORIZONTAL -> ScrollMode.HORIZONTAL
                    else -> ScrollMode.VERTICAL // Default to vertical for unsupported modes
                }
                _state.update { it.copy(scrollMode = scrollMode) }
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
            onDownload = { fileBytes, fileName, mimeType ->
                viewModelScope.launch {
                    when (pendingAttachmentAction) {
                        AttachmentAction.OPEN -> openAttachmentFile(fileBytes, fileName, mimeType)
                        AttachmentAction.DOWNLOAD -> saveAttachmentFile(fileBytes, fileName)
                        null -> saveAttachmentFile(fileBytes, fileName) // Default to save
                    }
                    pendingAttachmentAction = null
                }
            },
            onLinkClick = { link ->
                onAction(ReaderAction.OpenLink(link))
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

    /**
     * Handles a single tap on the page. When tap-to-turn is enabled and the tap lands in an
     * edge zone, navigate between pages; otherwise fall back to toggling the toolbar.
     *
     * The turn direction follows the scroll orientation: vertical scrolling uses the top and
     * bottom thirds (top = previous, bottom = next), horizontal scrolling uses the left and
     * right thirds (left = previous, right = next). The middle third always toggles the toolbar.
     */
    private fun handleTapToTurnOrToggle(action: ReaderAction.TapToTurnOrToggle) {
        val state = _state.value

        // Fall back to toolbar toggle when the feature is off, auto-scroll is running
        // (tap should pause instead), or dimensions are missing (avoid divide-by-zero).
        if (!state.tapToTurnPage || state.isAutoScrollActive ||
            action.width <= 0f || action.height <= 0f
        ) {
            onAction(ReaderAction.ToggleToolbar)
            return
        }

        val position = if (state.scrollMode == ScrollMode.HORIZONTAL) {
            action.x / action.width
        } else {
            action.y / action.height
        }

        when {
            position < TAP_TURN_ZONE_FRACTION -> onAction(ReaderAction.PreviousPage)
            position > 1f - TAP_TURN_ZONE_FRACTION -> onAction(ReaderAction.NextPage)
            else -> onAction(ReaderAction.ToggleToolbar)
        }
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
            is ReaderAction.TapToTurnOrToggle -> {
                // A tap elsewhere dismisses the highlight bar rather than falling
                // through to the toolbar toggle, which is what a floating bar
                // anchored to the page should do.
                if (_state.value.isHighlightPickerVisible) {
                    _state.update {
                        it.copy(
                            isHighlightPickerVisible = false,
                            editingHighlightId = null,
                            editingAnchor = null,
                            capturedSelection = null
                        )
                    }
                } else {
                    handleTapToTurnOrToggle(action)
                }
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
                viewModelScope.launch {
                    preferencesRepository.setReaderQuickZoomPreset(QuickZoomPreset.FIT_PAGE)
                }
            }
            is ReaderAction.ZoomFitWidth -> {
                pdfViewer?.zoomTo(PdfViewer.Zoom.PAGE_WIDTH)
                viewModelScope.launch {
                    preferencesRepository.setReaderQuickZoomPreset(QuickZoomPreset.FIT_WIDTH)
                }
            }
            is ReaderAction.ZoomActualSize -> {
                pdfViewer?.zoomTo(PdfViewer.Zoom.ACTUAL_SIZE)
                viewModelScope.launch {
                    preferencesRepository.setReaderQuickZoomPreset(QuickZoomPreset.ACTUAL_SIZE)
                }
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

            is ReaderAction.SetBrightness -> {
                _state.update { it.copy(brightness = action.brightness) }
                viewModelScope.launch {
                    preferencesRepository.setReaderBrightness(action.brightness)
                }
            }
            is ReaderAction.SetScrollMode -> {
                _state.update { it.copy(scrollMode = action.mode) }
                val scrollMode = when (action.mode) {
                    ScrollMode.VERTICAL -> PdfViewer.PageScrollMode.VERTICAL
                    ScrollMode.HORIZONTAL -> PdfViewer.PageScrollMode.HORIZONTAL
                }
                pdfViewer?.pageScrollMode = scrollMode
                // Horizontal mode needs that axis, so the lock cannot survive the
                // switch. The viewer releases it too; this keeps our state honest.
                if (action.mode == ScrollMode.HORIZONTAL && _state.value.lockHorizontalScroll) {
                    _state.update { it.copy(lockHorizontalScroll = false) }
                    applyHorizontalScrollLock(false)
                    viewModelScope.launch {
                        filePreferenceDao.save(
                            (filePreferenceDao.get(pdfPath) ?: FilePreferenceEntity(pdfPath = pdfPath))
                                .copy(
                                    lockHorizontalScroll = false,
                                    updatedAt = System.currentTimeMillis()
                                )
                        )
                    }
                }
                // Persist to global settings
                viewModelScope.launch {
                    val domainMode = when (action.mode) {
                        ScrollMode.VERTICAL -> DomainScrollMode.VERTICAL
                        ScrollMode.HORIZONTAL -> DomainScrollMode.HORIZONTAL
                    }
                    preferencesRepository.setReaderScrollMode(domainMode)
                }
            }
            is ReaderAction.SetSnapEnabled -> {
                _state.update { it.copy(isSnapEnabled = action.enabled) }
                pdfViewer?.snapPage = action.enabled
                // Persist to global settings
                viewModelScope.launch {
                    preferencesRepository.setReaderSnapToPages(action.enabled)
                }
            }
            is ReaderAction.SetKeepScreenOn -> {
                _state.update { it.copy(keepScreenOn = action.enabled) }
                viewModelScope.launch {
                    preferencesRepository.setReaderKeepScreenOn(action.enabled)
                }
            }
            is ReaderAction.SetScreenOrientation -> {
                _state.update { it.copy(screenOrientation = action.orientation) }
                // Persist to global settings
                viewModelScope.launch {
                    val domainOrientation = when (action.orientation) {
                        ScreenOrientation.AUTO -> DomainScreenOrientation.AUTO
                        ScreenOrientation.PORTRAIT -> DomainScreenOrientation.PORTRAIT
                        ScreenOrientation.LANDSCAPE -> DomainScreenOrientation.LANDSCAPE
                    }
                    preferencesRepository.setReaderScreenOrientation(domainOrientation)
                }
            }
            is ReaderAction.SetReadingTheme -> {
                _state.update { it.copy(readingTheme = action.theme) }
                val themeName = when (action.theme) {
                    ReadingTheme.LIGHT -> "light"
                    ReadingTheme.DARK -> "dark"
                    ReadingTheme.SEPIA -> "sepia"
                    ReadingTheme.BLACK -> "black"
                }
                pdfViewer?.ui?.setReadingTheme(themeName)
                // Persist to global settings
                viewModelScope.launch {
                    val domainTheme = when (action.theme) {
                        ReadingTheme.LIGHT -> DomainReadingTheme.LIGHT
                        ReadingTheme.SEPIA -> DomainReadingTheme.SEPIA
                        ReadingTheme.DARK -> DomainReadingTheme.DARK
                        ReadingTheme.BLACK -> DomainReadingTheme.BLACK
                    }
                    preferencesRepository.setReaderTheme(domainTheme)
                }
            }

            is ReaderAction.SetDoubleTapZoom -> {
                _state.update { it.copy(doubleTapZoom = action.zoom) }
                viewModelScope.launch {
                    preferencesRepository.setReaderDoubleTapZoom(action.zoom)
                }
            }

            is ReaderAction.SetAutoHideToolbar -> {
                _state.update { it.copy(autoHideToolbar = action.enabled) }
                viewModelScope.launch {
                    preferencesRepository.setReaderAutoHideToolbar(action.enabled)
                }
            }

            is ReaderAction.SetScrubberOnScroll -> {
                _state.update { it.copy(scrubberOnScroll = action.enabled) }
                viewModelScope.launch {
                    preferencesRepository.setReaderScrubberOnScroll(action.enabled)
                }
            }

            is ReaderAction.SetTapToTurnPage -> {
                _state.update { it.copy(tapToTurnPage = action.enabled) }
                viewModelScope.launch {
                    preferencesRepository.setReaderTapToTurnPage(action.enabled)
                }
            }

            is ReaderAction.SetLockHorizontalScroll -> {
                // Nothing to lock along the axis the document scrolls on.
                if (!_state.value.canLockHorizontalScroll) return

                _state.update { it.copy(lockHorizontalScroll = action.enabled) }
                applyHorizontalScrollLock(action.enabled)
                viewModelScope.launch {
                    filePreferenceDao.save(
                        (filePreferenceDao.get(pdfPath) ?: FilePreferenceEntity(pdfPath = pdfPath))
                            .copy(
                                lockHorizontalScroll = action.enabled,
                                updatedAt = System.currentTimeMillis()
                            )
                    )
                }
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
            is ReaderAction.AddToFavorite -> addToFavorite()
            is ReaderAction.ShowRemoveFavoriteDialog -> _state.update { it.copy(isRemoveFavoriteDialogVisible = true) }
            is ReaderAction.HideRemoveFavoriteDialog -> _state.update { it.copy(isRemoveFavoriteDialogVisible = false) }
            is ReaderAction.ConfirmRemoveFavorite -> confirmRemoveFavorite()
            is ReaderAction.ShareDocument -> viewModelScope.launch { _events.send(ReaderEvent.ShareDocument) }
            is ReaderAction.PrintDocument -> printDocument()
            is ReaderAction.OpenWithExternal -> openWithExternal()
            is ReaderAction.SaveDocument -> saveDocument()
            is ReaderAction.SaveDocumentWithPicker -> viewModelScope.launch { _events.send(ReaderEvent.SaveDocumentPicker) }
            is ReaderAction.CloseDocument -> viewModelScope.launch { _events.send(ReaderEvent.DocumentClosed) }
            is ReaderAction.OpenLink -> openLink(action.url)

            // Top bar menu
            is ReaderAction.ShowTopBarMenu -> _state.update { it.copy(isTopBarMenuVisible = true) }
            is ReaderAction.HideTopBarMenu -> _state.update { it.copy(isTopBarMenuVisible = false) }

            is ReaderAction.ShowInfoDialog -> _state.update { it.copy(isInfoDialogVisible = true) }
            is ReaderAction.HideInfoDialog -> _state.update { it.copy(isInfoDialogVisible = false) }
            is ReaderAction.ShowDeleteDialog -> _state.update { it.copy(isDeleteDialogVisible = true) }
            is ReaderAction.HideDeleteDialog -> _state.update { it.copy(isDeleteDialogVisible = false) }
            is ReaderAction.ConfirmDelete -> deleteDocument()

            is ReaderAction.ToggleRotationLock -> _state.update { it.copy(isRotationLocked = !it.isRotationLocked) }

            // Highlights
            // Only tracks the live selection. It deliberately does not close the
            // picker, because dismissing the selection action mode clears the
            // selection, which would otherwise close the picker the instant it opened.
            is ReaderAction.TextSelectionChanged -> {
                _state.update { it.copy(pendingSelection = action.selection) }
            }

            is ReaderAction.StartHighlight -> {
                val selection = _state.value.pendingSelection ?: return
                _state.update {
                    it.copy(
                        isHighlightPickerVisible = true,
                        editingHighlightId = null,
                        capturedSelection = selection
                    )
                }
            }

            is ReaderAction.ApplyHighlightColor -> applyHighlightColor(action.color)

            // From the selection action bar, which shows the colours inline, so there
            // is no separate picker step to capture the selection first.
            is ReaderAction.HighlightSelection -> {
                val selection = _state.value.pendingSelection ?: return
                _state.update { it.copy(capturedSelection = selection, editingHighlightId = null) }
                applyHighlightColor(action.color)
            }

            is ReaderAction.HighlightTapped -> {
                _state.update {
                    it.copy(
                        isHighlightPickerVisible = true,
                        editingHighlightId = action.highlight.id,
                        editingAnchor = action.highlight.anchor
                    )
                }
            }

            is ReaderAction.DeleteHighlight -> {
                viewModelScope.launch {
                    annotationDao.deleteById(action.highlightId)
                    _state.update {
                        it.copy(
                            isHighlightPickerVisible = false,
                            editingHighlightId = null,
                            editingAnchor = null
                        )
                    }
                }
            }

            is ReaderAction.SetHighlightLabel -> {
                viewModelScope.launch {
                    val existing = annotationDao.getById(action.highlightId) ?: return@launch
                    annotationDao.update(
                        existing.copy(
                            label = action.label?.takeIf { it.isNotBlank() },
                            updatedAt = System.currentTimeMillis()
                        )
                    )
                }
            }

            is ReaderAction.DismissHighlightPicker -> {
                _state.update {
                    it.copy(
                        isHighlightPickerVisible = false,
                        editingHighlightId = null,
                        editingAnchor = null,
                        capturedSelection = null
                    )
                }
            }

            is ReaderAction.GoToHighlight -> goToHighlight(action.highlightId)

            is ReaderAction.ShowHighlightsSheet -> _state.update {
                it.copy(isHighlightsSheetVisible = true, highlightsSheetQuery = action.query)
            }
            is ReaderAction.HideHighlightsSheet -> _state.update { it.copy(isHighlightsSheetVisible = false) }

            is ReaderAction.NextHighlight -> stepHighlight(forward = true)
            is ReaderAction.PreviousHighlight -> stepHighlight(forward = false)

            is ReaderAction.HideHighlightNav -> {
                _state.update { it.copy(isHighlightNavVisible = false, currentHighlightIndex = -1) }
            }

            is ReaderAction.ShowBakeHighlightsDialog -> {
                if (_state.value.highlights.isEmpty()) {
                    viewModelScope.launch {
                        _events.send(
                            ReaderEvent.ShowMessage(
                                applicationContext.getString(R.string.bake_highlights_none)
                            )
                        )
                    }
                } else {
                    _state.update { it.copy(isBakeHighlightsDialogVisible = true) }
                }
            }

            is ReaderAction.HideBakeHighlightsDialog -> {
                _state.update { it.copy(isBakeHighlightsDialogVisible = false) }
            }

            is ReaderAction.SaveDecryptedCopy -> {
                viewModelScope.launch {
                    if (documentPassword == null) {
                        _events.send(
                            ReaderEvent.Error(
                                applicationContext.getString(R.string.save_decrypted_no_password)
                            )
                        )
                    } else {
                        _events.send(ReaderEvent.SaveDecryptedCopyPicker)
                    }
                }
            }

            is ReaderAction.ConfirmBakeHighlights -> {
                _state.update { it.copy(isBakeHighlightsDialogVisible = false) }
                viewModelScope.launch { _events.send(ReaderEvent.BakeHighlightsPicker) }
            }

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
                            title = applicationContext.getString(R.string.page_current, currentPage + 1)
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

            is ReaderAction.OpenAttachment -> {
                pendingAttachmentAction = AttachmentAction.OPEN
                viewModelScope.launch {
                    pdfViewer?.ui?.performSidebarTreeItemClick(action.attachment.id)
                }
            }

            is ReaderAction.DownloadAttachment -> {
                pendingAttachmentAction = AttachmentAction.DOWNLOAD
                viewModelScope.launch {
                    pdfViewer?.ui?.performSidebarTreeItemClick(action.attachment.id)
                }
            }
        }
    }

    private fun submitPassword(password: String, remember: Boolean) {
        viewModelScope.launch {
            if (remember && preferencesRepository.preferences.first().rememberPasswords) {
                passwordStorage.savePassword(pdfPath, password)
            }
            awaitingStoredPasswordResult = false
            // Held so a decrypted copy can be written without asking again. Cleared
            // below if the viewer comes back asking for the password, which means
            // this one was wrong.
            documentPassword = password
            _state.update {
                it.copy(
                    passwordSubmitted = true,
                    isPasswordRequired = false,
                    isPasswordProtected = true
                )
            }
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
            // Update state
            val isFav = favoriteRepository.isFavorite(pdfPath)
            _state.update { it.copy(isFavorite = isFav) }
        }
    }

    private fun addToFavorite() {
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
            favoriteRepository.addFavorite(pdfFile)
            _state.update { it.copy(isFavorite = true) }
            _events.send(ReaderEvent.FavoriteAdded)
        }
    }

    private fun confirmRemoveFavorite() {
        viewModelScope.launch {
            _state.update { it.copy(isRemoveFavoriteDialogVisible = false) }
            favoriteRepository.removeFavorite(pdfPath)
            _state.update { it.copy(isFavorite = false) }
            _events.send(ReaderEvent.ShowMessage("Removed from favourites"))
        }
    }

    private fun openWithExternal() {
        viewModelScope.launch {
            try {
                val file = File(pdfPath)
                val uri = androidx.core.content.FileProvider.getUriForFile(
                    applicationContext,
                    "${applicationContext.packageName}.provider",
                    file
                )
                val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, "application/pdf")
                    addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                // Create chooser to let user pick the app
                val chooser = android.content.Intent.createChooser(intent, "Open with")
                chooser.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                applicationContext.startActivity(chooser)
            } catch (e: Exception) {
                _events.send(ReaderEvent.Error("Failed to open with external app: ${e.message}"))
            }
        }
    }

    private fun openLink(url: String) {
        viewModelScope.launch {
            try {
                val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                    data = android.net.Uri.parse(url)
                    addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                applicationContext.startActivity(intent)
            } catch (e: Exception) {
                _events.send(ReaderEvent.Error("Failed to open link: ${e.message}"))
            }
        }
    }

    private fun saveDocument() {
        viewModelScope.launch {
            try {
                val sourceFile = File(pdfPath)
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val destFile = File(downloadsDir, sourceFile.name)

                // If file already exists, add number suffix
                var finalFile = destFile
                var counter = 1
                while (finalFile.exists()) {
                    val nameWithoutExt = sourceFile.nameWithoutExtension
                    val ext = sourceFile.extension
                    finalFile = File(downloadsDir, "${nameWithoutExt}_$counter.$ext")
                    counter++
                }

                sourceFile.copyTo(finalFile)
                _events.send(ReaderEvent.ShowMessage("Saved to Downloads: ${finalFile.name}"))
            } catch (e: Exception) {
                _events.send(ReaderEvent.Error("Failed to save document: ${e.message}"))
            }
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

    private suspend fun openAttachmentFile(fileBytes: ByteArray, fileName: String?, mimeType: String?) {
        try {
            // Save to cache directory
            val cacheDir = File(applicationContext.cacheDir, "attachments")
            if (!cacheDir.exists()) cacheDir.mkdirs()

            val finalFileName = fileName ?: "attachment_${System.currentTimeMillis()}"
            val file = File(cacheDir, finalFileName)
            file.writeBytes(fileBytes)

            // Get content URI using FileProvider
            val uri = androidx.core.content.FileProvider.getUriForFile(
                applicationContext,
                "${applicationContext.packageName}.provider",
                file
            )

            // Determine MIME type
            val resolvedMimeType = mimeType
                ?: android.webkit.MimeTypeMap.getSingleton()
                    .getMimeTypeFromExtension(file.extension.lowercase())
                ?: "*/*"

            // Create open intent
            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                setDataAndType(uri, resolvedMimeType)
                addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            // Check if there's an app that can handle this
            if (intent.resolveActivity(applicationContext.packageManager) != null) {
                applicationContext.startActivity(intent)
            } else {
                // No app found, offer to save instead
                _events.send(ReaderEvent.ShowMessage("No app found to open this file. Saving to Downloads..."))
                saveAttachmentFile(fileBytes, fileName)
            }
        } catch (e: Exception) {
            _events.send(ReaderEvent.Error("Failed to open attachment: ${e.message}"))
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

    fun saveToUri(uri: android.net.Uri) {
        viewModelScope.launch {
            try {
                val sourceFile = File(pdfPath)
                applicationContext.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    sourceFile.inputStream().use { inputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
                _events.send(ReaderEvent.ShowMessage("Document saved successfully"))
            } catch (e: Exception) {
                _events.send(ReaderEvent.Error("Failed to save: ${e.message}"))
            }
        }
    }

    /**
     * Writes the stored highlights into a copy of the PDF at [uri].
     *
     * Runs off the main thread: this reads and rewrites the whole document, which is
     * far too slow to sit on the UI thread for a large file.
     *
     * The Room records are deliberately kept. They remain the source of truth in the
     * app, and the copy is an export rather than a migration.
     */
    fun bakeHighlightsToUri(uri: android.net.Uri) {
        val highlights = _state.value.highlights
        if (highlights.isEmpty()) return

        viewModelScope.launch {
            _state.update { it.copy(isBakingHighlights = true) }
            try {
                val written = withContext(Dispatchers.IO) {
                    applicationContext.contentResolver.openOutputStream(uri)?.use { output ->
                        HighlightBaker.bake(File(pdfPath), output, highlights)
                    } ?: throw IOException("Could not open the destination file")
                }
                _events.send(
                    ReaderEvent.ShowMessage(
                        applicationContext.getString(R.string.bake_highlights_done, written)
                    )
                )
            } catch (e: Exception) {
                Timber.e(e, "Failed to bake highlights into a copy")
                _events.send(
                    ReaderEvent.Error(
                        applicationContext.getString(
                            R.string.bake_highlights_failed,
                            e.message ?: ""
                        )
                    )
                )
            } finally {
                _state.update { it.copy(isBakingHighlights = false) }
            }
        }
    }

    /**
     * Write a copy of this document with the encryption stripped, reusing the
     * password it was already opened with.
     *
     * This exists because the print menu's "Save as PDF" was the only route people
     * found, and that rasterises every page: slow, much larger, and the text stops
     * being selectable. Going through iText keeps the document intact.
     */
    fun saveDecryptedCopyToUri(uri: android.net.Uri) {
        val password = documentPassword
        if (password == null) {
            viewModelScope.launch {
                _events.send(
                    ReaderEvent.Error(
                        applicationContext.getString(R.string.save_decrypted_no_password)
                    )
                )
            }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isSavingDecryptedCopy = true) }
            try {
                withContext(Dispatchers.IO) {
                    // unlockPdf writes to a path, the picker hands back a document
                    // uri, so stage it in the cache and stream it across.
                    val staged = File.createTempFile("decrypted", ".pdf", applicationContext.cacheDir)
                    try {
                        pdfToolsRepository
                            .unlockPdf(pdfPath, staged.absolutePath, password)
                            .getOrThrow()

                        applicationContext.contentResolver.openOutputStream(uri)?.use { output ->
                            staged.inputStream().use { it.copyTo(output) }
                        } ?: throw IOException("Could not open the destination file")
                    } finally {
                        staged.delete()
                    }
                }
                _events.send(
                    ReaderEvent.ShowMessage(
                        applicationContext.getString(R.string.save_decrypted_done)
                    )
                )
            } catch (e: Exception) {
                Timber.e(e, "Failed to write a decrypted copy")
                _events.send(
                    ReaderEvent.Error(
                        applicationContext.getString(
                            R.string.save_decrypted_failed,
                            e.message ?: ""
                        )
                    )
                )
            } finally {
                _state.update { it.copy(isSavingDecryptedCopy = false) }
            }
        }
    }

    /** Suggested filename for the decrypted copy. */
    fun getDecryptedFileName(): String {
        return "${File(pdfPath).nameWithoutExtension}-unlocked.pdf"
    }

    fun getDocumentFileName(): String {
        return File(pdfPath).name
    }

    /** Suggested name for the highlighted copy, so it is not mistaken for the original. */
    fun getHighlightedFileName(): String {
        val file = File(pdfPath)
        return "${file.nameWithoutExtension}-highlighted.pdf"
    }

    private companion object {
        // Fraction of the page's leading/trailing edge that acts as a page-turn tap zone.
        // The middle (1 - 2 * fraction) toggles the toolbar instead.
        const val TAP_TURN_ZONE_FRACTION = 1f / 3f

        // Time allowed for a page to render after goToPage before trying to pulse a
        // highlight on it. scrollToHighlight only finds elements on rendered pages.
        const val HIGHLIGHT_SCROLL_DELAY_MS = 350L
    }
}
