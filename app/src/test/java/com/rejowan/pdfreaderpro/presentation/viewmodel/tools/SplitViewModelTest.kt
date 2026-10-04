package com.rejowan.pdfreaderpro.presentation.viewmodel.tools

import android.app.Application
import android.os.Environment
import app.cash.turbine.test
import com.rejowan.pdfreaderpro.domain.repository.PdfToolsRepository
import com.rejowan.pdfreaderpro.presentation.screens.tools.split.SplitMode
import com.rejowan.pdfreaderpro.presentation.screens.tools.split.SplitViewModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkStatic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import io.mockk.every
import java.io.ByteArrayInputStream
import java.io.File
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SplitViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var pdfToolsRepository: PdfToolsRepository
    private lateinit var context: Application
    private lateinit var viewModel: SplitViewModel

    @get:Rule
    val folder = TemporaryFolder()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        pdfToolsRepository = mockk(relaxed = true)
        context = mockk(relaxed = true)

        // The view model copies the picked document into the cache before it can
        // report a page count, and everything that validates page ranges needs that
        // page count. Giving it a real directory and a real stream makes that path
        // succeed, which is what puts the range logic within reach of a test.
        every { context.cacheDir } returns folder.newFolder("cache")
        every { context.contentResolver.openInputStream(any()) } answers {
            ByteArrayInputStream("%PDF-1.4 pretend document".toByteArray())
        }
        every { context.contentResolver.query(any(), any(), any(), any(), any()) } returns null

        coEvery { pdfToolsRepository.getPageCount(any()) } returns Result.success(10)

        // Splitting writes into a folder under the public Documents directory,
        // which off device throws from inside the coroutine.
        mockkStatic(Environment::class)
        every {
            Environment.getExternalStoragePublicDirectory(any())
        } returns folder.newFolder("documents")
    }

    /**
     * A view model with the document already loaded.
     *
     * Loading is asynchronous, so this waits for it. Without that the page count is
     * still zero, every range validates trivially, and the tests pass for the wrong
     * reason.
     */
    private fun TestScope.viewModelWithSource(): SplitViewModel {
        val vm = createViewModel()
        vm.setSourceFile(mockk(relaxed = true))
        advanceUntilIdle()
        return vm
    }

    @After
    fun teardown() {
        unmockkStatic(Environment::class)
        Dispatchers.resetMain()
    }

    private fun createViewModel(): SplitViewModel {
        return SplitViewModel(
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
    fun `initial state has BY_RANGES as default split mode`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.state.test {
            val state = awaitItem()
            assertEquals(SplitMode.BY_RANGES, state.splitMode)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `initial state has generated output prefix`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.state.test {
            val state = awaitItem()
            assertTrue(state.outputPrefix.startsWith("split_"))
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
    fun `initial state has empty ranges input`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.state.test {
            val state = awaitItem()
            assertEquals("", state.rangesInput)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `initial state has 5 as default everyNPages`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.state.test {
            val state = awaitItem()
            assertEquals(5, state.everyNPages)
            cancelAndIgnoreRemainingEvents()
        }
    }
    // endregion

    // region setSplitMode Tests
    @Test
    fun `setSplitMode updates split mode to BY_RANGES`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.setSplitMode(SplitMode.BY_RANGES)
        advanceUntilIdle()

        viewModel.state.test {
            val state = awaitItem()
            assertEquals(SplitMode.BY_RANGES, state.splitMode)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `setSplitMode updates split mode to EVERY_N_PAGES`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.setSplitMode(SplitMode.EVERY_N_PAGES)
        advanceUntilIdle()

        viewModel.state.test {
            val state = awaitItem()
            assertEquals(SplitMode.EVERY_N_PAGES, state.splitMode)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `setSplitMode updates split mode to INTO_PAGES`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.setSplitMode(SplitMode.INTO_PAGES)
        advanceUntilIdle()

        viewModel.state.test {
            val state = awaitItem()
            assertEquals(SplitMode.INTO_PAGES, state.splitMode)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `setSplitMode updates split mode to SPECIFIC_PAGES`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.setSplitMode(SplitMode.SPECIFIC_PAGES)
        advanceUntilIdle()

        viewModel.state.test {
            val state = awaitItem()
            assertEquals(SplitMode.SPECIFIC_PAGES, state.splitMode)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `setSplitMode clears error`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.setSplitMode(SplitMode.EVERY_N_PAGES)
        advanceUntilIdle()

        viewModel.state.test {
            val state = awaitItem()
            assertNull(state.error)
            cancelAndIgnoreRemainingEvents()
        }
    }
    // endregion

    // region setRangesInput Tests
    @Test
    fun `setRangesInput updates ranges input`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.setRangesInput("1-5, 6-10")
        advanceUntilIdle()

        viewModel.state.test {
            val state = awaitItem()
            assertEquals("1-5, 6-10", state.rangesInput)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `setRangesInput with valid input has no error`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.setRangesInput("1-5")
        advanceUntilIdle()

        viewModel.state.test {
            val state = awaitItem()
            assertNull(state.rangesError)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `setRangesInput with empty input has no error`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.setRangesInput("")
        advanceUntilIdle()

        viewModel.state.test {
            val state = awaitItem()
            assertNull(state.rangesError)
            cancelAndIgnoreRemainingEvents()
        }
    }
    // endregion

    // region setEveryNPages Tests
    @Test
    fun `setEveryNPages updates value`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.setEveryNPages(3)
        advanceUntilIdle()

        viewModel.state.test {
            val state = awaitItem()
            assertEquals(3, state.everyNPages)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `setEveryNPages coerces value to minimum 1`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.setEveryNPages(0)
        advanceUntilIdle()

        viewModel.state.test {
            val state = awaitItem()
            assertEquals(1, state.everyNPages)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `setEveryNPages clears error`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.setEveryNPages(10)
        advanceUntilIdle()

        viewModel.state.test {
            val state = awaitItem()
            assertNull(state.error)
            cancelAndIgnoreRemainingEvents()
        }
    }
    // endregion

    // region setSpecificPagesInput Tests
    @Test
    fun `setSpecificPagesInput updates input`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.setSpecificPagesInput("1, 3, 5-8")
        advanceUntilIdle()

        viewModel.state.test {
            val state = awaitItem()
            assertEquals("1, 3, 5-8", state.specificPagesInput)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `setSpecificPagesInput with empty input has no error`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.setSpecificPagesInput("")
        advanceUntilIdle()

        viewModel.state.test {
            val state = awaitItem()
            assertNull(state.specificPagesError)
            cancelAndIgnoreRemainingEvents()
        }
    }
    // endregion

    // region setOutputPrefix Tests
    @Test
    fun `setOutputPrefix updates output prefix`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.setOutputPrefix("my_split")
        advanceUntilIdle()

        viewModel.state.test {
            val state = awaitItem()
            assertEquals("my_split", state.outputPrefix)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `setOutputPrefix with empty string updates state`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.setOutputPrefix("")
        advanceUntilIdle()

        viewModel.state.test {
            val state = awaitItem()
            assertEquals("", state.outputPrefix)
            cancelAndIgnoreRemainingEvents()
        }
    }
    // endregion

    // region split Validation Tests
    @Test
    fun `split without source file sets error`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.split()
        advanceUntilIdle()

        viewModel.state.test {
            val state = awaitItem()
            assertEquals("No PDF file selected", state.error)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `split with blank output prefix sets error`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.setOutputPrefix("")
        viewModel.split()
        advanceUntilIdle()

        viewModel.state.test {
            val state = awaitItem()
            assertNotNull(state.error)
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
    fun `reset clears all state and generates new prefix`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.setOutputPrefix("custom_prefix")
        viewModel.setSplitMode(SplitMode.INTO_PAGES)
        viewModel.setEveryNPages(10)
        viewModel.reset()
        advanceUntilIdle()

        viewModel.state.test {
            val state = awaitItem()
            assertNull(state.sourceFile)
            assertEquals(SplitMode.BY_RANGES, state.splitMode)
            assertTrue(state.outputPrefix.startsWith("split_"))
            assertEquals(5, state.everyNPages)
            assertFalse(state.isProcessing)
            assertEquals(0f, state.progress)
            assertNull(state.error)
            assertNull(state.result)
            cancelAndIgnoreRemainingEvents()
        }
    }
    // endregion

    // region SplitMode enum Tests
    @Test
    fun `SplitMode BY_RANGES exists`() {
        assertEquals("BY_RANGES", SplitMode.BY_RANGES.name)
    }

    @Test
    fun `SplitMode EVERY_N_PAGES exists`() {
        assertEquals("EVERY_N_PAGES", SplitMode.EVERY_N_PAGES.name)
    }

    @Test
    fun `SplitMode INTO_PAGES exists`() {
        assertEquals("INTO_PAGES", SplitMode.INTO_PAGES.name)
    }

    @Test
    fun `SplitMode SPECIFIC_PAGES exists`() {
        assertEquals("SPECIFIC_PAGES", SplitMode.SPECIFIC_PAGES.name)
    }

    @Test
    fun `SplitMode has 4 values`() {
        assertEquals(4, SplitMode.entries.size)
    }
    // endregion

    // region Range validation
    // Reached through setRangesInput, which is what the screen calls on every
    // keystroke. Nothing here was covered before: the validation only runs once a
    // document is loaded, and no test had loaded one.

    private fun TestScope.rangesErrorFor(input: String): String? {
        val vm = viewModelWithSource()
        vm.setRangesInput(input)
        return vm.state.value.rangesError
    }

    @Test
    fun `a valid single range is accepted`() = runTest {
        assertNull(rangesErrorFor("1-5"))
    }

    @Test
    fun `several valid ranges are accepted`() = runTest {
        assertNull(rangesErrorFor("1-3, 4-6, 7-10"))
    }

    @Test
    fun `a single page is a valid range`() = runTest {
        assertNull(rangesErrorFor("7"))
    }

    @Test
    fun `blank input is not an error, it is just unfinished`() = runTest {
        assertNull(rangesErrorFor("   "))
    }

    @Test
    fun `a page beyond the document is rejected`() = runTest {
        val error = rangesErrorFor("11")
        assertNotNull(error)
        assertTrue(error!!.contains("11"))
    }

    @Test
    fun `a range ending beyond the document is rejected`() = runTest {
        assertNotNull(rangesErrorFor("5-11"))
    }

    @Test
    fun `page zero is rejected, pages are counted from one`() = runTest {
        assertNotNull(rangesErrorFor("0"))
    }

    @Test
    fun `a backwards range is rejected`() = runTest {
        val error = rangesErrorFor("8-3")
        assertNotNull(error)
        assertTrue(error!!.contains("start > end"))
    }

    @Test
    fun `text where a number belongs is rejected`() = runTest {
        assertNotNull(rangesErrorFor("one-five"))
    }

    @Test
    fun `too many dashes is rejected rather than guessed at`() = runTest {
        assertNotNull(rangesErrorFor("1-5-9"))
    }

    @Test
    fun `surrounding spaces are tolerated`() = runTest {
        assertNull(rangesErrorFor("  1 - 5 ,  6 - 10  "))
    }

    @Test
    fun `an empty segment between commas is skipped`() = runTest {
        assertNull(rangesErrorFor("1-5, , 6-10"))
    }

    @Test
    fun `the input is kept even when it does not validate`() = runTest {
        val vm = viewModelWithSource()
        vm.setRangesInput("99")
        // Rejecting the text as well as flagging it would delete what is being typed.
        assertEquals("99", vm.state.value.rangesInput)
    }
    // endregion

    // region Specific page validation
    private fun TestScope.specificErrorFor(input: String): String? {
        val vm = viewModelWithSource()
        vm.setSpecificPagesInput(input)
        return vm.state.value.specificPagesError
    }

    @Test
    fun `a list of single pages is accepted`() = runTest {
        assertNull(specificErrorFor("1, 3, 5"))
    }

    @Test
    fun `pages and ranges can be mixed`() = runTest {
        assertNull(specificErrorFor("1, 3-5, 9"))
    }

    @Test
    fun `a specific page beyond the document is rejected`() = runTest {
        assertNotNull(specificErrorFor("1, 99"))
    }

    @Test
    fun `a backwards range among specific pages is rejected`() = runTest {
        assertNotNull(specificErrorFor("9-2"))
    }

    @Test
    fun `text among specific pages is rejected`() = runTest {
        assertNotNull(specificErrorFor("1, two, 3"))
    }
    // endregion

    // region Defaults from the loaded document
    @Test
    fun `loading a document fills in a default range covering all of it`() = runTest {
        val vm = viewModelWithSource()

        val ranges = vm.state.value.rangesInput
        assertTrue("expected a default range, got '$ranges'", ranges.isNotBlank())
        // Ten pages splits down the middle rather than offering one huge range.
        assertEquals("1-5, 6-10", ranges)
    }

    @Test
    fun `a short document gets a single range`() = runTest {
        coEvery { pdfToolsRepository.getPageCount(any()) } returns Result.success(4)
        val vm = viewModelWithSource()

        assertEquals("1-4", vm.state.value.rangesInput)
    }

    @Test
    fun `loading a document records its page count`() = runTest {
        val vm = viewModelWithSource()

        assertEquals(10, vm.state.value.sourceFile?.pageCount)
    }

    @Test
    fun `every n pages is clamped to the document length`() = runTest {
        val vm = viewModelWithSource()

        vm.setEveryNPages(500)
        assertEquals(10, vm.state.value.everyNPages)
    }

    @Test
    fun `every n pages is never less than one`() = runTest {
        val vm = viewModelWithSource()

        vm.setEveryNPages(0)
        assertEquals(1, vm.state.value.everyNPages)
    }
    // endregion

    // region Refusing to split
    @Test
    fun `splitting without a document asks for one`() = runTest {
        val vm = createViewModel()

        vm.split()
        advanceUntilIdle()

        assertNotNull(vm.state.value.error)
        coVerify(exactly = 0) { pdfToolsRepository.splitPdf(any(), any(), any(), any()) }
    }

    @Test
    fun `splitting without a prefix asks for one`() = runTest {
        val vm = viewModelWithSource()
        vm.setOutputPrefix("")

        vm.split()
        advanceUntilIdle()

        assertNotNull(vm.state.value.error)
        coVerify(exactly = 0) { pdfToolsRepository.splitPdf(any(), any(), any(), any()) }
    }

    @Test
    fun `ranges mode with nothing typed asks for ranges`() = runTest {
        val vm = viewModelWithSource()
        vm.setSplitMode(SplitMode.BY_RANGES)
        vm.setRangesInput("")

        vm.split()
        advanceUntilIdle()

        assertNotNull(vm.state.value.error)
        coVerify(exactly = 0) { pdfToolsRepository.splitPdf(any(), any(), any(), any()) }
    }

    @Test
    fun `ranges mode surfaces the validation message rather than a generic one`() = runTest {
        // The message under the field is the one that says what is wrong, so it is
        // the one that has to be shown when the button is pressed anyway.
        val vm = viewModelWithSource()
        vm.setSplitMode(SplitMode.BY_RANGES)
        vm.setRangesInput("1-99")

        vm.split()
        advanceUntilIdle()

        assertEquals(vm.state.value.rangesError, vm.state.value.error)
        coVerify(exactly = 0) { pdfToolsRepository.splitPdf(any(), any(), any(), any()) }
    }

    @Test
    fun `extract mode with nothing typed asks for pages`() = runTest {
        val vm = viewModelWithSource()
        vm.setSplitMode(SplitMode.SPECIFIC_PAGES)
        vm.setSpecificPagesInput("")

        vm.split()
        advanceUntilIdle()

        assertNotNull(vm.state.value.error)
        coVerify(exactly = 0) { pdfToolsRepository.extractPages(any(), any(), any(), any()) }
    }

    @Test
    fun `extract mode surfaces the validation message`() = runTest {
        val vm = viewModelWithSource()
        vm.setSplitMode(SplitMode.SPECIFIC_PAGES)
        vm.setSpecificPagesInput("4, nonsense")

        vm.split()
        advanceUntilIdle()

        assertEquals(vm.state.value.specificPagesError, vm.state.value.error)
        coVerify(exactly = 0) { pdfToolsRepository.extractPages(any(), any(), any(), any()) }
    }
    // endregion

    // region Splitting by ranges
    @Test
    fun `the ranges typed are the ranges split on`() = runTest {
        val vm = viewModelWithSource()
        vm.setSplitMode(SplitMode.BY_RANGES)
        vm.setRangesInput("1-3, 4-6, 9")
        val ranges = slot<List<String>>()
        coEvery {
            pdfToolsRepository.splitPdf(any(), any(), capture(ranges), any())
        } returns Result.success(listOf("a.pdf"))

        vm.split()
        advanceUntilIdle()

        assertEquals(listOf("1-3", "4-6", "9"), ranges.captured)
    }

    @Test
    fun `the output folder is named after the prefix`() = runTest {
        val vm = viewModelWithSource()
        vm.setSplitMode(SplitMode.BY_RANGES)
        vm.setRangesInput("1-3")
        vm.setOutputPrefix("chapter")
        val outputDir = slot<String>()
        coEvery {
            pdfToolsRepository.splitPdf(any(), capture(outputDir), any(), any())
        } returns Result.success(listOf("a.pdf"))

        vm.split()
        advanceUntilIdle()

        assertTrue(outputDir.captured.endsWith("split_chapter"))
        assertTrue(File(outputDir.captured).isDirectory)
    }
    // endregion

    // region Splitting every n pages
    @Test
    fun `every n pages covers the document once, with no gaps`() = runTest {
        val vm = viewModelWithSource()
        vm.setSplitMode(SplitMode.EVERY_N_PAGES)
        vm.setEveryNPages(3)
        val ranges = slot<List<String>>()
        coEvery {
            pdfToolsRepository.splitPdf(any(), any(), capture(ranges), any())
        } returns Result.success(listOf("a.pdf"))

        vm.split()
        advanceUntilIdle()

        // Ten pages in threes: the last chunk is short rather than running past
        // the end of the document.
        assertEquals(listOf("1-3", "4-6", "7-9", "10-10"), ranges.captured)
    }

    @Test
    fun `a chunk size the document divides evenly leaves no short chunk`() = runTest {
        val vm = viewModelWithSource()
        vm.setSplitMode(SplitMode.EVERY_N_PAGES)
        vm.setEveryNPages(5)
        val ranges = slot<List<String>>()
        coEvery {
            pdfToolsRepository.splitPdf(any(), any(), capture(ranges), any())
        } returns Result.success(listOf("a.pdf"))

        vm.split()
        advanceUntilIdle()

        assertEquals(listOf("1-5", "6-10"), ranges.captured)
    }

    @Test
    fun `a chunk size of one gives a range per page`() = runTest {
        val vm = viewModelWithSource()
        vm.setSplitMode(SplitMode.EVERY_N_PAGES)
        vm.setEveryNPages(1)
        val ranges = slot<List<String>>()
        coEvery {
            pdfToolsRepository.splitPdf(any(), any(), capture(ranges), any())
        } returns Result.success(listOf("a.pdf"))

        vm.split()
        advanceUntilIdle()

        assertEquals(10, ranges.captured.size)
        assertEquals("1-1", ranges.captured.first())
        assertEquals("10-10", ranges.captured.last())
    }
    // endregion

    // region Splitting into single pages
    @Test
    fun `into pages hands the whole document to the repository`() = runTest {
        val vm = viewModelWithSource()
        vm.setSplitMode(SplitMode.INTO_PAGES)
        coEvery {
            pdfToolsRepository.splitIntoPages(any(), any(), any())
        } returns Result.success((1..10).map { "page$it.pdf" })

        vm.split()
        advanceUntilIdle()

        coVerify(exactly = 1) { pdfToolsRepository.splitIntoPages(any(), any(), any()) }
        assertEquals(10, vm.state.value.result!!.createdFiles.size)
    }
    // endregion

    // region Extracting pages
    @Test
    fun `extracting collects the pages typed, in order and without repeats`() = runTest {
        val vm = viewModelWithSource()
        vm.setSplitMode(SplitMode.SPECIFIC_PAGES)
        vm.setSpecificPagesInput("7, 2-4, 2")
        val pages = slot<List<Int>>()
        coEvery {
            pdfToolsRepository.extractPages(any(), any(), capture(pages), any())
        } returns Result.success(Unit)

        vm.split()
        advanceUntilIdle()

        assertEquals(listOf(2, 3, 4, 7), pages.captured)
    }

    @Test
    fun `extracting writes one file, named for the prefix`() = runTest {
        val vm = viewModelWithSource()
        vm.setSplitMode(SplitMode.SPECIFIC_PAGES)
        vm.setSpecificPagesInput("1-2")
        vm.setOutputPrefix("pickings")
        coEvery {
            pdfToolsRepository.extractPages(any(), any(), any(), any())
        } returns Result.success(Unit)

        vm.split()
        advanceUntilIdle()

        val created = vm.state.value.result!!.createdFiles
        assertEquals(1, created.size)
        assertTrue(created.single().endsWith("pickings_extracted.pdf"))
    }
    // endregion

    // region Reporting
    @Test
    fun `progress from the repository reaches the screen`() = runTest {
        val vm = viewModelWithSource()
        vm.setSplitMode(SplitMode.BY_RANGES)
        vm.setRangesInput("1-5")
        val seen = mutableListOf<Float>()
        coEvery {
            pdfToolsRepository.splitPdf(any(), any(), any(), any())
        } answers {
            val onProgress = arg<(Float) -> Unit>(3)
            onProgress(0.5f)
            seen += vm.state.value.progress
            Result.success(listOf("a.pdf"))
        }

        vm.split()
        advanceUntilIdle()

        assertEquals(listOf(0.5f), seen)
        assertEquals(1f, vm.state.value.progress, 0.001f)
        assertFalse(vm.state.value.isProcessing)
    }

    @Test
    fun `a failure is reported and does not leave the tool processing`() = runTest {
        val vm = viewModelWithSource()
        vm.setSplitMode(SplitMode.BY_RANGES)
        vm.setRangesInput("1-5")
        coEvery {
            pdfToolsRepository.splitPdf(any(), any(), any(), any())
        } returns Result.failure(RuntimeException("the document is damaged"))

        vm.split()
        advanceUntilIdle()

        assertEquals("the document is damaged", vm.state.value.error)
        assertFalse(vm.state.value.isProcessing)
        assertNull(vm.state.value.result)
    }

    @Test
    fun `the result lists every file that was written`() = runTest {
        val vm = viewModelWithSource()
        vm.setSplitMode(SplitMode.BY_RANGES)
        vm.setRangesInput("1-3, 4-6")
        coEvery {
            pdfToolsRepository.splitPdf(any(), any(), any(), any())
        } returns Result.success(listOf("part1.pdf", "part2.pdf"))

        vm.split()
        advanceUntilIdle()

        assertEquals(listOf("part1.pdf", "part2.pdf"), vm.state.value.result!!.createdFiles)
    }
    // endregion
}
