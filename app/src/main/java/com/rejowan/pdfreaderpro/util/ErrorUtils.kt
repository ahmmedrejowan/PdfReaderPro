package com.rejowan.pdfreaderpro.util

import android.content.Context
import android.os.Environment
import android.os.StatFs
import com.rejowan.pdfreaderpro.R
import java.io.FileNotFoundException
import java.io.IOException

/**
 * Categorized error types for better error handling and recovery options.
 */
sealed class AppError(
    val messageResId: Int,
    val isRecoverable: Boolean,
    val recoveryHintResId: Int? = null
) {
    /** File not found or inaccessible */
    data object FileNotFound : AppError(
        messageResId = R.string.error_file_not_found,
        isRecoverable = false
    )

    /** PDF file is corrupted or invalid */
    data object CorruptedPdf : AppError(
        messageResId = R.string.error_corrupted_pdf,
        isRecoverable = false,
        recoveryHintResId = R.string.error_hint_corrupted_pdf
    )

    /** Invalid PDF format */
    data object InvalidPdf : AppError(
        messageResId = R.string.error_invalid_pdf,
        isRecoverable = false,
        recoveryHintResId = R.string.error_hint_invalid_pdf
    )

    /** Password required to open PDF */
    data object PasswordRequired : AppError(
        messageResId = R.string.error_password_required,
        isRecoverable = true,
        recoveryHintResId = R.string.error_hint_password_required
    )

    /** Wrong password provided */
    data object WrongPassword : AppError(
        messageResId = R.string.error_wrong_password,
        isRecoverable = true,
        recoveryHintResId = R.string.error_hint_wrong_password
    )

    /** Storage is full */
    data object StorageFull : AppError(
        messageResId = R.string.error_storage_full,
        isRecoverable = true,
        recoveryHintResId = R.string.error_hint_storage_full
    )

    /** Permission denied */
    data object PermissionDenied : AppError(
        messageResId = R.string.error_permission_denied,
        isRecoverable = true,
        recoveryHintResId = R.string.error_hint_permission_denied
    )

    /** Network error */
    data object NetworkError : AppError(
        messageResId = R.string.error_network,
        isRecoverable = true,
        recoveryHintResId = R.string.error_hint_network
    )

    /** Operation timed out */
    data object Timeout : AppError(
        messageResId = R.string.error_timeout,
        isRecoverable = true,
        recoveryHintResId = R.string.error_hint_timeout
    )

    /** Out of memory */
    data object OutOfMemory : AppError(
        messageResId = R.string.error_out_of_memory,
        isRecoverable = true,
        recoveryHintResId = R.string.error_hint_out_of_memory
    )

    /** Generic/unknown error */
    data class Unknown(val originalMessage: String?) : AppError(
        messageResId = R.string.error_generic,
        isRecoverable = true,
        recoveryHintResId = R.string.error_hint_generic
    )
}

/**
 * Utility object for error handling and classification.
 */
object ErrorUtils {

    // Minimum storage space required (50 MB) for safety
    private const val MIN_STORAGE_BYTES = 50L * 1024 * 1024

    /**
     * Classifies an exception into an AppError type for user-friendly display.
     */
    fun classifyError(exception: Throwable): AppError {
        val message = exception.message?.lowercase() ?: ""

        return when {
            // File not found
            exception is FileNotFoundException -> AppError.FileNotFound

            // Storage full detection
            isStorageFullError(exception) -> AppError.StorageFull

            // Out of memory
            exception is OutOfMemoryError -> AppError.OutOfMemory
            message.contains("out of memory") -> AppError.OutOfMemory

            // Permission denied
            exception is SecurityException -> AppError.PermissionDenied
            message.contains("permission denied") -> AppError.PermissionDenied
            message.contains("eacces") -> AppError.PermissionDenied

            // Password/encryption issues (iText specific)
            exception.javaClass.name.contains("BadPasswordException") -> AppError.WrongPassword
            message.contains("bad password") -> AppError.WrongPassword
            message.contains("password") && message.contains("required") -> AppError.PasswordRequired
            message.contains("encrypted") -> AppError.PasswordRequired

            // Invalid PDF detection (check before corrupted)
            isInvalidPdfError(exception) -> AppError.InvalidPdf

            // Corrupted PDF detection
            isCorruptedPdfError(exception) -> AppError.CorruptedPdf

            // Timeout (check before network to catch "connection timeout")
            message.contains("timeout") -> AppError.Timeout
            message.contains("timed out") -> AppError.Timeout

            // Network errors
            message.contains("network") -> AppError.NetworkError
            message.contains("connection") -> AppError.NetworkError
            message.contains("host") -> AppError.NetworkError

            // Generic fallback
            else -> AppError.Unknown(exception.message)
        }
    }

    /**
     * Checks if the exception indicates storage is full.
     */
    private fun isStorageFullError(exception: Throwable): Boolean {
        val message = exception.message?.lowercase() ?: ""
        return when {
            message.contains("no space left") -> true
            message.contains("enospc") -> true
            message.contains("storage") && message.contains("full") -> true
            message.contains("disk full") -> true
            message.contains("not enough space") -> true
            // IOException during write operations often indicates storage issues
            exception is IOException && message.contains("write") && message.contains("fail") -> true
            else -> false
        }
    }

    /**
     * Checks if the exception indicates a corrupted PDF.
     */
    private fun isCorruptedPdfError(exception: Throwable): Boolean {
        val message = exception.message?.lowercase() ?: ""
        val className = exception.javaClass.name.lowercase()

        // Don't classify as corrupted if it's an invalid PDF format issue
        if (isInvalidPdfError(exception)) return false

        return when {
            message.contains("corrupted") -> true
            message.contains("corrupt") -> true
            message.contains("trailer not found") -> true
            message.contains("invalid xref") -> true
            message.contains("unexpected end of file") -> true
            message.contains("eof marker") -> true
            className.contains("invalidpdf") -> true
            className.contains("pdfexception") && message.contains("invalid") -> true
            else -> false
        }
    }

    /**
     * Checks if the exception indicates an invalid PDF format.
     */
    private fun isInvalidPdfError(exception: Throwable): Boolean {
        val message = exception.message?.lowercase() ?: ""

        return when {
            message.contains("not a pdf") -> true
            message.contains("invalid pdf") -> true
            message.contains("pdf header signature not found") -> true
            message.contains("magic number") -> true
            message.contains("file is not a valid pdf") -> true
            else -> false
        }
    }

    /**
     * Checks available storage space.
     * Returns true if there's enough space for the operation.
     */
    fun hasEnoughStorage(requiredBytes: Long = MIN_STORAGE_BYTES): Boolean {
        return try {
            val stat = StatFs(Environment.getExternalStorageDirectory().path)
            val availableBytes = stat.availableBlocksLong * stat.blockSizeLong
            availableBytes >= requiredBytes
        } catch (e: Exception) {
            // If we can't check, assume there's space
            true
        }
    }

    /**
     * Gets available storage space in bytes.
     */
    fun getAvailableStorageBytes(): Long {
        return try {
            val stat = StatFs(Environment.getExternalStorageDirectory().path)
            stat.availableBlocksLong * stat.blockSizeLong
        } catch (e: Exception) {
            -1L
        }
    }

    /**
     * Formats available storage for display.
     */
    fun formatAvailableStorage(context: Context): String {
        val bytes = getAvailableStorageBytes()
        return if (bytes >= 0) {
            FormattingUtils.formattedFileSize(bytes)
        } else {
            context.getString(R.string.unknown)
        }
    }

    /**
     * Gets user-friendly error message from an exception.
     */
    fun getUserFriendlyMessage(context: Context, exception: Throwable): String {
        val appError = classifyError(exception)
        return context.getString(appError.messageResId)
    }

    /**
     * Gets recovery hint for an error if available.
     */
    fun getRecoveryHint(context: Context, appError: AppError): String? {
        return appError.recoveryHintResId?.let { context.getString(it) }
    }

    /**
     * Checks if an error is recoverable (retry makes sense).
     */
    fun isRecoverable(exception: Throwable): Boolean {
        return classifyError(exception).isRecoverable
    }

    /**
     * Validates a PDF file before processing.
     * Returns null if valid, or an AppError if invalid.
     */
    fun validatePdfFile(filePath: String): AppError? {
        val file = java.io.File(filePath)

        // Check existence
        if (!file.exists()) {
            return AppError.FileNotFound
        }

        // Check readability
        if (!file.canRead()) {
            return AppError.PermissionDenied
        }

        // Check if empty
        if (file.length() == 0L) {
            return AppError.CorruptedPdf
        }

        // Check PDF header magic bytes
        try {
            file.inputStream().use { stream ->
                val header = ByteArray(5)
                val bytesRead = stream.read(header)
                if (bytesRead < 5) {
                    return AppError.CorruptedPdf
                }
                val headerString = String(header, Charsets.US_ASCII)
                if (!headerString.startsWith("%PDF-")) {
                    return AppError.InvalidPdf
                }
            }
        } catch (e: Exception) {
            return AppError.CorruptedPdf
        }

        return null
    }

    /**
     * Pre-flight check before file operations.
     * Validates storage space and permissions.
     */
    fun preflightCheck(estimatedSizeBytes: Long = MIN_STORAGE_BYTES): AppError? {
        if (!hasEnoughStorage(estimatedSizeBytes)) {
            return AppError.StorageFull
        }
        return null
    }
}
