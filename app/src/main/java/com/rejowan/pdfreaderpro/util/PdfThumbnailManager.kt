package com.rejowan.pdfreaderpro.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import android.util.LruCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import androidx.core.graphics.createBitmap

/**
 * Manages PDF thumbnail generation and caching.
 */
object PdfThumbnailManager {

    // In-memory cache for thumbnails
    private val memoryCache: LruCache<String, Bitmap> = LruCache(50)

    // Thumbnail size
    private const val THUMBNAIL_WIDTH = 200
    private const val THUMBNAIL_HEIGHT = 280

    /**
     * Get thumbnail for a PDF file. Returns cached version if available,
     * otherwise generates a new one.
     */
    suspend fun getThumbnail(context: Context, pdfPath: String): Bitmap? {
        // Check memory cache first
        memoryCache.get(pdfPath)?.let { return it }

        // Check disk cache
        val cachedFile = getCacheFile(context, pdfPath)
        if (cachedFile.exists()) {
            val bitmap = loadBitmapFromFile(cachedFile)
            bitmap?.let {
                memoryCache.put(pdfPath, it)
                return it
            }
        }

        // Generate new thumbnail
        return generateThumbnail(context, pdfPath)
    }

    /**
     * Generate thumbnail from PDF file.
     */
    private suspend fun generateThumbnail(context: Context, pdfPath: String): Bitmap? {
        return withContext(Dispatchers.IO) {
            try {
                val file = File(pdfPath)
                if (!file.exists()) return@withContext null

                val fileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
                val pdfRenderer = PdfRenderer(fileDescriptor)

                if (pdfRenderer.pageCount == 0) {
                    pdfRenderer.close()
                    fileDescriptor.close()
                    return@withContext null
                }

                val page = pdfRenderer.openPage(0)

                // Calculate scaled dimensions maintaining aspect ratio
                val aspectRatio = page.width.toFloat() / page.height.toFloat()
                val width: Int
                val height: Int

                if (aspectRatio > THUMBNAIL_WIDTH.toFloat() / THUMBNAIL_HEIGHT) {
                    width = THUMBNAIL_WIDTH
                    height = (THUMBNAIL_WIDTH / aspectRatio).toInt()
                } else {
                    height = THUMBNAIL_HEIGHT
                    width = (THUMBNAIL_HEIGHT * aspectRatio).toInt()
                }

                val bitmap = createBitmap(width, height)

                // Render page to bitmap
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)

                page.close()
                pdfRenderer.close()
                fileDescriptor.close()

                // Cache the bitmap
                memoryCache.put(pdfPath, bitmap)
                saveBitmapToFile(bitmap, getCacheFile(context, pdfPath))

                bitmap
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    /**
     * Get cache file path for a PDF.
     */
    private fun getCacheFile(context: Context, pdfPath: String): File {
        val cacheDir = File(context.cacheDir, "pdf_thumbnails")
        if (!cacheDir.exists()) {
            cacheDir.mkdirs()
        }
        val fileName = pdfPath.hashCode().toString() + ".jpg"
        return File(cacheDir, fileName)
    }

    /**
     * Load bitmap from file.
     */
    private fun loadBitmapFromFile(file: File): Bitmap? {
        return try {
            android.graphics.BitmapFactory.decodeFile(file.absolutePath)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Save bitmap to file.
     */
    private fun saveBitmapToFile(bitmap: Bitmap, file: File) {
        try {
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Clear all cached thumbnails.
     */
    fun clearCache(context: Context) {
        memoryCache.evictAll()
        val cacheDir = File(context.cacheDir, "pdf_thumbnails")
        cacheDir.deleteRecursively()
    }

    /**
     * Remove thumbnail for a specific PDF (e.g., when file is deleted).
     */
    fun removeThumbnail(context: Context, pdfPath: String) {
        memoryCache.remove(pdfPath)
        getCacheFile(context, pdfPath).delete()
    }
}
