package com.rejowan.pdfreaderpro.presentation.screens.tools.pdftoimage

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

enum class ImageFormat(val extension: String, val label: String) {
    PNG("png", "PNG"),
    JPG("jpg", "JPG")
}

enum class PageSelection {
    ALL,
    CUSTOM
}

data class SourceFile(
    val uri: Uri,
    val path: String,
    val name: String,
    val size: Long,
    val pageCount: Int = 0,
    val previewBitmap: Bitmap? = null
)

data class PdfToImageState(
    val sourceFile: SourceFile? = null,
    val imageFormat: ImageFormat = ImageFormat.PNG,
    val pageSelection: PageSelection = PageSelection.ALL,
    val customPages: String = "",
    val isLoading: Boolean = false,
    val isProcessing: Boolean = false,
    val progress: Float = 0f,
    val error: String? = null,
    val result: PdfToImageResult? = null
)

data class PdfToImageResult(
    val outputDir: String,
    val imagePaths: List<String>,
    val imageCount: Int,
    val format: String
)

class PdfToImageViewModel(
    private val pdfToolsRepository: PdfToolsRepository,
    private val context: Context
) : ViewModel() {

    private val _state = MutableStateFlow(PdfToImageState())
    val state: StateFlow<PdfToImageState> = _state.asStateFlow()

    fun setSourceFile(uri: Uri) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            try {
                val path = copyUriToCache(uri)
                if (path != null) {
                    val file = File(path)
                    val pageCount = pdfToolsRepository.getPageCount(path).getOrDefault(0)
                    val preview = generatePreview(path)

                    _state.update {
                        it.copy(
                            sourceFile = SourceFile(
                                uri = uri,
                                path = path,
                                name = getFileNameFromUri(uri) ?: file.name,
                                size = file.length(),
                                pageCount = pageCount,
                                previewBitmap = preview
                            ),
                            isLoading = false,
                            error = null,
                            result = null
                        )
                    }
                } else {
                    _state.update { it.copy(isLoading = false, error = "Failed to load PDF file") }
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to set source file")
                _state.update { it.copy(isLoading = false, error = "Failed to load PDF: ${e.message}") }
            }
        }
    }

    private suspend fun generatePreview(pdfPath: String): Bitmap? = withContext(Dispatchers.IO) {
        try {
            val file = File(pdfPath)
            val fd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
            val renderer = PdfRenderer(fd)

            val page = renderer.openPage(0)

            val scale = 2
            val bitmap = Bitmap.createBitmap(
                page.width * scale,
                page.height * scale,
                Bitmap.Config.ARGB_8888
            )
            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
            page.close()

            renderer.close()
            fd.close()

            bitmap
        } catch (e: Exception) {
            Timber.e(e, "Failed to generate preview")
            null
        }
    }

    fun setImageFormat(format: ImageFormat) {
        _state.update { it.copy(imageFormat = format) }
    }

    fun setPageSelection(selection: PageSelection) {
        _state.update { it.copy(pageSelection = selection) }
    }

    fun setCustomPages(pages: String) {
        _state.update { it.copy(customPages = pages) }
    }

    fun exportImages() {
        val currentState = _state.value
        val sourceFile = currentState.sourceFile

        if (sourceFile == null) {
            _state.update { it.copy(error = "Please select a PDF file first") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isProcessing = true, progress = 0f, error = null) }

            // Create output directory
            val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
            val timestamp = dateFormat.format(Date())
            val baseName = File(sourceFile.path).nameWithoutExtension
            val outputDirName = "${baseName}_images_$timestamp"
            val outputDir = File(getOutputDirectory(), outputDirName)

            if (!outputDir.exists()) {
                outputDir.mkdirs()
            }

            // Calculate pages to export
            val pages = calculatePages(
                currentState.pageSelection,
                currentState.customPages,
                sourceFile.pageCount
            )

            val result = pdfToolsRepository.pdfToImages(
                inputPath = sourceFile.path,
                outputDir = outputDir.absolutePath,
                format = currentState.imageFormat.extension,
                pages = pages,
                onProgress = { progress ->
                    _state.update { it.copy(progress = progress) }
                }
            )

            result.fold(
                onSuccess = { imagePaths ->
                    _state.update {
                        it.copy(
                            isProcessing = false,
                            progress = 1f,
                            result = PdfToImageResult(
                                outputDir = outputDir.absolutePath,
                                imagePaths = imagePaths,
                                imageCount = imagePaths.size,
                                format = currentState.imageFormat.extension.uppercase()
                            )
                        )
                    }
                },
                onFailure = { error ->
                    Timber.e(error, "PDF to images export failed")
                    _state.update {
                        it.copy(
                            isProcessing = false,
                            error = error.message ?: "Failed to export images"
                        )
                    }
                }
            )
        }
    }

    private fun calculatePages(
        selection: PageSelection,
        customPages: String,
        totalPages: Int
    ): List<Int>? {
        return when (selection) {
            PageSelection.ALL -> null
            PageSelection.CUSTOM -> parseCustomPages(customPages, totalPages)
        }
    }

    private fun parseCustomPages(input: String, totalPages: Int): List<Int> {
        val pages = mutableSetOf<Int>()
        val parts = input.split(",")

        for (part in parts) {
            val trimmed = part.trim()
            if (trimmed.contains("-")) {
                val range = trimmed.split("-")
                if (range.size == 2) {
                    val start = range[0].trim().toIntOrNull() ?: continue
                    val end = range[1].trim().toIntOrNull() ?: continue
                    for (i in start..end) {
                        if (i in 1..totalPages) pages.add(i)
                    }
                }
            } else {
                val page = trimmed.toIntOrNull()
                if (page != null && page in 1..totalPages) {
                    pages.add(page)
                }
            }
        }

        return pages.sorted()
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }

    fun reset() {
        _state.update { PdfToImageState() }
    }

    private fun getOutputDirectory(): String {
        val picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        val pdfToolsDir = File(picturesDir, "PdfReaderPro")
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
            val cacheFile = File(context.cacheDir, "pdftoimage_temp/$fileName")
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
