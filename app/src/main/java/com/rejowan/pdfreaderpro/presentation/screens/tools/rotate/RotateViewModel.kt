package com.rejowan.pdfreaderpro.presentation.screens.tools.rotate

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
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
 * Rotation angle options.
 */
enum class RotationAngle(val degrees: Int, val label: String) {
    ROTATE_90(90, "90° Right"),
    ROTATE_180(180, "180°"),
    ROTATE_270(270, "90° Left")
}

/**
 * Page selection mode for rotation.
 */
enum class PageSelectionMode {
    ALL_PAGES,
    SELECTED_PAGES
}

/**
 * Quick selection options for pages.
 */
enum class QuickSelection(val label: String) {
    ALL("All"),
    ODD("Odd"),
    EVEN("Even"),
    FIRST_HALF("First Half"),
    SECOND_HALF("Second Half"),
    EVERY_2ND("Every 2nd"),
    EVERY_3RD("Every 3rd")
}

data class PageInfo(
    val pageNumber: Int,
    val thumbnail: Bitmap?,
    val currentRotation: Int = 0,
    val isSelected: Boolean = false
)

data class SourceFile(
    val uri: Uri,
    val path: String,
    val name: String,
    val size: Long,
    val pageCount: Int = 0,
    val pages: List<PageInfo> = emptyList()
)

data class RotateState(
    val sourceFile: SourceFile? = null,
    val rotationAngle: RotationAngle = RotationAngle.ROTATE_90,
    val selectionMode: PageSelectionMode = PageSelectionMode.ALL_PAGES,
    val outputFileName: String = "",
    val isLoading: Boolean = false,
    val isProcessing: Boolean = false,
    val progress: Float = 0f,
    val error: String? = null,
    val result: RotateResult? = null
)

data class RotateResult(
    val outputPath: String,
    val pageCount: Int,
    val fileSize: Long,
    val rotatedPages: Int
)

class RotateViewModel(
    private val pdfToolsRepository: PdfToolsRepository,
    private val context: Context
) : ViewModel() {

    private val _state = MutableStateFlow(RotateState())
    val state: StateFlow<RotateState> = _state.asStateFlow()

    fun setSourceFile(uri: Uri) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            try {
                val path = copyUriToCache(uri)
                if (path != null) {
                    val file = File(path)
                    val pageCount = pdfToolsRepository.getPageCount(path).getOrDefault(0)
                    val pages = generatePageThumbnails(path, pageCount)

                    // Select all pages by default
                    val selectedPages = pages.map { it.copy(isSelected = true) }

                    _state.update {
                        it.copy(
                            sourceFile = SourceFile(
                                uri = uri,
                                path = path,
                                name = getFileNameFromUri(uri) ?: file.name,
                                size = file.length(),
                                pageCount = pageCount,
                                pages = selectedPages
                            ),
                            selectionMode = PageSelectionMode.ALL_PAGES,
                            isLoading = false,
                            error = null,
                            result = null
                        )
                    }

                    // Generate default output name
                    val baseName = file.nameWithoutExtension
                    _state.update { it.copy(outputFileName = "${baseName}_rotated") }
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

            for (i in 0 until minOf(pageCount, 50)) { // Limit to 50 pages for performance
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
                    PageInfo(
                        pageNumber = i + 1,
                        thumbnail = bitmap,
                        currentRotation = 0,
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

    fun setRotationAngle(angle: RotationAngle) {
        _state.update { it.copy(rotationAngle = angle) }
    }

    fun setSelectionMode(mode: PageSelectionMode) {
        _state.update { current ->
            val updatedPages = current.sourceFile?.pages?.map { page ->
                page.copy(isSelected = mode == PageSelectionMode.ALL_PAGES)
            } ?: emptyList()

            current.copy(
                selectionMode = mode,
                sourceFile = current.sourceFile?.copy(pages = updatedPages)
            )
        }
    }

    fun togglePageSelection(pageNumber: Int) {
        _state.update { current ->
            val updatedPages = current.sourceFile?.pages?.map { page ->
                if (page.pageNumber == pageNumber) {
                    page.copy(isSelected = !page.isSelected)
                } else page
            } ?: emptyList()

            current.copy(
                sourceFile = current.sourceFile?.copy(pages = updatedPages),
                selectionMode = PageSelectionMode.SELECTED_PAGES
            )
        }
    }

    fun selectAllPages() {
        _state.update { current ->
            val updatedPages = current.sourceFile?.pages?.map { page ->
                page.copy(isSelected = true)
            } ?: emptyList()

            current.copy(
                sourceFile = current.sourceFile?.copy(pages = updatedPages),
                selectionMode = PageSelectionMode.ALL_PAGES
            )
        }
    }

    fun deselectAllPages() {
        _state.update { current ->
            val updatedPages = current.sourceFile?.pages?.map { page ->
                page.copy(isSelected = false)
            } ?: emptyList()

            current.copy(
                sourceFile = current.sourceFile?.copy(pages = updatedPages),
                selectionMode = PageSelectionMode.SELECTED_PAGES
            )
        }
    }

    fun applyQuickSelection(selection: QuickSelection) {
        _state.update { current ->
            val totalPages = current.sourceFile?.pageCount ?: 0
            val updatedPages = current.sourceFile?.pages?.map { page ->
                val isSelected = when (selection) {
                    QuickSelection.ALL -> true
                    QuickSelection.ODD -> page.pageNumber % 2 == 1
                    QuickSelection.EVEN -> page.pageNumber % 2 == 0
                    QuickSelection.FIRST_HALF -> page.pageNumber <= (totalPages + 1) / 2
                    QuickSelection.SECOND_HALF -> page.pageNumber > totalPages / 2
                    QuickSelection.EVERY_2ND -> page.pageNumber % 2 == 0
                    QuickSelection.EVERY_3RD -> page.pageNumber % 3 == 0
                }
                page.copy(isSelected = isSelected)
            } ?: emptyList()

            current.copy(
                sourceFile = current.sourceFile?.copy(pages = updatedPages),
                selectionMode = if (selection == QuickSelection.ALL) {
                    PageSelectionMode.ALL_PAGES
                } else {
                    PageSelectionMode.SELECTED_PAGES
                }
            )
        }
    }

    fun selectPageRange(start: Int, end: Int) {
        _state.update { current ->
            val updatedPages = current.sourceFile?.pages?.map { page ->
                page.copy(isSelected = page.pageNumber in start..end)
            } ?: emptyList()

            current.copy(
                sourceFile = current.sourceFile?.copy(pages = updatedPages),
                selectionMode = PageSelectionMode.SELECTED_PAGES
            )
        }
    }

    fun setOutputFileName(name: String) {
        _state.update { it.copy(outputFileName = name) }
    }

    fun rotate() {
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

        // Get pages to rotate
        val pagesToRotate = when (currentState.selectionMode) {
            PageSelectionMode.ALL_PAGES -> null // null means all pages
            PageSelectionMode.SELECTED_PAGES -> {
                val selected = sourceFile.pages.filter { it.isSelected }.map { it.pageNumber }
                if (selected.isEmpty()) {
                    _state.update { it.copy(error = "Please select at least one page to rotate") }
                    return
                }
                selected
            }
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

            val result = pdfToolsRepository.rotatePages(
                inputPath = sourceFile.path,
                outputPath = outputPath,
                rotation = currentState.rotationAngle.degrees,
                pages = pagesToRotate,
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
                            result = RotateResult(
                                outputPath = outputPath,
                                pageCount = pageCount,
                                fileSize = outputFile.length(),
                                rotatedPages = pagesToRotate?.size ?: sourceFile.pageCount
                            )
                        )
                    }
                },
                onFailure = { error ->
                    Timber.e(error, "Rotation failed")
                    _state.update {
                        it.copy(
                            isProcessing = false,
                            error = error.message ?: "Rotation failed"
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
        _state.update { RotateState() }
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
            val cacheFile = File(context.cacheDir, "rotate_temp/$fileName")
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
