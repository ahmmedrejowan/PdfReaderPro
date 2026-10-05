package com.rejowan.pdfreaderpro.presentation.viewmodel.tools

import android.app.Application
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.Environment
import android.os.ParcelFileDescriptor
import app.cash.turbine.test
import com.rejowan.pdfreaderpro.R
import com.rejowan.pdfreaderpro.domain.repository.PdfToolsRepository
import com.rejowan.pdfreaderpro.presentation.screens.tools.reorder.PageItem
import com.rejowan.pdfreaderpro.presentation.screens.tools.reorder.ReorderViewModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkConstructor
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
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import kotlinx.coroutines.test.TestScope
import java.io.ByteArrayInputStream
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
class ReorderViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var pdfToolsRepository: PdfToolsRepository
    private lateinit var context: Application
    private lateinit var viewModel: ReorderViewModel

    @get:Rule
    val folder = TemporaryFolder()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        pdfToolsRepository = mockk(relaxed = true)
        context = mockk(relaxed = true)

        coEvery { pdfToolsRepository.getPageCount(any()) } returns Result.success(10)

        every { context.cacheDir } returns folder.newFolder("cache")
        every { context.contentResolver.openInputStream(any()) } answers {
            ByteArrayInputStream("%PDF-1.4 pretend document".toByteArray())
        }
        every { context.contentResolver.query(any(), any(), any(), any(), any()) } returns null

        mockkStatic(Environment::class)
        every {
            Environment.getExternalStoragePublicDirectory(any())
        } returns folder.newFolder("documents")

        // Thumbnails come from the platform renderer, which has no JVM
        // implementation. Standing it up is what makes the page list, and so
        // everything reordering does to it, reachable from a unit test.
        mockkStatic(ParcelFileDescriptor::class)
        every { ParcelFileDescriptor.open(any(), any()) } returns mockk(relaxed = true)
        mockkConstructor(PdfRenderer::class)
        val page = mockk<PdfRenderer.Page>(relaxed = true)
        every { page.width } returns 600
        every { page.height } returns 800
        every { anyConstructed<PdfRenderer>().openPage(any()) } returns page
        mockkStatic(Bitmap::class)
        every {
            Bitmap.createBitmap(any<Int>(), any<Int>(), any())
        } returns mockk(relaxed = true)
    }

    @After
    fun teardown() {
        unmockkStatic(Environment::class)
        unmockkStatic(ParcelFileDescriptor::class)
        unmockkStatic(Bitmap::class)
        unmockkConstructor(PdfRenderer::class)
        Dispatchers.resetMain()
    }

    private fun createViewModel(): ReorderViewModel {
        return ReorderViewModel(
            pdfToolsRepository = pdfToolsRepository,
            context = context
        )
    }

    // region Initial State Tests
    @Test
    fun `initial state has no source file`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.state.test {
            val state = awaitItem()
            assertNull(state.sourceFile)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `initial state has empty pages list`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.state.test {
            val state = awaitItem()
            assertTrue(state.pages.isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `initial state has empty output filename`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.state.test {
            val state = awaitItem()
            assertEquals("", state.outputFileName)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `initial state is not loading`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.state.test {
            val state = awaitItem()
            assertFalse(state.isLoading)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `initial state is not processing`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.state.test {
            val state = awaitItem()
            assertFalse(state.isProcessing)
            assertEquals(0f, state.progress)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `initial state has no error`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.state.test {
            val state = awaitItem()
            assertNull(state.error)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `initial state has no result`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.state.test {
            val state = awaitItem()
            assertNull(state.result)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `initial state has no changes`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.state.test {
            val state = awaitItem()
            assertFalse(state.hasChanges)
            cancelAndIgnoreRemainingEvents()
        }
    }
    // endregion

    // region setOutputFileName Tests
    @Test
    fun `setOutputFileName updates filename`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.setOutputFileName("my_reordered_file")
        advanceUntilIdle()

        viewModel.state.test {
            val state = awaitItem()
            assertEquals("my_reordered_file", state.outputFileName)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `setOutputFileName with empty string updates state`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.setOutputFileName("")
        advanceUntilIdle()

        viewModel.state.test {
            val state = awaitItem()
            assertEquals("", state.outputFileName)
            cancelAndIgnoreRemainingEvents()
        }
    }
    // endregion

    // region reorder Validation Tests
    @Test
    fun `reorder without source file sets error`() = runTest {
        every { context.getString(R.string.error_select_pdf_first) } returns "Please select a PDF file first"
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.reorder()
        advanceUntilIdle()

        viewModel.state.test {
            val state = awaitItem()
            assertEquals("Please select a PDF file first", state.error)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `reorder with blank output filename sets error`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.setOutputFileName("")
        viewModel.reorder()
        advanceUntilIdle()

        viewModel.state.test {
            val state = awaitItem()
            assertNotNull(state.error)
            cancelAndIgnoreRemainingEvents()
        }
    }
    // endregion

    // region clearResult Tests
    @Test
    fun `clearResult sets result to null`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.clearResult()
        advanceUntilIdle()

        viewModel.state.test {
            val state = awaitItem()
            assertNull(state.result)
            cancelAndIgnoreRemainingEvents()
        }
    }
    // endregion

    // region clearError Tests
    @Test
    fun `clearError sets error to null`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.clearError()
        advanceUntilIdle()

        viewModel.state.test {
            val state = awaitItem()
            assertNull(state.error)
            cancelAndIgnoreRemainingEvents()
        }
    }
    // endregion

    // region reset Tests
    @Test
    fun `reset clears all state`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.setOutputFileName("custom_name")
        viewModel.reset()
        advanceUntilIdle()

        viewModel.state.test {
            val state = awaitItem()
            assertNull(state.sourceFile)
            assertTrue(state.pages.isEmpty())
            assertEquals("", state.outputFileName)
            assertFalse(state.isLoading)
            assertFalse(state.isProcessing)
            assertEquals(0f, state.progress)
            assertNull(state.error)
            assertNull(state.result)
            assertFalse(state.hasChanges)
            cancelAndIgnoreRemainingEvents()
        }
    }
    // endregion

    // region PageItem Tests
    @Test
    fun `PageItem has correct properties`() {
        val pageItem = PageItem(
            originalIndex = 0,
            pageNumber = 1,
            thumbnail = null
        )

        assertEquals(0, pageItem.originalIndex)
        assertEquals(1, pageItem.pageNumber)
        assertNull(pageItem.thumbnail)
    }

    @Test
    fun `PageItem originalIndex is 0-based`() {
        val pageItem = PageItem(
            originalIndex = 5,
            pageNumber = 6,
            thumbnail = null
        )

        assertEquals(5, pageItem.originalIndex)
        assertEquals(6, pageItem.pageNumber)
    }
    // endregion

    /** setSourceFile is async, so wait for the pages to actually arrive. */
    private fun TestScope.loadDocument(vm: ReorderViewModel): ReorderViewModel {
        vm.setSourceFile(mockk(relaxed = true))
        repeat(200) {
            advanceUntilIdle()
            if (vm.state.value.pages.isNotEmpty()) return vm
            Thread.sleep(10)
        }
        error("document never loaded")
    }

    private fun order(vm: ReorderViewModel) = vm.state.value.pages.map { it.pageNumber }

    // region Loading a document
    @Test
    fun `loading lists one page per page in the document`() = runTest {
        val vm = loadDocument(createViewModel())

        assertEquals(10, vm.state.value.pages.size)
        assertEquals((1..10).toList(), order(vm))
        assertFalse(vm.state.value.isLoading)
    }

    @Test
    fun `pages start in their original order, with nothing to save yet`() = runTest {
        val vm = loadDocument(createViewModel())

        assertEquals((0..9).toList(), vm.state.value.pages.map { it.originalIndex })
        assertFalse(vm.state.value.hasChanges)
    }

    @Test
    fun `the suggested name marks the file as reordered`() = runTest {
        val vm = loadDocument(createViewModel())
        assertTrue(vm.state.value.outputFileName.endsWith("_reordered"))
    }

    @Test
    fun `a document that cannot be read is reported rather than left blank`() = runTest {
        every { context.contentResolver.openInputStream(any()) } returns null
        val vm = createViewModel()

        vm.setSourceFile(mockk(relaxed = true))
        advanceUntilIdle()

        assertNotNull(vm.state.value.error)
        assertFalse(vm.state.value.isLoading)
        assertNull(vm.state.value.sourceFile)
    }

    @Test
    fun `only the first hundred pages are listed`() = runTest {
        // The renderer loop is capped, and a huge document must not be a hang.
        coEvery { pdfToolsRepository.getPageCount(any()) } returns Result.success(500)
        val vm = loadDocument(createViewModel())

        assertEquals(100, vm.state.value.pages.size)
    }
    // endregion

    // region Moving pages around
    @Test
    fun `moving a page to the front puts it there and leaves the rest in order`() = runTest {
        val vm = loadDocument(createViewModel())

        vm.movePage(4, 0)

        assertEquals(listOf(5, 1, 2, 3, 4, 6, 7, 8, 9, 10), order(vm))
    }

    @Test
    fun `moving a page marks the document as changed`() = runTest {
        val vm = loadDocument(createViewModel())

        vm.movePage(0, 3)

        assertTrue(vm.state.value.hasChanges)
    }

    @Test
    fun `moving a page back where it came from is not a change`() = runTest {
        // hasChanges is compared against the original order, not counted, so
        // undoing by hand has to leave the document clean.
        val vm = loadDocument(createViewModel())

        vm.movePage(2, 7)
        vm.movePage(7, 2)

        assertEquals((1..10).toList(), order(vm))
        assertFalse(vm.state.value.hasChanges)
    }

    @Test
    fun `resetting puts every page back in its original place`() = runTest {
        val vm = loadDocument(createViewModel())
        vm.movePage(0, 9)
        vm.movePage(3, 1)

        vm.resetOrder()

        assertEquals((1..10).toList(), order(vm))
        assertFalse(vm.state.value.hasChanges)
    }
    // endregion

    // region Refusing to reorder
    @Test
    fun `reordering without a document asks for one`() = runTest {
        val vm = createViewModel()

        vm.reorder()
        advanceUntilIdle()

        assertNotNull(vm.state.value.error)
        coVerify(exactly = 0) { pdfToolsRepository.reorderPages(any(), any(), any(), any()) }
    }

    @Test
    fun `reordering without an output name asks for one`() = runTest {
        val vm = loadDocument(createViewModel())
        vm.movePage(0, 4)
        vm.setOutputFileName("")

        vm.reorder()
        advanceUntilIdle()

        assertNotNull(vm.state.value.error)
        coVerify(exactly = 0) { pdfToolsRepository.reorderPages(any(), any(), any(), any()) }
    }

    @Test
    fun `reordering an untouched document does nothing but say so`() = runTest {
        val vm = loadDocument(createViewModel())

        vm.reorder()
        advanceUntilIdle()

        assertNotNull(vm.state.value.error)
        coVerify(exactly = 0) { pdfToolsRepository.reorderPages(any(), any(), any(), any()) }
    }
    // endregion

    // region Writing the reordered document
    @Test
    fun `the order sent for writing is the order on screen, numbered from one`() = runTest {
        val vm = loadDocument(createViewModel())
        vm.movePage(9, 0)
        val newOrder = slot<List<Int>>()
        coEvery {
            pdfToolsRepository.reorderPages(any(), any(), capture(newOrder), any())
        } returns Result.success(Unit)

        vm.reorder()
        advanceUntilIdle()

        assertEquals(listOf(10, 1, 2, 3, 4, 5, 6, 7, 8, 9), newOrder.captured)
    }

    @Test
    fun `a finished reorder reports where it was written`() = runTest {
        val vm = loadDocument(createViewModel())
        vm.movePage(0, 1)
        vm.setOutputFileName("shuffled")
        coEvery {
            pdfToolsRepository.reorderPages(any(), any(), any(), any())
        } returns Result.success(Unit)

        vm.reorder()
        advanceUntilIdle()

        val result = vm.state.value.result
        assertNotNull(result)
        assertTrue(result!!.outputPath.endsWith("shuffled.pdf"))
        assertFalse(vm.state.value.isProcessing)
    }

    @Test
    fun `an existing file is not written over, a numbered one is used instead`() = runTest {
        val vm = loadDocument(createViewModel())
        vm.movePage(0, 1)
        vm.setOutputFileName("shuffled")
        val documents = Environment.getExternalStoragePublicDirectory(null)
        File(documents, "PdfReaderPro").mkdirs()
        File(documents, "PdfReaderPro/shuffled.pdf").writeText("someone else's work")
        coEvery {
            pdfToolsRepository.reorderPages(any(), any(), any(), any())
        } returns Result.success(Unit)

        vm.reorder()
        advanceUntilIdle()

        assertTrue(vm.state.value.result!!.outputPath.endsWith("shuffled_1.pdf"))
    }

    @Test
    fun `overwriting writes through a temporary file, then replaces the original`() = runTest {
        // Reordering in place cannot read and write the same file at once, so the
        // repository must be pointed somewhere else and the original swapped after.
        val vm = loadDocument(createViewModel())
        vm.movePage(0, 1)
        vm.setOverwriteOriginal(true)
        val sourcePath = vm.state.value.sourceFile!!.path
        val target = slot<String>()
        coEvery {
            pdfToolsRepository.reorderPages(any(), capture(target), any(), any())
        } answers {
            File(target.captured).writeText("reordered bytes")
            Result.success(Unit)
        }

        vm.reorder()
        advanceUntilIdle()

        assertNotEquals(sourcePath, target.captured)
        assertFalse(File(target.captured).exists())
        assertEquals("reordered bytes", File(sourcePath).readText())
        assertEquals(sourcePath, vm.state.value.result!!.outputPath)
    }

    @Test
    fun `progress from the repository reaches the screen`() = runTest {
        val vm = loadDocument(createViewModel())
        vm.movePage(0, 1)
        val seen = mutableListOf<Float>()
        coEvery {
            pdfToolsRepository.reorderPages(any(), any(), any(), any())
        } answers {
            val onProgress = arg<(Float) -> Unit>(3)
            onProgress(0.25f)
            seen += vm.state.value.progress
            onProgress(0.75f)
            seen += vm.state.value.progress
            Result.success(Unit)
        }

        vm.reorder()
        advanceUntilIdle()

        assertEquals(listOf(0.25f, 0.75f), seen)
        assertEquals(1f, vm.state.value.progress, 0.001f)
    }

    @Test
    fun `a failure is reported and does not leave the document processing`() = runTest {
        val vm = loadDocument(createViewModel())
        vm.movePage(0, 1)
        coEvery {
            pdfToolsRepository.reorderPages(any(), any(), any(), any())
        } returns Result.failure(RuntimeException("page 3 is damaged"))

        vm.reorder()
        advanceUntilIdle()

        assertEquals("page 3 is damaged", vm.state.value.error)
        assertFalse(vm.state.value.isProcessing)
        assertNull(vm.state.value.result)
    }

    @Test
    fun `a failed overwrite leaves no temporary file behind`() = runTest {
        val vm = loadDocument(createViewModel())
        vm.movePage(0, 1)
        vm.setOverwriteOriginal(true)
        val target = slot<String>()
        coEvery {
            pdfToolsRepository.reorderPages(any(), capture(target), any(), any())
        } answers {
            File(target.captured).writeText("half written")
            Result.failure(RuntimeException("ran out of space"))
        }

        vm.reorder()
        advanceUntilIdle()

        assertFalse(File(target.captured).exists())
        assertNotNull(vm.state.value.error)
    }
    // endregion

    // region Clearing
    @Test
    fun `clearing the error leaves the pages alone`() = runTest {
        val vm = loadDocument(createViewModel())
        vm.reorder()
        advanceUntilIdle()

        vm.clearError()

        assertNull(vm.state.value.error)
        assertEquals(10, vm.state.value.pages.size)
    }

    @Test
    fun `clearing the result keeps the document open for another go`() = runTest {
        val vm = loadDocument(createViewModel())
        vm.movePage(0, 1)
        coEvery {
            pdfToolsRepository.reorderPages(any(), any(), any(), any())
        } returns Result.success(Unit)
        vm.reorder()
        advanceUntilIdle()

        vm.clearResult()

        assertNull(vm.state.value.result)
        assertNotNull(vm.state.value.sourceFile)
    }

    @Test
    fun `resetting puts the tool back to empty`() = runTest {
        val vm = loadDocument(createViewModel())
        vm.movePage(0, 1)

        vm.reset()

        assertNull(vm.state.value.sourceFile)
        assertTrue(vm.state.value.pages.isEmpty())
        assertFalse(vm.state.value.hasChanges)
    }
    // endregion
}
