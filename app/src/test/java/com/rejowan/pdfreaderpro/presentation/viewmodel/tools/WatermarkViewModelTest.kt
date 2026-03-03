package com.rejowan.pdfreaderpro.presentation.viewmodel.tools

import android.content.Context
import androidx.compose.ui.graphics.Color
import app.cash.turbine.test
import com.rejowan.pdfreaderpro.domain.repository.PdfToolsRepository
import com.rejowan.pdfreaderpro.presentation.screens.tools.watermark.PageSelection
import com.rejowan.pdfreaderpro.presentation.screens.tools.watermark.WatermarkPosition
import com.rejowan.pdfreaderpro.presentation.screens.tools.watermark.WatermarkType
import com.rejowan.pdfreaderpro.presentation.screens.tools.watermark.WatermarkViewModel
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
class WatermarkViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var pdfToolsRepository: PdfToolsRepository
    private lateinit var context: Context
    private lateinit var viewModel: WatermarkViewModel

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

    private fun createViewModel(): WatermarkViewModel {
        return WatermarkViewModel(
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
    fun `initial state has TEXT as default watermark type`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.state.test {
            val state = awaitItem()
            assertEquals(WatermarkType.TEXT, state.watermarkType)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `initial state has default watermark text`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.state.test {
            val state = awaitItem()
            assertEquals("CONFIDENTIAL", state.watermarkText)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `initial state has default font size`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.state.test {
            val state = awaitItem()
            assertEquals(48f, state.fontSize)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `initial state has default text opacity`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.state.test {
            val state = awaitItem()
            assertEquals(50f, state.textOpacity)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `initial state has default text rotation`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.state.test {
            val state = awaitItem()
            assertEquals(-45f, state.textRotation)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `initial state has no image path`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.state.test {
            val state = awaitItem()
            assertNull(state.imagePath)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `initial state has default image scale`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.state.test {
            val state = awaitItem()
            assertEquals(30f, state.imageScale)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `initial state has default image opacity`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.state.test {
            val state = awaitItem()
            assertEquals(50f, state.imageOpacity)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `initial state has CENTER as default position`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.state.test {
            val state = awaitItem()
            assertEquals(WatermarkPosition.CENTER, state.position)
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

    // region setWatermarkType Tests
    @Test
    fun `setWatermarkType updates to TEXT`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.setWatermarkType(WatermarkType.TEXT)
        advanceUntilIdle()

        viewModel.state.test {
            val state = awaitItem()
            assertEquals(WatermarkType.TEXT, state.watermarkType)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `setWatermarkType updates to IMAGE`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.setWatermarkType(WatermarkType.IMAGE)
        advanceUntilIdle()

        viewModel.state.test {
            val state = awaitItem()
            assertEquals(WatermarkType.IMAGE, state.watermarkType)
            cancelAndIgnoreRemainingEvents()
        }
    }
    // endregion

    // region Text Watermark Settings Tests
    @Test
    fun `setWatermarkText updates text`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.setWatermarkText("DRAFT")
        advanceUntilIdle()

        viewModel.state.test {
            val state = awaitItem()
            assertEquals("DRAFT", state.watermarkText)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `setFontSize updates font size`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.setFontSize(72f)
        advanceUntilIdle()

        viewModel.state.test {
            val state = awaitItem()
            assertEquals(72f, state.fontSize)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `setFontSize coerces value to minimum`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.setFontSize(5f)
        advanceUntilIdle()

        viewModel.state.test {
            val state = awaitItem()
            assertEquals(12f, state.fontSize)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `setFontSize coerces value to maximum`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.setFontSize(300f)
        advanceUntilIdle()

        viewModel.state.test {
            val state = awaitItem()
            assertEquals(200f, state.fontSize)
            cancelAndIgnoreRemainingEvents()
        }
    }

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

    @Test
    fun `setTextOpacity updates opacity`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.setTextOpacity(75f)
        advanceUntilIdle()

        viewModel.state.test {
            val state = awaitItem()
            assertEquals(75f, state.textOpacity)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `setTextOpacity coerces value to minimum`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.setTextOpacity(0f)
        advanceUntilIdle()

        viewModel.state.test {
            val state = awaitItem()
            assertEquals(1f, state.textOpacity)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `setTextOpacity coerces value to maximum`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.setTextOpacity(150f)
        advanceUntilIdle()

        viewModel.state.test {
            val state = awaitItem()
            assertEquals(100f, state.textOpacity)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `setTextRotation updates rotation`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.setTextRotation(45f)
        advanceUntilIdle()

        viewModel.state.test {
            val state = awaitItem()
            assertEquals(45f, state.textRotation)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `setTextRotation coerces value to minimum`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.setTextRotation(-200f)
        advanceUntilIdle()

        viewModel.state.test {
            val state = awaitItem()
            assertEquals(-180f, state.textRotation)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `setTextRotation coerces value to maximum`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.setTextRotation(200f)
        advanceUntilIdle()

        viewModel.state.test {
            val state = awaitItem()
            assertEquals(180f, state.textRotation)
            cancelAndIgnoreRemainingEvents()
        }
    }
    // endregion

    // region Image Watermark Settings Tests
    @Test
    fun `setImageScale updates scale`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.setImageScale(50f)
        advanceUntilIdle()

        viewModel.state.test {
            val state = awaitItem()
            assertEquals(50f, state.imageScale)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `setImageScale coerces value to minimum`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.setImageScale(0f)
        advanceUntilIdle()

        viewModel.state.test {
            val state = awaitItem()
            assertEquals(1f, state.imageScale)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `setImageScale coerces value to maximum`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.setImageScale(150f)
        advanceUntilIdle()

        viewModel.state.test {
            val state = awaitItem()
            assertEquals(100f, state.imageScale)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `setImageOpacity updates opacity`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.setImageOpacity(75f)
        advanceUntilIdle()

        viewModel.state.test {
            val state = awaitItem()
            assertEquals(75f, state.imageOpacity)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `setImageOpacity coerces value to minimum`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.setImageOpacity(0f)
        advanceUntilIdle()

        viewModel.state.test {
            val state = awaitItem()
            assertEquals(1f, state.imageOpacity)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `setImageOpacity coerces value to maximum`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.setImageOpacity(150f)
        advanceUntilIdle()

        viewModel.state.test {
            val state = awaitItem()
            assertEquals(100f, state.imageOpacity)
            cancelAndIgnoreRemainingEvents()
        }
    }
    // endregion

    // region Common Settings Tests
    @Test
    fun `setPosition updates position to TOP_LEFT`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.setPosition(WatermarkPosition.TOP_LEFT)
        advanceUntilIdle()

        viewModel.state.test {
            val state = awaitItem()
            assertEquals(WatermarkPosition.TOP_LEFT, state.position)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `setPosition updates position to TILED`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.setPosition(WatermarkPosition.TILED)
        advanceUntilIdle()

        viewModel.state.test {
            val state = awaitItem()
            assertEquals(WatermarkPosition.TILED, state.position)
            cancelAndIgnoreRemainingEvents()
        }
    }

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

    @Test
    fun `setOutputFileName updates filename`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.setOutputFileName("my_watermarked_doc")
        advanceUntilIdle()

        viewModel.state.test {
            val state = awaitItem()
            assertEquals("my_watermarked_doc", state.outputFileName)
            cancelAndIgnoreRemainingEvents()
        }
    }
    // endregion

    // region applyWatermark Validation Tests
    @Test
    fun `applyWatermark without source file sets error`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.applyWatermark()
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

        viewModel.setWatermarkType(WatermarkType.IMAGE)
        viewModel.setWatermarkText("SAMPLE")
        viewModel.setFontSize(100f)
        viewModel.setPosition(WatermarkPosition.TILED)
        viewModel.setPageSelection(PageSelection.ODD)
        viewModel.setOutputFileName("custom_name")
        viewModel.reset()
        advanceUntilIdle()

        viewModel.state.test {
            val state = awaitItem()
            assertNull(state.sourceFile)
            assertEquals(WatermarkType.TEXT, state.watermarkType)
            assertEquals("CONFIDENTIAL", state.watermarkText)
            assertEquals(48f, state.fontSize)
            assertEquals(WatermarkPosition.CENTER, state.position)
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

    // region WatermarkType enum Tests
    @Test
    fun `WatermarkType has TEXT`() {
        assertEquals("TEXT", WatermarkType.TEXT.name)
    }

    @Test
    fun `WatermarkType has IMAGE`() {
        assertEquals("IMAGE", WatermarkType.IMAGE.name)
    }

    @Test
    fun `WatermarkType has 2 values`() {
        assertEquals(2, WatermarkType.entries.size)
    }
    // endregion

    // region WatermarkPosition enum Tests
    @Test
    fun `WatermarkPosition CENTER has correct label`() {
        assertEquals("Center", WatermarkPosition.CENTER.label)
    }

    @Test
    fun `WatermarkPosition TOP_LEFT has correct label`() {
        assertEquals("Top Left", WatermarkPosition.TOP_LEFT.label)
    }

    @Test
    fun `WatermarkPosition TOP_CENTER has correct label`() {
        assertEquals("Top Center", WatermarkPosition.TOP_CENTER.label)
    }

    @Test
    fun `WatermarkPosition TOP_RIGHT has correct label`() {
        assertEquals("Top Right", WatermarkPosition.TOP_RIGHT.label)
    }

    @Test
    fun `WatermarkPosition BOTTOM_LEFT has correct label`() {
        assertEquals("Bottom Left", WatermarkPosition.BOTTOM_LEFT.label)
    }

    @Test
    fun `WatermarkPosition BOTTOM_CENTER has correct label`() {
        assertEquals("Bottom Center", WatermarkPosition.BOTTOM_CENTER.label)
    }

    @Test
    fun `WatermarkPosition BOTTOM_RIGHT has correct label`() {
        assertEquals("Bottom Right", WatermarkPosition.BOTTOM_RIGHT.label)
    }

    @Test
    fun `WatermarkPosition TILED has correct label`() {
        assertEquals("Tiled", WatermarkPosition.TILED.label)
    }

    @Test
    fun `WatermarkPosition has 8 values`() {
        assertEquals(8, WatermarkPosition.entries.size)
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
    fun `PageSelection has 4 values`() {
        assertEquals(4, PageSelection.entries.size)
    }
    // endregion
}
