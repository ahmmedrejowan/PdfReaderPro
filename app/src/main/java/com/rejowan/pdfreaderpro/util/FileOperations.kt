package com.rejowan.pdfreaderpro.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import androidx.core.content.FileProvider
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream

object FileOperations {

    private const val SHARED_PDF_CACHE_DIR = "shared_pdfs"
    private const val MAX_CACHE_AGE_MS = 14 * 24 * 60 * 60 * 1000L // 14 days

    /**
     * Copies a content URI to the app's cache directory.
     * Returns the local file path, or null if copy failed.
     */
    fun copyContentUriToCache(context: Context, uri: Uri): String? {
        return try {
            val fileName = getFileNameFromUri(context, uri) ?: "shared_${System.currentTimeMillis()}.pdf"
            val cacheDir = File(context.cacheDir, SHARED_PDF_CACHE_DIR).apply { mkdirs() }
            val destFile = File(cacheDir, fileName)

            // If file already exists with same name, add timestamp
            val finalFile = if (destFile.exists()) {
                val nameWithoutExt = fileName.substringBeforeLast(".")
                val ext = fileName.substringAfterLast(".", "pdf")
                File(cacheDir, "${nameWithoutExt}_${System.currentTimeMillis()}.$ext")
            } else {
                destFile
            }

            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(finalFile).use { output ->
                    input.copyTo(output)
                }
            }

            Timber.d("Copied content URI to: ${finalFile.absolutePath}")
            finalFile.absolutePath
        } catch (e: Exception) {
            Timber.e(e, "Error copying content URI to cache")
            null
        }
    }

    /**
     * Gets the display name of a file from a content URI.
     */
    fun getFileNameFromUri(context: Context, uri: Uri): String? {
        return try {
            when (uri.scheme) {
                "content" -> {
                    context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                        if (cursor.moveToFirst()) {
                            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                            if (nameIndex >= 0) cursor.getString(nameIndex) else null
                        } else null
                    }
                }
                "file" -> uri.lastPathSegment
                else -> null
            }
        } catch (e: Exception) {
            Timber.e(e, "Error getting file name from URI")
            null
        }
    }

    /**
     * Cleans up old cached PDFs that are older than MAX_CACHE_AGE_MS.
     */
    fun cleanupOldCachedPdfs(context: Context) {
        try {
            val cacheDir = File(context.cacheDir, SHARED_PDF_CACHE_DIR)
            if (!cacheDir.exists()) return

            val cutoffTime = System.currentTimeMillis() - MAX_CACHE_AGE_MS
            cacheDir.listFiles()?.forEach { file ->
                if (file.lastModified() < cutoffTime) {
                    file.delete()
                    Timber.d("Deleted old cached PDF: ${file.name}")
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Error cleaning up cached PDFs")
        }
    }

    /**
     * Checks if a URI is a content URI that needs to be copied.
     */
    fun isContentUri(uri: Uri): Boolean {
        return uri.scheme == "content"
    }

    /**
     * Resolves a URI to a file path. For content URIs, copies to cache first.
     * Returns the file path or null if resolution failed.
     */
    fun resolveUriToPath(context: Context, uri: Uri): String? {
        return when (uri.scheme) {
            "file" -> uri.path
            "content" -> copyContentUriToCache(context, uri)
            else -> null
        }
    }

    fun sharePdf(context: Context, filePath: String) {
        try {
            val file = File(filePath)
            if (!file.exists()) {
                Timber.e("File not found: $filePath")
                return
            }

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            context.startActivity(
                Intent.createChooser(shareIntent, "Share PDF")
            )
        } catch (e: Exception) {
            Timber.e(e, "Error sharing file")
        }
    }

    fun renameFile(filePath: String, newName: String): Boolean {
        return try {
            val file = File(filePath)
            if (!file.exists()) {
                Timber.e("File not found: $filePath")
                return false
            }

            val newFileName = if (newName.endsWith(".pdf", ignoreCase = true)) {
                newName
            } else {
                "$newName.pdf"
            }

            val newFile = File(file.parentFile, newFileName)

            if (newFile.exists()) {
                Timber.e("File with name $newFileName already exists")
                return false
            }

            file.renameTo(newFile)
        } catch (e: Exception) {
            Timber.e(e, "Error renaming file")
            false
        }
    }

    fun deleteFile(filePath: String): Boolean {
        return try {
            val file = File(filePath)
            if (!file.exists()) {
                Timber.e("File not found: $filePath")
                return false
            }
            file.delete()
        } catch (e: Exception) {
            Timber.e(e, "Error deleting file")
            false
        }
    }

    fun getFileSize(filePath: String): Long {
        return try {
            File(filePath).length()
        } catch (e: Exception) {
            0L
        }
    }

    fun fileExists(filePath: String): Boolean {
        return try {
            File(filePath).exists()
        } catch (e: Exception) {
            false
        }
    }
}
