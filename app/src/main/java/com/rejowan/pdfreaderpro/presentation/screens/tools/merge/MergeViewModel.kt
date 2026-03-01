package com.rejowan.pdfreaderpro.presentation.screens.tools.merge

import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rejowan.pdfreaderpro.domain.repository.PdfToolsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class MergeFile(
    val uri: Uri,
    val path: String,
    val name: String,
    val size: Long,
    val pageCount: Int = 0
)

data class MergeState(
    val selectedFiles: List<MergeFile> = emptyList(),
    val outputFileName: String = "",
    val isProcessing: Boolean = false,
    val progress: Float = 0f,
    val error: String? = null,
    val result: MergeResult? = null
)

data class MergeResult(
    val outputPath: String,
    val pageCount: Int,
    val fileSize: Long
)

class MergeViewModel(
    private val pdfToolsRepository: PdfToolsRepository,
    private val context: Context
) : ViewModel() {

    private val _state = MutableStateFlow(MergeState())
    val state: StateFlow<MergeState> = _state.asStateFlow()

    init {
        generateDefaultFileName()
    }

    private fun generateDefaultFileName() {
        val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
        val timestamp = dateFormat.format(Date())
        _state.update { it.copy(outputFileName = "merged_$timestamp") }
    }

    fun addFiles(uris: List<Uri>) {
        viewModelScope.launch {
            val newFiles = uris.mapNotNull { uri ->
                try {
                    val path = getPathFromUri(uri)
                    if (path != null && File(path).exists()) {
                        val file = File(path)
                        val pageCount = pdfToolsRepository.getPageCount(path).getOrDefault(0)
                        MergeFile(
                            uri = uri,
                            path = path,
                            name = file.name,
                            size = file.length(),
                            pageCount = pageCount
                        )
                    } else {
                        // Try to copy from content URI
                        val tempPath = copyUriToCache(uri)
                        if (tempPath != null) {
                            val file = File(tempPath)
                            val pageCount = pdfToolsRepository.getPageCount(tempPath).getOrDefault(0)
                            MergeFile(
                                uri = uri,
                                path = tempPath,
                                name = getFileNameFromUri(uri) ?: file.name,
                                size = file.length(),
                                pageCount = pageCount
                            )
                        } else null
                    }
                } catch (e: Exception) {
                    Timber.e(e, "Failed to add file: $uri")
                    null
                }
            }

            _state.update { current ->
                val existingPaths = current.selectedFiles.map { it.path }.toSet()
                val filteredNew = newFiles.filter { it.path !in existingPaths }
                current.copy(
                    selectedFiles = current.selectedFiles + filteredNew,
                    error = null
                )
            }
        }
    }

    fun removeFile(file: MergeFile) {
        _state.update { current ->
            current.copy(selectedFiles = current.selectedFiles.filter { it.path != file.path })
        }
    }

    fun moveFile(fromIndex: Int, toIndex: Int) {
        _state.update { current ->
            val files = current.selectedFiles.toMutableList()
            if (fromIndex in files.indices && toIndex in files.indices) {
                val item = files.removeAt(fromIndex)
                files.add(toIndex, item)
            }
            current.copy(selectedFiles = files)
        }
    }

    fun setOutputFileName(name: String) {
        _state.update { it.copy(outputFileName = name) }
    }

    fun merge() {
        val currentState = _state.value
        if (currentState.selectedFiles.size < 2) {
            _state.update { it.copy(error = "Select at least 2 PDF files") }
            return
        }

        if (currentState.outputFileName.isBlank()) {
            _state.update { it.copy(error = "Enter an output file name") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isProcessing = true, progress = 0f, error = null) }

            val outputDir = getOutputDirectory()
            val outputPath = "$outputDir/${currentState.outputFileName}.pdf"

            val result = pdfToolsRepository.mergePdfs(
                inputPaths = currentState.selectedFiles.map { it.path },
                outputPath = outputPath,
                onProgress = { progress ->
                    _state.update { it.copy(progress = progress) }
                }
            )

            result.fold(
                onSuccess = {
                    val outputFile = File(outputPath)
                    val pageCount = pdfToolsRepository.getPageCount(outputPath).getOrDefault(0)
                    _state.update {
                        it.copy(
                            isProcessing = false,
                            progress = 1f,
                            result = MergeResult(
                                outputPath = outputPath,
                                pageCount = pageCount,
                                fileSize = outputFile.length()
                            )
                        )
                    }
                },
                onFailure = { error ->
                    Timber.e(error, "Merge failed")
                    _state.update {
                        it.copy(
                            isProcessing = false,
                            error = error.message ?: "Merge failed"
                        )
                    }
                }
            )
        }
    }

    fun clearResult() {
        _state.update { it.copy(result = null) }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }

    fun reset() {
        _state.update {
            MergeState()
        }
        generateDefaultFileName()
    }

    private fun getOutputDirectory(): String {
        val documentsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
        val pdfToolsDir = File(documentsDir, "PdfReaderPro")
        if (!pdfToolsDir.exists()) {
            pdfToolsDir.mkdirs()
        }
        return pdfToolsDir.absolutePath
    }

    private fun getPathFromUri(uri: Uri): String? {
        // Handle file:// URIs
        if (uri.scheme == "file") {
            return uri.path
        }

        // Try to get path from content:// URI
        return try {
            context.contentResolver.openFileDescriptor(uri, "r")?.use { _ ->
                // If we can open it, copy to cache
                null // We'll handle via copyUriToCache
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun copyUriToCache(uri: Uri): String? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val fileName = getFileNameFromUri(uri) ?: "temp_${System.currentTimeMillis()}.pdf"
            val cacheFile = File(context.cacheDir, "merge_temp/$fileName")
            cacheFile.parentFile?.mkdirs()

            cacheFile.outputStream().use { output ->
                inputStream.copyTo(output)
            }

            cacheFile.absolutePath
        } catch (e: Exception) {
            Timber.e(e, "Failed to copy URI to cache")
            null
        }
    }

    private fun getFileNameFromUri(uri: Uri): String? {
        return try {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                if (cursor.moveToFirst() && nameIndex >= 0) {
                    cursor.getString(nameIndex)
                } else null
            }
        } catch (e: Exception) {
            null
        }
    }
}
