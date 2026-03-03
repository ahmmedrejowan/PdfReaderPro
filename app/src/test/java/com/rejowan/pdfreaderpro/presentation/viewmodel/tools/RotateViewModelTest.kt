package com.rejowan.pdfreaderpro.presentation.viewmodel.tools

import android.content.Context
import app.cash.turbine.test
import com.rejowan.pdfreaderpro.domain.repository.PdfToolsRepository
import com.rejowan.pdfreaderpro.presentation.screens.tools.rotate.PageSelectionMode
import com.rejowan.pdfreaderpro.presentation.screens.tools.rotate.QuickSelection
import com.rejowan.pdfreaderpro.presentation.screens.tools.rotate.RotateViewModel
import com.rejowan.pdfreaderpro.presentation.screens.tools.rotate.RotationAngle
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
class RotateViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var pdfToolsRepository: PdfToolsRepository
    private lateinit var context: Context
    private lateinit var viewModel: RotateViewModel

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
}
