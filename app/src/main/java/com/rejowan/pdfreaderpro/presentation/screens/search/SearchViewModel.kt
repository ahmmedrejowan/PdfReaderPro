package com.rejowan.pdfreaderpro.presentation.screens.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rejowan.pdfreaderpro.domain.model.PdfFile
import com.rejowan.pdfreaderpro.domain.repository.FavoriteRepository
import com.rejowan.pdfreaderpro.domain.repository.PdfFileRepository
import com.rejowan.pdfreaderpro.domain.repository.RecentRepository
import com.rejowan.pdfreaderpro.util.FileOperations
import java.io.File
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SearchViewModel(
    private val pdfFileRepository: PdfFileRepository,
    private val favoriteRepository: FavoriteRepository,
    private val recentRepository: RecentRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    val searchResults: StateFlow<List<PdfFile>> = _searchQuery
        .debounce(300)
        .flatMapLatest { query ->
            if (query.isBlank()) {
                flowOf(emptyList())
            } else {
                _isSearching.value = true
                pdfFileRepository.searchPdfs(query)
            }
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    init {
        viewModelScope.launch {
            searchResults.collect {
                _isSearching.value = false
            }
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun clearSearch() {
        _searchQuery.value = ""
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
                // Re-trigger search to refresh results
                val currentQuery = _searchQuery.value
                _searchQuery.value = ""
                _searchQuery.value = currentQuery
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
                // Re-trigger search to refresh results
                val currentQuery = _searchQuery.value
                _searchQuery.value = ""
                _searchQuery.value = currentQuery
                onComplete(true)
            } else {
                onComplete(false)
            }
        }
    }
}
