package com.rejowan.pdfreaderpro.presentation.screens.tools.reorder

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

data class PageItem(
    val originalIndex: Int,  // Original page index (0-based)
    val pageNumber: Int,     // Display number (1-based)
    val thumbnail: Bitmap?
)

data class SourceFile(
    val uri: Uri,
    val path: String,
    val name: String,
    val size: Long,
    val pageCount: Int = 0
)

data class ReorderState(
    val sourceFile: SourceFile? = null,
    val pages: List<PageItem> = emptyList(),
    val outputFileName: String = "",
    val isLoading: Boolean = false,
    val isProcessing: Boolean = false,
    val progress: Float = 0f,
    val error: String? = null,
    val result: ReorderResult? = null,
    val hasChanges: Boolean = false
)

data class ReorderResult(
    val outputPath: String,
    val pageCount: Int,
    val fileSize: Long
)

class ReorderViewModel(
    private val pdfToolsRepository: PdfToolsRepository,
    private val context: Context
) : ViewModel() {

    private val _state = MutableStateFlow(ReorderState())
    val state: StateFlow<ReorderState> = _state.asStateFlow()

    private var originalOrder: List<Int> = emptyList()

    fun setSourceFile(uri: Uri) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            try {
                val path = copyUriToCache(uri)
                if (path != null) {
                    val file = File(path)
                    val pageCount = pdfToolsRepository.getPageCount(path).getOrDefault(0)
                    val pages = generatePageThumbnails(path, pageCount)

                    originalOrder = pages.map { it.originalIndex }

                    _state.update {
                        it.copy(
                            sourceFile = SourceFile(
                                uri = uri,
                                path = path,
                                name = getFileNameFromUri(uri) ?: file.name,
                                size = file.length(),
                                pageCount = pageCount
                            ),
                            pages = pages,
                            isLoading = false,
                            error = null,
                            result = null,
                            hasChanges = false
                        )
                    }

                    // Generate default output name
                    val baseName = file.nameWithoutExtension
                    _state.update { it.copy(outputFileName = "${baseName}_reordered") }
                } else {
                    _state.update { it.copy(isLoading = false, error = "Failed to load PDF file") }
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to set source file")
                _state.update { it.copy(isLoading = false, error = "Failed to load PDF: ${e.message}") }
            }
        }
    }

    private suspend fun generatePageThumbnails(
        pdfPath: String,
        pageCount: Int
    ): List<PageItem> = withContext(Dispatchers.IO) {
        val pages = mutableListOf<PageItem>()
        try {
            val file = File(pdfPath)
            val fd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
            val renderer = PdfRenderer(fd)

            for (i in 0 until minOf(pageCount, 100)) { // Limit to 100 pages
                val page = renderer.openPage(i)

                // Create higher quality thumbnail
                val thumbnailSize = 300
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

                pages.add(
                    PageItem(
                        originalIndex = i,
                        pageNumber = i + 1,
                        thumbnail = bitmap
                    )
                )
            }

            renderer.close()
            fd.close()
        } catch (e: Exception) {
            Timber.e(e, "Failed to generate thumbnails")
        }
        pages
    }

    fun movePage(fromIndex: Int, toIndex: Int) {
        _state.update { current ->
            val pages = current.pages.toMutableList()
            val item = pages.removeAt(fromIndex)
            pages.add(toIndex, item)

            // Check if order has changed from original
            val currentOrder = pages.map { it.originalIndex }
            val hasChanges = currentOrder != originalOrder

            current.copy(pages = pages, hasChanges = hasChanges)
        }
    }

    fun setOutputFileName(name: String) {
        _state.update { it.copy(outputFileName = name) }
    }

    fun reorder() {
        val currentState = _state.value
        val sourceFile = currentState.sourceFile

        if (sourceFile == null) {
            _state.update { it.copy(error = "Please select a PDF file first") }
            return
        }

        if (currentState.outputFileName.isBlank()) {
            _state.update { it.copy(error = "Please enter an output file name") }
            return
        }

        if (!currentState.hasChanges) {
            _state.update { it.copy(error = "No changes made to page order") }
            return
        }

        // Get the new page order (1-based for the repository)
        val newOrder = currentState.pages.map { it.originalIndex + 1 }

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

            val result = pdfToolsRepository.reorderPages(
                inputPath = sourceFile.path,
                outputPath = outputPath,
                newOrder = newOrder,
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
                            result = ReorderResult(
                                outputPath = outputPath,
                                pageCount = pageCount,
                                fileSize = outputFile.length()
                            )
                        )
                    }
                },
                onFailure = { error ->
                    Timber.e(error, "Reorder failed")
                    _state.update {
                        it.copy(
                            isProcessing = false,
                            error = error.message ?: "Reorder failed"
                        )
                    }
                }
            )
        }
    }

    fun resetOrder() {
        _state.update { current ->
            val sortedPages = current.pages.sortedBy { it.originalIndex }
            current.copy(pages = sortedPages, hasChanges = false)
        }
    }

    fun clearResult() {
        _state.update { it.copy(result = null) }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }

    fun reset() {
        _state.update { ReorderState() }
    }

    private fun getOutputDirectory(): String {
        val documentsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
        val pdfToolsDir = File(documentsDir, "PdfReaderPro")
        if (!pdfToolsDir.exists()) {
            pdfToolsDir.mkdirs()
        }
        return pdfToolsDir.absolutePath
    }

    private fun copyUriToCache(uri: Uri): String? {
        return try {
            if (uri.scheme == "file") {
                return uri.path
            }

            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val fileName = getFileNameFromUri(uri) ?: "temp_${System.currentTimeMillis()}.pdf"
            val cacheFile = File(context.cacheDir, "reorder_temp/$fileName")
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
