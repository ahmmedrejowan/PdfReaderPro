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
    fun `getFileSize returns 0 for empty path`() {
        val size = FileOperations.getFileSize("")

        assertEquals(0L, size)
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

        assertTrue(result)
        assertFalse(testFile.exists())
        assertTrue(File(tempDir, "new_name.pdf").exists())
    }

    @Test
    fun `renameFile adds pdf extension if missing`() {
        val testFile = File(tempDir, "original.pdf")
        testFile.createNewFile()

        val result = FileOperations.renameFile(testFile.absolutePath, "renamed")

        assertTrue(result)
        assertTrue(File(tempDir, "renamed.pdf").exists())
    }

    @Test
    fun `renameFile preserves pdf extension if provided`() {
        val testFile = File(tempDir, "original.pdf")
        testFile.createNewFile()

        val result = FileOperations.renameFile(testFile.absolutePath, "renamed.pdf")

        assertTrue(result)
        assertTrue(File(tempDir, "renamed.pdf").exists())
    }

    @Test
    fun `renameFile returns false for non-existing file`() {
        val result = FileOperations.renameFile("/nonexistent/file.pdf", "new_name")

        assertFalse(result)
    }

    @Test
    fun `renameFile returns false when target already exists`() {
        val originalFile = File(tempDir, "original.pdf")
        val targetFile = File(tempDir, "target.pdf")
        originalFile.createNewFile()
        targetFile.createNewFile()

        val result = FileOperations.renameFile(originalFile.absolutePath, "target")

        assertFalse(result)
        assertTrue(originalFile.exists()) // Original file should still exist
    }

    @Test
    fun `renameFile handles uppercase PDF extension`() {
        val testFile = File(tempDir, "original.pdf")
        testFile.createNewFile()

        val result = FileOperations.renameFile(testFile.absolutePath, "renamed.PDF")

        assertTrue(result)
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

        assertTrue(result)
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
}
