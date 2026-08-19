package com.rejowan.pdfreaderpro.presentation.viewmodel.tools

import android.app.Application
import app.cash.turbine.test
import com.rejowan.pdfreaderpro.domain.repository.PdfToolsRepository
import com.rejowan.pdfreaderpro.presentation.screens.tools.compress.CompressionLevel
import com.rejowan.pdfreaderpro.presentation.screens.tools.compress.CompressResult
import com.rejowan.pdfreaderpro.presentation.screens.tools.compress.CompressViewModel
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import android.os.Environment
import io.mockk.every
import io.mockk.coVerify
import io.mockk.slot
import io.mockk.mockkConstructor
import io.mockk.unmockkConstructor
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
class CompressViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var pdfToolsRepository: PdfToolsRepository
    private lateinit var context: Application
    private lateinit var viewModel: CompressViewModel

    @get:Rule
    val folder = TemporaryFolder()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        pdfToolsRepository = mockk(relaxed = true)
        context = mockk(relaxed = true)

        // The guards run against a loaded document, and the copy behind loading
        // fails silently on a relaxed mock. Environment is mocked because the
        // output directory is the public Documents folder, which off-device throws
        // inside the coroutine and surfaces in whichever test runs next.
        every { context.cacheDir } returns folder.newFolder("cache")
        every { context.contentResolver.openInputStream(any()) } answers {
            ByteArrayInputStream("%PDF-1.4 pretend document".toByteArray())
        }
        every { context.contentResolver.query(any(), any(), any(), any(), any()) } returns null
        // Result is a value class, so a relaxed mock cannot stand in for one; the
        // call has to be stubbed or loading dies before the document is set.
        coEvery {
            pdfToolsRepository.analyzeCompressionPotential(any())
        } returns Result.failure(RuntimeException("no analysis in tests"))


        // The tool shows the first page next to its settings, drawn by the
        // platform renderer, which has no JVM implementation.
        mockkStatic(ParcelFileDescriptor::class)
        every { ParcelFileDescriptor.open(any(), any()) } returns mockk(relaxed = true)
        mockkConstructor(PdfRenderer::class)
        val previewPage = mockk<PdfRenderer.Page>(relaxed = true)
        every { previewPage.width } returns 600
        every { previewPage.height } returns 800
        every { anyConstructed<PdfRenderer>().pageCount } returns 5
        every { anyConstructed<PdfRenderer>().openPage(any()) } returns previewPage
        mockkStatic(Bitmap::class)
        every {
            Bitmap.createBitmap(any<Int>(), any<Int>(), any())
        } returns mockk(relaxed = true)

        mockkStatic(Environment::class)
        every {
            Environment.getExternalStoragePublicDirectory(any())
        } returns folder.newFolder("documents")

        coEvery { pdfToolsRepository.getPageCount(any()) } returns Result.success(10)
    }

    @After
    fun teardown() {
        unmockkStatic(Environment::class)
        unmockkStatic(ParcelFileDescriptor::class)
        unmockkStatic(Bitmap::class)
        unmockkConstructor(PdfRenderer::class)
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

    private fun TestScope.loadDocument(vm: CompressViewModel) {
        vm.setSourceFile(mockk(relaxed = true))
        // Loading starts on the main dispatcher and then does file work off it, so
        // advancing the scheduler alone does not see the result.
        repeat(200) {
            advanceUntilIdle()
            if (vm.state.value.sourceFile != null) return
            Thread.sleep(10)
        }
        error("document never loaded")
    }

    // region Refusing to compress
    @Test
    fun `without a document it asks for one and compresses nothing`() = runTest {
        val vm = createViewModel()
        vm.compress()
        advanceUntilIdle()

        assertNotNull(vm.state.value.error)
        coVerify(exactly = 0) { pdfToolsRepository.compressPdf(any(), any(), any(), any()) }
    }

    @Test
    fun `without an output name it asks for one and compresses nothing`() = runTest {
        val vm = createViewModel()
        loadDocument(vm)
        vm.setOutputFileName("")
        vm.compress()
        advanceUntilIdle()

        assertNotNull(vm.state.value.error)
        coVerify(exactly = 0) { pdfToolsRepository.compressPdf(any(), any(), any(), any()) }
    }
    // endregion

    // region Compressing
    private fun TestScope.qualityUsedFor(level: CompressionLevel): Float {
        val quality = slot<Float>()
        coEvery {
            pdfToolsRepository.compressPdf(any(), any(), capture(quality), any())
        } returns Result.success(1_000L)

        val vm = createViewModel()
        loadDocument(vm)
        vm.setCompressionLevel(level)
        vm.compress()
        advanceUntilIdle()
        return quality.captured
    }

    @Test
    fun `each compression level asks for its own quality`() = runTest {
        // The levels exist to mean different things; if they all sent the same
        // number the choice would be decorative.
        val qualities = CompressionLevel.entries.map { qualityUsedFor(it) }
        assertEquals(qualities.size, qualities.distinct().size)
    }

    @Test
    fun `a higher compression level asks for lower quality`() = runTest {
        val low = qualityUsedFor(CompressionLevel.LOW)
        val high = qualityUsedFor(CompressionLevel.HIGH)
        assertTrue("expected HIGH compression to use lower quality than LOW", high < low)
    }

    @Test
    fun `the document that was loaded is the one compressed`() = runTest {
        coEvery {
            pdfToolsRepository.compressPdf(any(), any(), any(), any())
        } returns Result.success(1_000L)

        val vm = createViewModel()
        loadDocument(vm)
        val path = vm.state.value.sourceFile?.path
        vm.compress()
        advanceUntilIdle()

        coVerify { pdfToolsRepository.compressPdf(path!!, any(), any(), any()) }
    }

    @Test
    fun `a failure is surfaced and processing stops`() = runTest {
        coEvery {
            pdfToolsRepository.compressPdf(any(), any(), any(), any())
        } returns Result.failure(RuntimeException("corrupt"))

        val vm = createViewModel()
        loadDocument(vm)
        vm.compress()
        advanceUntilIdle()

        assertNotNull(vm.state.value.error)
        assertFalse(vm.state.value.isProcessing)
    }

    @Test
    fun `loading a document records its page count`() = runTest {
        val vm = createViewModel()
        loadDocument(vm)
        assertEquals(10, vm.state.value.sourceFile?.pageCount)
    }
    // endregion

    // region Where the finished document goes
    @Test
    fun `an existing file is not written over, a numbered one is used instead`() = runTest {
        val vm = createViewModel()
        loadDocument(vm)
        vm.setOutputFileName("compressed")
        val documents = Environment.getExternalStoragePublicDirectory(null)
        java.io.File(documents, "PdfReaderPro").mkdirs()
        java.io.File(documents, "PdfReaderPro/compressed.pdf").writeText("someone else's work")
        coEvery { pdfToolsRepository.compressPdf(any(), any(), any(), any()) } returns Result.success(1024L)

        vm.compress()
        advanceUntilIdle()

        assertTrue(vm.state.value.result!!.outputPath.endsWith("compressed_1.pdf"))
    }

    @Test
    fun `overwriting writes through a temporary file, then replaces the original`() = runTest {
        val vm = createViewModel()
        loadDocument(vm)
        vm.setOverwriteOriginal(true)
        val sourcePath = vm.state.value.sourceFile!!.path
        val target = slot<String>()
        coEvery {
            pdfToolsRepository.compressPdf(any(), capture(target), any(), any())
        } answers {
            java.io.File(target.captured).writeText("finished bytes")
            Result.success(1024L)
        }

        vm.compress()
        advanceUntilIdle()

        assertNotEquals(sourcePath, target.captured)
        assertFalse(java.io.File(target.captured).exists())
        assertEquals("finished bytes", java.io.File(sourcePath).readText())
    }

    @Test
    fun `progress from the repository reaches the screen`() = runTest {
        val vm = createViewModel()
        loadDocument(vm)
        val seen = mutableListOf<Float>()
        coEvery { pdfToolsRepository.compressPdf(any(), any(), any(), any()) } answers {
            val onProgress = arg<(Float) -> Unit>(3)
            onProgress(0.4f)
            seen += vm.state.value.progress
            Result.success(1024L)
        }

        vm.compress()
        advanceUntilIdle()

        assertEquals(listOf(0.4f), seen)
        assertEquals(1f, vm.state.value.progress, 0.001f)
    }
    // endregion

    // region What the tool estimates before compressing
    @Test
    fun `the estimate is turned into sizes the user can compare`() = runTest {
        // The repository reports ratios; the screen shows sizes, so the file size
        // is applied here and a mix-up would advertise the wrong saving.
        coEvery { pdfToolsRepository.analyzeCompressionPotential(any()) } returns Result.success(
            PdfToolsRepository.CompressionAnalysis(
                bytesPerPage = 50_000,
                hasImages = true,
                isAlreadyOptimized = false,
                estimatedRatioLow = 0.9f,
                estimatedRatioMedium = 0.6f,
                estimatedRatioHigh = 0.3f
            )
        )
        val vm = createViewModel()
        loadDocument(vm)

        val estimate = vm.state.value.sourceFile!!.compressionEstimate!!
        val size = vm.state.value.sourceFile!!.size
        assertEquals(50_000L, estimate.bytesPerPage)
        assertTrue(estimate.hasImages)
        assertEquals((size * 0.9f).toLong(), estimate.estimatedSizeLow)
        assertEquals((size * 0.3f).toLong(), estimate.estimatedSizeHigh)
    }

    @Test
    fun `the harder settings promise a smaller file than the gentler ones`() = runTest {
        coEvery { pdfToolsRepository.analyzeCompressionPotential(any()) } returns Result.success(
            PdfToolsRepository.CompressionAnalysis(
                bytesPerPage = 50_000,
                hasImages = true,
                isAlreadyOptimized = false,
                estimatedRatioLow = 0.9f,
                estimatedRatioMedium = 0.6f,
                estimatedRatioHigh = 0.3f
            )
        )
        val vm = createViewModel()
        loadDocument(vm)

        val estimate = vm.state.value.sourceFile!!.compressionEstimate!!
        assertTrue(estimate.estimatedSizeHigh <= estimate.estimatedSizeMedium)
        assertTrue(estimate.estimatedSizeMedium <= estimate.estimatedSizeLow)
    }

    @Test
    fun `a document that cannot be analysed still opens, just without an estimate`() = runTest {
        coEvery { pdfToolsRepository.analyzeCompressionPotential(any()) } returns
            Result.failure(RuntimeException("damaged document"))
        val vm = createViewModel()
        loadDocument(vm)

        assertNotNull(vm.state.value.sourceFile)
        assertNull(vm.state.value.sourceFile!!.compressionEstimate)
    }

    @Test
    fun `the suggested name marks the file as compressed`() = runTest {
        val vm = createViewModel()
        loadDocument(vm)

        assertTrue(vm.state.value.outputFileName.endsWith("_compressed"))
    }

    @Test
    fun `a document that cannot be read is reported rather than left blank`() = runTest {
        every { context.contentResolver.openInputStream(any()) } returns null
        val vm = createViewModel()

        vm.setSourceFile(mockk(relaxed = true))
        advanceUntilIdle()

        assertNotNull(vm.state.value.error)
        assertNull(vm.state.value.sourceFile)
    }
    // endregion
}
