package com.rejowan.pdfreaderpro.presentation.viewmodel.tools

import android.app.Application
import androidx.compose.ui.graphics.Color
import app.cash.turbine.test
import com.rejowan.pdfreaderpro.domain.repository.PdfToolsRepository
import com.rejowan.pdfreaderpro.presentation.screens.tools.watermark.PageSelection
import com.rejowan.pdfreaderpro.presentation.screens.tools.watermark.WatermarkPosition
import com.rejowan.pdfreaderpro.presentation.screens.tools.watermark.WatermarkType
import com.rejowan.pdfreaderpro.presentation.screens.tools.watermark.WatermarkViewModel
import android.os.Environment
import com.rejowan.pdfreaderpro.R
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.slot
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
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
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class WatermarkViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var pdfToolsRepository: PdfToolsRepository
    private lateinit var context: Application
    private lateinit var viewModel: WatermarkViewModel

    @get:Rule
    val folder = TemporaryFolder()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        pdfToolsRepository = mockk(relaxed = true)
        context = mockk(relaxed = true)

        // Loading a document is what supplies the page count every page selection
        // rule is written against, and the copy behind it fails silently against a
        // relaxed mock. Environment is mocked because the output directory is the
        // public Documents folder, which off-device throws inside the coroutine and
        // surfaces in whichever test runs next.
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
        assertEquals(R.string.position_center, WatermarkPosition.CENTER.labelRes)
    }

    @Test
    fun `WatermarkPosition TOP_LEFT has correct label`() {
        assertEquals(R.string.position_top_left, WatermarkPosition.TOP_LEFT.labelRes)
    }

    @Test
    fun `WatermarkPosition TOP_CENTER has correct label`() {
        assertEquals(R.string.position_top_center, WatermarkPosition.TOP_CENTER.labelRes)
    }

    @Test
    fun `WatermarkPosition TOP_RIGHT has correct label`() {
        assertEquals(R.string.position_top_right, WatermarkPosition.TOP_RIGHT.labelRes)
    }

    @Test
    fun `WatermarkPosition BOTTOM_LEFT has correct label`() {
        assertEquals(R.string.position_bottom_left, WatermarkPosition.BOTTOM_LEFT.labelRes)
    }

    @Test
    fun `WatermarkPosition BOTTOM_CENTER has correct label`() {
        assertEquals(R.string.position_bottom_center, WatermarkPosition.BOTTOM_CENTER.labelRes)
    }

    @Test
    fun `WatermarkPosition BOTTOM_RIGHT has correct label`() {
        assertEquals(R.string.position_bottom_right, WatermarkPosition.BOTTOM_RIGHT.labelRes)
    }

    @Test
    fun `WatermarkPosition TILED has correct label`() {
        assertEquals(R.string.position_tiled, WatermarkPosition.TILED.labelRes)
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

    // region Which pages get the watermark
    // None of this was covered: the rules need a loaded document and no test had
    // one, so every selection behaved as though the document were empty.

    private fun TestScope.loadDocument(vm: WatermarkViewModel) {
        vm.setSourceFile(mockk(relaxed = true))
        // Loading starts on the main dispatcher, which needs the scheduler advanced,
        // then renders a preview on Dispatchers.IO, which the scheduler does not
        // control. Both waits are needed.
        repeat(200) {
            advanceUntilIdle()
            if (vm.state.value.sourceFile != null) return
            Thread.sleep(10)
        }
        error("document never loaded")
    }

    /** Watermarks a document and reports the page list handed to the repository. */
    private fun TestScope.watermarkedPages(
        configure: (WatermarkViewModel) -> Unit
    ): List<Int>? {
        val captured = slot<List<Int>?>()
        coEvery {
            pdfToolsRepository.addTextWatermark(any(), any(), any(), captureNullable(captured), any())
        } returns Result.success(Unit)

        val vm = createViewModel()
        loadDocument(vm)
        vm.setWatermarkText("CONFIDENTIAL")
        configure(vm)
        vm.applyWatermark()
        advanceUntilIdle()

        return captured.captured
    }

    @Test
    fun `watermarking everything passes no page list, meaning all of them`() = runTest {
        assertNull(watermarkedPages { it.setPageSelection(PageSelection.ALL) })
    }

    @Test
    fun `odd only watermarks the odd pages`() = runTest {
        assertEquals(listOf(1, 3, 5, 7, 9), watermarkedPages { it.setPageSelection(PageSelection.ODD) })
    }

    @Test
    fun `even only watermarks the even pages`() = runTest {
        assertEquals(listOf(2, 4, 6, 8, 10), watermarkedPages { it.setPageSelection(PageSelection.EVEN) })
    }

    @Test
    fun `a custom list is honoured`() = runTest {
        val pages = watermarkedPages {
            it.setPageSelection(PageSelection.CUSTOM)
            it.setCustomPages("2, 4, 8")
        }
        assertEquals(listOf(2, 4, 8), pages)
    }

    @Test
    fun `a custom range is expanded`() = runTest {
        val pages = watermarkedPages {
            it.setPageSelection(PageSelection.CUSTOM)
            it.setCustomPages("4-7")
        }
        assertEquals(listOf(4, 5, 6, 7), pages)
    }

    @Test
    fun `custom pages past the end of the document are dropped`() = runTest {
        val pages = watermarkedPages {
            it.setPageSelection(PageSelection.CUSTOM)
            it.setCustomPages("3, 88")
        }
        assertEquals(listOf(3), pages)
    }

    @Test
    fun `custom pages are ordered and deduplicated`() = runTest {
        val pages = watermarkedPages {
            it.setPageSelection(PageSelection.CUSTOM)
            it.setCustomPages("7, 2, 7, 2-3")
        }
        assertEquals(listOf(2, 3, 7), pages)
    }

    @Test
    fun `text among custom pages is skipped, the rest still count`() = runTest {
        val pages = watermarkedPages {
            it.setPageSelection(PageSelection.CUSTOM)
            it.setCustomPages("5, nonsense, 6")
        }
        assertEquals(listOf(5, 6), pages)
    }

    @Test
    fun `page zero is dropped, pages are counted from one`() = runTest {
        val pages = watermarkedPages {
            it.setPageSelection(PageSelection.CUSTOM)
            it.setCustomPages("0, 1")
        }
        assertEquals(listOf(1), pages)
    }
    // endregion

    // region Refusing to run
    // Each of these returns before touching the repository, which is what stops a
    // half specified watermark being written over someone's document.

    @Test
    fun `without a document it asks for one and does nothing`() = runTest {
        val vm = createViewModel()
        vm.setWatermarkText("DRAFT")
        vm.applyWatermark()
        advanceUntilIdle()

        assertNotNull(vm.state.value.error)
        coVerify(exactly = 0) {
            pdfToolsRepository.addTextWatermark(any(), any(), any(), any(), any())
        }
    }

    @Test
    fun `without watermark text it asks for some and does nothing`() = runTest {
        val vm = createViewModel()
        loadDocument(vm)
        vm.setWatermarkText("   ")
        vm.applyWatermark()
        advanceUntilIdle()

        assertNotNull(vm.state.value.error)
        coVerify(exactly = 0) {
            pdfToolsRepository.addTextWatermark(any(), any(), any(), any(), any())
        }
    }

    @Test
    fun `without an output name it asks for one and does nothing`() = runTest {
        val vm = createViewModel()
        loadDocument(vm)
        vm.setWatermarkText("DRAFT")
        vm.setOutputFileName("")
        vm.applyWatermark()
        advanceUntilIdle()

        assertNotNull(vm.state.value.error)
        coVerify(exactly = 0) {
            pdfToolsRepository.addTextWatermark(any(), any(), any(), any(), any())
        }
    }

    @Test
    fun `an image watermark without an image does nothing`() = runTest {
        val vm = createViewModel()
        loadDocument(vm)
        vm.setWatermarkType(WatermarkType.IMAGE)
        vm.applyWatermark()
        advanceUntilIdle()

        assertNotNull(vm.state.value.error)
        coVerify(exactly = 0) {
            pdfToolsRepository.addImageWatermark(any(), any(), any(), any(), any())
        }
    }
    // endregion

    // region Reporting what happened
    @Test
    fun `a failure from the repository is surfaced rather than swallowed`() = runTest {
        coEvery {
            pdfToolsRepository.addTextWatermark(any(), any(), any(), any(), any())
        } returns Result.failure(RuntimeException("disk full"))

        val vm = createViewModel()
        loadDocument(vm)
        vm.setWatermarkText("DRAFT")
        vm.applyWatermark()
        advanceUntilIdle()

        assertNotNull(vm.state.value.error)
        assertFalse(vm.state.value.isProcessing)
    }

    @Test
    fun `the document is no longer marked as processing once it finishes`() = runTest {
        coEvery {
            pdfToolsRepository.addTextWatermark(any(), any(), any(), any(), any())
        } returns Result.success(Unit)

        val vm = createViewModel()
        loadDocument(vm)
        vm.setWatermarkText("DRAFT")
        vm.applyWatermark()
        advanceUntilIdle()

        assertFalse(vm.state.value.isProcessing)
    }

    @Test
    fun `loading a document records its page count`() = runTest {
        val vm = createViewModel()
        loadDocument(vm)
        assertEquals(10, vm.state.value.sourceFile?.pageCount)
    }
    // endregion

    // region An image watermark
    /** An image the user picked, as the chooser hands it over. */
    private fun pickedImage(content: String = "png-bytes"): android.net.Uri {
        val uri = mockk<android.net.Uri>(relaxed = true)
        every { context.contentResolver.openInputStream(uri) } answers {
            java.io.ByteArrayInputStream(content.toByteArray())
        }
        return uri
    }

    private fun TestScope.chooseImage(vm: WatermarkViewModel): String {
        vm.setWatermarkImage(pickedImage())
        repeat(200) {
            advanceUntilIdle()
            vm.state.value.imagePath?.let { return it }
            Thread.sleep(10)
        }
        error("image never loaded")
    }

    @Test
    fun `a chosen image is copied in, so a later pick cannot change it`() = runTest {
        val vm = createViewModel()

        val path = chooseImage(vm)

        assertTrue(java.io.File(path).exists())
        assertEquals("png-bytes", java.io.File(path).readText())
    }

    @Test
    fun `an image that cannot be read is reported`() = runTest {
        val unreadable = mockk<android.net.Uri>(relaxed = true)
        every { context.contentResolver.openInputStream(unreadable) } returns null
        val vm = createViewModel()

        vm.setWatermarkImage(unreadable)
        repeat(200) {
            advanceUntilIdle()
            if (vm.state.value.error != null) return@repeat
            Thread.sleep(10)
        }
        advanceUntilIdle()

        assertNotNull(vm.state.value.error)
        assertNull(vm.state.value.imagePath)
    }

    @Test
    fun `the image and its settings are what get sent`() = runTest {
        val vm = createViewModel()
        loadDocument(vm)
        val path = chooseImage(vm)
        vm.setWatermarkType(WatermarkType.IMAGE)
        vm.setImageScale(40f)
        vm.setImageOpacity(70f)
        val config = slot<PdfToolsRepository.ImageWatermarkConfig>()
        coEvery {
            pdfToolsRepository.addImageWatermark(any(), any(), capture(config), any(), any())
        } returns Result.success(Unit)

        vm.applyWatermark()
        advanceUntilIdle()

        assertEquals(path, config.captured.imagePath)
        assertEquals(40f, config.captured.scale)
        assertEquals(70f, config.captured.opacity)
    }

    @Test
    fun `image scale and opacity stay within what the tool can draw`() = runTest {
        val vm = createViewModel()

        vm.setImageScale(500f)
        vm.setImageOpacity(-20f)

        assertEquals(100f, vm.state.value.imageScale)
        assertEquals(1f, vm.state.value.imageOpacity)
    }
    // endregion

    // region Where the watermarked document goes
    @Test
    fun `an existing file is not written over, a numbered one is used instead`() = runTest {
        val vm = createViewModel()
        loadDocument(vm)
        vm.setWatermarkText("DRAFT")
        vm.setOutputFileName("stamped")
        val documents = android.os.Environment.getExternalStoragePublicDirectory(null)
        java.io.File(documents, "PdfReaderPro").mkdirs()
        java.io.File(documents, "PdfReaderPro/stamped.pdf").writeText("someone else's work")
        coEvery {
            pdfToolsRepository.addTextWatermark(any(), any(), any(), any(), any())
        } returns Result.success(Unit)

        vm.applyWatermark()
        advanceUntilIdle()

        assertTrue(vm.state.value.result!!.outputPath.endsWith("stamped_1.pdf"))
    }

    @Test
    fun `overwriting writes through a temporary file, then replaces the original`() = runTest {
        val vm = createViewModel()
        loadDocument(vm)
        vm.setWatermarkText("DRAFT")
        vm.setOverwriteOriginal(true)
        val sourcePath = vm.state.value.sourceFile!!.path
        val target = slot<String>()
        coEvery {
            pdfToolsRepository.addTextWatermark(any(), capture(target), any(), any(), any())
        } answers {
            java.io.File(target.captured).writeText("stamped bytes")
            Result.success(Unit)
        }

        vm.applyWatermark()
        advanceUntilIdle()

        assertNotEquals(sourcePath, target.captured)
        assertFalse(java.io.File(target.captured).exists())
        assertEquals("stamped bytes", java.io.File(sourcePath).readText())
    }

    @Test
    fun `progress from the repository reaches the screen`() = runTest {
        val vm = createViewModel()
        loadDocument(vm)
        vm.setWatermarkText("DRAFT")
        val seen = mutableListOf<Float>()
        coEvery {
            pdfToolsRepository.addTextWatermark(any(), any(), any(), any(), any())
        } answers {
            val onProgress = arg<(Float) -> Unit>(4)
            onProgress(0.4f)
            seen += vm.state.value.progress
            Result.success(Unit)
        }

        vm.applyWatermark()
        advanceUntilIdle()

        assertEquals(listOf(0.4f), seen)
        assertEquals(1f, vm.state.value.progress, 0.001f)
    }
    // endregion
}
