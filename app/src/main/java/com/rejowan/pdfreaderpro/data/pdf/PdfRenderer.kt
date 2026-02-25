package com.rejowan.pdfreaderpro.data.pdf

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * PDF page renderer with caching and color mode support.
 */
class PdfRenderer(
    private val document: MuPdfDocument,
    private val cache: PageCache = PageCache()
) {

    /**
     * Render a page with optional zoom and color mode.
     */
    suspend fun renderPage(
        pageIndex: Int,
        zoom: Float = 1f,
        colorMode: ColorMode = ColorMode.NORMAL
    ): Bitmap = withContext(Dispatchers.Default) {
        // Check cache first (only for normal color mode)
        if (colorMode == ColorMode.NORMAL) {
            cache.get(pageIndex, zoom)?.let { return@withContext it }
        }

        // Render page
        val page = document.loadPage(pageIndex)
        val bitmap = page.render(zoom)
        page.close()

        // Apply color transformation if needed
        val finalBitmap = when (colorMode) {
            ColorMode.NORMAL -> bitmap
            ColorMode.DARK -> applyDarkMode(bitmap)
            ColorMode.SEPIA -> applySepiaMode(bitmap)
            ColorMode.INVERTED -> applyInvertedMode(bitmap)
        }

        // Cache the normal version
        if (colorMode == ColorMode.NORMAL) {
            cache.put(pageIndex, zoom, finalBitmap)
        }

        finalBitmap
    }

    /**
     * Render page to fit specific dimensions.
     */
    suspend fun renderPageToFit(
        pageIndex: Int,
        maxWidth: Int,
        maxHeight: Int,
        colorMode: ColorMode = ColorMode.NORMAL
    ): Bitmap = withContext(Dispatchers.Default) {
        // Check cache first
        val cached = cache.getHighRes(pageIndex, maxWidth, maxHeight, colorMode.name)
        if (cached != null && !cached.isRecycled) {
            return@withContext cached
        }

        val page = document.loadPage(pageIndex)
        val bitmap = page.renderToSize(maxWidth, maxHeight)
        page.close()

        val finalBitmap = when (colorMode) {
            ColorMode.NORMAL -> bitmap
            ColorMode.DARK -> applyDarkMode(bitmap)
            ColorMode.SEPIA -> applySepiaMode(bitmap)
            ColorMode.INVERTED -> applyInvertedMode(bitmap)
        }

        // Cache the result
        cache.putHighRes(pageIndex, maxWidth, maxHeight, colorMode.name, finalBitmap)

        finalBitmap
    }

    /**
     * Render thumbnail for page navigation.
     */
    suspend fun renderThumbnail(
        pageIndex: Int,
        maxDimension: Int = 200
    ): Bitmap = withContext(Dispatchers.Default) {
        val page = document.loadPage(pageIndex)
        val bitmap = page.renderThumbnail(maxDimension)
        page.close()
        bitmap
    }

    /**
     * Pre-render adjacent pages for smoother navigation.
     */
    suspend fun preRenderAdjacent(
        currentPage: Int,
        zoom: Float = 1f,
        range: Int = 1
    ) = withContext(Dispatchers.Default) {
        val pageCount = document.pageCount
        val pagesToRender = mutableListOf<Int>()

        // Add adjacent pages
        for (offset in 1..range) {
            if (currentPage + offset < pageCount) {
                pagesToRender.add(currentPage + offset)
            }
            if (currentPage - offset >= 0) {
                pagesToRender.add(currentPage - offset)
            }
        }

        // Render pages that aren't cached
        for (pageIndex in pagesToRender) {
            if (cache.get(pageIndex, zoom) == null) {
                val page = document.loadPage(pageIndex)
                val bitmap = page.render(zoom)
                page.close()
                cache.put(pageIndex, zoom, bitmap)
            }
        }
    }

    /**
     * Clear the render cache.
     */
    suspend fun clearCache() {
        cache.clear()
    }

    private fun applyDarkMode(bitmap: Bitmap): Bitmap {
        val result = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(result)
        val paint = Paint()

        // Invert colors and reduce brightness
        val colorMatrix = ColorMatrix(
            floatArrayOf(
                -1f, 0f, 0f, 0f, 255f,
                0f, -1f, 0f, 0f, 255f,
                0f, 0f, -1f, 0f, 255f,
                0f, 0f, 0f, 1f, 0f
            )
        )
        paint.colorFilter = ColorMatrixColorFilter(colorMatrix)
        canvas.drawBitmap(bitmap, 0f, 0f, paint)

        if (bitmap != result && !bitmap.isRecycled) {
            bitmap.recycle()
        }

        return result
    }

    private fun applySepiaMode(bitmap: Bitmap): Bitmap {
        val result = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(result)
        val paint = Paint()

        // Sepia tone matrix
        val colorMatrix = ColorMatrix(
            floatArrayOf(
                0.393f, 0.769f, 0.189f, 0f, 0f,
                0.349f, 0.686f, 0.168f, 0f, 0f,
                0.272f, 0.534f, 0.131f, 0f, 0f,
                0f, 0f, 0f, 1f, 0f
            )
        )
        paint.colorFilter = ColorMatrixColorFilter(colorMatrix)
        canvas.drawBitmap(bitmap, 0f, 0f, paint)

        if (bitmap != result && !bitmap.isRecycled) {
            bitmap.recycle()
        }

        return result
    }

    private fun applyInvertedMode(bitmap: Bitmap): Bitmap {
        val result = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(result)
        val paint = Paint()

        // Simple color inversion
        val colorMatrix = ColorMatrix(
            floatArrayOf(
                -1f, 0f, 0f, 0f, 255f,
                0f, -1f, 0f, 0f, 255f,
                0f, 0f, -1f, 0f, 255f,
                0f, 0f, 0f, 1f, 0f
            )
        )
        paint.colorFilter = ColorMatrixColorFilter(colorMatrix)
        canvas.drawBitmap(bitmap, 0f, 0f, paint)

        if (bitmap != result && !bitmap.isRecycled) {
            bitmap.recycle()
        }

        return result
    }
}

/**
 * Color modes for PDF rendering.
 */
enum class ColorMode {
    NORMAL,     // White background
    DARK,       // Inverted (dark background)
    SEPIA,      // Warm sepia tones
    INVERTED    // Pure color inversion
}
