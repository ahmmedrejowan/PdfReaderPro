package com.rejowan.pdfreaderpro.presentation.screens.tools.unlock

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

data class SourceFile(
    val uri: Uri,
    val path: String,
    val name: String,
    val size: Long,
    val isPasswordProtected: Boolean = false
)

data class UnlockState(
    val sourceFile: SourceFile? = null,
    val password: String = "",
    val outputFileName: String = "",
    val overwriteOriginal: Boolean = false,
    val isLoading: Boolean = false,
    val isProcessing: Boolean = false,
    val progress: Float = 0f,
    val error: String? = null,
    val result: UnlockResult? = null
)

data class UnlockResult(
    val outputPath: String,
    val pageCount: Int,
    val fileSize: Long
)

class UnlockViewModel(
    private val pdfToolsRepository: PdfToolsRepository,
    private val context: Context
) : ViewModel() {

    private val _state = MutableStateFlow(UnlockState())
    val state: StateFlow<UnlockState> = _state.asStateFlow()

    fun setSourceFile(uri: Uri) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            try {
                val path = copyUriToCache(uri)
                if (path != null) {
                    val file = File(path)
                    val fileName = getFileNameFromUri(uri) ?: file.name

                    // Check if the file is password protected
                    val isProtected = pdfToolsRepository.isPasswordProtected(path).getOrDefault(false)

                    _state.update {
                        it.copy(
                            sourceFile = SourceFile(
                                uri = uri,
                                path = path,
                                name = fileName,
                                size = file.length(),
                                isPasswordProtected = isProtected
                            ),
                            isLoading = false,
                            error = if (!isProtected) "This PDF is not password protected" else null,
                            result = null
                        )
                    }

                    // Generate default output name
                    val baseName = file.nameWithoutExtension
                    _state.update { it.copy(outputFileName = "${baseName}_unlocked") }
                } else {
                    _state.update { it.copy(isLoading = false, error = "Failed to load PDF file") }
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to set source file")
                _state.update { it.copy(isLoading = false, error = "Failed to load PDF: ${e.message}") }
            }
        }
    }

    fun setPassword(password: String) {
        _state.update { it.copy(password = password) }
    }

    fun setOutputFileName(name: String) {
        _state.update { it.copy(outputFileName = name) }
    }

    fun setOverwriteOriginal(overwrite: Boolean) {
        _state.update { it.copy(overwriteOriginal = overwrite) }
    }

    fun unlock() {
        val currentState = _state.value
        val sourceFile = currentState.sourceFile

        if (sourceFile == null) {
            _state.update { it.copy(error = "Please select a PDF file first") }
            return
        }

        if (!sourceFile.isPasswordProtected) {
            _state.update { it.copy(error = "This PDF is not password protected") }
            return
        }

        if (currentState.password.isBlank()) {
            _state.update { it.copy(error = "Please enter the password") }
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
                tempPath = "${context.cacheDir}/unlock_temp_${System.currentTimeMillis()}.pdf"
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

            val result = pdfToolsRepository.unlockPdf(
                inputPath = sourceFile.path,
                outputPath = targetPath,
                password = currentState.password,
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
                            result = UnlockResult(
                                outputPath = outputPath,
                                pageCount = pageCount,
                                fileSize = outputFile.length()
                            )
                        )
                    }
                },
                onFailure = { error ->
                    Timber.e(error, "Unlock failed")
                    tempPath?.let { File(it).delete() }
                    _state.update {
                        it.copy(
                            isProcessing = false,
                            error = error.message ?: "Failed to unlock PDF"
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
        _state.update { UnlockState() }
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
            val cacheFile = File(context.cacheDir, "unlock_temp/$fileName")
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
