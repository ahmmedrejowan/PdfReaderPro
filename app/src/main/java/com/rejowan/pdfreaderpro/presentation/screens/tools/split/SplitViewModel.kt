package com.rejowan.pdfreaderpro.presentation.screens.tools.split

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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class SplitMode {
    BY_RANGES,      // Split by page ranges (e.g., 1-5, 6-10)
    EVERY_N_PAGES,  // Split every N pages
    INTO_PAGES,     // Split into individual pages
    SPECIFIC_PAGES  // Extract specific pages
}

data class SplitState(
    val sourceFile: SourceFile? = null,
    val splitMode: SplitMode = SplitMode.BY_RANGES,
    val rangesInput: String = "",           // For BY_RANGES: "1-5, 6-10, 11-15"
    val everyNPages: Int = 5,               // For EVERY_N_PAGES
    val specificPagesInput: String = "",    // For SPECIFIC_PAGES: "1, 3, 5-8, 12"
    val outputPrefix: String = "",
    val isProcessing: Boolean = false,
    val progress: Float = 0f,
    val error: String? = null,
    val result: SplitResult? = null
)

data class SourceFile(
    val uri: Uri?,
    val path: String,
    val name: String,
    val size: Long,
    val pageCount: Int
)

data class SplitResult(
    val outputDir: String,
    val createdFiles: List<String>,
    val totalPages: Int
)

class SplitViewModel(
    private val pdfToolsRepository: PdfToolsRepository,
    private val context: Context
) : ViewModel() {

    private val _state = MutableStateFlow(SplitState())
    val state: StateFlow<SplitState> = _state.asStateFlow()

    fun setSourceFile(uri: Uri) {
        viewModelScope.launch {
            try {
                val tempPath = copyUriToCache(uri)
                if (tempPath != null) {
                    val file = File(tempPath)
                    val pageCount = pdfToolsRepository.getPageCount(tempPath).getOrDefault(0)
                    val sourceFile = SourceFile(
                        uri = uri,
                        path = tempPath,
                        name = getFileNameFromUri(uri) ?: file.name,
                        size = file.length(),
                        pageCount = pageCount
                    )
                    _state.update {
                        it.copy(
                            sourceFile = sourceFile,
                            outputPrefix = file.nameWithoutExtension,
                            error = null
                        )
                    }
                    // Auto-generate ranges based on page count
                    generateDefaultRanges(pageCount)
                } else {
                    _state.update { it.copy(error = "Failed to load file") }
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to set source file")
                _state.update { it.copy(error = e.message ?: "Failed to load file") }
            }
        }
    }

    private fun generateDefaultRanges(pageCount: Int) {
        // Generate sensible default ranges
        val ranges = when {
            pageCount <= 5 -> "1-$pageCount"
            pageCount <= 10 -> "1-${pageCount / 2}, ${pageCount / 2 + 1}-$pageCount"
            else -> {
                val mid = pageCount / 2
                "1-$mid, ${mid + 1}-$pageCount"
            }
        }
        _state.update { it.copy(rangesInput = ranges) }
    }

    fun setSplitMode(mode: SplitMode) {
        _state.update { it.copy(splitMode = mode, error = null) }
    }

    fun setRangesInput(input: String) {
        _state.update { it.copy(rangesInput = input, error = null) }
    }

    fun setEveryNPages(n: Int) {
        _state.update { it.copy(everyNPages = n.coerceIn(1, 100), error = null) }
    }

    fun setSpecificPagesInput(input: String) {
        _state.update { it.copy(specificPagesInput = input, error = null) }
    }

    fun setOutputPrefix(prefix: String) {
        _state.update { it.copy(outputPrefix = prefix) }
    }

    fun split() {
        val currentState = _state.value
        val sourceFile = currentState.sourceFile

        if (sourceFile == null) {
            _state.update { it.copy(error = "No PDF file selected") }
            return
        }

        if (currentState.outputPrefix.isBlank()) {
            _state.update { it.copy(error = "Enter an output prefix") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isProcessing = true, progress = 0f, error = null) }

            val outputDir = getOutputDirectory(currentState.outputPrefix)

            val result = when (currentState.splitMode) {
                SplitMode.BY_RANGES -> splitByRanges(sourceFile, outputDir, currentState.rangesInput)
                SplitMode.EVERY_N_PAGES -> splitEveryNPages(sourceFile, outputDir, currentState.everyNPages)
                SplitMode.INTO_PAGES -> splitIntoPages(sourceFile, outputDir)
                SplitMode.SPECIFIC_PAGES -> extractSpecificPages(sourceFile, outputDir, currentState.specificPagesInput)
            }

            result.fold(
                onSuccess = { createdFiles ->
                    _state.update {
                        it.copy(
                            isProcessing = false,
                            progress = 1f,
                            result = SplitResult(
                                outputDir = outputDir,
                                createdFiles = createdFiles,
                                totalPages = createdFiles.size
                            )
                        )
                    }
                },
                onFailure = { error ->
                    Timber.e(error, "Split failed")
                    _state.update {
                        it.copy(
                            isProcessing = false,
                            error = error.message ?: "Split failed"
                        )
                    }
                }
            )
        }
    }

    private suspend fun splitByRanges(
        sourceFile: SourceFile,
        outputDir: String,
        rangesInput: String
    ): Result<List<String>> {
        val ranges = parseRangesInput(rangesInput)
        if (ranges.isEmpty()) {
            return Result.failure(IllegalArgumentException("Invalid page ranges"))
        }

        return pdfToolsRepository.splitPdf(
            inputPath = sourceFile.path,
            outputDir = outputDir,
            ranges = ranges,
            onProgress = { progress ->
                _state.update { it.copy(progress = progress) }
            }
        )
    }

    private suspend fun splitEveryNPages(
        sourceFile: SourceFile,
        outputDir: String,
        n: Int
    ): Result<List<String>> {
        val pageCount = sourceFile.pageCount
        val ranges = mutableListOf<String>()

        var start = 1
        while (start <= pageCount) {
            val end = minOf(start + n - 1, pageCount)
            ranges.add("$start-$end")
            start = end + 1
        }

        return pdfToolsRepository.splitPdf(
            inputPath = sourceFile.path,
            outputDir = outputDir,
            ranges = ranges,
            onProgress = { progress ->
                _state.update { it.copy(progress = progress) }
            }
        )
    }

    private suspend fun splitIntoPages(
        sourceFile: SourceFile,
        outputDir: String
    ): Result<List<String>> {
        return pdfToolsRepository.splitIntoPages(
            inputPath = sourceFile.path,
            outputDir = outputDir,
            onProgress = { progress ->
                _state.update { it.copy(progress = progress) }
            }
        )
    }

    private suspend fun extractSpecificPages(
        sourceFile: SourceFile,
        outputDir: String,
        pagesInput: String
    ): Result<List<String>> {
        val pages = parseSpecificPagesInput(pagesInput, sourceFile.pageCount)
        if (pages.isEmpty()) {
            return Result.failure(IllegalArgumentException("Invalid page selection"))
        }

        val outputPath = "$outputDir/${_state.value.outputPrefix}_extracted.pdf"
        return pdfToolsRepository.extractPages(
            inputPath = sourceFile.path,
            outputPath = outputPath,
            pages = pages,
            onProgress = { progress ->
                _state.update { it.copy(progress = progress) }
            }
        ).map { listOf(outputPath) }
    }

    private fun parseRangesInput(input: String): List<String> {
        return input
            .split(",")
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .filter { range ->
                // Validate range format: "N" or "N-M"
                val parts = range.split("-")
                when (parts.size) {
                    1 -> parts[0].toIntOrNull() != null
                    2 -> parts[0].toIntOrNull() != null && parts[1].toIntOrNull() != null
                    else -> false
                }
            }
    }

    private fun parseSpecificPagesInput(input: String, maxPages: Int): List<Int> {
        val pages = mutableSetOf<Int>()

        input.split(",").forEach { part ->
            val trimmed = part.trim()
            if (trimmed.contains("-")) {
                // Range: "5-8"
                val (start, end) = trimmed.split("-").map { it.trim().toIntOrNull() }
                if (start != null && end != null) {
                    for (i in start..end) {
                        if (i in 1..maxPages) pages.add(i)
                    }
                }
            } else {
                // Single page: "3"
                trimmed.toIntOrNull()?.let { page ->
                    if (page in 1..maxPages) pages.add(page)
                }
            }
        }

        return pages.sorted()
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }

    fun reset() {
        _state.update { SplitState() }
    }

    private fun getOutputDirectory(prefix: String): String {
        val documentsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
        val pdfToolsDir = File(documentsDir, "PdfReaderPro/split_$prefix")
        if (!pdfToolsDir.exists()) {
            pdfToolsDir.mkdirs()
        }
        return pdfToolsDir.absolutePath
    }

    private fun copyUriToCache(uri: Uri): String? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val fileName = getFileNameFromUri(uri) ?: "temp_${System.currentTimeMillis()}.pdf"
            val cacheFile = File(context.cacheDir, "split_temp/$fileName")
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
