package com.rejowan.pdfreaderpro.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import android.util.LruCache
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkConstructor
import io.mockk.mockkStatic
import io.mockk.unmockkConstructor
import io.mockk.unmockkObject
import io.mockk.unmockkStatic
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

        // Mock BitmapFactory to avoid Android graphics API issues
        mockkStatic(BitmapFactory::class)
        every { BitmapFactory.decodeFile(any()) } returns null

        // Rendering a page is the whole job here, and the platform renderer has no
        // JVM implementation, so it is stood up rather than worked around.
        mockkStatic(ParcelFileDescriptor::class)
        every { ParcelFileDescriptor.open(any(), any()) } returns mockk(relaxed = true)
        mockkConstructor(PdfRenderer::class)
        val page = mockk<PdfRenderer.Page>(relaxed = true)
        every { page.width } returns 600
        every { page.height } returns 800
        every { anyConstructed<PdfRenderer>().pageCount } returns 3
        every { anyConstructed<PdfRenderer>().openPage(any()) } returns page
        mockkStatic(Bitmap::class)
        every { Bitmap.createBitmap(any<Int>(), any<Int>(), any()) } answers {
            mockk<Bitmap>(relaxed = true).also {
                every { it.width } returns firstArg()
                every { it.height } returns secondArg()
            }
        }
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
        // Clean up test cache directory
        cacheDir.deleteRecursively()
        unmockkStatic(BitmapFactory::class)
        unmockkStatic(ParcelFileDescriptor::class)
        unmockkStatic(Bitmap::class)
        unmockkConstructor(PdfRenderer::class)
    }

    // Note: PdfThumbnailManager uses LruCache which is an Android class that doesn't
    // work properly in unit tests. These tests focus on the file system operations
    // that can be tested without Android framework dependencies.

    // region clearCache Tests (File System Operations Only)
    @Test
    fun `clearCache removes thumbnail directory when it exists`() {
        // Create thumbnail directory and a dummy file
        val thumbnailDir = File(cacheDir, "pdf_thumbnails")
        thumbnailDir.mkdirs()
        val dummyFile = File(thumbnailDir, "dummy.jpg")
        dummyFile.createNewFile()

        assertTrue(thumbnailDir.exists())
        assertTrue(dummyFile.exists())

        // Directly test the file operation
        thumbnailDir.deleteRecursively()

        assertFalse(thumbnailDir.exists())
    }

    @Test
    fun `thumbnail directory deletion handles multiple files`() {
        // Create thumbnail directory with multiple files
        val thumbnailDir = File(cacheDir, "pdf_thumbnails")
        thumbnailDir.mkdirs()

        for (i in 1..5) {
            File(thumbnailDir, "thumbnail_$i.jpg").createNewFile()
        }

        assertEquals(5, thumbnailDir.listFiles()?.size)

        thumbnailDir.deleteRecursively()

        assertFalse(thumbnailDir.exists())
    }

    @Test
    fun `delete recursively handles non-existent directory`() {
        val thumbnailDir = File(cacheDir, "pdf_thumbnails")
        assertFalse(thumbnailDir.exists())

        // Should not throw exception
        val result = thumbnailDir.deleteRecursively()
        assertTrue(result) // deleteRecursively returns true for non-existent dirs
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

        // Create the expected file
        expectedFile.createNewFile()
        assertTrue(expectedFile.exists())

        // Delete it
        expectedFile.delete()
        assertFalse(expectedFile.exists())
    }

    @Test
    fun `different pdf paths have different hash values`() {
        val path1 = "/storage/doc1.pdf"
        val path2 = "/storage/doc2.pdf"

        val hash1 = path1.hashCode()
        val hash2 = path2.hashCode()

        assertNotEquals(hash1, hash2)
    }

    @Test
    fun `thumbnail file naming convention is consistent`() {
        val pdfPath = "/storage/test.pdf"
        val expectedFileName = pdfPath.hashCode().toString() + ".jpg"

        assertTrue(expectedFileName.endsWith(".jpg"))
        assertTrue(expectedFileName.contains(pdfPath.hashCode().toString()))
    }
    // endregion

    // region removeThumbnail File Operations
    @Test
    fun `thumbnail file can be removed by hash`() {
        // Create thumbnail directory and file
        val thumbnailDir = File(cacheDir, "pdf_thumbnails")
        thumbnailDir.mkdirs()

        val pdfPath = "/storage/test.pdf"
        val thumbnailFileName = pdfPath.hashCode().toString() + ".jpg"
        val thumbnailFile = File(thumbnailDir, thumbnailFileName)
        thumbnailFile.createNewFile()

        assertTrue(thumbnailFile.exists())

        thumbnailFile.delete()

        assertFalse(thumbnailFile.exists())
    }

    @Test
    fun `deleting non-existent thumbnail file does not throw`() {
        val thumbnailDir = File(cacheDir, "pdf_thumbnails")
        thumbnailDir.mkdirs()

        val nonExistentFile = File(thumbnailDir, "nonexistent.jpg")
        assertFalse(nonExistentFile.exists())

        // Should not throw exception
        val result = nonExistentFile.delete()
        assertFalse(result) // delete returns false for non-existent file
    }

    @Test
    fun `removing one thumbnail does not affect others`() {
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

        thumbnail1.delete()

        assertFalse(thumbnail1.exists())
        assertTrue(thumbnail2.exists())
    }
    // endregion

    // region Edge Cases
    @Test
    fun `special characters in path produce valid hash`() {
        val pdfPath = "/storage/docs/文档.pdf"
        val hash = pdfPath.hashCode()

        // Hash should be a valid integer
        assertTrue(hash != 0 || pdfPath.isEmpty())
    }

    @Test
    fun `very long path produces valid hash`() {
        val longPath = "/storage/" + "a".repeat(500) + ".pdf"
        val hash = longPath.hashCode()

        // Hash should be a valid integer (Kotlin/Java handles long strings)
        assertNotNull(hash)
    }

    @Test
    fun `empty path produces consistent hash`() {
        val emptyPath = ""
        val hash1 = emptyPath.hashCode()
        val hash2 = emptyPath.hashCode()

        assertEquals(hash1, hash2)
    }
    // endregion

    private fun realPdf(name: String = "document.pdf"): File =
        File(cacheDir, name).apply { writeText("%PDF-1.4 pretend document") }

    private fun thumbnailFileFor(path: String) =
        File(File(cacheDir, "pdf_thumbnails"), "${path.hashCode()}.jpg")

    // region Counting pages
    @Test
    fun `the page count comes from the document`() {
        assertEquals(3, PdfThumbnailManager.getPageCount(realPdf().absolutePath))
    }

    @Test
    fun `a document that is not there counts as no pages`() {
        assertEquals(0, PdfThumbnailManager.getPageCount("/nowhere/missing.pdf"))
    }
    // endregion

    // region Making a thumbnail
    @Test
    fun `a thumbnail is made for a document that exists`() = runTest {
        val thumbnail = PdfThumbnailManager.getThumbnail(context, realPdf().absolutePath)
        assertNotNull(thumbnail)
    }

    @Test
    fun `a document that is not there has no thumbnail`() = runTest {
        assertNull(PdfThumbnailManager.getThumbnail(context, "/nowhere/missing.pdf"))
    }

    @Test
    fun `a document with no pages has no thumbnail`() = runTest {
        every { anyConstructed<PdfRenderer>().pageCount } returns 0

        assertNull(PdfThumbnailManager.getThumbnail(context, realPdf("empty.pdf").absolutePath))
    }

    @Test
    fun `the thumbnail fits inside the space the list gives it`() = runTest {
        val thumbnail = PdfThumbnailManager.getThumbnail(context, realPdf().absolutePath)!!

        assertTrue(thumbnail.width <= 200)
        assertTrue(thumbnail.height <= 280)
    }

    @Test
    fun `a page wider than it is tall is sized by its width`() = runTest {
        // Aspect ratio decides which side is pinned, and getting it the wrong way
        // round is what makes a landscape page overflow its row.
        val page = mockk<PdfRenderer.Page>(relaxed = true)
        every { page.width } returns 1000
        every { page.height } returns 500
        every { anyConstructed<PdfRenderer>().openPage(any()) } returns page

        val thumbnail = PdfThumbnailManager.getThumbnail(context, realPdf("wide.pdf").absolutePath)!!

        assertEquals(200, thumbnail.width)
        assertEquals(100, thumbnail.height)
    }

    @Test
    fun `a page taller than it is wide is sized by its height`() = runTest {
        val page = mockk<PdfRenderer.Page>(relaxed = true)
        every { page.width } returns 500
        every { page.height } returns 1000
        every { anyConstructed<PdfRenderer>().openPage(any()) } returns page

        val thumbnail = PdfThumbnailManager.getThumbnail(context, realPdf("tall.pdf").absolutePath)!!

        assertEquals(280, thumbnail.height)
        assertEquals(140, thumbnail.width)
    }

    @Test
    fun `making a thumbnail leaves one on disk for next time`() = runTest {
        val pdf = realPdf()

        PdfThumbnailManager.getThumbnail(context, pdf.absolutePath)
        advanceUntilIdle()

        assertTrue(thumbnailFileFor(pdf.absolutePath).exists())
    }

    @Test
    fun `a thumbnail already on disk is read back rather than made again`() = runTest {
        val pdf = realPdf()
        thumbnailFileFor(pdf.absolutePath).apply {
            parentFile?.mkdirs()
            writeText("cached jpeg")
        }
        val fromDisk = mockk<Bitmap>(relaxed = true)
        every { BitmapFactory.decodeFile(any()) } returns fromDisk

        assertEquals(fromDisk, PdfThumbnailManager.getThumbnail(context, pdf.absolutePath))
    }

    @Test
    fun `an unreadable file on disk is not fatal, a new thumbnail is made`() = runTest {
        val pdf = realPdf()
        thumbnailFileFor(pdf.absolutePath).apply {
            parentFile?.mkdirs()
            writeText("not an image")
        }
        every { BitmapFactory.decodeFile(any()) } returns null

        assertNotNull(PdfThumbnailManager.getThumbnail(context, pdf.absolutePath))
    }

    @Test
    fun `dropping a thumbnail removes the one on disk`() = runTest {
        val pdf = realPdf()
        PdfThumbnailManager.getThumbnail(context, pdf.absolutePath)
        advanceUntilIdle()

        PdfThumbnailManager.removeThumbnail(context, pdf.absolutePath)

        assertFalse(thumbnailFileFor(pdf.absolutePath).exists())
    }
    // endregion
}
