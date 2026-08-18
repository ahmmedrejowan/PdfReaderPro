package com.rejowan.pdfreaderpro.presentation.viewmodel.tools

import android.app.Application
import androidx.compose.ui.graphics.Color
import app.cash.turbine.test
import com.rejowan.pdfreaderpro.domain.repository.PdfToolsRepository
import com.rejowan.pdfreaderpro.presentation.screens.tools.pagenumbers.NumberFormat
import com.rejowan.pdfreaderpro.presentation.screens.tools.pagenumbers.NumberPosition
import com.rejowan.pdfreaderpro.presentation.screens.tools.pagenumbers.PageNumbersViewModel
import com.rejowan.pdfreaderpro.presentation.screens.tools.pagenumbers.PageSelection
import android.os.Environment
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.coEvery
import io.mockk.every
import io.mockk.slot
import io.mockk.coVerify
import java.io.ByteArrayInputStream
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
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PageNumbersViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var pdfToolsRepository: PdfToolsRepository
    private lateinit var context: Application
    private lateinit var viewModel: PageNumbersViewModel

    @get:Rule
    val folder = TemporaryFolder()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        pdfToolsRepository = mockk(relaxed = true)
        context = mockk(relaxed = true)

        // Loading a document is what gives the view model a page count, and every
        // page selection rule is expressed against it. A relaxed mock quietly fails
        // that copy, leaving the count at zero and the rules untestable, so the
        // cache directory and the stream are real here.
        every { context.cacheDir } returns folder.newFolder("cache")
        every { context.contentResolver.openInputStream(any()) } answers {
            ByteArrayInputStream("%PDF-1.4 pretend document".toByteArray())
        }
        every { context.contentResolver.query(any(), any(), any(), any(), any()) } returns null

        // The tool writes into the public Documents directory, which does not exist
        // off-device. Unmocked it throws inside the coroutine, so the repository is
        // never reached and the failure surfaces in whichever test runs next.
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

    private fun createViewModel(): PageNumbersViewModel {
        return PageNumbersViewModel(
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
    fun `initial state has BOTTOM_CENTER as default position`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.state.test {
            val state = awaitItem()
            assertEquals(NumberPosition.BOTTOM_CENTER, state.position)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `initial state has NUMBER_ONLY as default format`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.state.test {
            val state = awaitItem()
            assertEquals(NumberFormat.NUMBER_ONLY, state.format)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `initial state has default font size`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.state.test {
            val state = awaitItem()
            assertEquals(12f, state.fontSize)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `initial state has black as default text color`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.state.test {
            val state = awaitItem()
            assertEquals(Color.Black, state.textColor)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `initial state has 1 as default start number`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.state.test {
            val state = awaitItem()
            assertEquals(1, state.startNumber)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `initial state has empty custom prefix`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.state.test {
            val state = awaitItem()
            assertEquals("", state.customPrefix)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `initial state has empty custom suffix`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.state.test {
            val state = awaitItem()
            assertEquals("", state.customSuffix)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `initial state has default margin X`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.state.test {
            val state = awaitItem()
            assertEquals(36f, state.marginX)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `initial state has default margin Y`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.state.test {
            val state = awaitItem()
            assertEquals(30f, state.marginY)
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
    fun `initial state has 0 as skip first N`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.state.test {
            val state = awaitItem()
            assertEquals(0, state.skipFirstN)
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

    // region setPosition Tests
    @Test
    fun `setPosition updates to TOP_LEFT`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.setPosition(NumberPosition.TOP_LEFT)
        advanceUntilIdle()

        viewModel.state.test {
            val state = awaitItem()
            assertEquals(NumberPosition.TOP_LEFT, state.position)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `setPosition updates to TOP_CENTER`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.setPosition(NumberPosition.TOP_CENTER)
        advanceUntilIdle()

        viewModel.state.test {
            val state = awaitItem()
            assertEquals(NumberPosition.TOP_CENTER, state.position)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `setPosition updates to TOP_RIGHT`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.setPosition(NumberPosition.TOP_RIGHT)
        advanceUntilIdle()

        viewModel.state.test {
            val state = awaitItem()
            assertEquals(NumberPosition.TOP_RIGHT, state.position)
            cancelAndIgnoreRemainingEvents()
        }
    }
    // endregion

    // region setFormat Tests
    @Test
    fun `setFormat updates to PAGE_X`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.setFormat(NumberFormat.PAGE_X)
        advanceUntilIdle()

        viewModel.state.test {
            val state = awaitItem()
            assertEquals(NumberFormat.PAGE_X, state.format)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `setFormat updates to X_OF_Y`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.setFormat(NumberFormat.X_OF_Y)
        advanceUntilIdle()

        viewModel.state.test {
            val state = awaitItem()
            assertEquals(NumberFormat.X_OF_Y, state.format)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `setFormat updates to CUSTOM`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.setFormat(NumberFormat.CUSTOM)
        advanceUntilIdle()

        viewModel.state.test {
            val state = awaitItem()
            assertEquals(NumberFormat.CUSTOM, state.format)
            cancelAndIgnoreRemainingEvents()
        }
    }
    // endregion

    // region setFontSize Tests
    @Test
    fun `setFontSize updates font size`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.setFontSize(24f)
        advanceUntilIdle()

        viewModel.state.test {
            val state = awaitItem()
            assertEquals(24f, state.fontSize)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `setFontSize coerces value to minimum`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.setFontSize(2f)
        advanceUntilIdle()

        viewModel.state.test {
            val state = awaitItem()
            assertEquals(8f, state.fontSize)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `setFontSize coerces value to maximum`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.setFontSize(100f)
        advanceUntilIdle()

        viewModel.state.test {
            val state = awaitItem()
            assertEquals(72f, state.fontSize)
            cancelAndIgnoreRemainingEvents()
        }
    }
    // endregion

    // region setTextColor Tests
    @Test
    fun `setTextColor updates color`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.setTextColor(Color.Red)
        advanceUntilIdle()

        viewModel.state.test {
            val state = awaitItem()
            assertEquals(Color.Red, state.textColor)
            cancelAndIgnoreRemainingEvents()
        }
    }
    // endregion

    // region setStartNumber Tests
    @Test
    fun `setStartNumber updates start number`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.setStartNumber(5)
        advanceUntilIdle()

        viewModel.state.test {
            val state = awaitItem()
            assertEquals(5, state.startNumber)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `setStartNumber coerces value to minimum`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.setStartNumber(-5)
        advanceUntilIdle()

        viewModel.state.test {
            val state = awaitItem()
            assertEquals(1, state.startNumber)
            cancelAndIgnoreRemainingEvents()
        }
    }
    // endregion

    // region setCustomPrefix and setCustomSuffix Tests
    @Test
    fun `setCustomPrefix updates prefix`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.setCustomPrefix("Page ")
        advanceUntilIdle()

        viewModel.state.test {
            val state = awaitItem()
            assertEquals("Page ", state.customPrefix)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `setCustomSuffix updates suffix`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.setCustomSuffix(" of 10")
        advanceUntilIdle()

        viewModel.state.test {
            val state = awaitItem()
            assertEquals(" of 10", state.customSuffix)
            cancelAndIgnoreRemainingEvents()
        }
    }
    // endregion

    // region setMarginX and setMarginY Tests
    @Test
    fun `setMarginX updates margin`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.setMarginX(50f)
        advanceUntilIdle()

        viewModel.state.test {
            val state = awaitItem()
            assertEquals(50f, state.marginX)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `setMarginX coerces value to minimum`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.setMarginX(-10f)
        advanceUntilIdle()

        viewModel.state.test {
            val state = awaitItem()
            assertEquals(0f, state.marginX)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `setMarginX coerces value to maximum`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.setMarginX(300f)
        advanceUntilIdle()

        viewModel.state.test {
            val state = awaitItem()
            assertEquals(200f, state.marginX)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `setMarginY updates margin`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.setMarginY(50f)
        advanceUntilIdle()

        viewModel.state.test {
            val state = awaitItem()
            assertEquals(50f, state.marginY)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `setMarginY coerces value to minimum`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.setMarginY(-10f)
        advanceUntilIdle()

        viewModel.state.test {
            val state = awaitItem()
            assertEquals(0f, state.marginY)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `setMarginY coerces value to maximum`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.setMarginY(300f)
        advanceUntilIdle()

        viewModel.state.test {
            val state = awaitItem()
            assertEquals(200f, state.marginY)
            cancelAndIgnoreRemainingEvents()
        }
    }
    // endregion

    // region setPageSelection Tests
    @Test
    fun `setPageSelection updates to ODD`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.setPageSelection(PageSelection.ODD)
        advanceUntilIdle()

        viewModel.state.test {
            val state = awaitItem()
            assertEquals(PageSelection.ODD, state.pageSelection)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `setPageSelection updates to EVEN`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.setPageSelection(PageSelection.EVEN)
        advanceUntilIdle()

        viewModel.state.test {
            val state = awaitItem()
            assertEquals(PageSelection.EVEN, state.pageSelection)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `setPageSelection updates to SKIP_FIRST`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.setPageSelection(PageSelection.SKIP_FIRST)
        advanceUntilIdle()

        viewModel.state.test {
            val state = awaitItem()
            assertEquals(PageSelection.SKIP_FIRST, state.pageSelection)
            cancelAndIgnoreRemainingEvents()
        }
    }
    // endregion

    // region setCustomPages Tests
    @Test
    fun `setCustomPages updates pages`() = runTest {
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
    // endregion

    // region setSkipFirstN Tests
    @Test
    fun `setSkipFirstN updates value`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.setSkipFirstN(3)
        advanceUntilIdle()

        viewModel.state.test {
            val state = awaitItem()
            assertEquals(3, state.skipFirstN)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `setSkipFirstN coerces value to minimum`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.setSkipFirstN(-5)
        advanceUntilIdle()

        viewModel.state.test {
            val state = awaitItem()
            assertEquals(0, state.skipFirstN)
            cancelAndIgnoreRemainingEvents()
        }
    }
    // endregion

    // region setOutputFileName Tests
    @Test
    fun `setOutputFileName updates filename`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.setOutputFileName("my_numbered_doc")
        advanceUntilIdle()

        viewModel.state.test {
            val state = awaitItem()
            assertEquals("my_numbered_doc", state.outputFileName)
            cancelAndIgnoreRemainingEvents()
        }
    }
    // endregion

    // region applyPageNumbers Validation Tests
    @Test
    fun `applyPageNumbers without source file sets error`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.applyPageNumbers()
        advanceUntilIdle()

        viewModel.state.test {
            val state = awaitItem()
            assertEquals("Please select a PDF file first", state.error)
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

        viewModel.setPosition(NumberPosition.TOP_LEFT)
        viewModel.setFormat(NumberFormat.X_OF_Y)
        viewModel.setFontSize(24f)
        viewModel.setStartNumber(5)
        viewModel.setCustomPrefix("Page ")
        viewModel.setPageSelection(PageSelection.ODD)
        viewModel.setOutputFileName("custom_name")
        viewModel.reset()
        advanceUntilIdle()

        viewModel.state.test {
            val state = awaitItem()
            assertNull(state.sourceFile)
            assertEquals(NumberPosition.BOTTOM_CENTER, state.position)
            assertEquals(NumberFormat.NUMBER_ONLY, state.format)
            assertEquals(12f, state.fontSize)
            assertEquals(Color.Black, state.textColor)
            assertEquals(1, state.startNumber)
            assertEquals("", state.customPrefix)
            assertEquals("", state.customSuffix)
            assertEquals(PageSelection.ALL, state.pageSelection)
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

    // region NumberPosition enum Tests
    @Test
    fun `NumberPosition TOP_LEFT has correct label`() {
        assertEquals("Top Left", NumberPosition.TOP_LEFT.label)
    }

    @Test
    fun `NumberPosition TOP_CENTER has correct label`() {
        assertEquals("Top Center", NumberPosition.TOP_CENTER.label)
    }

    @Test
    fun `NumberPosition TOP_RIGHT has correct label`() {
        assertEquals("Top Right", NumberPosition.TOP_RIGHT.label)
    }

    @Test
    fun `NumberPosition BOTTOM_LEFT has correct label`() {
        assertEquals("Bottom Left", NumberPosition.BOTTOM_LEFT.label)
    }

    @Test
    fun `NumberPosition BOTTOM_CENTER has correct label`() {
        assertEquals("Bottom Center", NumberPosition.BOTTOM_CENTER.label)
    }

    @Test
    fun `NumberPosition BOTTOM_RIGHT has correct label`() {
        assertEquals("Bottom Right", NumberPosition.BOTTOM_RIGHT.label)
    }

    @Test
    fun `NumberPosition has 6 values`() {
        assertEquals(6, NumberPosition.entries.size)
    }
    // endregion

    // region NumberFormat enum Tests
    @Test
    fun `NumberFormat NUMBER_ONLY has correct properties`() {
        assertEquals("Number Only", NumberFormat.NUMBER_ONLY.label)
        assertEquals("1, 2, 3...", NumberFormat.NUMBER_ONLY.example)
    }

    @Test
    fun `NumberFormat PAGE_X has correct properties`() {
        assertEquals("Page X", NumberFormat.PAGE_X.label)
        assertEquals("Page 1, Page 2...", NumberFormat.PAGE_X.example)
    }

    @Test
    fun `NumberFormat X_OF_Y has correct properties`() {
        assertEquals("X of Y", NumberFormat.X_OF_Y.label)
        assertEquals("1 of 10, 2 of 10...", NumberFormat.X_OF_Y.example)
    }

    @Test
    fun `NumberFormat DASH_X_DASH has correct properties`() {
        assertEquals("- X -", NumberFormat.DASH_X_DASH.label)
        assertEquals("- 1 -, - 2 -...", NumberFormat.DASH_X_DASH.example)
    }

    @Test
    fun `NumberFormat CUSTOM has correct properties`() {
        assertEquals("Custom", NumberFormat.CUSTOM.label)
        assertEquals("Custom prefix/suffix", NumberFormat.CUSTOM.example)
    }

    @Test
    fun `NumberFormat has 5 values`() {
        assertEquals(5, NumberFormat.entries.size)
    }
    // endregion

    // region PageSelection enum Tests
    @Test
    fun `PageSelection has ALL`() {
        assertEquals("ALL", PageSelection.ALL.name)
    }

    @Test
    fun `PageSelection has ODD`() {
        assertEquals("ODD", PageSelection.ODD.name)
    }

    @Test
    fun `PageSelection has EVEN`() {
        assertEquals("EVEN", PageSelection.EVEN.name)
    }

    @Test
    fun `PageSelection has CUSTOM`() {
        assertEquals("CUSTOM", PageSelection.CUSTOM.name)
    }

    @Test
    fun `PageSelection has SKIP_FIRST`() {
        assertEquals("SKIP_FIRST", PageSelection.SKIP_FIRST.name)
    }

    @Test
    fun `PageSelection has 5 values`() {
        assertEquals(5, PageSelection.entries.size)
    }
    // endregion

    // region Which pages get numbered
    // The selection rules decide which pages are touched, and none of them were
    // covered: they only run once a document is loaded, and no test loaded one.

    /**
     * Loads a document and waits for it to actually arrive.
     *
     * Loading renders a preview on Dispatchers.IO, which the test scheduler does not
     * control, so advanceUntilIdle returns before the document is in state. Waiting
     * on the state itself is what makes this deterministic.
     */
    private fun TestScope.loadDocument(vm: PageNumbersViewModel) {
        vm.setSourceFile(mockk(relaxed = true))
        // Loading starts on the main dispatcher, which only runs when the scheduler
        // is advanced, then renders a preview on Dispatchers.IO, which runs in real
        // time and the scheduler knows nothing about. Neither waiting alone is
        // enough, so this drives one and waits on the other.
        repeat(200) {
            advanceUntilIdle()
            if (vm.state.value.sourceFile != null) return
            Thread.sleep(10)
        }
        error("document never loaded")
    }

    /** Runs the tool and reports the page list handed to the repository. */
    private fun TestScope.pagesNumberedWith(
        configure: (PageNumbersViewModel) -> Unit
    ): List<Int>? {
        val captured = slot<List<Int>?>()
        coEvery {
            pdfToolsRepository.addPageNumbers(any(), any(), any(), captureNullable(captured), any())
        } returns Result.success(Unit)

        val vm = createViewModel()
        loadDocument(vm)
        configure(vm)
        vm.applyPageNumbers()
        advanceUntilIdle()

        return captured.captured
    }

    @Test
    fun `numbering every page passes no page list, meaning all of them`() = runTest {
        val pages = pagesNumberedWith { it.setPageSelection(PageSelection.ALL) }
        assertNull(pages)
    }

    @Test
    fun `odd only numbers the odd pages`() = runTest {
        val pages = pagesNumberedWith { it.setPageSelection(PageSelection.ODD) }
        assertEquals(listOf(1, 3, 5, 7, 9), pages)
    }

    @Test
    fun `even only numbers the even pages`() = runTest {
        val pages = pagesNumberedWith { it.setPageSelection(PageSelection.EVEN) }
        assertEquals(listOf(2, 4, 6, 8, 10), pages)
    }

    @Test
    fun `skipping the first few starts after them`() = runTest {
        val pages = pagesNumberedWith {
            it.setPageSelection(PageSelection.SKIP_FIRST)
            it.setSkipFirstN(3)
        }
        assertEquals(listOf(4, 5, 6, 7, 8, 9, 10), pages)
    }

    @Test
    fun `a custom list of single pages is honoured`() = runTest {
        val pages = pagesNumberedWith {
            it.setPageSelection(PageSelection.CUSTOM)
            it.setCustomPages("2, 5, 9")
        }
        assertEquals(listOf(2, 5, 9), pages)
    }

    @Test
    fun `a custom range is expanded`() = runTest {
        val pages = pagesNumberedWith {
            it.setPageSelection(PageSelection.CUSTOM)
            it.setCustomPages("3-6")
        }
        assertEquals(listOf(3, 4, 5, 6), pages)
    }

    @Test
    fun `custom pages and ranges can be mixed`() = runTest {
        val pages = pagesNumberedWith {
            it.setPageSelection(PageSelection.CUSTOM)
            it.setCustomPages("1, 4-6, 10")
        }
        assertEquals(listOf(1, 4, 5, 6, 10), pages)
    }

    @Test
    fun `custom pages come back in order however they were typed`() = runTest {
        val pages = pagesNumberedWith {
            it.setPageSelection(PageSelection.CUSTOM)
            it.setCustomPages("9, 2, 5")
        }
        assertEquals(listOf(2, 5, 9), pages)
    }

    @Test
    fun `a page named twice is only numbered once`() = runTest {
        val pages = pagesNumberedWith {
            it.setPageSelection(PageSelection.CUSTOM)
            it.setCustomPages("4, 4, 3-5")
        }
        assertEquals(listOf(3, 4, 5), pages)
    }

    @Test
    fun `custom pages past the end of the document are dropped`() = runTest {
        val pages = pagesNumberedWith {
            it.setPageSelection(PageSelection.CUSTOM)
            it.setCustomPages("5, 99, 200")
        }
        assertEquals(listOf(5), pages)
    }

    @Test
    fun `page zero and negatives are dropped rather than numbered`() = runTest {
        val pages = pagesNumberedWith {
            it.setPageSelection(PageSelection.CUSTOM)
            it.setCustomPages("0, -3, 2")
        }
        assertEquals(listOf(2), pages)
    }

    @Test
    fun `text among custom pages is skipped, the rest still count`() = runTest {
        val pages = pagesNumberedWith {
            it.setPageSelection(PageSelection.CUSTOM)
            it.setCustomPages("2, banana, 7")
        }
        assertEquals(listOf(2, 7), pages)
    }

    @Test
    fun `a custom range that runs backwards numbers nothing`() = runTest {
        val pages = pagesNumberedWith {
            it.setPageSelection(PageSelection.CUSTOM)
            it.setCustomPages("8-3")
        }
        assertEquals(emptyList<Int>(), pages)
    }

    @Test
    fun `an empty custom list numbers nothing`() = runTest {
        val pages = pagesNumberedWith {
            it.setPageSelection(PageSelection.CUSTOM)
            it.setCustomPages("")
        }
        assertEquals(emptyList<Int>(), pages)
    }

    @Test
    fun `a range partly past the end keeps the part that fits`() = runTest {
        val pages = pagesNumberedWith {
            it.setPageSelection(PageSelection.CUSTOM)
            it.setCustomPages("8-20")
        }
        assertEquals(listOf(8, 9, 10), pages)
    }
    // endregion

    // region The document reaches the tool
    @Test
    fun `loading a document records its page count`() = runTest {
        val vm = createViewModel()
        loadDocument(vm)

        assertEquals(10, vm.state.value.sourceFile?.pageCount)
    }

    @Test
    fun `the tool is run against the loaded document`() = runTest {
        val vm = createViewModel()
        loadDocument(vm)
        val path = vm.state.value.sourceFile?.path

        coEvery {
            pdfToolsRepository.addPageNumbers(any(), any(), any(), any(), any())
        } returns Result.success(Unit)

        vm.applyPageNumbers()
        advanceUntilIdle()

        coVerify { pdfToolsRepository.addPageNumbers(path!!, any(), any(), any(), any()) }
    }
    // endregion
}
