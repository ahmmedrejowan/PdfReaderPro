package com.rejowan.pdfreaderpro.vms

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rejowan.pdfreaderpro.dataClasses.PdfFile
import com.rejowan.pdfreaderpro.interfaces.PdfFileRepository
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

    fun deletePdfFile(pdfFile: PdfFile) {
        viewModelScope.launch {
            val isDeleted = withContext(Dispatchers.IO) {
                repo.deletePdfFile(pdfFile)
            }

            withContext(Dispatchers.Main) {
                if (isDeleted) {
                    // Remove the file from the list
                    val updatedList = _pdfFiles.value?.filter { it.path != pdfFile.path }?.toList()
                    _pdfFiles.value = updatedList ?: emptyList()
                }
            }
        }
    }

    fun renamePdfFile(pdfFile: PdfFile, newName: String) {
        viewModelScope.launch {
            val updatedPdfFile = withContext(Dispatchers.IO) {
                repo.renamePdfFile(pdfFile, newName)
            }

            updatedPdfFile?.let {
                val updatedList =
                    _pdfFiles.value?.map { if (it.path == pdfFile.path) updatedPdfFile else it }
                _pdfFiles.value = updatedList ?: emptyList()
            }
        }
    }


}