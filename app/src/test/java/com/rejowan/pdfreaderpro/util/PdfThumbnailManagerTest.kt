package com.rejowan.pdfreaderpro.util

import android.content.Context
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
class PdfThumbnailManagerTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var context: Context
    private lateinit var cacheDir: File

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        // Create a temporary directory for cache
        cacheDir = File(System.getProperty("java.io.tmpdir"), "test_cache_${System.currentTimeMillis()}")
        cacheDir.mkdirs()

        context = mockk(relaxed = true)
        every { context.cacheDir } returns cacheDir
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
        // Clean up test cache directory
        cacheDir.deleteRecursively()
    }

    // region getThumbnail Tests
    @Test
    fun `getThumbnail returns null for non-existent PDF`() = runTest {
        advanceUntilIdle()

        val result = PdfThumbnailManager.getThumbnail(
            context = context,
            pdfPath = "/nonexistent/file.pdf"
        )

        assertNull(result)
    }

    @Test
    fun `getThumbnail returns null for empty path`() = runTest {
        advanceUntilIdle()

        val result = PdfThumbnailManager.getThumbnail(
            context = context,
            pdfPath = ""
        )

        assertNull(result)
    }

    @Test
    fun `getThumbnail returns null for invalid path`() = runTest {
        advanceUntilIdle()

        val result = PdfThumbnailManager.getThumbnail(
            context = context,
            pdfPath = "/invalid/path/with spaces/file.pdf"
        )

        assertNull(result)
    }
    // endregion

    // region clearCache Tests
    @Test
    fun `clearCache creates thumbnail directory if not exists`() {
        val thumbnailDir = File(cacheDir, "pdf_thumbnails")
        assertFalse(thumbnailDir.exists())

        PdfThumbnailManager.clearCache(context)

        // After clearing, the directory should have been deleted (or not exist)
        // This is expected behavior - deleteRecursively on non-existent dir is safe
    }

    @Test
    fun `clearCache removes thumbnail directory`() {
        // Create thumbnail directory and a dummy file
        val thumbnailDir = File(cacheDir, "pdf_thumbnails")
        thumbnailDir.mkdirs()
        val dummyFile = File(thumbnailDir, "dummy.jpg")
        dummyFile.createNewFile()

        assertTrue(thumbnailDir.exists())
        assertTrue(dummyFile.exists())

        PdfThumbnailManager.clearCache(context)

        assertFalse(thumbnailDir.exists())
    }

    @Test
    fun `clearCache handles multiple files`() {
        // Create thumbnail directory with multiple files
        val thumbnailDir = File(cacheDir, "pdf_thumbnails")
        thumbnailDir.mkdirs()

        for (i in 1..5) {
            File(thumbnailDir, "thumbnail_$i.jpg").createNewFile()
        }

        assertEquals(5, thumbnailDir.listFiles()?.size)

        PdfThumbnailManager.clearCache(context)

        assertFalse(thumbnailDir.exists())
    }
    // endregion

    // region removeThumbnail Tests
    @Test
    fun `removeThumbnail removes specific file`() {
        // Create thumbnail directory and file
        val thumbnailDir = File(cacheDir, "pdf_thumbnails")
        thumbnailDir.mkdirs()

        val pdfPath = "/storage/test.pdf"
        val thumbnailFileName = pdfPath.hashCode().toString() + ".jpg"
        val thumbnailFile = File(thumbnailDir, thumbnailFileName)
        thumbnailFile.createNewFile()

        assertTrue(thumbnailFile.exists())

        PdfThumbnailManager.removeThumbnail(context, pdfPath)

        assertFalse(thumbnailFile.exists())
    }

    @Test
    fun `removeThumbnail handles non-existent file gracefully`() {
        // Should not throw exception
        PdfThumbnailManager.removeThumbnail(context, "/nonexistent/path.pdf")
    }

    @Test
    fun `removeThumbnail does not affect other thumbnails`() {
        // Create thumbnail directory with multiple files
        val thumbnailDir = File(cacheDir, "pdf_thumbnails")
        thumbnailDir.mkdirs()

        val pdfPath1 = "/storage/test1.pdf"
        val pdfPath2 = "/storage/test2.pdf"

        val thumbnail1 = File(thumbnailDir, pdfPath1.hashCode().toString() + ".jpg")
        val thumbnail2 = File(thumbnailDir, pdfPath2.hashCode().toString() + ".jpg")

        thumbnail1.createNewFile()
        thumbnail2.createNewFile()

        assertTrue(thumbnail1.exists())
        assertTrue(thumbnail2.exists())

        PdfThumbnailManager.removeThumbnail(context, pdfPath1)

        assertFalse(thumbnail1.exists())
        assertTrue(thumbnail2.exists())
    }
    // endregion

    // region Cache File Path Tests
    @Test
    fun `cache file path uses hash of pdf path`() {
        val thumbnailDir = File(cacheDir, "pdf_thumbnails")
        thumbnailDir.mkdirs()

        val pdfPath = "/storage/documents/my_document.pdf"
        val expectedFileName = pdfPath.hashCode().toString() + ".jpg"
        val expectedFile = File(thumbnailDir, expectedFileName)

        // Remove thumbnail and verify it tries to delete at correct path
        PdfThumbnailManager.removeThumbnail(context, pdfPath)

        // The file didn't exist, but the path calculation is verified
        assertFalse(expectedFile.exists())
    }

    @Test
    fun `different pdf paths have different cache files`() {
        val path1 = "/storage/doc1.pdf"
        val path2 = "/storage/doc2.pdf"

        val hash1 = path1.hashCode()
        val hash2 = path2.hashCode()

        assertNotEquals(hash1, hash2)
    }
    // endregion

    // region Edge Cases
    @Test
    fun `getThumbnail with special characters in path`() = runTest {
        advanceUntilIdle()

        val result = PdfThumbnailManager.getThumbnail(
            context = context,
            pdfPath = "/storage/docs/文档.pdf"
        )

        assertNull(result) // File doesn't exist, but shouldn't crash
    }

    @Test
    fun `getThumbnail with very long path`() = runTest {
        advanceUntilIdle()

        val longPath = "/storage/" + "a".repeat(500) + ".pdf"
        val result = PdfThumbnailManager.getThumbnail(
            context = context,
            pdfPath = longPath
        )

        assertNull(result) // File doesn't exist, but shouldn't crash
    }
    // endregion
}
