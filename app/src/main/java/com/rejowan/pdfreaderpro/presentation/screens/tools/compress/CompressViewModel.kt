package com.rejowan.pdfreaderpro.presentation.screens.tools.compress

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
 * Compression quality levels with their corresponding quality values.
 */
enum class CompressionLevel(
    val label: String,
    val description: String,
    val quality: Float
) {
    LOW("Low", "Minimal compression, best quality", 0.8f),
    MEDIUM("Medium", "Balanced compression and quality", 0.5f),
    HIGH("High", "Maximum compression, smaller file", 0.2f)
}

data class SourceFile(
    val uri: Uri,
    val path: String,
    val name: String,
    val size: Long,
    val pageCount: Int = 0,
    val thumbnail: Bitmap? = null
)

data class CompressState(
    val sourceFile: SourceFile? = null,
    val compressionLevel: CompressionLevel = CompressionLevel.MEDIUM,
    val outputFileName: String = "",
    val isProcessing: Boolean = false,
    val progress: Float = 0f,
    val error: String? = null,
    val result: CompressResult? = null
)

data class CompressResult(
    val outputPath: String,
    val originalSize: Long,
    val compressedSize: Long,
    val pageCount: Int
) {
    val reductionPercentage: Float
        get() = if (originalSize > 0) {
            ((originalSize - compressedSize).toFloat() / originalSize) * 100f
        } else 0f

    val savedBytes: Long
        get() = originalSize - compressedSize
}

class CompressViewModel(
    private val pdfToolsRepository: PdfToolsRepository,
    private val context: Context
) : ViewModel() {

    private val _state = MutableStateFlow(CompressState())
    val state: StateFlow<CompressState> = _state.asStateFlow()

    private fun generateDefaultFileName() {
        val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
        val timestamp = dateFormat.format(Date())
        _state.update { it.copy(outputFileName = "compressed_$timestamp") }
    }

    fun setSourceFile(uri: Uri) {
        viewModelScope.launch {
            try {
                val path = copyUriToCache(uri)
                if (path != null) {
                    val file = File(path)
                    val pageCount = pdfToolsRepository.getPageCount(path).getOrDefault(0)
                    val thumbnail = generateThumbnail(path)

                    _state.update {
                        it.copy(
                            sourceFile = SourceFile(
                                uri = uri,
                                path = path,
                                name = getFileNameFromUri(uri) ?: file.name,
                                size = file.length(),
                                pageCount = pageCount,
                                thumbnail = thumbnail
                            ),
                            error = null,
                            result = null
                        )
                    }

                    // Generate default output name based on source file
                    val baseName = file.nameWithoutExtension
                    _state.update { it.copy(outputFileName = "${baseName}_compressed") }
                } else {
                    _state.update { it.copy(error = "Failed to load PDF file") }
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to set source file")
                _state.update { it.copy(error = "Failed to load PDF: ${e.message}") }
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

                val thumbnailSize = 120
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
            Timber.e(e, "Failed to generate thumbnail")
            null
        }
    }

    fun setCompressionLevel(level: CompressionLevel) {
        _state.update { it.copy(compressionLevel = level) }
    }

    fun setOutputFileName(name: String) {
        _state.update { it.copy(outputFileName = name) }
    }

    fun compress() {
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

            val outputDir = getOutputDirectory()
            var outputPath = "$outputDir/${currentState.outputFileName}.pdf"

            // Check if file exists and generate unique name
            var counter = 1
            while (File(outputPath).exists()) {
                outputPath = "$outputDir/${currentState.outputFileName}_$counter.pdf"
                counter++
            }

            val result = pdfToolsRepository.compressPdf(
                inputPath = sourceFile.path,
                outputPath = outputPath,
                quality = currentState.compressionLevel.quality,
                onProgress = { progress ->
                    _state.update { it.copy(progress = progress) }
                }
            )

            result.fold(
                onSuccess = { newSize ->
                    val pageCount = pdfToolsRepository.getPageCount(outputPath).getOrDefault(0)
                    _state.update {
                        it.copy(
                            isProcessing = false,
                            progress = 1f,
                            result = CompressResult(
                                outputPath = outputPath,
                                originalSize = sourceFile.size,
                                compressedSize = newSize,
                                pageCount = pageCount
                            )
                        )
                    }
                },
                onFailure = { error ->
                    Timber.e(error, "Compression failed")
                    _state.update {
                        it.copy(
                            isProcessing = false,
                            error = error.message ?: "Compression failed"
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
        _state.update { CompressState() }
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

    private fun copyUriToCache(uri: Uri): String? {
        return try {
            // First try direct file path
            if (uri.scheme == "file") {
                return uri.path
            }

            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val fileName = getFileNameFromUri(uri) ?: "temp_${System.currentTimeMillis()}.pdf"
            val cacheFile = File(context.cacheDir, "compress_temp/$fileName")
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
