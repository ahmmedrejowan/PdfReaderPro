package com.rejowan.pdfreaderpro.presentation.viewmodel.tools

import android.app.Application
import app.cash.turbine.test
import com.rejowan.pdfreaderpro.domain.repository.PdfToolsRepository
import com.rejowan.pdfreaderpro.presentation.screens.tools.pdftoimage.ImageFormat
import com.rejowan.pdfreaderpro.presentation.screens.tools.pdftoimage.PageSelection
import com.rejowan.pdfreaderpro.presentation.screens.tools.pdftoimage.PdfToImageViewModel
import android.os.Environment
import io.mockk.every
import io.mockk.coVerify
import io.mockk.slot
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import java.io.ByteArrayInputStream
import io.mockk.coEvery
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
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import kotlinx.coroutines.test.TestScope
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PdfToImageViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var pdfToolsRepository: PdfToolsRepository
    private lateinit var context: Application
    private lateinit var viewModel: PdfToImageViewModel

    @get:Rule
    val folder = TemporaryFolder()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        pdfToolsRepository = mockk(relaxed = true)
        context = mockk(relaxed = true)

        // Exporting is written against a loaded document, and the copy behind
        // loading fails silently on a relaxed mock. Environment is mocked because
        // images are written into the public Documents folder, which off-device
        // throws inside the coroutine and surfaces in whichever test runs next.
        every { context.cacheDir } returns folder.newFolder("cache")
        every { context.contentResolver.openInputStream(any()) } answers {
            ByteArrayInputStream("%PDF-1.4 pretend document".toByteArray())
        }
        every { context.contentResolver.query(any(), any(), any(), any(), any()) } returns null
        mockkStatic(Environment::class)
        every {
            Environment.getExternalStoragePublicDirectory(any())
        } returns folder.newFolder("documents")

        coEvery { pdfToolsRepository.getPageCount(any()) } returns Result.success(10)
    }

    @After
    fun teardown() {
        unmockkStatic(Environment::class)
        Dispatchers.resetMain()
    }

    private fun createViewModel(): PdfToImageViewModel {
        return PdfToImageViewModel(
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
    fun `initial state has PNG as default image format`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.state.test {
            val state = awaitItem()
            assertEquals(ImageFormat.PNG, state.imageFormat)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `initial state has ALL as default page selection`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.state.test {
            val state = awaitItem()
            assertEquals(PageSelection.ALL, state.pageSelection)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `initial state has empty custom pages`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.state.test {
            val state = awaitItem()
            assertEquals("", state.customPages)
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

    // region setImageFormat Tests
    @Test
    fun `setImageFormat updates to PNG`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.setImageFormat(ImageFormat.PNG)
        advanceUntilIdle()

        viewModel.state.test {
            val state = awaitItem()
            assertEquals(ImageFormat.PNG, state.imageFormat)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `setImageFormat updates to JPG`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.setImageFormat(ImageFormat.JPG)
        advanceUntilIdle()

        viewModel.state.test {
            val state = awaitItem()
            assertEquals(ImageFormat.JPG, state.imageFormat)
            cancelAndIgnoreRemainingEvents()
        }
    }
    // endregion

    // region setPageSelection Tests
    @Test
    fun `setPageSelection updates to ALL`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.setPageSelection(PageSelection.ALL)
        advanceUntilIdle()

        viewModel.state.test {
            val state = awaitItem()
            assertEquals(PageSelection.ALL, state.pageSelection)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `setPageSelection updates to CUSTOM`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.setPageSelection(PageSelection.CUSTOM)
        advanceUntilIdle()

        viewModel.state.test {
            val state = awaitItem()
            assertEquals(PageSelection.CUSTOM, state.pageSelection)
            cancelAndIgnoreRemainingEvents()
        }
    }
    // endregion

    // region setCustomPages Tests
    @Test
    fun `setCustomPages updates custom pages`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.setCustomPages("1-5, 8, 10")
        advanceUntilIdle()

        viewModel.state.test {
            val state = awaitItem()
            assertEquals("1-5, 8, 10", state.customPages)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `setCustomPages with empty string updates state`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.setCustomPages("")
        advanceUntilIdle()

        viewModel.state.test {
            val state = awaitItem()
            assertEquals("", state.customPages)
            cancelAndIgnoreRemainingEvents()
        }
    }
    // endregion

    // region exportImages Validation Tests
    @Test
    fun `exportImages without source file sets error`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.exportImages()
        advanceUntilIdle()

        viewModel.state.test {
            val state = awaitItem()
            assertEquals("Please select a PDF file first", state.error)
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

        viewModel.setImageFormat(ImageFormat.JPG)
        viewModel.setPageSelection(PageSelection.CUSTOM)
        viewModel.setCustomPages("1-5")
        viewModel.reset()
        advanceUntilIdle()

        viewModel.state.test {
            val state = awaitItem()
            assertNull(state.sourceFile)
            assertEquals(ImageFormat.PNG, state.imageFormat)
            assertEquals(PageSelection.ALL, state.pageSelection)
            assertEquals("", state.customPages)
            assertFalse(state.isLoading)
            assertFalse(state.isProcessing)
            assertEquals(0f, state.progress)
            assertNull(state.error)
            assertNull(state.result)
            cancelAndIgnoreRemainingEvents()
        }
    }
    // endregion

    // region ImageFormat enum Tests
    @Test
    fun `ImageFormat PNG has correct properties`() {
        assertEquals("png", ImageFormat.PNG.extension)
        assertEquals("PNG", ImageFormat.PNG.label)
    }

    @Test
    fun `ImageFormat JPG has correct properties`() {
        assertEquals("jpg", ImageFormat.JPG.extension)
        assertEquals("JPG", ImageFormat.JPG.label)
    }

    @Test
    fun `ImageFormat has 2 values`() {
        assertEquals(2, ImageFormat.entries.size)
    }
    // endregion

    // region PageSelection enum Tests
    @Test
    fun `PageSelection has ALL`() {
        assertEquals("ALL", PageSelection.ALL.name)
    }

    @Test
    fun `PageSelection has CUSTOM`() {
        assertEquals("CUSTOM", PageSelection.CUSTOM.name)
    }

    @Test
    fun `PageSelection has 2 values`() {
        assertEquals(2, PageSelection.entries.size)
    }
    // endregion

    private fun TestScope.loadDocument(vm: PdfToImageViewModel) {
        vm.setSourceFile(mockk(relaxed = true))
        repeat(200) {
            advanceUntilIdle()
            if (vm.state.value.sourceFile != null) return
            Thread.sleep(10)
        }
        error("document never loaded")
    }

    /** Exports and reports the page list handed to the repository. */
    private fun TestScope.exportedPages(configure: (PdfToImageViewModel) -> Unit): List<Int>? {
        val captured = slot<List<Int>?>()
        coEvery {
            pdfToolsRepository.pdfToImages(any(), any(), any(), captureNullable(captured), any())
        } returns Result.success(emptyList())

        val vm = createViewModel()
        loadDocument(vm)
        configure(vm)
        vm.exportImages()
        advanceUntilIdle()
        return captured.captured
    }

    // region Which pages get exported
    @Test
    fun `exporting everything passes no page list, meaning all of them`() = runTest {
        assertNull(exportedPages { it.setPageSelection(PageSelection.ALL) })
    }

    @Test
    fun `a custom list is honoured`() = runTest {
        val pages = exportedPages {
            it.setPageSelection(PageSelection.CUSTOM)
            it.setCustomPages("2, 6, 9")
        }
        assertEquals(listOf(2, 6, 9), pages)
    }

    @Test
    fun `a custom range is expanded`() = runTest {
        val pages = exportedPages {
            it.setPageSelection(PageSelection.CUSTOM)
            it.setCustomPages("3-5")
        }
        assertEquals(listOf(3, 4, 5), pages)
    }

    @Test
    fun `custom pages past the end of the document are dropped`() = runTest {
        val pages = exportedPages {
            it.setPageSelection(PageSelection.CUSTOM)
            it.setCustomPages("4, 400")
        }
        assertEquals(listOf(4), pages)
    }

    @Test
    fun `custom pages are ordered and deduplicated`() = runTest {
        val pages = exportedPages {
            it.setPageSelection(PageSelection.CUSTOM)
            it.setCustomPages("6, 1, 6")
        }
        assertEquals(listOf(1, 6), pages)
    }
    // endregion

    // region Refusing to export
    @Test
    fun `without a document it asks for one and exports nothing`() = runTest {
        val vm = createViewModel()
        vm.exportImages()
        advanceUntilIdle()

        assertNotNull(vm.state.value.error)
        coVerify(exactly = 0) {
            pdfToolsRepository.pdfToImages(any(), any(), any(), any(), any())
        }
    }

    @Test
    fun `a failure is surfaced and processing stops`() = runTest {
        coEvery {
            pdfToolsRepository.pdfToImages(any(), any(), any(), any(), any())
        } returns Result.failure(RuntimeException("out of space"))

        val vm = createViewModel()
        loadDocument(vm)
        vm.exportImages()
        advanceUntilIdle()

        assertNotNull(vm.state.value.error)
        assertFalse(vm.state.value.isProcessing)
    }

    @Test
    fun `the chosen image format is the one exported`() = runTest {
        // The repository takes the file extension, not the enum, so this also
        // pins that the mapping between them stays right.
        val format = slot<String>()
        coEvery {
            pdfToolsRepository.pdfToImages(any(), any(), capture(format), any(), any())
        } returns Result.success(emptyList())

        val vm = createViewModel()
        loadDocument(vm)
        vm.setImageFormat(ImageFormat.PNG)
        vm.exportImages()
        advanceUntilIdle()

        assertEquals(ImageFormat.PNG.extension, format.captured)
    }

    @Test
    fun `loading a document records its page count`() = runTest {
        val vm = createViewModel()
        loadDocument(vm)
        assertEquals(10, vm.state.value.sourceFile?.pageCount)
    }
    // endregion
}
