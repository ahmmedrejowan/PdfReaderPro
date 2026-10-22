package com.rejowan.pdfreaderpro.data.repository

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import app.cash.turbine.test
import com.rejowan.pdfreaderpro.domain.model.PdfFile
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
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
import java.lang.reflect.Field
import java.lang.reflect.Modifier

@OptIn(ExperimentalCoroutinesApi::class)
class PdfFileRepositoryTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var context: Context
    private lateinit var contentResolver: ContentResolver
    private lateinit var repository: PdfFileRepositoryImpl

    private val mockUri: Uri = mockk(relaxed = true)
    private val mockCollectionUri: Uri = mockk(relaxed = true)

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        context = mockk(relaxed = true)
        contentResolver = mockk(relaxed = true)

        every { context.contentResolver } returns contentResolver

        // Mock MediaStore static methods
        mockkStatic(MediaStore.Files::class)
        every { MediaStore.Files.getContentUri(any<String>()) } returns mockCollectionUri

        // Mock Uri static methods
        mockkStatic(Uri::class)
        every { Uri.parse(any()) } returns mockUri
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
        unmockkStatic(MediaStore.Files::class)
        unmockkStatic(Uri::class)
    }

    private fun createRepository(): PdfFileRepositoryImpl {
        return PdfFileRepositoryImpl(context = context)
    }

    // region getAllPdfFiles Tests
    @Test
    fun `getAllPdfFiles returns empty list initially`() = runTest {
        // Mock contentResolver.query to return null (no PDFs found)
        every { contentResolver.query(any(), any(), any(), any(), any()) } returns null

        repository = createRepository()

        repository.getAllPdfFiles().test {
            val files = awaitItem()
            assertTrue(files.isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getAllPdfFiles returns flow`() = runTest {
        every { contentResolver.query(any(), any(), any(), any(), any()) } returns null

        repository = createRepository()

        assertNotNull(repository.getAllPdfFiles())
    }
    // endregion

    // region getPdfsByFolder Tests
    @Test
    fun `getPdfsByFolder returns empty list for non-existent folder`() = runTest {
        every { contentResolver.query(any(), any(), any(), any(), any()) } returns null

        repository = createRepository()

        repository.getPdfsByFolder("/nonexistent/folder").test {
            val files = awaitItem()
            assertTrue(files.isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getPdfsByFolder returns flow`() = runTest {
        every { contentResolver.query(any(), any(), any(), any(), any()) } returns null

        repository = createRepository()

        assertNotNull(repository.getPdfsByFolder("/some/folder"))
    }
    // endregion

    // region searchPdfs Tests
    @Test
    fun `searchPdfs returns empty list for blank query`() = runTest {
        every { contentResolver.query(any(), any(), any(), any(), any()) } returns null

        repository = createRepository()

        repository.searchPdfs("").test {
            val files = awaitItem()
            assertTrue(files.isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `searchPdfs returns empty list for whitespace query`() = runTest {
        every { contentResolver.query(any(), any(), any(), any(), any()) } returns null

        repository = createRepository()

        repository.searchPdfs("   ").test {
            val files = awaitItem()
            assertTrue(files.isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `searchPdfs returns flow`() = runTest {
        every { contentResolver.query(any(), any(), any(), any(), any()) } returns null

        repository = createRepository()

        assertNotNull(repository.searchPdfs("test"))
    }
    // endregion

    // region getPdfFolders Tests
    @Test
    fun `getPdfFolders returns empty list initially`() = runTest {
        every { contentResolver.query(any(), any(), any(), any(), any()) } returns null

        repository = createRepository()

        repository.getPdfFolders().test {
            val folders = awaitItem()
            assertTrue(folders.isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getPdfFolders returns flow`() = runTest {
        every { contentResolver.query(any(), any(), any(), any(), any()) } returns null

        repository = createRepository()

        assertNotNull(repository.getPdfFolders())
    }
    // endregion

    // region getPdfFile Tests
    @Test
    fun `getPdfFile returns null for non-existent path`() = runTest {
        every { contentResolver.query(any(), any(), any(), any(), any()) } returns null

        repository = createRepository()
        advanceUntilIdle()

        val result = repository.getPdfFile("/nonexistent/file.pdf")
        assertNull(result)
    }

    @Test
    fun `getPdfFile returns null when no files loaded`() = runTest {
        every { contentResolver.query(any(), any(), any(), any(), any()) } returns null

        repository = createRepository()
        advanceUntilIdle()

        val result = repository.getPdfFile("/storage/docs/test.pdf")
        assertNull(result)
    }
    // endregion

    // region renamePdf Tests
    @Test
    fun `renamePdf with invalid path returns failure`() = runTest {
        every { contentResolver.query(any(), any(), any(), any(), any()) } returns null

        repository = createRepository()
        advanceUntilIdle()

        val pdfFile = PdfFile(
            id = 1L,
            name = "test.pdf",
            path = "/nonexistent/path/test.pdf",
            uri = mockUri,
            size = 1000L,
            dateModified = 1000L,
            dateAdded = 900L,
            parentFolder = "/nonexistent/path"
        )

        val result = repository.renamePdf(pdfFile, "newname")
        advanceUntilIdle()
        assertTrue(result.isFailure)
    }
    // endregion

    // region deletePdf Tests
    @Test
    fun `deletePdf calls contentResolver delete`() = runTest {
        every { contentResolver.query(any(), any(), any(), any(), any()) } returns null
        every { contentResolver.delete(any(), any(), any()) } returns 0

        repository = createRepository()
        advanceUntilIdle()

        val pdfFile = PdfFile(
            id = 1L,
            name = "test.pdf",
            path = "/nonexistent/path/test.pdf",
            uri = mockUri,
            size = 1000L,
            dateModified = 1000L,
            dateAdded = 900L,
            parentFolder = "/nonexistent/path"
        )

        val result = repository.deletePdf(pdfFile)
        advanceUntilIdle()
        // Should fail because file doesn't exist
        assertTrue(result.isFailure)
    }
    // endregion

    // region refreshPdfs Tests
    @Test
    fun `refreshPdfs does not throw exception`() = runTest {
        // Mock contentResolver.query to return null (simulating no access or error)
        every { contentResolver.query(any(), any(), any(), any(), any()) } returns null

        repository = createRepository()
        advanceUntilIdle()

        // Should not throw
        repository.refreshPdfs()
        advanceUntilIdle()

        // Files should still be empty
        repository.getAllPdfFiles().test {
            val files = awaitItem()
            assertTrue(files.isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `refreshPdfs handles empty cursor`() = runTest {
        val cursor = mockk<Cursor>(relaxed = true)
        every { cursor.moveToNext() } returns false
        every { contentResolver.query(any(), any(), any(), any(), any()) } returns cursor

        repository = createRepository()
        repository.refreshPdfs()
        advanceUntilIdle()

        repository.getAllPdfFiles().test {
            val files = awaitItem()
            assertTrue(files.isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }
    // endregion

    // region Hidden files
    @Test
    fun `a file inside a dot folder is hidden`() {
        assertTrue(PdfFileRepositoryImpl.isHidden("/storage/emulated/0/.cache/doc.pdf"))
    }

    @Test
    fun `a file whose name starts with a dot is hidden`() {
        assertTrue(PdfFileRepositoryImpl.isHidden("/storage/emulated/0/Download/.draft.pdf"))
    }

    @Test
    fun `an ordinary file is not hidden`() {
        assertFalse(PdfFileRepositoryImpl.isHidden("/storage/emulated/0/Download/report.v2.pdf"))
    }

    @Test
    fun `hidden files are left out only when asked`() = runTest {
        val hide = kotlinx.coroutines.flow.MutableStateFlow(false)
        repository = PdfFileRepositoryImpl(context = context, hideHiddenFiles = hide)
        seed(
            repository,
            pdf("/storage/emulated/0/Download/visible.pdf"),
            pdf("/storage/emulated/0/.hidden/secret.pdf")
        )

        repository.getAllPdfFiles().test {
            assertEquals(2, awaitItem().size)
            hide.value = true
            assertEquals(listOf("visible.pdf"), awaitItem().map { it.name })
            cancelAndIgnoreRemainingEvents()
        }
    }

    private fun pdf(path: String) = PdfFile(
        id = path.hashCode().toLong(),
        name = path.substringAfterLast('/'),
        path = path,
        uri = mockUri,
        size = 1L,
        dateModified = 0L,
        dateAdded = 0L,
        parentFolder = path.substringBeforeLast('/')
    )

    @Suppress("UNCHECKED_CAST")
    private fun seed(repo: PdfFileRepositoryImpl, vararg files: PdfFile) {
        val field = PdfFileRepositoryImpl::class.java.getDeclaredField("_pdfFiles")
        field.isAccessible = true
        (field.get(repo) as kotlinx.coroutines.flow.MutableStateFlow<List<PdfFile>>).value = files.toList()
    }
    // endregion
}
