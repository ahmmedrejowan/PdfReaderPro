package com.rejowan.pdfreaderpro.data.repository

import android.content.Context
import com.rejowan.pdfreaderpro.domain.repository.PdfToolsRepository
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
class PdfToolsRepositoryTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var context: Context
    private lateinit var repository: PdfToolsRepositoryImpl

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        context = mockk(relaxed = true)
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
    }

    private fun createRepository(): PdfToolsRepositoryImpl {
        return PdfToolsRepositoryImpl(context = context)
    }

    // region PdfPageSelection Tests
    @Test
    fun `PdfPageSelection with path only has null pages`() {
        val selection = PdfToolsRepository.PdfPageSelection(path = "/path/to/file.pdf")
        assertEquals("/path/to/file.pdf", selection.path)
        assertNull(selection.pages)
    }

    @Test
    fun `PdfPageSelection with pages stores them correctly`() {
        val selection = PdfToolsRepository.PdfPageSelection(
            path = "/path/to/file.pdf",
            pages = listOf(1, 2, 3)
        )
        assertEquals("/path/to/file.pdf", selection.path)
        assertEquals(listOf(1, 2, 3), selection.pages)
    }

    @Test
    fun `PdfPageSelection with empty pages list`() {
        val selection = PdfToolsRepository.PdfPageSelection(
            path = "/path/to/file.pdf",
            pages = emptyList()
        )
        assertEquals(emptyList<Int>(), selection.pages)
    }
    // endregion

    // region CompressionAnalysis Tests
    @Test
    fun `CompressionAnalysis stores all properties`() {
        val analysis = PdfToolsRepository.CompressionAnalysis(
            bytesPerPage = 10000L,
            hasImages = true,
            isAlreadyOptimized = false,
            estimatedRatioLow = 0.9f,
            estimatedRatioMedium = 0.7f,
            estimatedRatioHigh = 0.5f
        )
        assertEquals(10000L, analysis.bytesPerPage)
        assertTrue(analysis.hasImages)
        assertFalse(analysis.isAlreadyOptimized)
        assertEquals(0.9f, analysis.estimatedRatioLow, 0.001f)
        assertEquals(0.7f, analysis.estimatedRatioMedium, 0.001f)
        assertEquals(0.5f, analysis.estimatedRatioHigh, 0.001f)
    }

    @Test
    fun `CompressionAnalysis with already optimized PDF`() {
        val analysis = PdfToolsRepository.CompressionAnalysis(
            bytesPerPage = 5000L,
            hasImages = false,
            isAlreadyOptimized = true,
            estimatedRatioLow = 0.95f,
            estimatedRatioMedium = 0.95f,
            estimatedRatioHigh = 0.95f
        )
        assertTrue(analysis.isAlreadyOptimized)
        assertFalse(analysis.hasImages)
    }
    // endregion

    // region PdfPermissions Tests
    @Test
    fun `PdfPermissions default values are all false`() {
        val permissions = PdfToolsRepository.PdfPermissions()
        assertFalse(permissions.allowPrinting)
        assertFalse(permissions.allowCopying)
        assertFalse(permissions.allowModifying)
        assertFalse(permissions.allowAnnotations)
    }

    @Test
    fun `PdfPermissions with all permissions enabled`() {
        val permissions = PdfToolsRepository.PdfPermissions(
            allowPrinting = true,
            allowCopying = true,
            allowModifying = true,
            allowAnnotations = true
        )
        assertTrue(permissions.allowPrinting)
        assertTrue(permissions.allowCopying)
        assertTrue(permissions.allowModifying)
        assertTrue(permissions.allowAnnotations)
    }

    @Test
    fun `PdfPermissions with partial permissions`() {
        val permissions = PdfToolsRepository.PdfPermissions(
            allowPrinting = true,
            allowCopying = false,
            allowModifying = false,
            allowAnnotations = true
        )
        assertTrue(permissions.allowPrinting)
        assertFalse(permissions.allowCopying)
        assertFalse(permissions.allowModifying)
        assertTrue(permissions.allowAnnotations)
    }
    // endregion

    // region TextWatermarkConfig Tests
    @Test
    fun `TextWatermarkConfig default values`() {
        val config = PdfToolsRepository.TextWatermarkConfig(text = "DRAFT")
        assertEquals("DRAFT", config.text)
        assertEquals(48f, config.fontSize, 0.001f)
        assertEquals(50f, config.opacity, 0.001f)
        assertEquals(-45f, config.rotation, 0.001f)
        assertEquals(PdfToolsRepository.WatermarkPosition.CENTER, config.position)
    }

    @Test
    fun `TextWatermarkConfig with custom values`() {
        val config = PdfToolsRepository.TextWatermarkConfig(
            text = "CONFIDENTIAL",
            fontSize = 72f,
            color = 0xFFFF0000.toInt(),
            opacity = 30f,
            rotation = 0f,
            position = PdfToolsRepository.WatermarkPosition.TOP_LEFT
        )
        assertEquals("CONFIDENTIAL", config.text)
        assertEquals(72f, config.fontSize, 0.001f)
        assertEquals(30f, config.opacity, 0.001f)
        assertEquals(0f, config.rotation, 0.001f)
        assertEquals(PdfToolsRepository.WatermarkPosition.TOP_LEFT, config.position)
    }
    // endregion

    // region ImageWatermarkConfig Tests
    @Test
    fun `ImageWatermarkConfig default values`() {
        val config = PdfToolsRepository.ImageWatermarkConfig(imagePath = "/path/to/logo.png")
        assertEquals("/path/to/logo.png", config.imagePath)
        assertEquals(30f, config.scale, 0.001f)
        assertEquals(50f, config.opacity, 0.001f)
        assertEquals(PdfToolsRepository.WatermarkPosition.CENTER, config.position)
    }

    @Test
    fun `ImageWatermarkConfig with custom values`() {
        val config = PdfToolsRepository.ImageWatermarkConfig(
            imagePath = "/path/to/watermark.png",
            scale = 50f,
            opacity = 75f,
            position = PdfToolsRepository.WatermarkPosition.BOTTOM_RIGHT
        )
        assertEquals("/path/to/watermark.png", config.imagePath)
        assertEquals(50f, config.scale, 0.001f)
        assertEquals(75f, config.opacity, 0.001f)
        assertEquals(PdfToolsRepository.WatermarkPosition.BOTTOM_RIGHT, config.position)
    }
    // endregion

    // region PageNumberConfig Tests
    @Test
    fun `PageNumberConfig default values`() {
        val config = PdfToolsRepository.PageNumberConfig()
        assertEquals(PdfToolsRepository.PageNumberPosition.BOTTOM_CENTER, config.position)
        assertEquals(PdfToolsRepository.PageNumberFormat.NUMBER_ONLY, config.format)
        assertEquals(12f, config.fontSize, 0.001f)
        assertEquals(1, config.startNumber)
        assertEquals("", config.prefix)
        assertEquals("", config.suffix)
        assertEquals(36f, config.marginX, 0.001f)
        assertEquals(30f, config.marginY, 0.001f)
    }

    @Test
    fun `PageNumberConfig with custom values`() {
        val config = PdfToolsRepository.PageNumberConfig(
            position = PdfToolsRepository.PageNumberPosition.TOP_RIGHT,
            format = PdfToolsRepository.PageNumberFormat.X_OF_Y,
            fontSize = 14f,
            color = 0xFF0000FF.toInt(),
            startNumber = 5,
            prefix = "Page ",
            suffix = " -",
            marginX = 50f,
            marginY = 40f
        )
        assertEquals(PdfToolsRepository.PageNumberPosition.TOP_RIGHT, config.position)
        assertEquals(PdfToolsRepository.PageNumberFormat.X_OF_Y, config.format)
        assertEquals(14f, config.fontSize, 0.001f)
        assertEquals(5, config.startNumber)
        assertEquals("Page ", config.prefix)
        assertEquals(" -", config.suffix)
        assertEquals(50f, config.marginX, 0.001f)
        assertEquals(40f, config.marginY, 0.001f)
    }
    // endregion

    // region WatermarkPosition enum Tests
    @Test
    fun `WatermarkPosition has CENTER`() {
        assertEquals("CENTER", PdfToolsRepository.WatermarkPosition.CENTER.name)
    }

    @Test
    fun `WatermarkPosition has TOP_LEFT`() {
        assertEquals("TOP_LEFT", PdfToolsRepository.WatermarkPosition.TOP_LEFT.name)
    }

    @Test
    fun `WatermarkPosition has TOP_CENTER`() {
        assertEquals("TOP_CENTER", PdfToolsRepository.WatermarkPosition.TOP_CENTER.name)
    }

    @Test
    fun `WatermarkPosition has TOP_RIGHT`() {
        assertEquals("TOP_RIGHT", PdfToolsRepository.WatermarkPosition.TOP_RIGHT.name)
    }

    @Test
    fun `WatermarkPosition has BOTTOM_LEFT`() {
        assertEquals("BOTTOM_LEFT", PdfToolsRepository.WatermarkPosition.BOTTOM_LEFT.name)
    }

    @Test
    fun `WatermarkPosition has BOTTOM_CENTER`() {
        assertEquals("BOTTOM_CENTER", PdfToolsRepository.WatermarkPosition.BOTTOM_CENTER.name)
    }

    @Test
    fun `WatermarkPosition has BOTTOM_RIGHT`() {
        assertEquals("BOTTOM_RIGHT", PdfToolsRepository.WatermarkPosition.BOTTOM_RIGHT.name)
    }

    @Test
    fun `WatermarkPosition has TILED`() {
        assertEquals("TILED", PdfToolsRepository.WatermarkPosition.TILED.name)
    }

    @Test
    fun `WatermarkPosition has 8 values`() {
        assertEquals(8, PdfToolsRepository.WatermarkPosition.entries.size)
    }
    // endregion

    // region PageNumberPosition enum Tests
    @Test
    fun `PageNumberPosition has TOP_LEFT`() {
        assertEquals("TOP_LEFT", PdfToolsRepository.PageNumberPosition.TOP_LEFT.name)
    }

    @Test
    fun `PageNumberPosition has TOP_CENTER`() {
        assertEquals("TOP_CENTER", PdfToolsRepository.PageNumberPosition.TOP_CENTER.name)
    }

    @Test
    fun `PageNumberPosition has TOP_RIGHT`() {
        assertEquals("TOP_RIGHT", PdfToolsRepository.PageNumberPosition.TOP_RIGHT.name)
    }

    @Test
    fun `PageNumberPosition has BOTTOM_LEFT`() {
        assertEquals("BOTTOM_LEFT", PdfToolsRepository.PageNumberPosition.BOTTOM_LEFT.name)
    }

    @Test
    fun `PageNumberPosition has BOTTOM_CENTER`() {
        assertEquals("BOTTOM_CENTER", PdfToolsRepository.PageNumberPosition.BOTTOM_CENTER.name)
    }

    @Test
    fun `PageNumberPosition has BOTTOM_RIGHT`() {
        assertEquals("BOTTOM_RIGHT", PdfToolsRepository.PageNumberPosition.BOTTOM_RIGHT.name)
    }

    @Test
    fun `PageNumberPosition has 6 values`() {
        assertEquals(6, PdfToolsRepository.PageNumberPosition.entries.size)
    }
    // endregion

    // region PageNumberFormat enum Tests
    @Test
    fun `PageNumberFormat has NUMBER_ONLY`() {
        assertEquals("NUMBER_ONLY", PdfToolsRepository.PageNumberFormat.NUMBER_ONLY.name)
    }

    @Test
    fun `PageNumberFormat has PAGE_X`() {
        assertEquals("PAGE_X", PdfToolsRepository.PageNumberFormat.PAGE_X.name)
    }

    @Test
    fun `PageNumberFormat has X_OF_Y`() {
        assertEquals("X_OF_Y", PdfToolsRepository.PageNumberFormat.X_OF_Y.name)
    }

    @Test
    fun `PageNumberFormat has DASH_X_DASH`() {
        assertEquals("DASH_X_DASH", PdfToolsRepository.PageNumberFormat.DASH_X_DASH.name)
    }

    @Test
    fun `PageNumberFormat has CUSTOM`() {
        assertEquals("CUSTOM", PdfToolsRepository.PageNumberFormat.CUSTOM.name)
    }

    @Test
    fun `PageNumberFormat has 5 values`() {
        assertEquals(5, PdfToolsRepository.PageNumberFormat.entries.size)
    }
    // endregion

    // region Repository Method Error Handling Tests
    @Test
    fun `mergePdfs with empty input list returns failure`() = runTest {
        repository = createRepository()
        advanceUntilIdle()

        val result = repository.mergePdfs(
            inputPaths = emptyList(),
            outputPath = "/output/merged.pdf"
        )

        assertTrue(result.isFailure)
    }

    @Test
    fun `mergePdfs with non-existent files returns failure`() = runTest {
        repository = createRepository()
        advanceUntilIdle()

        val result = repository.mergePdfs(
            inputPaths = listOf("/nonexistent/file1.pdf", "/nonexistent/file2.pdf"),
            outputPath = "/output/merged.pdf"
        )

        assertTrue(result.isFailure)
    }

    @Test
    fun `splitPdf with empty ranges returns failure`() = runTest {
        repository = createRepository()
        advanceUntilIdle()

        val result = repository.splitPdf(
            inputPath = "/nonexistent/file.pdf",
            outputDir = "/output",
            ranges = emptyList()
        )

        assertTrue(result.isFailure)
    }

    @Test
    fun `splitIntoPages with non-existent file returns failure`() = runTest {
        repository = createRepository()
        advanceUntilIdle()

        val result = repository.splitIntoPages(
            inputPath = "/nonexistent/file.pdf",
            outputDir = "/output"
        )

        assertTrue(result.isFailure)
    }

    @Test
    fun `extractPages with empty pages list returns failure`() = runTest {
        repository = createRepository()
        advanceUntilIdle()

        val result = repository.extractPages(
            inputPath = "/nonexistent/file.pdf",
            outputPath = "/output/extracted.pdf",
            pages = emptyList()
        )

        assertTrue(result.isFailure)
    }

    @Test
    fun `rotatePages with non-existent file returns failure`() = runTest {
        repository = createRepository()
        advanceUntilIdle()

        val result = repository.rotatePages(
            inputPath = "/nonexistent/file.pdf",
            outputPath = "/output/rotated.pdf",
            rotation = 90
        )

        assertTrue(result.isFailure)
    }

    @Test
    fun `reorderPages with empty order returns failure`() = runTest {
        repository = createRepository()
        advanceUntilIdle()

        val result = repository.reorderPages(
            inputPath = "/nonexistent/file.pdf",
            outputPath = "/output/reordered.pdf",
            newOrder = emptyList()
        )

        assertTrue(result.isFailure)
    }

    @Test
    fun `compressPdf with non-existent file returns failure`() = runTest {
        repository = createRepository()
        advanceUntilIdle()

        val result = repository.compressPdf(
            inputPath = "/nonexistent/file.pdf",
            outputPath = "/output/compressed.pdf"
        )

        assertTrue(result.isFailure)
    }

    @Test
    fun `imagesToPdf with empty image list returns failure`() = runTest {
        repository = createRepository()
        advanceUntilIdle()

        val result = repository.imagesToPdf(
            imagePaths = emptyList(),
            outputPath = "/output/images.pdf"
        )

        assertTrue(result.isFailure)
    }

    @Test
    fun `pdfToImages with non-existent file returns failure`() = runTest {
        repository = createRepository()
        advanceUntilIdle()

        val result = repository.pdfToImages(
            inputPath = "/nonexistent/file.pdf",
            outputDir = "/output"
        )

        assertTrue(result.isFailure)
    }

    @Test
    fun `getPageCount with non-existent file returns failure`() = runTest {
        repository = createRepository()
        advanceUntilIdle()

        val result = repository.getPageCount("/nonexistent/file.pdf")

        assertTrue(result.isFailure)
    }

    @Test
    fun `analyzeCompressionPotential with non-existent file returns failure`() = runTest {
        repository = createRepository()
        advanceUntilIdle()

        val result = repository.analyzeCompressionPotential("/nonexistent/file.pdf")

        assertTrue(result.isFailure)
    }

    @Test
    fun `lockPdf with non-existent file returns failure`() = runTest {
        repository = createRepository()
        advanceUntilIdle()

        val result = repository.lockPdf(
            inputPath = "/nonexistent/file.pdf",
            outputPath = "/output/locked.pdf",
            userPassword = "user123",
            ownerPassword = "owner123"
        )

        assertTrue(result.isFailure)
    }

    @Test
    fun `unlockPdf with non-existent file returns failure`() = runTest {
        repository = createRepository()
        advanceUntilIdle()

        val result = repository.unlockPdf(
            inputPath = "/nonexistent/file.pdf",
            outputPath = "/output/unlocked.pdf",
            password = "password"
        )

        assertTrue(result.isFailure)
    }

    @Test
    fun `isPasswordProtected with non-existent file returns failure`() = runTest {
        repository = createRepository()
        advanceUntilIdle()

        val result = repository.isPasswordProtected("/nonexistent/file.pdf")

        assertTrue(result.isFailure)
    }

    @Test
    fun `removePages with empty pages list returns failure`() = runTest {
        repository = createRepository()
        advanceUntilIdle()

        val result = repository.removePages(
            inputPath = "/nonexistent/file.pdf",
            outputPath = "/output/removed.pdf",
            pagesToRemove = emptyList()
        )

        assertTrue(result.isFailure)
    }

    @Test
    fun `addTextWatermark with non-existent file returns failure`() = runTest {
        repository = createRepository()
        advanceUntilIdle()

        val config = PdfToolsRepository.TextWatermarkConfig(text = "DRAFT")
        val result = repository.addTextWatermark(
            inputPath = "/nonexistent/file.pdf",
            outputPath = "/output/watermarked.pdf",
            config = config
        )

        assertTrue(result.isFailure)
    }

    @Test
    fun `addImageWatermark with non-existent file returns failure`() = runTest {
        repository = createRepository()
        advanceUntilIdle()

        val config = PdfToolsRepository.ImageWatermarkConfig(imagePath = "/path/to/logo.png")
        val result = repository.addImageWatermark(
            inputPath = "/nonexistent/file.pdf",
            outputPath = "/output/watermarked.pdf",
            config = config
        )

        assertTrue(result.isFailure)
    }

    @Test
    fun `addPageNumbers with non-existent file returns failure`() = runTest {
        repository = createRepository()
        advanceUntilIdle()

        val config = PdfToolsRepository.PageNumberConfig()
        val result = repository.addPageNumbers(
            inputPath = "/nonexistent/file.pdf",
            outputPath = "/output/numbered.pdf",
            config = config
        )

        assertTrue(result.isFailure)
    }

    @Test
    fun `mergePdfsWithSelection with empty selections returns failure`() = runTest {
        repository = createRepository()
        advanceUntilIdle()

        val result = repository.mergePdfsWithSelection(
            selections = emptyList(),
            outputPath = "/output/merged.pdf"
        )

        assertTrue(result.isFailure)
    }
    // endregion

    // region Data Class Equality Tests
    @Test
    fun `PdfPageSelection equals works correctly`() {
        val selection1 = PdfToolsRepository.PdfPageSelection(
            path = "/path/file.pdf",
            pages = listOf(1, 2, 3)
        )
        val selection2 = PdfToolsRepository.PdfPageSelection(
            path = "/path/file.pdf",
            pages = listOf(1, 2, 3)
        )
        assertEquals(selection1, selection2)
    }

    @Test
    fun `CompressionAnalysis equals works correctly`() {
        val analysis1 = PdfToolsRepository.CompressionAnalysis(
            bytesPerPage = 1000L,
            hasImages = true,
            isAlreadyOptimized = false,
            estimatedRatioLow = 0.9f,
            estimatedRatioMedium = 0.7f,
            estimatedRatioHigh = 0.5f
        )
        val analysis2 = PdfToolsRepository.CompressionAnalysis(
            bytesPerPage = 1000L,
            hasImages = true,
            isAlreadyOptimized = false,
            estimatedRatioLow = 0.9f,
            estimatedRatioMedium = 0.7f,
            estimatedRatioHigh = 0.5f
        )
        assertEquals(analysis1, analysis2)
    }

    @Test
    fun `PdfPermissions equals works correctly`() {
        val permissions1 = PdfToolsRepository.PdfPermissions(
            allowPrinting = true,
            allowCopying = false,
            allowModifying = true,
            allowAnnotations = false
        )
        val permissions2 = PdfToolsRepository.PdfPermissions(
            allowPrinting = true,
            allowCopying = false,
            allowModifying = true,
            allowAnnotations = false
        )
        assertEquals(permissions1, permissions2)
    }
    // endregion
}
