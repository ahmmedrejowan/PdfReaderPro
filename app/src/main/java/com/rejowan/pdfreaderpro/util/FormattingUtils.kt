package com.rejowan.pdfreaderpro.util

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object FormattingUtils {

    fun formattedFileSize(length: Long): String {
        val kb = 1024
        val mb = kb * 1024
        val gb = mb * 1024
        return when {
            length >= gb -> String.format(Locale.US, "%.1f GB", length / gb.toFloat())
            length >= mb -> String.format(Locale.US, "%.1f MB", length / mb.toFloat())
            length >= kb -> String.format(Locale.US, "%.1f KB", length / kb.toFloat())
            else -> String.format(Locale.US, "%d B", length)
        }
    }

    fun truncateName(name: String, maxLength: Int = 32): String {
        if (name.length <= maxLength) return name
        val first = name.substring(0, 18)
        val last = name.substring(name.length - 10, name.length)
        return "$first...$last"
    }

    fun formattedDate(timestamp: Long): String {
        val date = Date(timestamp)
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.US)
        return sdf.format(date)
    }

    fun formattedDateTime(timestamp: Long): String {
        val date = Date(timestamp)
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.US)
        return sdf.format(date)
    }

    fun extractParentFolders(fullPath: String): String {
        val path = fullPath.removePrefix("/")
        val parts = path.split("/")
        if (parts.size < 4) return "Storage"
        val relevantParts = parts.drop(3).dropLast(1)
        return relevantParts.joinToString(" > ")
    }

    suspend fun generateThumbnail(pdfFilePath: String): Bitmap? = withContext(Dispatchers.IO) {
        var pdfRenderer: PdfRenderer? = null
        var currentPage: PdfRenderer.Page? = null
        try {
            val file = File(pdfFilePath)
            if (!file.exists()) {
                return@withContext null
            }

            val fileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
            pdfRenderer = PdfRenderer(fileDescriptor)

            currentPage = pdfRenderer.openPage(0)
            val bitmap = Bitmap.createBitmap(
                currentPage.width / 4,
                currentPage.height / 4,
                Bitmap.Config.ARGB_8888
            )
            currentPage.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
            bitmap
        } catch (ex: OutOfMemoryError) {
            Timber.e(ex, "Memory issue generating thumbnail for: $pdfFilePath")
            null
        } catch (ex: Exception) {
            Timber.e(ex, "Error generating thumbnail for: $pdfFilePath")
            null
        } finally {
            currentPage?.close()
            pdfRenderer?.close()
        }
    }

    fun getPdfPageCount(path: String): Int {
        var pdfRenderer: PdfRenderer? = null
        return try {
            val file = File(path)
            if (!file.exists()) return 0

            val fileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
            pdfRenderer = PdfRenderer(fileDescriptor)
            pdfRenderer.pageCount
        } catch (ex: OutOfMemoryError) {
            Timber.e(ex, "Memory issue getting page count for: $path")
            0
        } catch (ex: Exception) {
            Timber.e(ex, "Error getting page count for: $path")
            0
        } finally {
            pdfRenderer?.close()
        }
    }
}
