package com.androvine.pdfreaderpro.vms

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.androvine.pdfreaderpro.dataClasses.PdfFile
import com.androvine.pdfreaderpro.repoModels.PdfFileRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PdfListViewModel(private val repo: PdfFileRepository) : ViewModel() {

    private val _pdfFiles = MutableLiveData<List<PdfFile>>()
    val pdfFiles: LiveData<List<PdfFile>> get() = _pdfFiles

    init {
        loadPdfFiles()
    }

    private fun loadPdfFiles() {
        viewModelScope.launch {
            val files = withContext(Dispatchers.IO) {
                repo.getAllPdfFiles()
            }
            _pdfFiles.value = files
        }
    }
}