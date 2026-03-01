package com.rejowan.pdfreaderpro.presentation.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rejowan.pdfreaderpro.domain.model.FolderSortOption
import com.rejowan.pdfreaderpro.domain.model.PdfFile
import com.rejowan.pdfreaderpro.domain.model.PdfFolder
import com.rejowan.pdfreaderpro.domain.model.RecentFile
import com.rejowan.pdfreaderpro.domain.model.SortOption
import com.rejowan.pdfreaderpro.domain.model.ViewMode
import com.rejowan.pdfreaderpro.domain.repository.FavoriteRepository
import com.rejowan.pdfreaderpro.domain.repository.PdfFileRepository
import com.rejowan.pdfreaderpro.domain.repository.PreferencesRepository
import com.rejowan.pdfreaderpro.domain.repository.RecentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber

class HomeViewModel(
    private val pdfFileRepository: PdfFileRepository,
    private val recentRepository: RecentRepository,
    private val favoriteRepository: FavoriteRepository,
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _viewMode = MutableStateFlow(ViewMode.LIST)
    val viewMode: StateFlow<ViewMode> = _viewMode.asStateFlow()

    private val _sortOption = MutableStateFlow(SortOption.NAME_ASC)
    val sortOption: StateFlow<SortOption> = _sortOption.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _folderSortOption = MutableStateFlow(FolderSortOption.NAME_ASC)
    val folderSortOption: StateFlow<FolderSortOption> = _folderSortOption.asStateFlow()

    private val _folderSearchQuery = MutableStateFlow("")
    val folderSearchQuery: StateFlow<String> = _folderSearchQuery.asStateFlow()

    val allFiles: StateFlow<List<PdfFile>> = combine(
        pdfFileRepository.getAllPdfFiles(),
        _sortOption
    ) { files, sort ->
        sortFiles(files, sort)
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val recentFiles: StateFlow<List<RecentFile>> = recentRepository.getRecentFiles()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val favoriteFiles: StateFlow<List<PdfFile>> = favoriteRepository.getFavorites()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val folders: StateFlow<List<PdfFolder>> = combine(
        pdfFileRepository.getPdfFolders(),
        _folderSortOption,
        _folderSearchQuery
    ) { folders, sort, query ->
        val filtered = if (query.isBlank()) folders else folders.filter {
            it.name.contains(query, ignoreCase = true)
        }
        sortFolders(filtered, sort)
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val searchResults: StateFlow<List<PdfFile>> = combine(
        pdfFileRepository.searchPdfs(_searchQuery.value),
        _searchQuery
    ) { results, query ->
        if (query.isBlank()) emptyList() else results
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            try {
                val prefs = preferencesRepository.preferences.first()
                _viewMode.value = prefs.defaultViewMode
                _sortOption.value = prefs.defaultSortOption

                pdfFileRepository.refreshPdfs()
            } catch (e: Exception) {
                Timber.e(e, "Error loading initial data")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                pdfFileRepository.refreshPdfs()
            } catch (e: Exception) {
                Timber.e(e, "Error refreshing PDFs")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun setViewMode(mode: ViewMode) {
        _viewMode.value = mode
        viewModelScope.launch {
            preferencesRepository.setDefaultViewMode(mode)
        }
    }

    fun setSortOption(option: SortOption) {
        _sortOption.value = option
        viewModelScope.launch {
            preferencesRepository.setDefaultSortOption(option)
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setFolderSortOption(option: FolderSortOption) {
        _folderSortOption.value = option
    }

    fun setFolderSearchQuery(query: String) {
        _folderSearchQuery.value = query
    }

    fun toggleFavorite(pdfFile: PdfFile) {
        viewModelScope.launch {
            favoriteRepository.toggleFavorite(pdfFile)
        }
    }

    fun removeFromRecent(path: String) {
        viewModelScope.launch {
            recentRepository.removeRecent(path)
        }
    }

    fun clearAllRecent() {
        viewModelScope.launch {
            recentRepository.clearAllRecent()
        }
    }

    fun clearAllFavorites() {
        viewModelScope.launch {
            favoriteRepository.clearAllFavorites()
        }
    }

    suspend fun isFavorite(path: String): Boolean {
        return favoriteRepository.isFavorite(path)
    }

    private fun sortFiles(files: List<PdfFile>, sortOption: SortOption): List<PdfFile> {
        return when (sortOption) {
            SortOption.NAME_ASC -> files.sortedBy { it.name.lowercase() }
            SortOption.NAME_DESC -> files.sortedByDescending { it.name.lowercase() }
            SortOption.DATE_DESC -> files.sortedByDescending { it.dateModified }
            SortOption.DATE_ASC -> files.sortedBy { it.dateModified }
            SortOption.SIZE_DESC -> files.sortedByDescending { it.size }
            SortOption.SIZE_ASC -> files.sortedBy { it.size }
        }
    }

    private fun sortFolders(folders: List<PdfFolder>, sortOption: FolderSortOption): List<PdfFolder> {
        return when (sortOption) {
            FolderSortOption.NAME_ASC -> folders.sortedBy { it.name.lowercase() }
            FolderSortOption.NAME_DESC -> folders.sortedByDescending { it.name.lowercase() }
            FolderSortOption.COUNT_DESC -> folders.sortedByDescending { it.pdfCount }
            FolderSortOption.COUNT_ASC -> folders.sortedBy { it.pdfCount }
        }
    }
}
