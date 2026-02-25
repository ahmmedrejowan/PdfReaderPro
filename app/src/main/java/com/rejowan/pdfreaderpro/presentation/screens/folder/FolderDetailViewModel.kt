package com.rejowan.pdfreaderpro.presentation.screens.folder

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rejowan.pdfreaderpro.domain.model.PdfFile
import com.rejowan.pdfreaderpro.domain.model.SortOption
import com.rejowan.pdfreaderpro.domain.model.ViewMode
import com.rejowan.pdfreaderpro.domain.repository.FavoriteRepository
import com.rejowan.pdfreaderpro.domain.repository.PdfFileRepository
import com.rejowan.pdfreaderpro.domain.repository.PreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class FolderDetailViewModel(
    private val pdfFileRepository: PdfFileRepository,
    private val favoriteRepository: FavoriteRepository,
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {

    private val _files = MutableStateFlow<List<PdfFile>>(emptyList())
    val files: StateFlow<List<PdfFile>> = _files.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _viewMode = MutableStateFlow(ViewMode.LIST)
    val viewMode: StateFlow<ViewMode> = _viewMode.asStateFlow()

    private val _sortOption = MutableStateFlow(SortOption.NAME_ASC)
    val sortOption: StateFlow<SortOption> = _sortOption.asStateFlow()

    private var currentFolderPath: String = ""

    init {
        loadPreferences()
    }

    private fun loadPreferences() {
        viewModelScope.launch {
            val prefs = preferencesRepository.preferences.first()
            _viewMode.value = prefs.defaultViewMode
            _sortOption.value = prefs.defaultSortOption
        }
    }

    fun loadFilesForFolder(folderPath: String) {
        currentFolderPath = folderPath
        viewModelScope.launch {
            _isLoading.value = true
            try {
                pdfFileRepository.getPdfsByFolder(folderPath).collect { files ->
                    _files.value = sortFiles(files, _sortOption.value)
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _isLoading.value = false
            }
        }
    }

    fun setViewMode(mode: ViewMode) {
        _viewMode.value = mode
    }

    fun setSortOption(option: SortOption) {
        _sortOption.value = option
        _files.value = sortFiles(_files.value, option)
    }

    fun toggleFavorite(pdfFile: PdfFile) {
        viewModelScope.launch {
            favoriteRepository.toggleFavorite(pdfFile)
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
}
