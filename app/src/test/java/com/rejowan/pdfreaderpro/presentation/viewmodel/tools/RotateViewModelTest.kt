package com.rejowan.pdfreaderpro.presentation.viewmodel.tools

import android.app.Application
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.Environment
import android.os.ParcelFileDescriptor
import app.cash.turbine.test
import com.rejowan.pdfreaderpro.domain.repository.PdfToolsRepository
import com.rejowan.pdfreaderpro.presentation.screens.tools.rotate.PageSelectionMode
import com.rejowan.pdfreaderpro.presentation.screens.tools.rotate.QuickSelection
import com.rejowan.pdfreaderpro.presentation.screens.tools.rotate.RotateViewModel
import com.rejowan.pdfreaderpro.presentation.screens.tools.rotate.RotationAngle
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
class RotateViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var pdfToolsRepository: PdfToolsRepository
    private lateinit var context: Application
    private lateinit var viewModel: RotateViewModel

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
        // device. Standing it up is what makes selecting pages testable at all.
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

    private fun createViewModel(): RotateViewModel {
        return RotateViewModel(
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
    fun `initial state has ROTATE_90 as default rotation angle`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.state.test {
            val state = awaitItem()
            assertEquals(RotationAngle.ROTATE_90, state.rotationAngle)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `initial state has ALL_PAGES as default selection mode`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.state.test {
            val state = awaitItem()
            assertEquals(PageSelectionMode.ALL_PAGES, state.selectionMode)
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

    // region setRotationAngle Tests
    @Test
    fun `setRotationAngle updates to ROTATE_90`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.setRotationAngle(RotationAngle.ROTATE_90)
        advanceUntilIdle()

        viewModel.state.test {
            val state = awaitItem()
            assertEquals(RotationAngle.ROTATE_90, state.rotationAngle)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `setRotationAngle updates to ROTATE_180`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.setRotationAngle(RotationAngle.ROTATE_180)
        advanceUntilIdle()

        viewModel.state.test {
            val state = awaitItem()
            assertEquals(RotationAngle.ROTATE_180, state.rotationAngle)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `setRotationAngle updates to ROTATE_270`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.setRotationAngle(RotationAngle.ROTATE_270)
        advanceUntilIdle()

        viewModel.state.test {
            val state = awaitItem()
            assertEquals(RotationAngle.ROTATE_270, state.rotationAngle)
            cancelAndIgnoreRemainingEvents()
        }
    }
    // endregion

    // region setSelectionMode Tests
    @Test
    fun `setSelectionMode updates to ALL_PAGES`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.setSelectionMode(PageSelectionMode.ALL_PAGES)
        advanceUntilIdle()

        viewModel.state.test {
            val state = awaitItem()
            assertEquals(PageSelectionMode.ALL_PAGES, state.selectionMode)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `setSelectionMode updates to SELECTED_PAGES`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.setSelectionMode(PageSelectionMode.SELECTED_PAGES)
        advanceUntilIdle()

        viewModel.state.test {
            val state = awaitItem()
            assertEquals(PageSelectionMode.SELECTED_PAGES, state.selectionMode)
            cancelAndIgnoreRemainingEvents()
        }
    }
    // endregion

    // region setOutputFileName Tests
    @Test
    fun `setOutputFileName updates filename`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.setOutputFileName("my_rotated_file")
        advanceUntilIdle()

        viewModel.state.test {
            val state = awaitItem()
            assertEquals("my_rotated_file", state.outputFileName)
            cancelAndIgnoreRemainingEvents()
        }
    }
    // endregion

    // region rotate Validation Tests
    @Test
    fun `rotate without source file sets error`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.rotate()
        advanceUntilIdle()

        viewModel.state.test {
            val state = awaitItem()
            assertEquals("Please select a PDF file first", state.error)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `rotate with blank output filename sets error`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.setOutputFileName("")
        viewModel.rotate()
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
        viewModel.setRotationAngle(RotationAngle.ROTATE_180)
        viewModel.reset()
        advanceUntilIdle()

        viewModel.state.test {
            val state = awaitItem()
            assertNull(state.sourceFile)
            assertEquals(RotationAngle.ROTATE_90, state.rotationAngle)
            assertEquals(PageSelectionMode.ALL_PAGES, state.selectionMode)
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

    // region RotationAngle enum Tests
    @Test
    fun `RotationAngle ROTATE_90 has correct properties`() {
        assertEquals(90, RotationAngle.ROTATE_90.degrees)
        assertEquals("90° Right", RotationAngle.ROTATE_90.label)
    }

    @Test
    fun `RotationAngle ROTATE_180 has correct properties`() {
        assertEquals(180, RotationAngle.ROTATE_180.degrees)
        assertEquals("180°", RotationAngle.ROTATE_180.label)
    }

    @Test
    fun `RotationAngle ROTATE_270 has correct properties`() {
        assertEquals(270, RotationAngle.ROTATE_270.degrees)
        assertEquals("90° Left", RotationAngle.ROTATE_270.label)
    }

    @Test
    fun `RotationAngle has 3 values`() {
        assertEquals(3, RotationAngle.entries.size)
    }
    // endregion

    // region PageSelectionMode enum Tests
    @Test
    fun `PageSelectionMode has ALL_PAGES`() {
        assertEquals("ALL_PAGES", PageSelectionMode.ALL_PAGES.name)
    }

    @Test
    fun `PageSelectionMode has SELECTED_PAGES`() {
        assertEquals("SELECTED_PAGES", PageSelectionMode.SELECTED_PAGES.name)
    }

    @Test
    fun `PageSelectionMode has 2 values`() {
        assertEquals(2, PageSelectionMode.entries.size)
    }
    // endregion

    // region QuickSelection enum Tests
    @Test
    fun `QuickSelection ALL has correct label`() {
        assertEquals("All", QuickSelection.ALL.label)
    }

    @Test
    fun `QuickSelection ODD has correct label`() {
        assertEquals("Odd", QuickSelection.ODD.label)
    }

    @Test
    fun `QuickSelection EVEN has correct label`() {
        assertEquals("Even", QuickSelection.EVEN.label)
    }

    @Test
    fun `QuickSelection FIRST_HALF has correct label`() {
        assertEquals("First Half", QuickSelection.FIRST_HALF.label)
    }

    @Test
    fun `QuickSelection SECOND_HALF has correct label`() {
        assertEquals("Second Half", QuickSelection.SECOND_HALF.label)
    }

    @Test
    fun `QuickSelection EVERY_2ND has correct label`() {
        assertEquals("Every 2nd", QuickSelection.EVERY_2ND.label)
    }

    @Test
    fun `QuickSelection EVERY_3RD has correct label`() {
        assertEquals("Every 3rd", QuickSelection.EVERY_3RD.label)
    }

    @Test
    fun `QuickSelection has 7 values`() {
        assertEquals(7, QuickSelection.entries.size)
    }
    // endregion

    /** setSourceFile is async, so wait for the page list to actually arrive. */
    private fun TestScope.loadDocument(vm: RotateViewModel): RotateViewModel {
        vm.setSourceFile(mockk(relaxed = true))
        repeat(200) {
            advanceUntilIdle()
            if (vm.state.value.sourceFile?.pages?.isNotEmpty() == true) return vm
            Thread.sleep(10)
        }
        error("document never loaded")
    }

    private fun selected(vm: RotateViewModel) =
        vm.state.value.sourceFile!!.pages.filter { it.isSelected }.map { it.pageNumber }

    // region Loading a document
    @Test
    fun `loading lists one entry per page`() = runTest {
        val vm = loadDocument(createViewModel())

        assertEquals(10, vm.state.value.sourceFile!!.pages.size)
        assertEquals((1..10).toList(), vm.state.value.sourceFile!!.pages.map { it.pageNumber })
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

    // region Choosing which pages to rotate
    @Test
    fun `tapping a page selects just that one and leaves whole document mode`() = runTest {
        val vm = loadDocument(createViewModel())

        vm.deselectAllPages()
        vm.togglePageSelection(3)

        assertEquals(listOf(3), selected(vm))
        assertEquals(PageSelectionMode.SELECTED_PAGES, vm.state.value.selectionMode)
    }

    @Test
    fun `tapping the same page twice leaves it unselected`() = runTest {
        val vm = loadDocument(createViewModel())
        vm.deselectAllPages()

        vm.togglePageSelection(3)
        vm.togglePageSelection(3)

        assertTrue(selected(vm).isEmpty())
    }

    @Test
    fun `select all marks every page and returns to whole document mode`() = runTest {
        val vm = loadDocument(createViewModel())
        vm.deselectAllPages()

        vm.selectAllPages()

        assertEquals((1..10).toList(), selected(vm))
        assertEquals(PageSelectionMode.ALL_PAGES, vm.state.value.selectionMode)
    }

    @Test
    fun `switching to whole document mode selects everything again`() = runTest {
        val vm = loadDocument(createViewModel())
        vm.deselectAllPages()

        vm.setSelectionMode(PageSelectionMode.ALL_PAGES)

        assertEquals((1..10).toList(), selected(vm))
    }

    @Test
    fun `switching to picking pages starts from nothing selected`() = runTest {
        val vm = loadDocument(createViewModel())

        vm.setSelectionMode(PageSelectionMode.SELECTED_PAGES)

        assertTrue(selected(vm).isEmpty())
    }

    @Test
    fun `a page range selects its ends and everything between`() = runTest {
        val vm = loadDocument(createViewModel())

        vm.selectPageRange(4, 7)

        assertEquals(listOf(4, 5, 6, 7), selected(vm))
    }

    @Test
    fun `a range beyond the document simply selects what is there`() = runTest {
        val vm = loadDocument(createViewModel())

        vm.selectPageRange(8, 200)

        assertEquals(listOf(8, 9, 10), selected(vm))
    }
    // endregion

    // region Quick selections
    // Each is described by a label the user reads, so what it picks has to match it.

    @Test
    fun `odd picks the odd numbered pages`() = runTest {
        val vm = loadDocument(createViewModel())
        vm.applyQuickSelection(QuickSelection.ODD)
        assertEquals(listOf(1, 3, 5, 7, 9), selected(vm))
    }

    @Test
    fun `even picks the even numbered pages`() = runTest {
        val vm = loadDocument(createViewModel())
        vm.applyQuickSelection(QuickSelection.EVEN)
        assertEquals(listOf(2, 4, 6, 8, 10), selected(vm))
    }

    @Test
    fun `first and second half split the document between them`() = runTest {
        val vm = loadDocument(createViewModel())

        vm.applyQuickSelection(QuickSelection.FIRST_HALF)
        val first = selected(vm)
        vm.applyQuickSelection(QuickSelection.SECOND_HALF)
        val second = selected(vm)

        assertEquals(listOf(1, 2, 3, 4, 5), first)
        assertEquals(listOf(6, 7, 8, 9, 10), second)
        assertTrue((first + second).sorted() == (1..10).toList())
    }

    @Test
    fun `every third picks every third page`() = runTest {
        val vm = loadDocument(createViewModel())
        vm.applyQuickSelection(QuickSelection.EVERY_3RD)
        assertEquals(listOf(3, 6, 9), selected(vm))
    }

    @Test
    fun `all selects everything and means the whole document`() = runTest {
        val vm = loadDocument(createViewModel())
        vm.deselectAllPages()

        vm.applyQuickSelection(QuickSelection.ALL)

        assertEquals((1..10).toList(), selected(vm))
        assertEquals(PageSelectionMode.ALL_PAGES, vm.state.value.selectionMode)
    }

    @Test
    fun `an odd length document keeps the middle page in the first half only`() = runTest {
        // With 9 pages the halves must still cover the document exactly once.
        coEvery { pdfToolsRepository.getPageCount(any()) } returns Result.success(9)
        val vm = loadDocument(createViewModel())

        vm.applyQuickSelection(QuickSelection.FIRST_HALF)
        val first = selected(vm)
        vm.applyQuickSelection(QuickSelection.SECOND_HALF)
        val second = selected(vm)

        assertEquals(listOf(1, 2, 3, 4, 5), first)
        assertEquals(listOf(5, 6, 7, 8, 9), second)
    }
    // endregion

    // region Refusing to rotate
    @Test
    fun `rotating without a document asks for one`() = runTest {
        val vm = createViewModel()

        vm.rotate()
        advanceUntilIdle()

        assertNotNull(vm.state.value.error)
        coVerify(exactly = 0) { pdfToolsRepository.rotatePages(any(), any(), any(), any(), any()) }
    }

    @Test
    fun `rotating without an output name asks for one`() = runTest {
        val vm = loadDocument(createViewModel())
        vm.setOutputFileName("")

        vm.rotate()
        advanceUntilIdle()

        assertNotNull(vm.state.value.error)
        coVerify(exactly = 0) { pdfToolsRepository.rotatePages(any(), any(), any(), any(), any()) }
    }

    @Test
    fun `picking pages but selecting none asks for a page`() = runTest {
        val vm = loadDocument(createViewModel())
        vm.setSelectionMode(PageSelectionMode.SELECTED_PAGES)

        vm.rotate()
        advanceUntilIdle()

        assertNotNull(vm.state.value.error)
        coVerify(exactly = 0) { pdfToolsRepository.rotatePages(any(), any(), any(), any(), any()) }
    }
    // endregion

    // region Rotating
    @Test
    fun `whole document mode sends no page list, meaning every page`() = runTest {
        val vm = loadDocument(createViewModel())
        val pages = slot<List<Int>?>()
        coEvery {
            pdfToolsRepository.rotatePages(any(), any(), any(), captureNullable(pages), any())
        } returns Result.success(Unit)

        vm.rotate()
        advanceUntilIdle()

        assertNull(pages.captured)
        assertEquals(10, vm.state.value.result!!.rotatedPages)
    }

    @Test
    fun `only the pages the user picked are sent`() = runTest {
        val vm = loadDocument(createViewModel())
        vm.deselectAllPages()
        vm.togglePageSelection(2)
        vm.togglePageSelection(5)
        val pages = slot<List<Int>?>()
        coEvery {
            pdfToolsRepository.rotatePages(any(), any(), any(), captureNullable(pages), any())
        } returns Result.success(Unit)

        vm.rotate()
        advanceUntilIdle()

        assertEquals(listOf(2, 5), pages.captured)
        assertEquals(2, vm.state.value.result!!.rotatedPages)
    }

    @Test
    fun `the chosen angle is sent in degrees`() = runTest {
        val vm = loadDocument(createViewModel())
        vm.setRotationAngle(RotationAngle.ROTATE_270)
        val rotation = slot<Int>()
        coEvery {
            pdfToolsRepository.rotatePages(any(), any(), capture(rotation), any(), any())
        } returns Result.success(Unit)

        vm.rotate()
        advanceUntilIdle()

        assertEquals(270, rotation.captured)
    }

    @Test
    fun `an existing file is not written over, a numbered one is used instead`() = runTest {
        val vm = loadDocument(createViewModel())
        vm.setOutputFileName("turned")
        val documents = Environment.getExternalStoragePublicDirectory(null)
        File(documents, "PdfReaderPro").mkdirs()
        File(documents, "PdfReaderPro/turned.pdf").writeText("someone else's work")
        coEvery {
            pdfToolsRepository.rotatePages(any(), any(), any(), any(), any())
        } returns Result.success(Unit)

        vm.rotate()
        advanceUntilIdle()

        assertTrue(vm.state.value.result!!.outputPath.endsWith("turned_1.pdf"))
    }

    @Test
    fun `overwriting writes through a temporary file, then replaces the original`() = runTest {
        val vm = loadDocument(createViewModel())
        vm.setOverwriteOriginal(true)
        val sourcePath = vm.state.value.sourceFile!!.path
        val target = slot<String>()
        coEvery {
            pdfToolsRepository.rotatePages(any(), capture(target), any(), any(), any())
        } answers {
            File(target.captured).writeText("rotated bytes")
            Result.success(Unit)
        }

        vm.rotate()
        advanceUntilIdle()

        assertNotEquals(sourcePath, target.captured)
        assertFalse(File(target.captured).exists())
        assertEquals("rotated bytes", File(sourcePath).readText())
        assertEquals(sourcePath, vm.state.value.result!!.outputPath)
    }

    @Test
    fun `progress from the repository reaches the screen`() = runTest {
        val vm = loadDocument(createViewModel())
        val seen = mutableListOf<Float>()
        coEvery {
            pdfToolsRepository.rotatePages(any(), any(), any(), any(), any())
        } answers {
            val onProgress = arg<(Float) -> Unit>(4)
            onProgress(0.4f)
            seen += vm.state.value.progress
            Result.success(Unit)
        }

        vm.rotate()
        advanceUntilIdle()

        assertEquals(listOf(0.4f), seen)
        assertEquals(1f, vm.state.value.progress, 0.001f)
    }

    @Test
    fun `a failure is reported and does not leave the tool processing`() = runTest {
        val vm = loadDocument(createViewModel())
        coEvery {
            pdfToolsRepository.rotatePages(any(), any(), any(), any(), any())
        } returns Result.failure(RuntimeException("page 4 is damaged"))

        vm.rotate()
        advanceUntilIdle()

        assertEquals("page 4 is damaged", vm.state.value.error)
        assertFalse(vm.state.value.isProcessing)
        assertNull(vm.state.value.result)
    }

    @Test
    fun `a failed overwrite leaves no temporary file behind`() = runTest {
        val vm = loadDocument(createViewModel())
        vm.setOverwriteOriginal(true)
        val target = slot<String>()
        coEvery {
            pdfToolsRepository.rotatePages(any(), capture(target), any(), any(), any())
        } answers {
            File(target.captured).writeText("half written")
            Result.failure(RuntimeException("ran out of space"))
        }

        vm.rotate()
        advanceUntilIdle()

        assertFalse(File(target.captured).exists())
        assertNotNull(vm.state.value.error)
    }
    // endregion

    // region Clearing
    @Test
    fun `clearing the result keeps the document open for another go`() = runTest {
        val vm = loadDocument(createViewModel())
        coEvery {
            pdfToolsRepository.rotatePages(any(), any(), any(), any(), any())
        } returns Result.success(Unit)
        vm.rotate()
        advanceUntilIdle()

        vm.clearResult()

        assertNull(vm.state.value.result)
        assertNotNull(vm.state.value.sourceFile)
    }

    @Test
    fun `clearing the error keeps the document open`() = runTest {
        val vm = createViewModel()
        vm.rotate()
        advanceUntilIdle()

        vm.clearError()

        assertNull(vm.state.value.error)
    }

    @Test
    fun `resetting puts the tool back to empty`() = runTest {
        val vm = loadDocument(createViewModel())

        vm.reset()

        assertNull(vm.state.value.sourceFile)
        assertEquals(RotationAngle.ROTATE_90, vm.state.value.rotationAngle)
    }
    // endregion
}
