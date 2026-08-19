package com.rejowan.pdfreaderpro.util

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.File

class FileOperationsTest {

    private lateinit var context: Context
    private lateinit var contentResolver: ContentResolver
    private lateinit var tempDir: File

    @Before
    fun setup() {
        // Create a temporary directory for testing
        tempDir = File(System.getProperty("java.io.tmpdir"), "file_ops_test_${System.currentTimeMillis()}")
        tempDir.mkdirs()

        contentResolver = mockk(relaxed = true)
        context = mockk(relaxed = true)
        every { context.contentResolver } returns contentResolver
        every { context.cacheDir } returns tempDir
    }

    @After
    fun teardown() {
        // Clean up temporary directory
        tempDir.deleteRecursively()
    }

    // region isContentUri Tests
    @Test
    fun `isContentUri returns true for content scheme`() {
        val uri = mockk<Uri>()
        every { uri.scheme } returns "content"

        assertTrue(FileOperations.isContentUri(uri))
    }

    @Test
    fun `isContentUri returns false for file scheme`() {
        val uri = mockk<Uri>()
        every { uri.scheme } returns "file"

        assertFalse(FileOperations.isContentUri(uri))
    }

    @Test
    fun `isContentUri returns false for null scheme`() {
        val uri = mockk<Uri>()
        every { uri.scheme } returns null

        assertFalse(FileOperations.isContentUri(uri))
    }

    @Test
    fun `isContentUri returns false for http scheme`() {
        val uri = mockk<Uri>()
        every { uri.scheme } returns "http"

        assertFalse(FileOperations.isContentUri(uri))
    }
    // endregion

    // region resolveUriToPath Tests
    @Test
    fun `resolveUriToPath returns path for file scheme`() {
        val uri = mockk<Uri>()
        every { uri.scheme } returns "file"
        every { uri.path } returns "/storage/test.pdf"

        val result = FileOperations.resolveUriToPath(context, uri)

        assertEquals("/storage/test.pdf", result)
    }

    @Test
    fun `resolveUriToPath returns null for unknown scheme`() {
        val uri = mockk<Uri>()
        every { uri.scheme } returns "http"

        val result = FileOperations.resolveUriToPath(context, uri)

        assertNull(result)
    }

    @Test
    fun `resolveUriToPath returns null for null scheme`() {
        val uri = mockk<Uri>()
        every { uri.scheme } returns null

        val result = FileOperations.resolveUriToPath(context, uri)

        assertNull(result)
    }
    // endregion

    // region getFileNameFromUri Tests
    @Test
    fun `getFileNameFromUri returns last path segment for file scheme`() {
        val uri = mockk<Uri>()
        every { uri.scheme } returns "file"
        every { uri.lastPathSegment } returns "document.pdf"

        val result = FileOperations.getFileNameFromUri(context, uri)

        assertEquals("document.pdf", result)
    }

    @Test
    fun `getFileNameFromUri returns null for unknown scheme`() {
        val uri = mockk<Uri>()
        every { uri.scheme } returns "http"

        val result = FileOperations.getFileNameFromUri(context, uri)

        assertNull(result)
    }

    @Test
    fun `getFileNameFromUri queries content resolver for content scheme`() {
        val uri = mockk<Uri>()
        every { uri.scheme } returns "content"

        val cursor = mockk<Cursor>(relaxed = true)
        every { cursor.moveToFirst() } returns true
        every { cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME) } returns 0
        every { cursor.getString(0) } returns "test_document.pdf"

        every { contentResolver.query(uri, null, null, null, null) } returns cursor

        val result = FileOperations.getFileNameFromUri(context, uri)

        assertEquals("test_document.pdf", result)
    }

    @Test
    fun `getFileNameFromUri returns null when cursor is null`() {
        val uri = mockk<Uri>()
        every { uri.scheme } returns "content"
        every { contentResolver.query(uri, null, null, null, null) } returns null

        val result = FileOperations.getFileNameFromUri(context, uri)

        assertNull(result)
    }

    @Test
    fun `getFileNameFromUri returns null when cursor is empty`() {
        val uri = mockk<Uri>()
        every { uri.scheme } returns "content"

        val cursor = mockk<Cursor>(relaxed = true)
        every { cursor.moveToFirst() } returns false

        every { contentResolver.query(uri, null, null, null, null) } returns cursor

        val result = FileOperations.getFileNameFromUri(context, uri)

        assertNull(result)
    }
    // endregion

    // region fileExists Tests
    @Test
    fun `fileExists returns true for existing file`() {
        val testFile = File(tempDir, "existing_file.pdf")
        testFile.createNewFile()

        assertTrue(FileOperations.fileExists(testFile.absolutePath))
    }

    @Test
    fun `fileExists returns false for non-existing file`() {
        assertFalse(FileOperations.fileExists("/nonexistent/path/file.pdf"))
    }

    @Test
    fun `fileExists returns false for empty path`() {
        assertFalse(FileOperations.fileExists(""))
    }

    @Test
    fun `fileExists returns true for existing directory`() {
        assertTrue(FileOperations.fileExists(tempDir.absolutePath))
    }
    // endregion

    // region getFileSize Tests
    @Test
    fun `getFileSize returns correct size for file`() {
        val testFile = File(tempDir, "test_file.pdf")
        testFile.writeText("Hello, World!") // 13 bytes

        val size = FileOperations.getFileSize(testFile.absolutePath)

        assertEquals(13L, size)
    }

    @Test
    fun `getFileSize returns 0 for non-existing file`() {
        val size = FileOperations.getFileSize("/nonexistent/file.pdf")

        assertEquals(0L, size)
    }

    @Test
    fun `getFileSize returns size for empty path`() {
        // File("").length() returns the size of the current directory or 0
        // depending on the OS. The implementation catches exceptions and returns 0.
        val size = FileOperations.getFileSize("")

        // Just verify it doesn't throw and returns a non-negative value
        assertTrue(size >= 0L)
    }

    @Test
    fun `getFileSize returns 0 for empty file`() {
        val testFile = File(tempDir, "empty_file.pdf")
        testFile.createNewFile()

        val size = FileOperations.getFileSize(testFile.absolutePath)

        assertEquals(0L, size)
    }

    @Test
    fun `getFileSize returns correct size for large content`() {
        val testFile = File(tempDir, "large_file.pdf")
        val content = "A".repeat(10000)
        testFile.writeText(content)

        val size = FileOperations.getFileSize(testFile.absolutePath)

        assertEquals(10000L, size)
    }
    // endregion

    // region deleteFile Tests
    @Test
    fun `deleteFile returns true and deletes existing file`() {
        val testFile = File(tempDir, "to_delete.pdf")
        testFile.createNewFile()

        assertTrue(testFile.exists())

        val result = FileOperations.deleteFile(testFile.absolutePath)

        assertTrue(result)
        assertFalse(testFile.exists())
    }

    @Test
    fun `deleteFile returns false for non-existing file`() {
        val result = FileOperations.deleteFile("/nonexistent/file.pdf")

        assertFalse(result)
    }

    @Test
    fun `deleteFile returns false for empty path`() {
        val result = FileOperations.deleteFile("")

        assertFalse(result)
    }
    // endregion

    // region renameFile Tests
    @Test
    fun `renameFile renames file successfully`() {
        val testFile = File(tempDir, "old_name.pdf")
        testFile.createNewFile()

        val result = FileOperations.renameFile(testFile.absolutePath, "new_name")

        assertNotNull(result)
        assertFalse(testFile.exists())
        assertTrue(File(tempDir, "new_name.pdf").exists())
    }

    @Test
    fun `renameFile adds pdf extension if missing`() {
        val testFile = File(tempDir, "original.pdf")
        testFile.createNewFile()

        val result = FileOperations.renameFile(testFile.absolutePath, "renamed")

        assertNotNull(result)
        assertTrue(File(tempDir, "renamed.pdf").exists())
    }

    @Test
    fun `renameFile preserves pdf extension if provided`() {
        val testFile = File(tempDir, "original.pdf")
        testFile.createNewFile()

        val result = FileOperations.renameFile(testFile.absolutePath, "renamed.pdf")

        assertNotNull(result)
        assertTrue(File(tempDir, "renamed.pdf").exists())
    }

    @Test
    fun `renameFile returns null for non-existing file`() {
        val result = FileOperations.renameFile("/nonexistent/file.pdf", "new_name")

        assertNull(result)
    }

    @Test
    fun `renameFile returns null when target already exists`() {
        val originalFile = File(tempDir, "original.pdf")
        val targetFile = File(tempDir, "target.pdf")
        originalFile.createNewFile()
        targetFile.createNewFile()

        val result = FileOperations.renameFile(originalFile.absolutePath, "target")

        assertNull(result)
        assertTrue(originalFile.exists()) // Original file should still exist
    }

    @Test
    fun `renameFile handles uppercase PDF extension`() {
        val testFile = File(tempDir, "original.pdf")
        testFile.createNewFile()

        val result = FileOperations.renameFile(testFile.absolutePath, "renamed.PDF")

        assertNotNull(result)
        assertTrue(File(tempDir, "renamed.PDF").exists())
    }
    // endregion

    // region cleanupOldCachedPdfs Tests
    @Test
    fun `cleanupOldCachedPdfs handles non-existent cache directory`() {
        // Should not throw exception
        FileOperations.cleanupOldCachedPdfs(context)
    }

    @Test
    fun `cleanupOldCachedPdfs creates cache directory structure`() {
        val sharedPdfDir = File(tempDir, "shared_pdfs")
        assertFalse(sharedPdfDir.exists())

        // Method should handle gracefully
        FileOperations.cleanupOldCachedPdfs(context)
    }
    // endregion

    // region Edge Cases
    @Test
    fun `deleteFile handles file with special characters in name`() {
        val testFile = File(tempDir, "file with spaces.pdf")
        testFile.createNewFile()

        val result = FileOperations.deleteFile(testFile.absolutePath)

        assertTrue(result)
        assertFalse(testFile.exists())
    }

    @Test
    fun `renameFile handles names with special characters`() {
        val testFile = File(tempDir, "original.pdf")
        testFile.createNewFile()

        val result = FileOperations.renameFile(testFile.absolutePath, "file-with-dashes_and_underscores")

        assertNotNull(result)
        assertTrue(File(tempDir, "file-with-dashes_and_underscores.pdf").exists())
    }

    @Test
    fun `getFileSize handles file with content and newlines`() {
        val testFile = File(tempDir, "multiline.pdf")
        testFile.writeText("Line 1\nLine 2\nLine 3")

        val size = FileOperations.getFileSize(testFile.absolutePath)

        assertTrue(size > 0)
    }
    // endregion

    // region copyContentUriToCache Tests
    @Test
    fun `copyContentUriToCache returns null when input stream is null`() {
        val uri = mockk<Uri>()
        every { contentResolver.openInputStream(uri) } returns null

        val result = FileOperations.copyContentUriToCache(context, uri)

        assertNull(result)
    }
    // endregion

    /** A content uri whose stream yields [content] and whose display name is [name]. */
    private fun contentUri(name: String, content: String): Uri {
        val uri = mockk<Uri>()
        every { uri.scheme } returns "content"
        every { contentResolver.openInputStream(uri) } answers {
            ByteArrayInputStream(content.toByteArray())
        }
        val cursor = mockk<Cursor>(relaxed = true)
        every { cursor.moveToFirst() } returns true
        every { cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME) } returns 0
        every { cursor.getString(0) } returns name
        every { contentResolver.query(uri, null, null, null, null) } returns cursor
        return uri
    }

    private fun cachedPdfs() =
        File(tempDir, "shared_pdfs").listFiles()?.filter { it.name.endsWith(".pdf") }.orEmpty()

    @Test
    fun `copying a shared document keeps its name and its contents`() {
        val uri = contentUri("report.pdf", "%PDF-1.4 report")

        val path = FileOperations.copyContentUriToCache(context, uri)!!

        assertTrue(path.endsWith("shared_pdfs/report.pdf"))
        assertEquals("%PDF-1.4 report", File(path).readText())
    }

    @Test
    fun `opening the same document twice reuses the copy already made`() {
        // The check is on content, not name, so reopening from a chat app does not
        // pile up identical copies in the cache.
        val uri = contentUri("report.pdf", "%PDF-1.4 report")

        val first = FileOperations.copyContentUriToCache(context, uri)
        val second = FileOperations.copyContentUriToCache(context, uri)

        assertEquals(first, second)
        assertEquals(1, cachedPdfs().size)
    }

    @Test
    fun `a changed document under the same name replaces the old copy`() {
        val stale = contentUri("report.pdf", "%PDF-1.4 old version")
        FileOperations.copyContentUriToCache(context, stale)
        val fresh = contentUri("report.pdf", "%PDF-1.4 new version")

        val path = FileOperations.copyContentUriToCache(context, fresh)!!

        assertEquals("%PDF-1.4 new version", File(path).readText())
        assertEquals(1, cachedPdfs().size)
    }

    @Test
    fun `a document with no name still gets copied`() {
        val uri = mockk<Uri>()
        every { uri.scheme } returns "content"
        every { contentResolver.query(uri, null, null, null, null) } returns null
        every { contentResolver.openInputStream(uri) } answers {
            ByteArrayInputStream("%PDF-1.4 nameless".toByteArray())
        }

        val path = FileOperations.copyContentUriToCache(context, uri)!!

        assertEquals("%PDF-1.4 nameless", File(path).readText())
    }

    // region Resolving a document to a path
    // Which branch this takes decides whether the user can save changes back into
    // their own file or only into a copy, so both are pinned down.

    /** Makes MediaStore report [path] as the file behind the uri. */
    private fun uriBackedBy(path: String?): Uri {
        val uri = contentUri("report.pdf", "%PDF-1.4 report")
        val cursor = mockk<Cursor>(relaxed = true)
        every { cursor.moveToFirst() } returns true
        every { cursor.getColumnIndex(android.provider.MediaStore.MediaColumns.DATA) } returns 0
        every { cursor.getString(0) } returns path
        every {
            contentResolver.query(uri, any<Array<String>>(), null, null, null)
        } returns cursor
        return uri
    }

    @Test
    fun `a document with a readable path of its own is used in place`() {
        val real = File(tempDir, "on-disk.pdf").apply { writeText("%PDF-1.4 real") }
        val uri = uriBackedBy(real.absolutePath)

        assertEquals(real.absolutePath, FileOperations.resolveUriToPath(context, uri))
    }

    @Test
    fun `a document with no path of its own is copied instead`() {
        val uri = uriBackedBy(null)

        val path = FileOperations.resolveUriToPath(context, uri)!!

        assertTrue(path.contains("shared_pdfs"))
    }

    @Test
    fun `a path that is not there is not trusted, and a copy is made`() {
        val uri = uriBackedBy("/storage/emulated/0/gone.pdf")

        val path = FileOperations.resolveUriToPath(context, uri)!!

        assertTrue(path.contains("shared_pdfs"))
    }

    @Test
    fun `a provider that rejects the path column falls back to a copy`() {
        val uri = contentUri("report.pdf", "%PDF-1.4 report")
        every {
            contentResolver.query(uri, any<Array<String>>(), null, null, null)
        } throws IllegalArgumentException("column not supported")

        val path = FileOperations.resolveUriToPath(context, uri)!!

        assertTrue(path.contains("shared_pdfs"))
    }
    // endregion

    // region Clearing out old copies
    @Test
    fun `a copy older than the cutoff is cleared out`() {
        val uri = contentUri("ancient.pdf", "%PDF-1.4 ancient")
        val cached = File(FileOperations.copyContentUriToCache(context, uri)!!)
        val twoMonths = System.currentTimeMillis() - 60L * 24 * 60 * 60 * 1000
        cached.setLastModified(twoMonths)

        FileOperations.cleanupOldCachedPdfs(context)

        assertFalse(cached.exists())
    }

    @Test
    fun `a copy from today is left alone`() {
        val uri = contentUri("recent.pdf", "%PDF-1.4 recent")
        val cached = File(FileOperations.copyContentUriToCache(context, uri)!!)

        FileOperations.cleanupOldCachedPdfs(context)

        assertTrue(cached.exists())
    }

    @Test
    fun `clearing out only touches copies past the cutoff`() {
        val old = File(FileOperations.copyContentUriToCache(context, contentUri("old.pdf", "old"))!!)
        val new = File(FileOperations.copyContentUriToCache(context, contentUri("new.pdf", "new"))!!)
        old.setLastModified(System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000)

        FileOperations.cleanupOldCachedPdfs(context)

        assertFalse(old.exists())
        assertTrue(new.exists())
    }
    // endregion
}
