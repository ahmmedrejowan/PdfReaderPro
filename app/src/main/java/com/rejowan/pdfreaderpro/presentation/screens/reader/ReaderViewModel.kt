package com.rejowan.pdfreaderpro.presentation.screens.reader

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rejowan.pdfreaderpro.data.pdf.ColorMode
import com.rejowan.pdfreaderpro.data.pdf.MuPdfDocument
import com.rejowan.pdfreaderpro.data.pdf.PasswordRequiredException
import com.rejowan.pdfreaderpro.data.pdf.PdfRenderer
import com.rejowan.pdfreaderpro.data.pdf.SearchResult
import com.rejowan.pdfreaderpro.domain.repository.FavoriteRepository
import com.rejowan.pdfreaderpro.domain.repository.RecentRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class ReaderViewModel(
    private val recentRepository: RecentRepository,
    private val favoriteRepository: FavoriteRepository,
    private val applicationContext: Context,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    // Get path and initial page from navigation arguments
    private val pdfPath: String = savedStateHandle.get<String>("path") ?: ""
    private val initialPage: Int = savedStateHandle.get<Int>("page") ?: 0
    private val fromIntent: Boolean = savedStateHandle.get<Boolean>("fromIntent") ?: false

    private val _state = MutableStateFlow(ReaderState(documentPath = pdfPath))
    val state: StateFlow<ReaderState> = _state.asStateFlow()

    private val _events = Channel<ReaderEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    // PDF document and renderer
    private var document: MuPdfDocument? = null
    private var renderer: PdfRenderer? = null

    // Page bitmaps cache for UI
    private val _pageBitmaps = MutableStateFlow<Map<Int, Bitmap>>(emptyMap())
    val pageBitmaps: StateFlow<Map<Int, Bitmap>> = _pageBitmaps.asStateFlow()

    // Current page bitmap
    private val _currentPageBitmap = MutableStateFlow<Bitmap?>(null)
    val currentPageBitmap: StateFlow<Bitmap?> = _currentPageBitmap.asStateFlow()

    // Search job
    private var searchJob: Job? = null

    // Page save job (debounced)
    private var pageSaveJob: Job? = null

    init {
        loadDocument()
    }

    /**
     * Load the PDF document.
     */
    private fun loadDocument(password: String? = null) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            val result = if (pdfPath.startsWith("content://")) {
                MuPdfDocument.openFromUri(applicationContext, Uri.parse(pdfPath), password)
            } else {
                MuPdfDocument.open(pdfPath, password)
            }

            result.fold(
                onSuccess = { doc ->
                    document = doc
                    renderer = PdfRenderer(doc)

                    _state.update {
                        it.copy(
                            isLoading = false,
                            documentTitle = doc.title ?: File(pdfPath).nameWithoutExtension,
                            totalPages = doc.pageCount,
                            currentPage = initialPage.coerceIn(0, doc.pageCount - 1),
                            tableOfContents = doc.getOutline(),
                            isPasswordRequired = false,
                            isPasswordError = false
                        )
                    }

                    // Render initial page
                    renderCurrentPage()

                    // Update recent files
                    updateRecentFile()
                },
                onFailure = { error ->
                    when (error) {
                        is PasswordRequiredException -> {
                            _state.update {
                                it.copy(
                                    isLoading = false,
                                    isPasswordRequired = true,
                                    isPasswordError = password != null
                                )
                            }
                        }
                        else -> {
                            _state.update {
                                it.copy(
                                    isLoading = false,
                                    error = error.message ?: "Failed to open PDF"
                                )
                            }
                        }
                    }
                }
            )
        }
    }

    /**
     * Handle reader actions.
     */
    fun onAction(action: ReaderAction) {
        when (action) {
            // Navigation
            is ReaderAction.GoToPage -> goToPage(action.page)
            is ReaderAction.NextPage -> goToPage(_state.value.currentPage + 1)
            is ReaderAction.PreviousPage -> goToPage(_state.value.currentPage - 1)

            // Zoom
            is ReaderAction.SetZoom -> setZoom(action.zoom)
            is ReaderAction.ZoomIn -> setZoom(_state.value.zoom * 1.25f)
            is ReaderAction.ZoomOut -> setZoom(_state.value.zoom / 1.25f)
            is ReaderAction.ResetZoom -> setZoom(1f)

            // UI visibility
            is ReaderAction.ToggleToolbar -> toggleToolbar()
            is ReaderAction.ToggleFullScreen -> toggleFullScreen()
            is ReaderAction.ShowPageJumpDialog -> _state.update { it.copy(isPageJumpDialogVisible = true) }
            is ReaderAction.HidePageJumpDialog -> _state.update { it.copy(isPageJumpDialogVisible = false) }
            is ReaderAction.ShowTableOfContents -> _state.update { it.copy(isTableOfContentsVisible = true) }
            is ReaderAction.HideTableOfContents -> _state.update { it.copy(isTableOfContentsVisible = false) }
            is ReaderAction.ShowPageThumbnails -> _state.update { it.copy(isPageThumbnailsVisible = true) }
            is ReaderAction.HidePageThumbnails -> _state.update { it.copy(isPageThumbnailsVisible = false) }
            is ReaderAction.ShowSettingsPanel -> _state.update { it.copy(isSettingsPanelVisible = true) }
            is ReaderAction.HideSettingsPanel -> _state.update { it.copy(isSettingsPanelVisible = false) }

            // Reading settings
            is ReaderAction.SetColorMode -> setColorMode(action.mode)
            is ReaderAction.SetBrightness -> _state.update { it.copy(brightness = action.brightness) }
            is ReaderAction.SetScrollDirection -> _state.update { it.copy(scrollDirection = action.direction) }
            is ReaderAction.SetKeepScreenOn -> _state.update { it.copy(keepScreenOn = action.enabled) }

            // Search
            is ReaderAction.Search -> search(action.query)
            is ReaderAction.NextSearchResult -> nextSearchResult()
            is ReaderAction.PreviousSearchResult -> previousSearchResult()
            is ReaderAction.ClearSearch -> clearSearch()
            is ReaderAction.ToggleSearch -> toggleSearch()

            // Password
            is ReaderAction.SubmitPassword -> submitPassword(action.password, action.remember)

            // Document actions
            is ReaderAction.ToggleFavorite -> toggleFavorite()
            is ReaderAction.ShareDocument -> shareDocument()
            is ReaderAction.CloseDocument -> closeDocument()
        }
    }

    private fun goToPage(page: Int) {
        val totalPages = _state.value.totalPages
        if (totalPages == 0) return

        val targetPage = page.coerceIn(0, totalPages - 1)
        if (targetPage == _state.value.currentPage) return

        _state.update { it.copy(currentPage = targetPage) }
        renderCurrentPage()
        savePageProgress()
    }

    private fun setZoom(zoom: Float) {
        val state = _state.value
        val newZoom = zoom.coerceIn(state.minZoom, state.maxZoom)
        _state.update { it.copy(zoom = newZoom) }
        renderCurrentPage()
    }

    private fun setColorMode(mode: ColorMode) {
        _state.update { it.copy(colorMode = mode) }
        renderCurrentPage()
    }

    private fun toggleToolbar() {
        _state.update { it.copy(isToolbarVisible = !it.isToolbarVisible) }
    }

    private fun toggleFullScreen() {
        _state.update {
            it.copy(
                isFullScreen = !it.isFullScreen,
                isToolbarVisible = it.isFullScreen // Show toolbar when exiting fullscreen
            )
        }
    }

    private fun toggleSearch() {
        _state.update {
            it.copy(
                isSearchActive = !it.isSearchActive,
                searchQuery = if (it.isSearchActive) "" else it.searchQuery,
                searchResults = if (it.isSearchActive) emptyList() else it.searchResults
            )
        }
    }

    private fun search(query: String) {
        if (query.isBlank()) {
            clearSearch()
            return
        }

        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            _state.update { it.copy(searchQuery = query, isSearching = true, searchResults = emptyList()) }

            val doc = document ?: return@launch
            val results = mutableListOf<SearchResult>()

            for (pageIndex in 0 until doc.pageCount) {
                val pageResults = doc.searchPage(pageIndex, query)
                results.addAll(pageResults)

                // Update results incrementally
                if (pageResults.isNotEmpty()) {
                    _state.update { it.copy(searchResults = results.toList()) }
                }
            }

            _state.update {
                it.copy(
                    isSearching = false,
                    searchResults = results,
                    currentSearchIndex = if (results.isNotEmpty()) 0 else -1
                )
            }

            // Navigate to first result
            if (results.isNotEmpty()) {
                goToPage(results[0].page)
            }
        }
    }

    private fun nextSearchResult() {
        val state = _state.value
        if (state.searchResults.isEmpty()) return

        val nextIndex = (state.currentSearchIndex + 1) % state.searchResults.size
        _state.update { it.copy(currentSearchIndex = nextIndex) }

        val result = state.searchResults[nextIndex]
        goToPage(result.page)
    }

    private fun previousSearchResult() {
        val state = _state.value
        if (state.searchResults.isEmpty()) return

        val prevIndex = if (state.currentSearchIndex <= 0) {
            state.searchResults.size - 1
        } else {
            state.currentSearchIndex - 1
        }
        _state.update { it.copy(currentSearchIndex = prevIndex) }

        val result = state.searchResults[prevIndex]
        goToPage(result.page)
    }

    private fun clearSearch() {
        searchJob?.cancel()
        _state.update {
            it.copy(
                searchQuery = "",
                searchResults = emptyList(),
                currentSearchIndex = 0,
                isSearching = false
            )
        }
    }

    private fun submitPassword(password: String, remember: Boolean) {
        loadDocument(password)
        // TODO: If remember is true, store password securely
    }

    private fun toggleFavorite() {
        viewModelScope.launch {
            val state = _state.value
            val file = File(pdfPath)
            val pdfFile = com.rejowan.pdfreaderpro.domain.model.PdfFile(
                id = pdfPath.hashCode().toLong(),
                name = state.documentTitle ?: file.name,
                path = pdfPath,
                uri = android.net.Uri.fromFile(file),
                size = file.length(),
                dateModified = file.lastModified(),
                dateAdded = file.lastModified(),
                pageCount = state.totalPages,
                parentFolder = file.parent ?: ""
            )
            favoriteRepository.toggleFavorite(pdfFile)
        }
    }

    private fun shareDocument() {
        viewModelScope.launch {
            _events.send(ReaderEvent.ShareDocument)
        }
    }

    private fun closeDocument() {
        viewModelScope.launch {
            _events.send(ReaderEvent.DocumentClosed)
        }
    }

    /**
     * Render the current page bitmap.
     */
    private fun renderCurrentPage() {
        viewModelScope.launch {
            val state = _state.value
            val pageRenderer = renderer ?: return@launch

            try {
                val bitmap = pageRenderer.renderPage(
                    pageIndex = state.currentPage,
                    zoom = state.zoom,
                    colorMode = state.colorMode
                )
                _currentPageBitmap.value = bitmap

                // Pre-render adjacent pages
                pageRenderer.preRenderAdjacent(state.currentPage, state.zoom)
            } catch (e: Exception) {
                _events.send(ReaderEvent.Error("Failed to render page: ${e.message}"))
            }
        }
    }

    /**
     * Render a specific page for thumbnails.
     */
    fun renderThumbnail(pageIndex: Int, onResult: (Bitmap?) -> Unit) {
        viewModelScope.launch {
            val pageRenderer = renderer ?: return@launch
            try {
                val bitmap = pageRenderer.renderThumbnail(pageIndex)
                onResult(bitmap)
            } catch (e: Exception) {
                onResult(null)
            }
        }
    }

    /**
     * Update recent files.
     */
    private fun updateRecentFile() {
        viewModelScope.launch {
            val state = _state.value
            if (state.totalPages > 0) {
                val file = File(pdfPath)
                val fileName = state.documentTitle ?: file.name
                recentRepository.addOrUpdateRecent(
                    path = pdfPath,
                    name = fileName,
                    size = file.length(),
                    totalPages = state.totalPages,
                    currentPage = state.currentPage
                )
            }
        }
    }

    /**
     * Save page progress (debounced).
     */
    private fun savePageProgress() {
        pageSaveJob?.cancel()
        pageSaveJob = viewModelScope.launch {
            delay(1000) // Debounce 1 second
            recentRepository.updateLastPage(pdfPath, _state.value.currentPage)
        }
    }

    /**
     * Check if document is favorited.
     */
    suspend fun isFavorite(): Boolean {
        return favoriteRepository.isFavorite(pdfPath)
    }

    override fun onCleared() {
        super.onCleared()
        // Save final reading position
        viewModelScope.launch {
            val state = _state.value
            if (state.totalPages > 0) {
                recentRepository.updateLastPage(pdfPath, state.currentPage)
            }
        }
        // Close document and clear cache
        viewModelScope.launch(Dispatchers.IO) {
            renderer?.clearCache()
            document?.close()
        }
    }
}
