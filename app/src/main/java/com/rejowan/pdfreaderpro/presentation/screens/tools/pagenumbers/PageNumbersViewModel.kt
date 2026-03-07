package com.rejowan.pdfreaderpro.presentation.screens.tools.pagenumbers

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.Environment
import android.os.ParcelFileDescriptor
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
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

enum class NumberPosition(val label: String) {
    TOP_LEFT("Top Left"),
    TOP_CENTER("Top Center"),
    TOP_RIGHT("Top Right"),
    BOTTOM_LEFT("Bottom Left"),
    BOTTOM_CENTER("Bottom Center"),
    BOTTOM_RIGHT("Bottom Right")
}

enum class NumberFormat(val label: String, val example: String) {
    NUMBER_ONLY("Number Only", "1, 2, 3..."),
    PAGE_X("Page X", "Page 1, Page 2..."),
    X_OF_Y("X of Y", "1 of 10, 2 of 10..."),
    DASH_X_DASH("- X -", "- 1 -, - 2 -..."),
    CUSTOM("Custom", "Custom prefix/suffix")
}

enum class PageSelection {
    ALL,
    ODD,
    EVEN,
    CUSTOM,
    SKIP_FIRST
}

data class SourceFile(
    val uri: Uri,
    val path: String,
    val name: String,
    val size: Long,
    val pageCount: Int = 0,
    val previewBitmap: Bitmap? = null
)

data class PageNumbersState(
    val sourceFile: SourceFile? = null,

    // Page number settings
    val position: NumberPosition = NumberPosition.BOTTOM_CENTER,
    val format: NumberFormat = NumberFormat.NUMBER_ONLY,
    val fontSize: Float = 12f,
    val textColor: Color = Color.Black,
    val startNumber: Int = 1,
    val customPrefix: String = "",
    val customSuffix: String = "",
    val marginX: Float = 36f,
    val marginY: Float = 30f,

    // Page selection
    val pageSelection: PageSelection = PageSelection.ALL,
    val customPages: String = "",
    val skipFirstN: Int = 0,

    // Output
    val outputFileName: String = "",
    val overwriteOriginal: Boolean = false,

    // State
    val isLoading: Boolean = false,
    val isProcessing: Boolean = false,
    val progress: Float = 0f,
    val error: String? = null,
    val result: PageNumbersResult? = null
)

data class PageNumbersResult(
    val outputPath: String,
    val pageCount: Int,
    val numberedPages: Int,
    val fileSize: Long
)

class PageNumbersViewModel(
    private val pdfToolsRepository: PdfToolsRepository,
    private val context: Context
) : ViewModel() {

    private val _state = MutableStateFlow(PageNumbersState())
    val state: StateFlow<PageNumbersState> = _state.asStateFlow()

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

                    // Generate default output name
                    val baseName = file.nameWithoutExtension
                    _state.update { it.copy(outputFileName = "${baseName}_numbered") }
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

    fun setPosition(position: NumberPosition) {
        _state.update { it.copy(position = position) }
    }

    fun setFormat(format: NumberFormat) {
        _state.update { it.copy(format = format) }
    }

    fun setFontSize(size: Float) {
        _state.update { it.copy(fontSize = size.coerceIn(8f, 72f)) }
    }

    fun setTextColor(color: Color) {
        _state.update { it.copy(textColor = color) }
    }

    fun setStartNumber(number: Int) {
        _state.update { it.copy(startNumber = number.coerceAtLeast(1)) }
    }

    fun setCustomPrefix(prefix: String) {
        _state.update { it.copy(customPrefix = prefix) }
    }

    fun setCustomSuffix(suffix: String) {
        _state.update { it.copy(customSuffix = suffix) }
    }

    fun setMarginX(margin: Float) {
        _state.update { it.copy(marginX = margin.coerceIn(0f, 200f)) }
    }

    fun setMarginY(margin: Float) {
        _state.update { it.copy(marginY = margin.coerceIn(0f, 200f)) }
    }

    fun setPageSelection(selection: PageSelection) {
        _state.update { it.copy(pageSelection = selection) }
    }

    fun setCustomPages(pages: String) {
        _state.update { it.copy(customPages = pages) }
    }

    fun setSkipFirstN(n: Int) {
        _state.update { it.copy(skipFirstN = n.coerceAtLeast(0)) }
    }

    fun setOutputFileName(name: String) {
        _state.update { it.copy(outputFileName = name) }
    }

    fun setOverwriteOriginal(overwrite: Boolean) {
        _state.update { it.copy(overwriteOriginal = overwrite) }
    }

    fun applyPageNumbers() {
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

        viewModelScope.launch {
            _state.update { it.copy(isProcessing = true, progress = 0f, error = null) }

            val outputPath: String
            val tempPath: String?

            if (currentState.overwriteOriginal) {
                tempPath = "${context.cacheDir}/pagenumbers_temp_${System.currentTimeMillis()}.pdf"
                outputPath = sourceFile.path
            } else {
                tempPath = null
                val outputDir = getOutputDirectory()
                var path = "$outputDir/${currentState.outputFileName}.pdf"
                var counter = 1
                while (File(path).exists()) {
                    path = "$outputDir/${currentState.outputFileName}_$counter.pdf"
                    counter++
                }
                outputPath = path
            }

            val targetPath = tempPath ?: outputPath

            // Calculate pages to number
            val pages = calculatePages(
                currentState.pageSelection,
                currentState.customPages,
                currentState.skipFirstN,
                sourceFile.pageCount
            )

            val config = PdfToolsRepository.PageNumberConfig(
                position = mapPosition(currentState.position),
                format = mapFormat(currentState.format),
                fontSize = currentState.fontSize,
                color = currentState.textColor.toArgb(),
                startNumber = currentState.startNumber,
                prefix = currentState.customPrefix,
                suffix = currentState.customSuffix,
                marginX = currentState.marginX,
                marginY = currentState.marginY
            )

            val result = pdfToolsRepository.addPageNumbers(
                inputPath = sourceFile.path,
                outputPath = targetPath,
                config = config,
                pages = pages,
                onProgress = { progress ->
                    _state.update { it.copy(progress = progress) }
                }
            )

            result.fold(
                onSuccess = {
                    if (tempPath != null) {
                        try {
                            val tempFile = File(tempPath)
                            val originalFile = File(outputPath)
                            originalFile.delete()
                            tempFile.copyTo(originalFile, overwrite = true)
                            tempFile.delete()
                        } catch (e: Exception) {
                            Timber.e(e, "Failed to replace original file")
                            _state.update { it.copy(isProcessing = false, error = "Failed to replace original file") }
                            return@launch
                        }
                    }

                    val outputFile = File(outputPath)
                    val newPageCount = pdfToolsRepository.getPageCount(outputPath).getOrDefault(0)
                    _state.update {
                        it.copy(
                            isProcessing = false,
                            progress = 1f,
                            result = PageNumbersResult(
                                outputPath = outputPath,
                                pageCount = newPageCount,
                                numberedPages = pages?.size ?: sourceFile.pageCount,
                                fileSize = outputFile.length()
                            )
                        )
                    }
                },
                onFailure = { error ->
                    Timber.e(error, "Add page numbers failed")
                    tempPath?.let { File(it).delete() }
                    _state.update {
                        it.copy(
                            isProcessing = false,
                            error = error.message ?: "Failed to add page numbers"
                        )
                    }
                }
            )
        }
    }

    private fun calculatePages(
        selection: PageSelection,
        customPages: String,
        skipFirstN: Int,
        totalPages: Int
    ): List<Int>? {
        return when (selection) {
            PageSelection.ALL -> null
            PageSelection.ODD -> (1..totalPages).filter { it % 2 == 1 }
            PageSelection.EVEN -> (1..totalPages).filter { it % 2 == 0 }
            PageSelection.CUSTOM -> parseCustomPages(customPages, totalPages)
            PageSelection.SKIP_FIRST -> ((skipFirstN + 1)..totalPages).toList()
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

    private fun mapPosition(position: NumberPosition): PdfToolsRepository.PageNumberPosition {
        return when (position) {
            NumberPosition.TOP_LEFT -> PdfToolsRepository.PageNumberPosition.TOP_LEFT
            NumberPosition.TOP_CENTER -> PdfToolsRepository.PageNumberPosition.TOP_CENTER
            NumberPosition.TOP_RIGHT -> PdfToolsRepository.PageNumberPosition.TOP_RIGHT
            NumberPosition.BOTTOM_LEFT -> PdfToolsRepository.PageNumberPosition.BOTTOM_LEFT
            NumberPosition.BOTTOM_CENTER -> PdfToolsRepository.PageNumberPosition.BOTTOM_CENTER
            NumberPosition.BOTTOM_RIGHT -> PdfToolsRepository.PageNumberPosition.BOTTOM_RIGHT
        }
    }

    private fun mapFormat(format: NumberFormat): PdfToolsRepository.PageNumberFormat {
        return when (format) {
            NumberFormat.NUMBER_ONLY -> PdfToolsRepository.PageNumberFormat.NUMBER_ONLY
            NumberFormat.PAGE_X -> PdfToolsRepository.PageNumberFormat.PAGE_X
            NumberFormat.X_OF_Y -> PdfToolsRepository.PageNumberFormat.X_OF_Y
            NumberFormat.DASH_X_DASH -> PdfToolsRepository.PageNumberFormat.DASH_X_DASH
            NumberFormat.CUSTOM -> PdfToolsRepository.PageNumberFormat.CUSTOM
        }
    }

    fun clearResult() {
        _state.update { it.copy(result = null) }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }

    fun reset() {
        _state.update { PageNumbersState() }
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
            val cacheFile = File(context.cacheDir, "pagenumbers_temp/$fileName")
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
