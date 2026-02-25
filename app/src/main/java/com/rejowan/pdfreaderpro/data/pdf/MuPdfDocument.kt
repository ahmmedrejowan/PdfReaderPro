package com.rejowan.pdfreaderpro.data.pdf

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import com.artifex.mupdf.fitz.Document
import com.artifex.mupdf.fitz.Matrix
import com.artifex.mupdf.fitz.Outline
import com.artifex.mupdf.fitz.Page
import com.artifex.mupdf.fitz.android.AndroidDrawDevice
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

/**
 * Wrapper around MuPDF Document for safe handling and resource management.
 */
class MuPdfDocument private constructor(
    private val document: Document,
    val path: String
) : AutoCloseable {

    val pageCount: Int get() = document.countPages()

    val needsPassword: Boolean get() = document.needsPassword()

    val title: String?
        get() = try {
            document.getMetaData(Document.META_INFO_TITLE)?.takeIf { it.isNotBlank() }
        } catch (e: Exception) {
            null
        }

    val author: String?
        get() = try {
            document.getMetaData(Document.META_INFO_AUTHOR)?.takeIf { it.isNotBlank() }
        } catch (e: Exception) {
            null
        }

    /**
     * Authenticate document with password.
     * @return true if authentication successful
     */
    fun authenticate(password: String): Boolean {
        return document.authenticatePassword(password)
    }

    /**
     * Load a page for rendering.
     */
    fun loadPage(index: Int): MuPdfPage {
        require(index in 0 until pageCount) { "Page index $index out of bounds (0-${pageCount - 1})" }
        return MuPdfPage(document.loadPage(index), index)
    }

    /**
     * Get document outline (table of contents).
     */
    fun getOutline(): List<OutlineItem> {
        return try {
            val outline = document.loadOutline()
            if (outline != null) {
                parseOutline(outline, 0)
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun parseOutline(outlines: Array<Outline>, level: Int): List<OutlineItem> {
        val items = mutableListOf<OutlineItem>()
        for (outline in outlines) {
            val pageNum = try {
                document.pageNumberFromLocation(document.resolveLink(outline))
            } catch (e: Exception) {
                0
            }
            items.add(
                OutlineItem(
                    title = outline.title ?: "Untitled",
                    page = pageNum,
                    level = level
                )
            )
            outline.down?.let { children ->
                items.addAll(parseOutline(children, level + 1))
            }
        }
        return items
    }

    /**
     * Search for text in a specific page.
     * Returns list of search results with count (bounds extraction requires native access).
     */
    fun searchPage(pageIndex: Int, query: String): List<SearchResult> {
        if (query.isBlank()) return emptyList()

        return try {
            val page = document.loadPage(pageIndex)
            val hits = page.search(query)
            page.destroy()

            // Return results with placeholder bounds - actual highlighting
            // would need proper native JNI access to Quad fields
            hits?.mapIndexed { index, _ ->
                SearchResult(
                    page = pageIndex,
                    resultIndex = index,
                    bounds = SearchBounds(0f, 0f, 0f, 0f)
                )
            } ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Get text content from a page (simplified).
     */
    fun getPageText(pageIndex: Int): String {
        // Full text extraction would require traversing StructuredText blocks
        // This is a placeholder - proper implementation needs JNI work
        return ""
    }

    override fun close() {
        try {
            document.destroy()
        } catch (e: Exception) {
            // Ignore cleanup errors
        }
    }

    companion object {
        /**
         * Open a PDF document from file path.
         */
        suspend fun open(path: String, password: String? = null): Result<MuPdfDocument> =
            withContext(Dispatchers.IO) {
                try {
                    val document = Document.openDocument(path)
                    val pdfDoc = MuPdfDocument(document, path)

                    if (pdfDoc.needsPassword) {
                        if (password != null) {
                            if (!pdfDoc.authenticate(password)) {
                                pdfDoc.close()
                                return@withContext Result.failure(PasswordRequiredException("Invalid password"))
                            }
                        } else {
                            return@withContext Result.failure(PasswordRequiredException("Password required"))
                        }
                    }

                    Result.success(pdfDoc)
                } catch (e: Exception) {
                    Result.failure(e)
                }
            }

        /**
         * Open a PDF document from content URI.
         */
        suspend fun openFromUri(
            context: Context,
            uri: Uri,
            password: String? = null
        ): Result<MuPdfDocument> = withContext(Dispatchers.IO) {
            try {
                // Copy to cache for MuPDF access
                val cacheFile = File(context.cacheDir, "temp_${System.currentTimeMillis()}.pdf")
                context.contentResolver.openInputStream(uri)?.use { input ->
                    FileOutputStream(cacheFile).use { output ->
                        input.copyTo(output)
                    }
                }

                open(cacheFile.absolutePath, password)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}

/**
 * Wrapper for MuPDF Page with rendering capabilities.
 */
class MuPdfPage(
    private val page: Page,
    val index: Int
) : AutoCloseable {

    val width: Float get() = page.bounds.x1 - page.bounds.x0
    val height: Float get() = page.bounds.y1 - page.bounds.y0

    /**
     * Render page to bitmap at specified scale.
     */
    fun render(scale: Float = 1f): Bitmap {
        val bounds = page.bounds
        val pageWidth = bounds.x1 - bounds.x0
        val pageHeight = bounds.y1 - bounds.y0

        // Ensure minimum dimensions and valid scale
        val effectiveScale = scale.coerceAtLeast(0.1f)
        val width = ((pageWidth * effectiveScale).toInt()).coerceAtLeast(1)
        val height = ((pageHeight * effectiveScale).toInt()).coerceAtLeast(1)

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        // Fill with white background first
        bitmap.eraseColor(android.graphics.Color.WHITE)

        val matrix = Matrix(effectiveScale, effectiveScale)
        val device = AndroidDrawDevice(bitmap, 0, 0, 0, 0, width, height)
        page.run(device, matrix, null)
        device.close()

        return bitmap
    }

    /**
     * Render page to bitmap with specific dimensions.
     * Maintains aspect ratio and centers content.
     */
    fun renderToSize(targetWidth: Int, targetHeight: Int): Bitmap {
        val bounds = page.bounds
        val pageWidth = bounds.x1 - bounds.x0
        val pageHeight = bounds.y1 - bounds.y0

        // Validate inputs
        require(targetWidth > 0 && targetHeight > 0) { "Target dimensions must be positive" }
        require(pageWidth > 0 && pageHeight > 0) { "Page dimensions must be positive" }

        val scaleX = targetWidth.toFloat() / pageWidth
        val scaleY = targetHeight.toFloat() / pageHeight
        val scale = minOf(scaleX, scaleY)

        val width = (pageWidth * scale).toInt().coerceAtLeast(1)
        val height = (pageHeight * scale).toInt().coerceAtLeast(1)

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        // Fill with white background first
        bitmap.eraseColor(android.graphics.Color.WHITE)

        val matrix = Matrix(scale, scale)
        val device = AndroidDrawDevice(bitmap, 0, 0, 0, 0, width, height)
        page.run(device, matrix, null)
        device.close()

        return bitmap
    }

    /**
     * Render thumbnail with maximum dimension.
     */
    fun renderThumbnail(maxDimension: Int = 256): Bitmap {
        val bounds = page.bounds
        val pageWidth = bounds.x1 - bounds.x0
        val pageHeight = bounds.y1 - bounds.y0

        val scale = if (pageWidth > pageHeight) {
            maxDimension.toFloat() / pageWidth
        } else {
            maxDimension.toFloat() / pageHeight
        }

        return render(scale)
    }

    override fun close() {
        try {
            page.destroy()
        } catch (e: Exception) {
            // Ignore cleanup errors
        }
    }
}

// Data classes for PDF metadata and search

data class OutlineItem(
    val title: String,
    val page: Int,
    val level: Int = 0
)

data class SearchResult(
    val page: Int,
    val resultIndex: Int,
    val bounds: SearchBounds
)

data class SearchBounds(
    val x0: Float,
    val y0: Float,
    val x1: Float,
    val y1: Float
)

class PasswordRequiredException(message: String) : Exception(message)
