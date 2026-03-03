package com.rejowan.pdfreaderpro.presentation.viewmodel.tools

import android.content.Context
import app.cash.turbine.test
import com.rejowan.pdfreaderpro.domain.repository.PdfToolsRepository
import com.rejowan.pdfreaderpro.presentation.screens.tools.split.SplitMode
import com.rejowan.pdfreaderpro.presentation.screens.tools.split.SplitViewModel
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
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SplitViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var pdfToolsRepository: PdfToolsRepository
    private lateinit var context: Context
    private lateinit var viewModel: SplitViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        pdfToolsRepository = mockk(relaxed = true)
        context = mockk(relaxed = true)

        coEvery { pdfToolsRepository.getPageCount(any()) } returns Result.success(10)
    }

    @After
    fun teardown() {
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
}
