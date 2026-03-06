package com.rejowan.pdfreaderpro.presentation.screens.tools.lock

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

data class SourceFile(
    val uri: Uri,
    val path: String,
    val name: String,
    val size: Long,
    val pageCount: Int = 0,
    val thumbnail: Bitmap? = null
)

data class LockState(
    val sourceFile: SourceFile? = null,
    val userPassword: String = "",
    val ownerPassword: String = "",
    val allowPrinting: Boolean = false,
    val allowCopying: Boolean = false,
    val allowModifying: Boolean = false,
    val allowAnnotations: Boolean = false,
    val outputFileName: String = "",
    val overwriteOriginal: Boolean = false,
    val isLoading: Boolean = false,
    val isProcessing: Boolean = false,
    val progress: Float = 0f,
    val error: String? = null,
    val result: LockResult? = null
)

data class LockResult(
    val outputPath: String,
    val pageCount: Int,
    val fileSize: Long
)

class LockViewModel(
    private val pdfToolsRepository: PdfToolsRepository,
    private val context: Context
) : ViewModel() {

    private val _state = MutableStateFlow(LockState())
    val state: StateFlow<LockState> = _state.asStateFlow()

    fun setSourceFile(uri: Uri) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

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
                            isLoading = false,
                            error = null,
                            result = null
                        )
                    }

                    // Generate default output name
                    val baseName = file.nameWithoutExtension
                    _state.update { it.copy(outputFileName = "${baseName}_locked") }
                } else {
                    _state.update { it.copy(isLoading = false, error = "Failed to load PDF file") }
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to set source file")
                _state.update { it.copy(isLoading = false, error = "Failed to load PDF: ${e.message}") }
            }
        }
    }

    private suspend fun generateThumbnail(pdfPath: String): Bitmap? = withContext(Dispatchers.IO) {
        try {
            val file = File(pdfPath)
            val fd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
            val renderer = PdfRenderer(fd)
            val page = renderer.openPage(0)

            val thumbnailSize = 200
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
        } catch (e: Exception) {
            Timber.e(e, "Failed to generate thumbnail")
            null
        }
    }

    fun setUserPassword(password: String) {
        _state.update { it.copy(userPassword = password) }
    }

    fun setOwnerPassword(password: String) {
        _state.update { it.copy(ownerPassword = password) }
    }

    fun setAllowPrinting(allow: Boolean) {
        _state.update { it.copy(allowPrinting = allow) }
    }

    fun setAllowCopying(allow: Boolean) {
        _state.update { it.copy(allowCopying = allow) }
    }

    fun setAllowModifying(allow: Boolean) {
        _state.update { it.copy(allowModifying = allow) }
    }

    fun setAllowAnnotations(allow: Boolean) {
        _state.update { it.copy(allowAnnotations = allow) }
    }

    fun setOutputFileName(name: String) {
        _state.update { it.copy(outputFileName = name) }
    }

    fun setOverwriteOriginal(overwrite: Boolean) {
        _state.update { it.copy(overwriteOriginal = overwrite) }
    }

    fun lock() {
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

        // Validate owner password
        if (currentState.ownerPassword.isBlank()) {
            _state.update { it.copy(error = "Owner password is required") }
            return
        }

        if (currentState.ownerPassword.length < 4) {
            _state.update { it.copy(error = "Owner password must be at least 4 characters") }
            return
        }

        // Validate user password if provided
        if (currentState.userPassword.isNotEmpty() && currentState.userPassword.length < 4) {
            _state.update { it.copy(error = "User password must be at least 4 characters") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isProcessing = true, progress = 0f, error = null) }

            val outputPath: String
            val tempPath: String?

            if (currentState.overwriteOriginal) {
                tempPath = "${context.cacheDir}/lock_temp_${System.currentTimeMillis()}.pdf"
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

            val result = pdfToolsRepository.lockPdf(
                inputPath = sourceFile.path,
                outputPath = targetPath,
                userPassword = currentState.userPassword,
                ownerPassword = currentState.ownerPassword,
                permissions = PdfToolsRepository.PdfPermissions(
                    allowPrinting = currentState.allowPrinting,
                    allowCopying = currentState.allowCopying,
                    allowModifying = currentState.allowModifying,
                    allowAnnotations = currentState.allowAnnotations
                ),
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
                    val pageCount = pdfToolsRepository.getPageCount(outputPath).getOrDefault(0)
                    _state.update {
                        it.copy(
                            isProcessing = false,
                            progress = 1f,
                            result = LockResult(
                                outputPath = outputPath,
                                pageCount = pageCount,
                                fileSize = outputFile.length()
                            )
                        )
                    }
                },
                onFailure = { error ->
                    Timber.e(error, "Lock failed")
                    tempPath?.let { File(it).delete() }
                    _state.update {
                        it.copy(
                            isProcessing = false,
                            error = error.message ?: "Failed to lock PDF"
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
        _state.update { LockState() }
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
            val cacheFile = File(context.cacheDir, "lock_temp/$fileName")
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
