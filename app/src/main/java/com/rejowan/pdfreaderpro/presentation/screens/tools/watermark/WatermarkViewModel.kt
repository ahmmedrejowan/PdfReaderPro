package com.rejowan.pdfreaderpro.presentation.screens.tools.watermark

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

enum class WatermarkType {
    TEXT,
    IMAGE
}

enum class WatermarkPosition(val label: String) {
    CENTER("Center"),
    TOP_LEFT("Top Left"),
    TOP_CENTER("Top Center"),
    TOP_RIGHT("Top Right"),
    BOTTOM_LEFT("Bottom Left"),
    BOTTOM_CENTER("Bottom Center"),
    BOTTOM_RIGHT("Bottom Right"),
    TILED("Tiled")
}

enum class PageSelection {
    ALL,
    ODD,
    EVEN,
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

data class WatermarkState(
    val sourceFile: SourceFile? = null,
    val watermarkType: WatermarkType = WatermarkType.TEXT,

    // Text watermark settings
    val watermarkText: String = "CONFIDENTIAL",
    val fontSize: Float = 48f,
    val textColor: Color = Color(0xFF808080),
    val textOpacity: Float = 50f,
    val textRotation: Float = -45f,

    // Image watermark settings
    val imagePath: String? = null,
    val imageUri: Uri? = null,
    val imageScale: Float = 30f,
    val imageOpacity: Float = 50f,

    // Common settings
    val position: WatermarkPosition = WatermarkPosition.CENTER,
    val pageSelection: PageSelection = PageSelection.ALL,
    val customPages: String = "",

    // Output
    val outputFileName: String = "",
    val overwriteOriginal: Boolean = false,

    // State
    val isLoading: Boolean = false,
    val isProcessing: Boolean = false,
    val progress: Float = 0f,
    val error: String? = null,
    val result: WatermarkResult? = null
)

data class WatermarkResult(
    val outputPath: String,
    val pageCount: Int,
    val fileSize: Long
)

class WatermarkViewModel(
    private val pdfToolsRepository: PdfToolsRepository,
    private val context: Context
) : ViewModel() {

    private val _state = MutableStateFlow(WatermarkState())
    val state: StateFlow<WatermarkState> = _state.asStateFlow()

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
                    _state.update { it.copy(outputFileName = "${baseName}_watermarked") }
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

            // Create high quality preview
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

    fun setWatermarkType(type: WatermarkType) {
        _state.update { it.copy(watermarkType = type) }
    }

    // Text watermark setters
    fun setWatermarkText(text: String) {
        _state.update { it.copy(watermarkText = text) }
    }

    fun setFontSize(size: Float) {
        _state.update { it.copy(fontSize = size.coerceIn(12f, 200f)) }
    }

    fun setTextColor(color: Color) {
        _state.update { it.copy(textColor = color) }
    }

    fun setTextOpacity(opacity: Float) {
        _state.update { it.copy(textOpacity = opacity.coerceIn(1f, 100f)) }
    }

    fun setTextRotation(rotation: Float) {
        _state.update { it.copy(textRotation = rotation.coerceIn(-180f, 180f)) }
    }

    // Image watermark setters
    fun setWatermarkImage(uri: Uri) {
        viewModelScope.launch {
            val path = copyImageToCache(uri)
            if (path != null) {
                _state.update { it.copy(imagePath = path, imageUri = uri) }
            } else {
                _state.update { it.copy(error = "Failed to load image") }
            }
        }
    }

    fun setImageScale(scale: Float) {
        _state.update { it.copy(imageScale = scale.coerceIn(1f, 100f)) }
    }

    fun setImageOpacity(opacity: Float) {
        _state.update { it.copy(imageOpacity = opacity.coerceIn(1f, 100f)) }
    }

    // Common setters
    fun setPosition(position: WatermarkPosition) {
        _state.update { it.copy(position = position) }
    }

    fun setPageSelection(selection: PageSelection) {
        _state.update { it.copy(pageSelection = selection) }
    }

    fun setCustomPages(pages: String) {
        _state.update { it.copy(customPages = pages) }
    }

    fun setOutputFileName(name: String) {
        _state.update { it.copy(outputFileName = name) }
    }

    fun setOverwriteOriginal(overwrite: Boolean) {
        _state.update { it.copy(overwriteOriginal = overwrite) }
    }

    fun applyWatermark() {
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

        if (currentState.watermarkType == WatermarkType.TEXT && currentState.watermarkText.isBlank()) {
            _state.update { it.copy(error = "Please enter watermark text") }
            return
        }

        if (currentState.watermarkType == WatermarkType.IMAGE && currentState.imagePath == null) {
            _state.update { it.copy(error = "Please select a watermark image") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isProcessing = true, progress = 0f, error = null) }

            val outputPath: String
            val tempPath: String?

            if (currentState.overwriteOriginal) {
                tempPath = "${context.cacheDir}/watermark_temp_${System.currentTimeMillis()}.pdf"
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

            // Calculate pages to apply watermark
            val pages = calculatePages(currentState.pageSelection, currentState.customPages, sourceFile.pageCount)

            val result = if (currentState.watermarkType == WatermarkType.TEXT) {
                val config = PdfToolsRepository.TextWatermarkConfig(
                    text = currentState.watermarkText,
                    fontSize = currentState.fontSize,
                    color = currentState.textColor.toArgb(),
                    opacity = currentState.textOpacity,
                    rotation = currentState.textRotation,
                    position = mapPosition(currentState.position)
                )
                pdfToolsRepository.addTextWatermark(
                    inputPath = sourceFile.path,
                    outputPath = targetPath,
                    config = config,
                    pages = pages,
                    onProgress = { progress ->
                        _state.update { it.copy(progress = progress) }
                    }
                )
            } else {
                val config = PdfToolsRepository.ImageWatermarkConfig(
                    imagePath = requireNotNull(currentState.imagePath),
                    scale = currentState.imageScale,
                    opacity = currentState.imageOpacity,
                    position = mapPosition(currentState.position)
                )
                pdfToolsRepository.addImageWatermark(
                    inputPath = sourceFile.path,
                    outputPath = targetPath,
                    config = config,
                    pages = pages,
                    onProgress = { progress ->
                        _state.update { it.copy(progress = progress) }
                    }
                )
            }

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
                            result = WatermarkResult(
                                outputPath = outputPath,
                                pageCount = newPageCount,
                                fileSize = outputFile.length()
                            )
                        )
                    }
                },
                onFailure = { error ->
                    Timber.e(error, "Watermark failed")
                    tempPath?.let { File(it).delete() }
                    _state.update {
                        it.copy(
                            isProcessing = false,
                            error = error.message ?: "Failed to add watermark"
                        )
                    }
                }
            )
        }
    }

    private fun calculatePages(selection: PageSelection, customPages: String, totalPages: Int): List<Int>? {
        return when (selection) {
            PageSelection.ALL -> null // null means all pages
            PageSelection.ODD -> (1..totalPages).filter { it % 2 == 1 }
            PageSelection.EVEN -> (1..totalPages).filter { it % 2 == 0 }
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

    private fun mapPosition(position: WatermarkPosition): PdfToolsRepository.WatermarkPosition {
        return when (position) {
            WatermarkPosition.CENTER -> PdfToolsRepository.WatermarkPosition.CENTER
            WatermarkPosition.TOP_LEFT -> PdfToolsRepository.WatermarkPosition.TOP_LEFT
            WatermarkPosition.TOP_CENTER -> PdfToolsRepository.WatermarkPosition.TOP_CENTER
            WatermarkPosition.TOP_RIGHT -> PdfToolsRepository.WatermarkPosition.TOP_RIGHT
            WatermarkPosition.BOTTOM_LEFT -> PdfToolsRepository.WatermarkPosition.BOTTOM_LEFT
            WatermarkPosition.BOTTOM_CENTER -> PdfToolsRepository.WatermarkPosition.BOTTOM_CENTER
            WatermarkPosition.BOTTOM_RIGHT -> PdfToolsRepository.WatermarkPosition.BOTTOM_RIGHT
            WatermarkPosition.TILED -> PdfToolsRepository.WatermarkPosition.TILED
        }
    }

    fun clearResult() {
        _state.update { it.copy(result = null) }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }

    fun reset() {
        _state.update { WatermarkState() }
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
            val cacheFile = File(context.cacheDir, "watermark_temp/$fileName")
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

    private suspend fun copyImageToCache(uri: Uri): String? = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return@withContext null
            val fileName = "watermark_image_${System.currentTimeMillis()}.png"
            val cacheFile = File(context.cacheDir, "watermark_temp/$fileName")
            cacheFile.parentFile?.mkdirs()

            cacheFile.outputStream().use { output ->
                inputStream.copyTo(output)
            }

            cacheFile.absolutePath
        } catch (e: Exception) {
            Timber.e(e, "Failed to copy image to cache")
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
