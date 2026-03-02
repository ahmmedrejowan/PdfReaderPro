package com.rejowan.pdfreaderpro.presentation.screens.tools.merge

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.Environment
import android.os.ParcelFileDescriptor
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rejowan.pdfreaderpro.domain.repository.PdfToolsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Represents page selection mode for a PDF file.
 */
sealed class PageSelection {
    data object All : PageSelection()
    data class Range(val start: Int, val end: Int) : PageSelection()
    data class Custom(val pages: List<Int>) : PageSelection()

    fun toDisplayString(totalPages: Int): String = when (this) {
        is All -> "All pages (1-$totalPages)"
        is Range -> "Pages $start-$end"
        is Custom -> if (pages.size <= 5) {
            "Pages ${pages.joinToString(", ")}"
        } else {
            "Pages ${pages.take(4).joinToString(", ")}... (${pages.size} pages)"
        }
    }

    fun toPageList(totalPages: Int): List<Int>? = when (this) {
        is All -> null // null means all pages
        is Range -> (start..end.coerceAtMost(totalPages)).toList()
        is Custom -> pages.filter { it in 1..totalPages }
    }

    fun getSelectedCount(totalPages: Int): Int = when (this) {
        is All -> totalPages
        is Range -> (end.coerceAtMost(totalPages) - start + 1).coerceAtLeast(0)
        is Custom -> pages.count { it in 1..totalPages }
    }
}

data class MergeFile(
    val uri: Uri,
    val path: String,
    val name: String,
    val size: Long,
    val pageCount: Int = 0,
    val thumbnail: Bitmap? = null,
    val pageSelection: PageSelection = PageSelection.All
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
                        val thumbnail = generateThumbnail(path)
                        MergeFile(
                            uri = uri,
                            path = path,
                            name = file.name,
                            size = file.length(),
                            pageCount = pageCount,
                            thumbnail = thumbnail
                        )
                    } else {
                        // Try to copy from content URI
                        val tempPath = copyUriToCache(uri)
                        if (tempPath != null) {
                            val file = File(tempPath)
                            val pageCount = pdfToolsRepository.getPageCount(tempPath).getOrDefault(0)
                            val thumbnail = generateThumbnail(tempPath)
                            MergeFile(
                                uri = uri,
                                path = tempPath,
                                name = getFileNameFromUri(uri) ?: file.name,
                                size = file.length(),
                                pageCount = pageCount,
                                thumbnail = thumbnail
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

    private suspend fun generateThumbnail(pdfPath: String): Bitmap? = withContext(Dispatchers.IO) {
        try {
            val file = File(pdfPath)
            val fd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
            val renderer = PdfRenderer(fd)

            if (renderer.pageCount > 0) {
                val page = renderer.openPage(0)

                // Create a compact thumbnail (48dp equivalent at ~2x density = 96px)
                val thumbnailSize = 96
                val aspectRatio = page.width.toFloat() / page.height.toFloat()
                val width: Int
                val height: Int
                if (aspectRatio > 1) {
                    width = thumbnailSize
                    height = (thumbnailSize / aspectRatio).toInt()
                } else {
                    height = thumbnailSize
                    width = (thumbnailSize * aspectRatio).toInt()
                }

                val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                page.close()
                renderer.close()
                fd.close()
                bitmap
            } else {
                renderer.close()
                fd.close()
                null
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to generate thumbnail for: $pdfPath")
            null
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

    fun updatePageSelection(file: MergeFile, selection: PageSelection) {
        _state.update { current ->
            current.copy(
                selectedFiles = current.selectedFiles.map {
                    if (it.path == file.path) it.copy(pageSelection = selection) else it
                }
            )
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

        // Check for empty page selections
        val emptySelectionFiles = currentState.selectedFiles.filter { file ->
            file.pageSelection.getSelectedCount(file.pageCount) == 0
        }
        if (emptySelectionFiles.isNotEmpty()) {
            val fileNames = emptySelectionFiles.joinToString(", ") { it.name }
            _state.update { it.copy(error = "No pages selected for: $fileNames") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isProcessing = true, progress = 0f, error = null) }

            val outputDir = getOutputDirectory()
            var outputPath = "$outputDir/${currentState.outputFileName}.pdf"

            // Check if file exists and generate unique name
            var counter = 1
            while (File(outputPath).exists()) {
                outputPath = "$outputDir/${currentState.outputFileName}_$counter.pdf"
                counter++
            }

            // Create page selections from merge files
            val selections = currentState.selectedFiles.map { file ->
                PdfToolsRepository.PdfPageSelection(
                    path = file.path,
                    pages = file.pageSelection.toPageList(file.pageCount)
                )
            }

            val result = pdfToolsRepository.mergePdfsWithSelection(
                selections = selections,
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
