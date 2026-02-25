package com.rejowan.pdfreaderpro.data.pdf

import android.graphics.Bitmap
import android.util.LruCache
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * LRU cache for rendered PDF pages.
 * Caches bitmaps by page index and zoom level for quick retrieval.
 */
class PageCache(
    maxMemoryMb: Int = 64
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
     * Get cached page bitmap if available.
     */
    suspend fun get(pageIndex: Int, zoom: Float): Bitmap? = mutex.withLock {
        cache.get(cacheKey(pageIndex, zoom))
    }

    /**
     * Put rendered page bitmap in cache.
     */
    suspend fun put(pageIndex: Int, zoom: Float, bitmap: Bitmap) = mutex.withLock {
        cache.put(cacheKey(pageIndex, zoom), bitmap)
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
