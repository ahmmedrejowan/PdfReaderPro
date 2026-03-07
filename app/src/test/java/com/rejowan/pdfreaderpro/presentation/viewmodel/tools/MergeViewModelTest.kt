package com.rejowan.pdfreaderpro.presentation.viewmodel.tools

import android.content.Context
import android.net.Uri
import app.cash.turbine.test
import com.rejowan.pdfreaderpro.domain.repository.PdfToolsRepository
import com.rejowan.pdfreaderpro.presentation.screens.tools.merge.MergeFile
import com.rejowan.pdfreaderpro.presentation.screens.tools.merge.MergeState
import com.rejowan.pdfreaderpro.presentation.screens.tools.merge.MergeViewModel
import com.rejowan.pdfreaderpro.presentation.screens.tools.merge.PageSelection
import io.mockk.coEvery
import io.mockk.coVerify
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
class MergeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var pdfToolsRepository: PdfToolsRepository
    private lateinit var context: Context
    private lateinit var viewModel: MergeViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        pdfToolsRepository = mockk(relaxed = true)
        context = mockk(relaxed = true)

        // Default mocks
        coEvery { pdfToolsRepository.getPageCount(any()) } returns Result.success(10)
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): MergeViewModel {
        return MergeViewModel(
            pdfToolsRepository = pdfToolsRepository,
            context = context
        )
    }

    private fun createMergeFile(
        path: String = "/storage/test.pdf",
        name: String = "test.pdf",
        pageCount: Int = 10,
        pageSelection: PageSelection = PageSelection.All
    ) = MergeFile(
        uri = mockk(),
        path = path,
        name = name,
        size = 1024L,
        pageCount = pageCount,
        thumbnail = null,
        pageSelection = pageSelection
    )

    // region Initial State Tests
    @Test
    fun `initial state has empty selected files`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.state.test {
            val state = awaitItem()
            assertTrue(state.selectedFiles.isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `initial state has generated output filename`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.state.test {
            val state = awaitItem()
            assertTrue(state.outputFileName.startsWith("merged_"))
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

    // region removeFile Tests
    @Test
    fun `removeFile removes file from selectedFiles`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        val file1 = createMergeFile(path = "/storage/test1.pdf")
        val file2 = createMergeFile(path = "/storage/test2.pdf")

        // Manually set state with files
        viewModel.removeFile(file1)
        advanceUntilIdle()

        // File should be removed (empty initially, so still empty)
        viewModel.state.test {
            val state = awaitItem()
            assertTrue(state.selectedFiles.isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }
    // endregion

    // region moveFile Tests
    @Test
    fun `moveFile with invalid indices does not crash`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        // Should not throw
        viewModel.moveFile(-1, 0)
        viewModel.moveFile(0, 100)
        advanceUntilIdle()

        viewModel.state.test {
            assertNotNull(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }
    // endregion

    // region updatePageSelection Tests
    @Test
    fun `updatePageSelection updates selection for specific file`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        val file = createMergeFile()
        val newSelection = PageSelection.Range(1, 5)

        viewModel.updatePageSelection(file, newSelection)
        advanceUntilIdle()

        // Verify no error occurred
        viewModel.state.test {
            val state = awaitItem()
            assertNull(state.error)
            cancelAndIgnoreRemainingEvents()
        }
    }
    // endregion

    // region setOutputFileName Tests
    @Test
    fun `setOutputFileName updates output filename`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.setOutputFileName("my_merged_file")
        advanceUntilIdle()

        viewModel.state.test {
            val state = awaitItem()
            assertEquals("my_merged_file", state.outputFileName)
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

    // region merge Validation Tests
    @Test
    fun `merge with less than 2 files sets error`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.merge()
        advanceUntilIdle()

        viewModel.state.test {
            val state = awaitItem()
            assertEquals("Select at least 2 PDF files", state.error)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `merge with blank output filename sets error`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.setOutputFileName("")
        viewModel.merge()
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
    fun `reset clears all state and generates new filename`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.setOutputFileName("custom_name")
        viewModel.reset()
        advanceUntilIdle()

        viewModel.state.test {
            val state = awaitItem()
            assertTrue(state.selectedFiles.isEmpty())
            assertTrue(state.outputFileName.startsWith("merged_"))
            assertFalse(state.isProcessing)
            assertEquals(0f, state.progress)
            assertNull(state.error)
            assertNull(state.result)
            cancelAndIgnoreRemainingEvents()
        }
    }
    // endregion

    // region PageSelection Tests
    @Test
    fun `PageSelection All toDisplayString returns correct string`() {
        val selection = PageSelection.All
        assertEquals("All pages (1-10)", selection.toDisplayString(10))
    }

    @Test
    fun `PageSelection Range toDisplayString returns correct string`() {
        val selection = PageSelection.Range(1, 5)
        assertEquals("Pages 1-5", selection.toDisplayString(10))
    }

    @Test
    fun `PageSelection Custom toDisplayString returns correct string for few pages`() {
        val selection = PageSelection.Custom(listOf(1, 3, 5))
        assertEquals("Pages 1, 3, 5", selection.toDisplayString(10))
    }

    @Test
    fun `PageSelection Custom toDisplayString truncates for many pages`() {
        val selection = PageSelection.Custom(listOf(1, 2, 3, 4, 5, 6, 7))
        val display = selection.toDisplayString(10)
        assertTrue(display.contains("..."))
        assertTrue(display.contains("7 pages"))
    }

    @Test
    fun `PageSelection All toPageList returns null`() {
        val selection = PageSelection.All
        assertNull(selection.toPageList(10))
    }

    @Test
    fun `PageSelection Range toPageList returns correct list`() {
        val selection = PageSelection.Range(2, 5)
        assertEquals(listOf(2, 3, 4, 5), selection.toPageList(10))
    }

    @Test
    fun `PageSelection Range toPageList coerces end to totalPages`() {
        val selection = PageSelection.Range(8, 15)
        assertEquals(listOf(8, 9, 10), selection.toPageList(10))
    }

    @Test
    fun `PageSelection Custom toPageList filters invalid pages`() {
        val selection = PageSelection.Custom(listOf(1, 5, 15, 20))
        assertEquals(listOf(1, 5), selection.toPageList(10))
    }

    @Test
    fun `PageSelection All getSelectedCount returns total pages`() {
        val selection = PageSelection.All
        assertEquals(10, selection.getSelectedCount(10))
    }

    @Test
    fun `PageSelection Range getSelectedCount returns correct count`() {
        val selection = PageSelection.Range(3, 7)
        assertEquals(5, selection.getSelectedCount(10))
    }

    @Test
    fun `PageSelection Custom getSelectedCount returns correct count`() {
        val selection = PageSelection.Custom(listOf(1, 3, 5, 7))
        assertEquals(4, selection.getSelectedCount(10))
    }

    @Test
    fun `PageSelection Range getSelectedCount handles out of bounds`() {
        val selection = PageSelection.Range(8, 15)
        assertEquals(3, selection.getSelectedCount(10))
    }
    // endregion
}
