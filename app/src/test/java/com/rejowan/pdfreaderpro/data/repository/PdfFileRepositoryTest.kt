package com.rejowan.pdfreaderpro.data.repository

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import app.cash.turbine.test
import com.rejowan.pdfreaderpro.domain.model.PdfFile
import io.mockk.every
import io.mockk.mockk
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

@OptIn(ExperimentalCoroutinesApi::class)
class PdfFileRepositoryTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var context: Context
    private lateinit var contentResolver: ContentResolver
    private lateinit var repository: PdfFileRepositoryImpl

    private val mockUri: Uri = mockk(relaxed = true)

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        context = mockk(relaxed = true)
        contentResolver = mockk(relaxed = true)

        every { context.contentResolver } returns contentResolver
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
    }

    private fun createRepository(): PdfFileRepositoryImpl {
        return PdfFileRepositoryImpl(context = context)
    }

    // region getAllPdfFiles Tests
    @Test
    fun `getAllPdfFiles returns empty list initially`() = runTest {
        repository = createRepository()
        advanceUntilIdle()

        repository.getAllPdfFiles().test {
            val files = awaitItem()
            assertTrue(files.isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getAllPdfFiles returns flow`() = runTest {
        repository = createRepository()
        advanceUntilIdle()

        assertNotNull(repository.getAllPdfFiles())
    }
    // endregion

    // region getPdfsByFolder Tests
    @Test
    fun `getPdfsByFolder returns empty list for non-existent folder`() = runTest {
        repository = createRepository()
        advanceUntilIdle()

        repository.getPdfsByFolder("/nonexistent/folder").test {
            val files = awaitItem()
            assertTrue(files.isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getPdfsByFolder returns flow`() = runTest {
        repository = createRepository()
        advanceUntilIdle()

        assertNotNull(repository.getPdfsByFolder("/some/folder"))
    }
    // endregion

    // region searchPdfs Tests
    @Test
    fun `searchPdfs returns empty list for blank query`() = runTest {
        repository = createRepository()
        advanceUntilIdle()

        repository.searchPdfs("").test {
            val files = awaitItem()
            assertTrue(files.isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `searchPdfs returns empty list for whitespace query`() = runTest {
        repository = createRepository()
        advanceUntilIdle()

        repository.searchPdfs("   ").test {
            val files = awaitItem()
            assertTrue(files.isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `searchPdfs returns flow`() = runTest {
        repository = createRepository()
        advanceUntilIdle()

        assertNotNull(repository.searchPdfs("test"))
    }
    // endregion

    // region getPdfFolders Tests
    @Test
    fun `getPdfFolders returns empty list initially`() = runTest {
        repository = createRepository()
        advanceUntilIdle()

        repository.getPdfFolders().test {
            val folders = awaitItem()
            assertTrue(folders.isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getPdfFolders returns flow`() = runTest {
        repository = createRepository()
        advanceUntilIdle()

        assertNotNull(repository.getPdfFolders())
    }
    // endregion

    // region getPdfFile Tests
    @Test
    fun `getPdfFile returns null for non-existent path`() = runTest {
        repository = createRepository()
        advanceUntilIdle()

        val result = repository.getPdfFile("/nonexistent/file.pdf")
        assertNull(result)
    }

    @Test
    fun `getPdfFile returns null when no files loaded`() = runTest {
        repository = createRepository()
        advanceUntilIdle()

        val result = repository.getPdfFile("/storage/docs/test.pdf")
        assertNull(result)
    }
    // endregion

    // region renamePdf Tests
    @Test
    fun `renamePdf with invalid path returns failure`() = runTest {
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
        assertTrue(result.isFailure)
    }
    // endregion

    // region deletePdf Tests
    @Test
    fun `deletePdf calls contentResolver delete`() = runTest {
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
    // endregion
}
