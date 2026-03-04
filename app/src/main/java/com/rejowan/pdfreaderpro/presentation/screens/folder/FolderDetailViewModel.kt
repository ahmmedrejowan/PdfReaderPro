package com.rejowan.pdfreaderpro.presentation.screens.folder

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rejowan.pdfreaderpro.domain.model.PdfFile
import com.rejowan.pdfreaderpro.domain.model.SortOption
import com.rejowan.pdfreaderpro.domain.model.ViewMode
import com.rejowan.pdfreaderpro.domain.repository.FavoriteRepository
import com.rejowan.pdfreaderpro.domain.repository.PdfFileRepository
import com.rejowan.pdfreaderpro.domain.repository.PreferencesRepository
import com.rejowan.pdfreaderpro.domain.repository.RecentRepository
import com.rejowan.pdfreaderpro.util.FileOperations
import java.io.File
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class FolderDetailViewModel(
    private val pdfFileRepository: PdfFileRepository,
    private val favoriteRepository: FavoriteRepository,
    private val preferencesRepository: PreferencesRepository,
    private val recentRepository: RecentRepository
) : ViewModel() {

    private val _files = MutableStateFlow<List<PdfFile>>(emptyList())
    val files: StateFlow<List<PdfFile>> = _files.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

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

    fun refreshFolder() {
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                pdfFileRepository.getPdfsByFolder(currentFolderPath).collect { files ->
                    _files.value = sortFiles(files, _sortOption.value)
                    _isRefreshing.value = false
                }
            } catch (e: Exception) {
                _isRefreshing.value = false
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

    /**
     * Renames a file and updates the path in favorites and recent databases.
     */
    fun renameFile(oldPath: String, newName: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            val newPath = FileOperations.renameFile(oldPath, newName)
            if (newPath != null) {
                val newFileName = File(newPath).name
                // Update path in favorites if present
                favoriteRepository.updatePath(oldPath, newPath, newFileName)
                // Update path in recent if present
                recentRepository.updatePath(oldPath, newPath, newFileName)
                // Reload folder contents
                loadFilesForFolder(currentFolderPath)
                onComplete(true)
            } else {
                onComplete(false)
            }
        }
    }

    /**
     * Deletes a file and removes it from favorites and recent databases.
     */
    fun deleteFile(path: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = FileOperations.deleteFile(path)
            if (success) {
                // Remove from favorites if present
                favoriteRepository.removeFavorite(path)
                // Remove from recent if present
                recentRepository.removeRecent(path)
                // Reload folder contents
                loadFilesForFolder(currentFolderPath)
                onComplete(true)
            } else {
                onComplete(false)
            }
        }
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
