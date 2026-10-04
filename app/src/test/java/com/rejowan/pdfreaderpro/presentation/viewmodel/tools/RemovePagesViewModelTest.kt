package com.rejowan.pdfreaderpro.presentation.viewmodel.tools

import android.app.Application
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.Environment
import android.os.ParcelFileDescriptor
import app.cash.turbine.test
import com.rejowan.pdfreaderpro.domain.repository.PdfToolsRepository
import com.rejowan.pdfreaderpro.presentation.screens.tools.removepages.RemovePagesViewModel
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
class RemovePagesViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var pdfToolsRepository: PdfToolsRepository
    private lateinit var context: Application
    private lateinit var viewModel: RemovePagesViewModel

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

        // The page list comes from the platform renderer, which does not exist off
        // device. Standing it up is what makes picking pages testable at all.
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

    private fun createViewModel(): RemovePagesViewModel {
        return RemovePagesViewModel(
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
    // endregion

    // region setOutputFileName Tests
    @Test
    fun `setOutputFileName updates filename`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.setOutputFileName("my_modified_file")
        advanceUntilIdle()

        viewModel.state.test {
            val state = awaitItem()
            assertEquals("my_modified_file", state.outputFileName)
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

    // region removePages Validation Tests
    @Test
    fun `removePages without source file sets error`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.removePages()
        advanceUntilIdle()

        viewModel.state.test {
            val state = awaitItem()
            assertEquals("Please select a PDF file first", state.error)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `removePages with blank output filename sets error`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.setOutputFileName("")
        viewModel.removePages()
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
            assertEquals("", state.outputFileName)
            assertFalse(state.isLoading)
            assertFalse(state.isProcessing)
            assertEquals(0f, state.progress)
            assertNull(state.error)
            assertNull(state.result)
            cancelAndIgnoreRemainingEvents()
        }
    }
    // endregion

    // region Selection Method Tests (without source file)
    @Test
    fun `selectAllPages does not crash without source file`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        // Should not throw
        viewModel.selectAllPages()
        advanceUntilIdle()

        viewModel.state.test {
            val state = awaitItem()
            assertNull(state.sourceFile)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `deselectAllPages does not crash without source file`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.deselectAllPages()
        advanceUntilIdle()

        viewModel.state.test {
            val state = awaitItem()
            assertNull(state.sourceFile)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `selectOddPages does not crash without source file`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.selectOddPages()
        advanceUntilIdle()

        viewModel.state.test {
            val state = awaitItem()
            assertNull(state.sourceFile)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `selectEvenPages does not crash without source file`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.selectEvenPages()
        advanceUntilIdle()

        viewModel.state.test {
            val state = awaitItem()
            assertNull(state.sourceFile)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `selectRange does not crash without source file`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.selectRange(1, 5)
        advanceUntilIdle()

        viewModel.state.test {
            val state = awaitItem()
            assertNull(state.sourceFile)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `selectFirstN does not crash without source file`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.selectFirstN(3)
        advanceUntilIdle()

        viewModel.state.test {
            val state = awaitItem()
            assertNull(state.sourceFile)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `selectLastN does not crash without source file`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.selectLastN(3)
        advanceUntilIdle()

        viewModel.state.test {
            val state = awaitItem()
            assertNull(state.sourceFile)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `selectBeforePage does not crash without source file`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.selectBeforePage(5)
        advanceUntilIdle()

        viewModel.state.test {
            val state = awaitItem()
            assertNull(state.sourceFile)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `selectAfterPage does not crash without source file`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.selectAfterPage(5)
        advanceUntilIdle()

        viewModel.state.test {
            val state = awaitItem()
            assertNull(state.sourceFile)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `togglePageSelection does not crash without source file`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.togglePageSelection(1)
        advanceUntilIdle()

        viewModel.state.test {
            val state = awaitItem()
            assertNull(state.sourceFile)
            cancelAndIgnoreRemainingEvents()
        }
    }
    // endregion

    /** setSourceFile is async, so wait for the page list to actually arrive. */
    private fun TestScope.loadDocument(vm: RemovePagesViewModel): RemovePagesViewModel {
        vm.setSourceFile(mockk(relaxed = true))
        repeat(200) {
            advanceUntilIdle()
            if (vm.state.value.sourceFile?.pages?.isNotEmpty() == true) return vm
            Thread.sleep(10)
        }
        error("document never loaded")
    }

    private fun selected(vm: RemovePagesViewModel) =
        vm.state.value.sourceFile!!.pages.filter { it.isSelected }.map { it.pageNumber }

    // region Loading a document
    @Test
    fun `loading lists one entry per page, none marked for removal`() = runTest {
        val vm = loadDocument(createViewModel())

        assertEquals(10, vm.state.value.sourceFile!!.pages.size)
        assertTrue(selected(vm).isEmpty())
        assertFalse(vm.state.value.isLoading)
    }

    @Test
    fun `a document that cannot be read is reported rather than left blank`() = runTest {
        every { context.contentResolver.openInputStream(any()) } returns null
        val vm = createViewModel()

        vm.setSourceFile(mockk(relaxed = true))
        advanceUntilIdle()

        assertNotNull(vm.state.value.error)
        assertNull(vm.state.value.sourceFile)
        assertFalse(vm.state.value.isLoading)
    }
    // endregion

    // region Picking pages to remove
    // Each of these is a button the user presses, so what it marks has to match
    // its label exactly: removing the wrong page destroys work.

    @Test
    fun `tapping a page marks just that one`() = runTest {
        val vm = loadDocument(createViewModel())

        vm.togglePageSelection(4)

        assertEquals(listOf(4), selected(vm))
    }

    @Test
    fun `tapping the same page twice leaves it unmarked`() = runTest {
        val vm = loadDocument(createViewModel())

        vm.togglePageSelection(4)
        vm.togglePageSelection(4)

        assertTrue(selected(vm).isEmpty())
    }

    @Test
    fun `select all marks every page`() = runTest {
        val vm = loadDocument(createViewModel())
        vm.selectAllPages()
        assertEquals((1..10).toList(), selected(vm))
    }

    @Test
    fun `deselect all clears what was marked`() = runTest {
        val vm = loadDocument(createViewModel())
        vm.selectAllPages()

        vm.deselectAllPages()

        assertTrue(selected(vm).isEmpty())
    }

    @Test
    fun `odd and even split the document between them`() = runTest {
        val vm = loadDocument(createViewModel())

        vm.selectOddPages()
        val odd = selected(vm)
        vm.selectEvenPages()
        val even = selected(vm)

        assertEquals(listOf(1, 3, 5, 7, 9), odd)
        assertEquals(listOf(2, 4, 6, 8, 10), even)
    }

    @Test
    fun `a range marks its ends and everything between`() = runTest {
        val vm = loadDocument(createViewModel())

        vm.selectRange(3, 6)

        assertEquals(listOf(3, 4, 5, 6), selected(vm))
    }

    @Test
    fun `before a page does not include the page itself`() = runTest {
        val vm = loadDocument(createViewModel())

        vm.selectBeforePage(4)

        assertEquals(listOf(1, 2, 3), selected(vm))
    }

    @Test
    fun `after a page does not include the page itself`() = runTest {
        val vm = loadDocument(createViewModel())

        vm.selectAfterPage(8)

        assertEquals(listOf(9, 10), selected(vm))
    }

    @Test
    fun `first n counts from the front`() = runTest {
        val vm = loadDocument(createViewModel())

        vm.selectFirstN(3)

        assertEquals(listOf(1, 2, 3), selected(vm))
    }

    @Test
    fun `last n counts from the back`() = runTest {
        val vm = loadDocument(createViewModel())

        vm.selectLastN(3)

        assertEquals(listOf(8, 9, 10), selected(vm))
    }

    @Test
    fun `asking for more than the document has marks all of it`() = runTest {
        val vm = loadDocument(createViewModel())

        vm.selectFirstN(99)

        assertEquals((1..10).toList(), selected(vm))
    }

    @Test
    fun `one selection replaces the last rather than adding to it`() = runTest {
        val vm = loadDocument(createViewModel())

        vm.selectFirstN(3)
        vm.selectLastN(2)

        assertEquals(listOf(9, 10), selected(vm))
    }
    // endregion

    // region Refusing to remove
    @Test
    fun `removing nothing asks for a page first`() = runTest {
        val vm = loadDocument(createViewModel())

        vm.removePages()
        advanceUntilIdle()

        assertNotNull(vm.state.value.error)
        coVerify(exactly = 0) { pdfToolsRepository.removePages(any(), any(), any(), any()) }
    }

    @Test
    fun `removing every page is refused rather than producing an empty document`() = runTest {
        val vm = loadDocument(createViewModel())
        vm.selectAllPages()

        vm.removePages()
        advanceUntilIdle()

        assertNotNull(vm.state.value.error)
        coVerify(exactly = 0) { pdfToolsRepository.removePages(any(), any(), any(), any()) }
    }

    @Test
    fun `leaving a single page is allowed`() = runTest {
        // The guard is on removing all of them, not on leaving few.
        val vm = loadDocument(createViewModel())
        vm.selectFirstN(9)
        coEvery {
            pdfToolsRepository.removePages(any(), any(), any(), any())
        } returns Result.success(Unit)

        vm.removePages()
        advanceUntilIdle()

        assertNull(vm.state.value.error)
        coVerify(exactly = 1) { pdfToolsRepository.removePages(any(), any(), any(), any()) }
    }
    // endregion

    // region Removing
    @Test
    fun `the pages sent are the ones marked`() = runTest {
        val vm = loadDocument(createViewModel())
        vm.togglePageSelection(2)
        vm.togglePageSelection(7)
        val pages = slot<List<Int>>()
        coEvery {
            pdfToolsRepository.removePages(any(), any(), capture(pages), any())
        } returns Result.success(Unit)

        vm.removePages()
        advanceUntilIdle()

        assertEquals(listOf(2, 7), pages.captured)
    }

    @Test
    fun `the result reports what was removed and what is left`() = runTest {
        val vm = loadDocument(createViewModel())
        vm.selectFirstN(3)
        coEvery {
            pdfToolsRepository.removePages(any(), any(), any(), any())
        } returns Result.success(Unit)
        coEvery { pdfToolsRepository.getPageCount(any()) } returns Result.success(7)

        vm.removePages()
        advanceUntilIdle()

        val result = vm.state.value.result!!
        assertEquals(3, result.removedPages)
        assertEquals(7, result.newPageCount)
        assertFalse(vm.state.value.isProcessing)
    }

    @Test
    fun `an existing file is not written over, a numbered one is used instead`() = runTest {
        val vm = loadDocument(createViewModel())
        vm.selectFirstN(2)
        vm.setOutputFileName("trimmed")
        val documents = Environment.getExternalStoragePublicDirectory(null)
        File(documents, "PdfReaderPro").mkdirs()
        File(documents, "PdfReaderPro/trimmed.pdf").writeText("someone else's work")
        coEvery {
            pdfToolsRepository.removePages(any(), any(), any(), any())
        } returns Result.success(Unit)

        vm.removePages()
        advanceUntilIdle()

        assertTrue(vm.state.value.result!!.outputPath.endsWith("trimmed_1.pdf"))
    }

    @Test
    fun `overwriting writes through a temporary file, then replaces the original`() = runTest {
        val vm = loadDocument(createViewModel())
        vm.selectFirstN(2)
        vm.setOverwriteOriginal(true)
        val sourcePath = vm.state.value.sourceFile!!.path
        val target = slot<String>()
        coEvery {
            pdfToolsRepository.removePages(any(), capture(target), any(), any())
        } answers {
            File(target.captured).writeText("trimmed bytes")
            Result.success(Unit)
        }

        vm.removePages()
        advanceUntilIdle()

        assertNotEquals(sourcePath, target.captured)
        assertFalse(File(target.captured).exists())
        assertEquals("trimmed bytes", File(sourcePath).readText())
        assertEquals(sourcePath, vm.state.value.result!!.outputPath)
    }

    @Test
    fun `progress from the repository reaches the screen`() = runTest {
        val vm = loadDocument(createViewModel())
        vm.selectFirstN(2)
        val seen = mutableListOf<Float>()
        coEvery {
            pdfToolsRepository.removePages(any(), any(), any(), any())
        } answers {
            val onProgress = arg<(Float) -> Unit>(3)
            onProgress(0.6f)
            seen += vm.state.value.progress
            Result.success(Unit)
        }

        vm.removePages()
        advanceUntilIdle()

        assertEquals(listOf(0.6f), seen)
        assertEquals(1f, vm.state.value.progress, 0.001f)
    }

    @Test
    fun `a failure is reported and does not leave the tool processing`() = runTest {
        val vm = loadDocument(createViewModel())
        vm.selectFirstN(2)
        coEvery {
            pdfToolsRepository.removePages(any(), any(), any(), any())
        } returns Result.failure(RuntimeException("page 3 is damaged"))

        vm.removePages()
        advanceUntilIdle()

        assertEquals("page 3 is damaged", vm.state.value.error)
        assertFalse(vm.state.value.isProcessing)
        assertNull(vm.state.value.result)
    }

    @Test
    fun `a failed overwrite leaves no temporary file behind`() = runTest {
        val vm = loadDocument(createViewModel())
        vm.selectFirstN(2)
        vm.setOverwriteOriginal(true)
        val target = slot<String>()
        coEvery {
            pdfToolsRepository.removePages(any(), capture(target), any(), any())
        } answers {
            File(target.captured).writeText("half written")
            Result.failure(RuntimeException("ran out of space"))
        }

        vm.removePages()
        advanceUntilIdle()

        assertFalse(File(target.captured).exists())
        assertNotNull(vm.state.value.error)
    }
    // endregion
}
