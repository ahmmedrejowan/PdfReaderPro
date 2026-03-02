package com.rejowan.pdfreaderpro.presentation.screens.tools.removepages

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

data class PageInfo(
    val pageNumber: Int,
    val thumbnail: Bitmap?,
    val isSelected: Boolean = false  // Selected means marked for removal
)

data class SourceFile(
    val uri: Uri,
    val path: String,
    val name: String,
    val size: Long,
    val pageCount: Int = 0,
    val pages: List<PageInfo> = emptyList()
)

data class RemovePagesState(
    val sourceFile: SourceFile? = null,
    val outputFileName: String = "",
    val isLoading: Boolean = false,
    val isProcessing: Boolean = false,
    val progress: Float = 0f,
    val error: String? = null,
    val result: RemovePagesResult? = null
)

data class RemovePagesResult(
    val outputPath: String,
    val originalPageCount: Int,
    val newPageCount: Int,
    val removedPages: Int,
    val fileSize: Long
)

class RemovePagesViewModel(
    private val pdfToolsRepository: PdfToolsRepository,
    private val context: Context
) : ViewModel() {

    private val _state = MutableStateFlow(RemovePagesState())
    val state: StateFlow<RemovePagesState> = _state.asStateFlow()

    fun setSourceFile(uri: Uri) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            try {
                val path = copyUriToCache(uri)
                if (path != null) {
                    val file = File(path)
                    val pageCount = pdfToolsRepository.getPageCount(path).getOrDefault(0)
                    val pages = generatePageThumbnails(path, pageCount)

                    _state.update {
                        it.copy(
                            sourceFile = SourceFile(
                                uri = uri,
                                path = path,
                                name = getFileNameFromUri(uri) ?: file.name,
                                size = file.length(),
                                pageCount = pageCount,
                                pages = pages
                            ),
                            isLoading = false,
                            error = null,
                            result = null
                        )
                    }

                    // Generate default output name
                    val baseName = file.nameWithoutExtension
                    _state.update { it.copy(outputFileName = "${baseName}_modified") }
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
    ): List<PageInfo> = withContext(Dispatchers.IO) {
        val pages = mutableListOf<PageInfo>()
        try {
            val file = File(pdfPath)
            val fd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
            val renderer = PdfRenderer(fd)

            for (i in 0 until minOf(pageCount, 100)) { // Limit to 100 pages
                val page = renderer.openPage(i)

                // Create higher quality thumbnail for readable content
                val thumbnailSize = 500
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
                    PageInfo(
                        pageNumber = i + 1,
                        thumbnail = bitmap,
                        isSelected = false
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

    fun togglePageSelection(pageNumber: Int) {
        _state.update { current ->
            val updatedPages = current.sourceFile?.pages?.map { page ->
                if (page.pageNumber == pageNumber) {
                    page.copy(isSelected = !page.isSelected)
                } else page
            } ?: emptyList()

            current.copy(
                sourceFile = current.sourceFile?.copy(pages = updatedPages)
            )
        }
    }

    fun selectAllPages() {
        _state.update { current ->
            val updatedPages = current.sourceFile?.pages?.map { page ->
                page.copy(isSelected = true)
            } ?: emptyList()

            current.copy(
                sourceFile = current.sourceFile?.copy(pages = updatedPages)
            )
        }
    }

    fun deselectAllPages() {
        _state.update { current ->
            val updatedPages = current.sourceFile?.pages?.map { page ->
                page.copy(isSelected = false)
            } ?: emptyList()

            current.copy(
                sourceFile = current.sourceFile?.copy(pages = updatedPages)
            )
        }
    }

    fun selectOddPages() {
        _state.update { current ->
            val updatedPages = current.sourceFile?.pages?.map { page ->
                page.copy(isSelected = page.pageNumber % 2 == 1)
            } ?: emptyList()

            current.copy(
                sourceFile = current.sourceFile?.copy(pages = updatedPages)
            )
        }
    }

    fun selectEvenPages() {
        _state.update { current ->
            val updatedPages = current.sourceFile?.pages?.map { page ->
                page.copy(isSelected = page.pageNumber % 2 == 0)
            } ?: emptyList()

            current.copy(
                sourceFile = current.sourceFile?.copy(pages = updatedPages)
            )
        }
    }

    fun selectRange(start: Int, end: Int) {
        _state.update { current ->
            val updatedPages = current.sourceFile?.pages?.map { page ->
                page.copy(isSelected = page.pageNumber in start..end)
            } ?: emptyList()

            current.copy(
                sourceFile = current.sourceFile?.copy(pages = updatedPages)
            )
        }
    }

    fun selectBeforePage(page: Int) {
        _state.update { current ->
            val updatedPages = current.sourceFile?.pages?.map { p ->
                p.copy(isSelected = p.pageNumber < page)
            } ?: emptyList()

            current.copy(
                sourceFile = current.sourceFile?.copy(pages = updatedPages)
            )
        }
    }

    fun selectAfterPage(page: Int) {
        _state.update { current ->
            val updatedPages = current.sourceFile?.pages?.map { p ->
                p.copy(isSelected = p.pageNumber > page)
            } ?: emptyList()

            current.copy(
                sourceFile = current.sourceFile?.copy(pages = updatedPages)
            )
        }
    }

    fun selectFirstN(n: Int) {
        _state.update { current ->
            val updatedPages = current.sourceFile?.pages?.map { page ->
                page.copy(isSelected = page.pageNumber <= n)
            } ?: emptyList()

            current.copy(
                sourceFile = current.sourceFile?.copy(pages = updatedPages)
            )
        }
    }

    fun selectLastN(n: Int) {
        _state.update { current ->
            val totalPages = current.sourceFile?.pageCount ?: 0
            val updatedPages = current.sourceFile?.pages?.map { page ->
                page.copy(isSelected = page.pageNumber > (totalPages - n))
            } ?: emptyList()

            current.copy(
                sourceFile = current.sourceFile?.copy(pages = updatedPages)
            )
        }
    }

    fun setOutputFileName(name: String) {
        _state.update { it.copy(outputFileName = name) }
    }

    fun removePages() {
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

        val pagesToRemove = sourceFile.pages.filter { it.isSelected }.map { it.pageNumber }

        if (pagesToRemove.isEmpty()) {
            _state.update { it.copy(error = "Please select at least one page to remove") }
            return
        }

        if (pagesToRemove.size >= sourceFile.pageCount) {
            _state.update { it.copy(error = "Cannot remove all pages from PDF") }
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

            val result = pdfToolsRepository.removePages(
                inputPath = sourceFile.path,
                outputPath = outputPath,
                pagesToRemove = pagesToRemove,
                onProgress = { progress ->
                    _state.update { it.copy(progress = progress) }
                }
            )

            result.fold(
                onSuccess = {
                    val outputFile = File(outputPath)
                    val newPageCount = pdfToolsRepository.getPageCount(outputPath).getOrDefault(0)
                    _state.update {
                        it.copy(
                            isProcessing = false,
                            progress = 1f,
                            result = RemovePagesResult(
                                outputPath = outputPath,
                                originalPageCount = sourceFile.pageCount,
                                newPageCount = newPageCount,
                                removedPages = pagesToRemove.size,
                                fileSize = outputFile.length()
                            )
                        )
                    }
                },
                onFailure = { error ->
                    Timber.e(error, "Remove pages failed")
                    _state.update {
                        it.copy(
                            isProcessing = false,
                            error = error.message ?: "Failed to remove pages"
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
        _state.update { RemovePagesState() }
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
            val cacheFile = File(context.cacheDir, "removepages_temp/$fileName")
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
