package com.rejowan.pdfreaderpro.utils

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FormattingUtils {

    companion object {

        fun formattedFileSize(length: Long): String {
            // to kb, mb , gb
            val kb = 1024
            val mb = kb * 1024
            val gb = mb * 1024
            return if (length >= gb) {
                String.format("%.1f GB", length / gb.toFloat())
            } else if (length >= mb) {
                String.format("%.1f MB", length / mb.toFloat())
            } else if (length >= kb) {
                String.format("%.1f KB", length / kb.toFloat())
            } else {
                String.format("%d B", length)
            }
        }

        fun resizeName(s: String): String {
            if (s.length <= 32) {
                return s
            }
            val first = s.substring(0, 18)
            val last = s.substring(s.length - 10, s.length)
            return "$first...$last"
        }

        fun formattedDate(lastModified: Long): String {
            val date = Date(lastModified * 1000)
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.US)
            return sdf.format(date)
        }

        fun generateNormalThumbnail(pdfFilePath: String): Bitmap? {
            var pdfRenderer: PdfRenderer? = null
            var currentPage: PdfRenderer.Page? = null
            try {
                val file = File(pdfFilePath)
                if (!file.exists()) {
                    return null
                }

                val fileDescriptor =
                    ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
                pdfRenderer = PdfRenderer(fileDescriptor)

                // Use the first page to generate the thumbnail
                currentPage = pdfRenderer.openPage(0)
                val bitmap: Bitmap = Bitmap.createBitmap(
                    currentPage.width / 4, currentPage.height / 4, Bitmap.Config.ARGB_8888
                )
                currentPage.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)

                return bitmap
            } catch (ex: OutOfMemoryError) {
                // Log.e("ThumbnailGenerator", "Memory issue generating thumbnail", ex)
                return null
            } catch (ex: Exception) {
                //  Log.e("ThumbnailGenerator", "Error generating thumbnail", ex)
                return null
            } finally {
                currentPage?.close()
                pdfRenderer?.close()
            }
        }

        fun extractParentFolders(fullPath: String): String {
            val path: String = if (fullPath.startsWith("/")) {
                fullPath.substring(1)
            } else {
                fullPath
            }
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

                val fileDescriptor =
                    ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
                pdfRenderer = PdfRenderer(fileDescriptor)

                // Use the first page to generate the thumbnail
                currentPage = pdfRenderer.openPage(0)
                val bitmap: Bitmap = Bitmap.createBitmap(
                    currentPage.width / 4, currentPage.height / 4, Bitmap.Config.ARGB_8888
                )
                currentPage.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)

                bitmap
            } catch (ex: OutOfMemoryError) {
                // Log.e("ThumbnailGenerator", "Memory issue generating thumbnail", ex)
                null
            } catch (ex: Exception) {
                //  Log.e("ThumbnailGenerator", "Error generating thumbnail", ex)
                null
            } finally {
                currentPage?.close()
                pdfRenderer?.close()
            }
        }


        fun getPdfPageCount(path: String): Int {
            val pageCount: Int

            var pdfRenderer: PdfRenderer? = null
            try {
                val file = File(path)
                if (!file.exists()) {
                    return 0
                }
                val fileDescriptor =
                    ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
                pdfRenderer = PdfRenderer(fileDescriptor)
                pageCount = pdfRenderer.pageCount

            } catch (ex: OutOfMemoryError) {
                return 0
            } catch (ex: Exception) {
                return 0
            } finally {
                pdfRenderer?.close()
            }


            return pageCount
        }

    }

}