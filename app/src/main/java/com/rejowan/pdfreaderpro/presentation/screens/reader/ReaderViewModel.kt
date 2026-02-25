package com.rejowan.pdfreaderpro.presentation.screens.reader

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rejowan.pdfreaderpro.data.local.PasswordStorage
import com.rejowan.pdfreaderpro.domain.repository.FavoriteRepository
import com.rejowan.pdfreaderpro.domain.repository.RecentRepository
import com.rejowan.pdfreaderpro.presentation.screens.reader.components.PdfInfo
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File

class ReaderViewModel(
    private val recentRepository: RecentRepository,
    private val favoriteRepository: FavoriteRepository,
    private val applicationContext: Context,
    savedStateHandle: SavedStateHandle,
    private val passwordStorage: PasswordStorage = PasswordStorage(applicationContext)
) : ViewModel() {

    private val pdfPath: String = savedStateHandle.get<String>("path") ?: ""

    private val _state = MutableStateFlow(ReaderState(documentPath = pdfPath))
    val state: StateFlow<ReaderState> = _state.asStateFlow()

    private val _events = Channel<ReaderEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    init {
        loadDocument()
    }

    private fun loadDocument() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            // TODO: Integrate PdfViewer library
            val file = File(pdfPath)
            _state.update {
                it.copy(
                    isLoading = false,
                    documentTitle = file.nameWithoutExtension,
                    error = "PDF viewer not yet integrated"
                )
            }
        }
    }

    fun onAction(action: ReaderAction) {
        when (action) {
            is ReaderAction.GoToPage -> _state.update { it.copy(currentPage = action.page) }
            is ReaderAction.NextPage -> _state.update { it.copy(currentPage = it.currentPage + 1) }
            is ReaderAction.PreviousPage -> _state.update { it.copy(currentPage = (it.currentPage - 1).coerceAtLeast(0)) }

            is ReaderAction.SetZoom -> _state.update { it.copy(zoom = action.zoom.coerceIn(it.minZoom, it.maxZoom)) }
            is ReaderAction.ZoomIn -> _state.update { it.copy(zoom = (it.zoom * 1.25f).coerceAtMost(it.maxZoom)) }
            is ReaderAction.ZoomOut -> _state.update { it.copy(zoom = (it.zoom / 1.25f).coerceAtLeast(it.minZoom)) }
            is ReaderAction.ResetZoom -> _state.update { it.copy(zoom = 1f) }

            is ReaderAction.ToggleToolbar -> _state.update { it.copy(isToolbarVisible = !it.isToolbarVisible) }
            is ReaderAction.ToggleFullScreen -> _state.update { it.copy(isFullScreen = !it.isFullScreen, isToolbarVisible = it.isFullScreen) }
            is ReaderAction.ShowPageJumpDialog -> _state.update { it.copy(isPageJumpDialogVisible = true) }
            is ReaderAction.HidePageJumpDialog -> _state.update { it.copy(isPageJumpDialogVisible = false) }
            is ReaderAction.ShowTableOfContents -> _state.update { it.copy(isTableOfContentsVisible = true) }
            is ReaderAction.HideTableOfContents -> _state.update { it.copy(isTableOfContentsVisible = false) }
            is ReaderAction.ShowPageThumbnails -> _state.update { it.copy(isPageThumbnailsVisible = true) }
            is ReaderAction.HidePageThumbnails -> _state.update { it.copy(isPageThumbnailsVisible = false) }
            is ReaderAction.ShowSettingsPanel -> _state.update { it.copy(isSettingsPanelVisible = true) }
            is ReaderAction.HideSettingsPanel -> _state.update { it.copy(isSettingsPanelVisible = false) }

            is ReaderAction.SetBrightness -> _state.update { it.copy(brightness = action.brightness) }
            is ReaderAction.SetScrollDirection -> _state.update { it.copy(scrollDirection = action.direction) }
            is ReaderAction.SetKeepScreenOn -> _state.update { it.copy(keepScreenOn = action.enabled) }

            is ReaderAction.Search -> { /* TODO */ }
            is ReaderAction.NextSearchResult -> { /* TODO */ }
            is ReaderAction.PreviousSearchResult -> { /* TODO */ }
            is ReaderAction.ClearSearch -> _state.update { it.copy(searchQuery = "", isSearching = false) }
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
        }
    }

    private fun submitPassword(password: String, remember: Boolean) {
        viewModelScope.launch {
            if (remember) {
                passwordStorage.savePassword(pdfPath, password)
            }
            // TODO: Authenticate with PDF library
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

    fun getPdfInfo(): PdfInfo {
        val file = File(pdfPath)
        return PdfInfo(
            title = _state.value.documentTitle,
            author = null,
            path = pdfPath,
            pageCount = _state.value.totalPages,
            fileSize = file.length(),
            lastModified = file.lastModified()
        )
    }

    suspend fun isFavorite(): Boolean = favoriteRepository.isFavorite(pdfPath)
}
