package com.rejowan.pdfreaderpro.presentation.viewmodel.tools

import android.content.Context
import app.cash.turbine.test
import com.rejowan.pdfreaderpro.domain.repository.PdfToolsRepository
import com.rejowan.pdfreaderpro.presentation.screens.tools.compress.CompressionLevel
import com.rejowan.pdfreaderpro.presentation.screens.tools.compress.CompressResult
import com.rejowan.pdfreaderpro.presentation.screens.tools.compress.CompressViewModel
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
class CompressViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var pdfToolsRepository: PdfToolsRepository
    private lateinit var context: Context
    private lateinit var viewModel: CompressViewModel

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

    private fun createViewModel(): CompressViewModel {
        return CompressViewModel(
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
    fun `initial state has MEDIUM as default compression level`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.state.test {
            val state = awaitItem()
            assertEquals(CompressionLevel.MEDIUM, state.compressionLevel)
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

    // region setCompressionLevel Tests
    @Test
    fun `setCompressionLevel updates to LOW`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.setCompressionLevel(CompressionLevel.LOW)
        advanceUntilIdle()

        viewModel.state.test {
            val state = awaitItem()
            assertEquals(CompressionLevel.LOW, state.compressionLevel)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `setCompressionLevel updates to MEDIUM`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.setCompressionLevel(CompressionLevel.MEDIUM)
        advanceUntilIdle()

        viewModel.state.test {
            val state = awaitItem()
            assertEquals(CompressionLevel.MEDIUM, state.compressionLevel)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `setCompressionLevel updates to HIGH`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.setCompressionLevel(CompressionLevel.HIGH)
        advanceUntilIdle()

        viewModel.state.test {
            val state = awaitItem()
            assertEquals(CompressionLevel.HIGH, state.compressionLevel)
            cancelAndIgnoreRemainingEvents()
        }
    }
    // endregion

    // region setOutputFileName Tests
    @Test
    fun `setOutputFileName updates filename`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.setOutputFileName("my_compressed_file")
        advanceUntilIdle()

        viewModel.state.test {
            val state = awaitItem()
            assertEquals("my_compressed_file", state.outputFileName)
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

    // region compress Validation Tests
    @Test
    fun `compress without source file sets error`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.compress()
        advanceUntilIdle()

        viewModel.state.test {
            val state = awaitItem()
            assertEquals("Please select a PDF file first", state.error)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `compress with blank output filename sets error`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.setOutputFileName("")
        viewModel.compress()
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
        viewModel.setCompressionLevel(CompressionLevel.HIGH)
        viewModel.reset()
        advanceUntilIdle()

        viewModel.state.test {
            val state = awaitItem()
            assertNull(state.sourceFile)
            assertEquals(CompressionLevel.MEDIUM, state.compressionLevel)
            assertFalse(state.isProcessing)
            assertEquals(0f, state.progress)
            assertNull(state.error)
            assertNull(state.result)
            cancelAndIgnoreRemainingEvents()
        }
    }
    // endregion

    // region CompressionLevel enum Tests
    @Test
    fun `CompressionLevel LOW has correct properties`() {
        assertEquals("Low", CompressionLevel.LOW.label)
        assertEquals("Minimal compression, best quality", CompressionLevel.LOW.description)
        assertEquals(0.8f, CompressionLevel.LOW.quality)
    }

    @Test
    fun `CompressionLevel MEDIUM has correct properties`() {
        assertEquals("Medium", CompressionLevel.MEDIUM.label)
        assertEquals("Balanced compression and quality", CompressionLevel.MEDIUM.description)
        assertEquals(0.5f, CompressionLevel.MEDIUM.quality)
    }

    @Test
    fun `CompressionLevel HIGH has correct properties`() {
        assertEquals("High", CompressionLevel.HIGH.label)
        assertEquals("Maximum compression, smaller file", CompressionLevel.HIGH.description)
        assertEquals(0.2f, CompressionLevel.HIGH.quality)
    }

    @Test
    fun `CompressionLevel has 3 values`() {
        assertEquals(3, CompressionLevel.entries.size)
    }
    // endregion

    // region CompressResult Tests
    @Test
    fun `CompressResult reductionPercentage calculates correctly`() {
        val result = CompressResult(
            outputPath = "/storage/output.pdf",
            originalSize = 1000L,
            compressedSize = 600L,
            pageCount = 10
        )
        assertEquals(40f, result.reductionPercentage)
    }

    @Test
    fun `CompressResult reductionPercentage returns 0 for zero original size`() {
        val result = CompressResult(
            outputPath = "/storage/output.pdf",
            originalSize = 0L,
            compressedSize = 0L,
            pageCount = 10
        )
        assertEquals(0f, result.reductionPercentage)
    }

    @Test
    fun `CompressResult savedBytes calculates correctly`() {
        val result = CompressResult(
            outputPath = "/storage/output.pdf",
            originalSize = 1000L,
            compressedSize = 600L,
            pageCount = 10
        )
        assertEquals(400L, result.savedBytes)
    }

    @Test
    fun `CompressResult handles size increase`() {
        val result = CompressResult(
            outputPath = "/storage/output.pdf",
            originalSize = 500L,
            compressedSize = 600L,
            pageCount = 10
        )
        assertEquals(-100L, result.savedBytes)
        assertEquals(-20f, result.reductionPercentage)
    }

    @Test
    fun `CompressResult 100 percent reduction`() {
        val result = CompressResult(
            outputPath = "/storage/output.pdf",
            originalSize = 1000L,
            compressedSize = 0L,
            pageCount = 10
        )
        assertEquals(100f, result.reductionPercentage)
        assertEquals(1000L, result.savedBytes)
    }

    @Test
    fun `CompressResult no reduction`() {
        val result = CompressResult(
            outputPath = "/storage/output.pdf",
            originalSize = 1000L,
            compressedSize = 1000L,
            pageCount = 10
        )
        assertEquals(0f, result.reductionPercentage)
        assertEquals(0L, result.savedBytes)
    }
    // endregion
}
