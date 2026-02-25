package com.rejowan.pdfreaderpro.data.pdf

import android.graphics.Bitmap
import android.util.LruCache
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * LRU cache for rendered PDF pages.
 * Caches bitmaps by page index, dimensions, and color mode for quick retrieval.
 */
class PageCache(
    maxMemoryMb: Int = 128 // Increased for high-res pages
) {
    private val maxSize = maxMemoryMb * 1024 * 1024 // Convert to bytes

    private val cache = object : LruCache<String, Bitmap>(maxSize) {
        override fun sizeOf(key: String, bitmap: Bitmap): Int {
            return bitmap.byteCount
        }

        override fun entryRemoved(
            evicted: Boolean,
            key: String,
            oldValue: Bitmap,
            newValue: Bitmap?
        ) {
            if (evicted && !oldValue.isRecycled) {
                // Don't recycle here as it might still be in use
                // Let the GC handle it
            }
        }
    }

    private val mutex = Mutex()

    /**
     * Generate cache key from page index and zoom level.
     */
    private fun cacheKey(pageIndex: Int, zoom: Float): String {
        return "${pageIndex}_${(zoom * 100).toInt()}"
    }

    /**
     * Generate cache key from page index, dimensions, and color mode.
     */
    private fun cacheKey(pageIndex: Int, width: Int, height: Int, colorMode: String): String {
        return "${pageIndex}_${width}x${height}_$colorMode"
    }

    /**
     * Get cached page bitmap if available.
     */
    suspend fun get(pageIndex: Int, zoom: Float): Bitmap? = mutex.withLock {
        cache.get(cacheKey(pageIndex, zoom))
    }

    /**
     * Get cached high-res page bitmap if available.
     */
    suspend fun getHighRes(
        pageIndex: Int,
        width: Int,
        height: Int,
        colorMode: String
    ): Bitmap? = mutex.withLock {
        cache.get(cacheKey(pageIndex, width, height, colorMode))
    }

    /**
     * Put rendered page bitmap in cache.
     */
    suspend fun put(pageIndex: Int, zoom: Float, bitmap: Bitmap) = mutex.withLock {
        cache.put(cacheKey(pageIndex, zoom), bitmap)
    }

    /**
     * Put high-res rendered page bitmap in cache.
     */
    suspend fun putHighRes(
        pageIndex: Int,
        width: Int,
        height: Int,
        colorMode: String,
        bitmap: Bitmap
    ) = mutex.withLock {
        cache.put(cacheKey(pageIndex, width, height, colorMode), bitmap)
    }

    /**
     * Remove specific page from cache.
     */
    suspend fun evict(pageIndex: Int) = mutex.withLock {
        // Remove all zoom levels for this page
        cache.snapshot().keys
            .filter { it.startsWith("${pageIndex}_") }
            .forEach { cache.remove(it) }
    }

    /**
     * Clear entire cache.
     */
    suspend fun clear() = mutex.withLock {
        cache.evictAll()
    }

    /**
     * Get current cache size in bytes.
     */
    fun currentSize(): Int = cache.size()

    /**
     * Get max cache size in bytes.
     */
    fun maxSize(): Int = cache.maxSize()
}
