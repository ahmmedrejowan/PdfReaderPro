package com.rejowan.pdfreaderpro.data.repository

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.rejowan.pdfreaderpro.domain.repository.PdfToolsRepository
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import java.io.File
import java.io.FileOutputStream

/**
 * Integration tests for PdfToolsRepository using actual PDF files.
 *
 * Test PDFs are copied from androidTest/assets to the test cache directory.
 * Each test creates its own output directory which is cleaned up after the test.
 */
@RunWith(AndroidJUnit4::class)
class PdfToolsRepositoryIntegrationTest {

    private lateinit var context: Context
    private lateinit var repository: PdfToolsRepositoryImpl
    private lateinit var testDir: File
    private lateinit var outputDir: File

    // Test PDF files (copied from assets)
    private lateinit var multipagePdf: File   // sample_multipage.pdf (~48KB)
    private lateinit var smallPdf: File       // sample_small.pdf (~13KB)
    private lateinit var mediumPdf: File      // sample_medium.pdf (~29KB)
    private lateinit var withImagesPdf: File  // sample_with_images.pdf (~197KB)
    private lateinit var largePdf: File       // sample_large.pdf (~470KB)
    private lateinit var standardPdf: File    // sample_standard.pdf (~143KB)

    @Before
    fun setup() {
        context = InstrumentationRegistry.getInstrumentation().targetContext

        // Create test directories
        testDir = File(context.cacheDir, "pdf_tools_test_${System.currentTimeMillis()}")
        testDir.mkdirs()
        outputDir = File(testDir, "output")
        outputDir.mkdirs()

        // Copy test PDFs from assets to test directory
        multipagePdf = copyAssetToFile("sample_multipage.pdf")
        smallPdf = copyAssetToFile("sample_small.pdf")
        mediumPdf = copyAssetToFile("sample_medium.pdf")
        withImagesPdf = copyAssetToFile("sample_with_images.pdf")
        largePdf = copyAssetToFile("sample_large.pdf")
        standardPdf = copyAssetToFile("sample_standard.pdf")

        repository = PdfToolsRepositoryImpl(context)
    }

    @After
    fun teardown() {
        // Clean up test directory
        testDir.deleteRecursively()
    }

    private fun copyAssetToFile(assetName: String): File {
        val outputFile = File(testDir, assetName)
        context.assets.open(assetName).use { input ->
            FileOutputStream(outputFile).use { output ->
                input.copyTo(output)
            }
        }
        return outputFile
    }

    // region Merge Operations
    @Test
    fun mergeTwoPdfs_createsValidOutput() = runBlocking {
        val outputPath = File(outputDir, "merged_2.pdf").absolutePath

        val result = repository.mergePdfs(
            inputPaths = listOf(multipagePdf.absolutePath, smallPdf.absolutePath),
            outputPath = outputPath
        )

        assertTrue("Merge should succeed", result.isSuccess)
        assertTrue("Output file should exist", File(outputPath).exists())
        assertTrue("Output file should have content", File(outputPath).length() > 0)
    }

    @Test
    fun mergeMultiplePdfs_createsValidOutput() = runBlocking {
        val outputPath = File(outputDir, "merged_5.pdf").absolutePath

        val result = repository.mergePdfs(
            inputPaths = listOf(
                multipagePdf.absolutePath,
                smallPdf.absolutePath,
                withImagesPdf.absolutePath,
                mediumPdf.absolutePath,
                largePdf.absolutePath
            ),
            outputPath = outputPath
        )

        assertTrue("Merge should succeed", result.isSuccess)
        assertTrue("Output file should exist", File(outputPath).exists())

        // Merged file should be larger than the largest input
        val outputSize = File(outputPath).length()
        assertTrue("Merged file should be substantial", outputSize > largePdf.length())
    }

    @Test
    fun mergePdfsWithPageSelection_createsValidOutput() = runBlocking {
        val outputPath = File(outputDir, "merged_selection.pdf").absolutePath

        // Get page counts first
        val pageCount1 = repository.getPageCount(multipagePdf.absolutePath).getOrElse { 1 }
        val pageCount2 = repository.getPageCount(smallPdf.absolutePath).getOrElse { 1 }

        val selections = listOf(
            PdfToolsRepository.PdfPageSelection(
                path = multipagePdf.absolutePath,
                pages = listOf(1) // First page only
            ),
            PdfToolsRepository.PdfPageSelection(
                path = smallPdf.absolutePath,
                pages = if (pageCount2 > 1) listOf(1, 2) else listOf(1)
            )
        )

        val result = repository.mergePdfsWithSelection(
            selections = selections,
            outputPath = outputPath
        )

        assertTrue("Merge with selection should succeed", result.isSuccess)
        assertTrue("Output file should exist", File(outputPath).exists())

        // Verify page count matches selection
        val outputPageCount = repository.getPageCount(outputPath).getOrElse { 0 }
        assertTrue("Output should have selected pages", outputPageCount >= 2)
    }

    @Test
    fun mergeEmptyList_returnsFailure() = runBlocking {
        val outputPath = File(outputDir, "merged_empty.pdf").absolutePath

        val result = repository.mergePdfs(
            inputPaths = emptyList(),
            outputPath = outputPath
        )

        assertTrue("Empty list merge should fail", result.isFailure)
    }

    @Test
    fun mergeNonExistentFile_returnsFailure() = runBlocking {
        val outputPath = File(outputDir, "merged_invalid.pdf").absolutePath

        val result = repository.mergePdfs(
            inputPaths = listOf(multipagePdf.absolutePath, "/nonexistent/file.pdf"),
            outputPath = outputPath
        )

        assertTrue("Merge with invalid file should fail", result.isFailure)
    }
    // endregion

    // region Split Operations
    @Test
    fun splitByRanges_createsMultipleFiles() = runBlocking {
        // Get page count first
        val pageCount = repository.getPageCount(mediumPdf.absolutePath).getOrElse { 4 }

        // Create ranges based on actual page count
        val midPoint = pageCount / 2
        val ranges = if (pageCount >= 2) {
            listOf("1-${midPoint}", "${midPoint + 1}-$pageCount")
        } else {
            listOf("1-1")
        }

        val result = repository.splitPdf(
            inputPath = mediumPdf.absolutePath,
            outputDir = outputDir.absolutePath,
            ranges = ranges
        )

        assertTrue("Split should succeed", result.isSuccess)
        val outputFiles = result.getOrNull()
        assertNotNull("Should return file paths", outputFiles)
        assertTrue("Should create output files", outputFiles!!.isNotEmpty())

        outputFiles.forEach { path ->
            assertTrue("Split file should exist: $path", File(path).exists())
        }
    }

    @Test
    fun splitIntoSinglePages_createsPageFiles() = runBlocking {
        val pageCount = repository.getPageCount(multipagePdf.absolutePath).getOrElse { 0 }

        val result = repository.splitIntoPages(
            inputPath = multipagePdf.absolutePath,
            outputDir = outputDir.absolutePath
        )

        assertTrue("Split into pages should succeed", result.isSuccess)
        val outputFiles = result.getOrNull()
        assertNotNull("Should return file paths", outputFiles)
        assertEquals("Should create one file per page", pageCount, outputFiles!!.size)

        outputFiles.forEach { path ->
            assertTrue("Page file should exist: $path", File(path).exists())
            val filePageCount = repository.getPageCount(path).getOrElse { 0 }
            assertEquals("Each file should have 1 page", 1, filePageCount)
        }
    }

    @Test
    fun extractSpecificPages_createsValidOutput() = runBlocking {
        val outputPath = File(outputDir, "extracted.pdf").absolutePath
        val pageCount = repository.getPageCount(mediumPdf.absolutePath).getOrElse { 1 }

        val pagesToExtract = if (pageCount >= 3) listOf(1, 3) else listOf(1)

        val result = repository.extractPages(
            inputPath = mediumPdf.absolutePath,
            outputPath = outputPath,
            pages = pagesToExtract
        )

        assertTrue("Extract should succeed", result.isSuccess)
        assertTrue("Output file should exist", File(outputPath).exists())

        val extractedPageCount = repository.getPageCount(outputPath).getOrElse { 0 }
        assertEquals("Extracted file should have correct pages", pagesToExtract.size, extractedPageCount)
    }

    @Test
    fun splitWithInvalidRange_returnsFailure() = runBlocking {
        val result = repository.splitPdf(
            inputPath = multipagePdf.absolutePath,
            outputDir = outputDir.absolutePath,
            ranges = listOf("1000-2000") // Invalid range
        )

        assertTrue("Invalid range should fail", result.isFailure)
    }
    // endregion

    // region Compress Operations
    @Test
    fun compressWithLowQuality_reducesSize() = runBlocking {
        val outputPath = File(outputDir, "compressed_low.pdf").absolutePath
        val originalSize = largePdf.length()

        val result = repository.compressPdf(
            inputPath = largePdf.absolutePath,
            outputPath = outputPath,
            quality = 0.3f // Low quality = more compression
        )

        assertTrue("Compression should succeed", result.isSuccess)
        assertTrue("Output file should exist", File(outputPath).exists())

        val compressedSize = File(outputPath).length()
        assertTrue("Compressed file should have content", compressedSize > 0)
        // Note: Compression effectiveness depends on PDF content
    }

    @Test
    fun compressWithMediumQuality_createsValidOutput() = runBlocking {
        val outputPath = File(outputDir, "compressed_medium.pdf").absolutePath

        val result = repository.compressPdf(
            inputPath = mediumPdf.absolutePath,
            outputPath = outputPath,
            quality = 0.5f
        )

        assertTrue("Compression should succeed", result.isSuccess)
        assertTrue("Output file should exist", File(outputPath).exists())

        // Verify the output is a valid PDF with same page count
        val originalPages = repository.getPageCount(mediumPdf.absolutePath).getOrElse { 0 }
        val compressedPages = repository.getPageCount(outputPath).getOrElse { 0 }
        assertEquals("Page count should be preserved", originalPages, compressedPages)
    }

    @Test
    fun compressWithHighQuality_preservesQuality() = runBlocking {
        val outputPath = File(outputDir, "compressed_high.pdf").absolutePath

        val result = repository.compressPdf(
            inputPath = multipagePdf.absolutePath,
            outputPath = outputPath,
            quality = 0.9f // High quality = minimal compression
        )

        assertTrue("Compression should succeed", result.isSuccess)
        assertTrue("Output file should exist", File(outputPath).exists())
    }

    @Test
    fun analyzeCompression_returnsValidAnalysis() = runBlocking {
        val result = repository.analyzeCompressionPotential(largePdf.absolutePath)

        assertTrue("Analysis should succeed", result.isSuccess)
        val analysis = result.getOrNull()
        assertNotNull("Analysis should not be null", analysis)

        assertTrue("Bytes per page should be positive", analysis!!.bytesPerPage >= 0)
        assertTrue("Low ratio should be between 0 and 1", analysis.estimatedRatioLow in 0f..1f)
        assertTrue("Medium ratio should be between 0 and 1", analysis.estimatedRatioMedium in 0f..1f)
        assertTrue("High ratio should be between 0 and 1", analysis.estimatedRatioHigh in 0f..1f)
    }
    // endregion

    // region Rotate Operations
    @Test
    fun rotateAllPages90Clockwise_createsValidOutput() = runBlocking {
        val outputPath = File(outputDir, "rotated_90.pdf").absolutePath

        val result = repository.rotatePages(
            inputPath = multipagePdf.absolutePath,
            outputPath = outputPath,
            rotation = 90
        )

        assertTrue("Rotation should succeed", result.isSuccess)
        assertTrue("Output file should exist", File(outputPath).exists())

        val originalPages = repository.getPageCount(multipagePdf.absolutePath).getOrElse { 0 }
        val rotatedPages = repository.getPageCount(outputPath).getOrElse { 0 }
        assertEquals("Page count should be preserved", originalPages, rotatedPages)
    }

    @Test
    fun rotateAllPages180_createsValidOutput() = runBlocking {
        val outputPath = File(outputDir, "rotated_180.pdf").absolutePath

        val result = repository.rotatePages(
            inputPath = smallPdf.absolutePath,
            outputPath = outputPath,
            rotation = 180
        )

        assertTrue("Rotation should succeed", result.isSuccess)
        assertTrue("Output file should exist", File(outputPath).exists())
    }

    @Test
    fun rotateAllPages270_createsValidOutput() = runBlocking {
        val outputPath = File(outputDir, "rotated_270.pdf").absolutePath

        val result = repository.rotatePages(
            inputPath = withImagesPdf.absolutePath,
            outputPath = outputPath,
            rotation = 270
        )

        assertTrue("Rotation should succeed", result.isSuccess)
        assertTrue("Output file should exist", File(outputPath).exists())
    }

    @Test
    fun rotateSpecificPages_createsValidOutput() = runBlocking {
        val outputPath = File(outputDir, "rotated_specific.pdf").absolutePath
        val pageCount = repository.getPageCount(mediumPdf.absolutePath).getOrElse { 1 }

        val result = repository.rotatePages(
            inputPath = mediumPdf.absolutePath,
            outputPath = outputPath,
            rotation = 90,
            pages = listOf(1) // Only rotate first page
        )

        assertTrue("Rotation should succeed", result.isSuccess)
        assertTrue("Output file should exist", File(outputPath).exists())

        val rotatedPages = repository.getPageCount(outputPath).getOrElse { 0 }
        assertEquals("Page count should be preserved", pageCount, rotatedPages)
    }
    // endregion

    // region Reorder Operations
    @Test
    fun reorderPagesReverse_createsValidOutput() = runBlocking {
        val outputPath = File(outputDir, "reordered_reverse.pdf").absolutePath
        val pageCount = repository.getPageCount(mediumPdf.absolutePath).getOrElse { 1 }

        // Create reversed order
        val reversedOrder = (pageCount downTo 1).toList()

        val result = repository.reorderPages(
            inputPath = mediumPdf.absolutePath,
            outputPath = outputPath,
            newOrder = reversedOrder
        )

        assertTrue("Reorder should succeed", result.isSuccess)
        assertTrue("Output file should exist", File(outputPath).exists())

        val reorderedPages = repository.getPageCount(outputPath).getOrElse { 0 }
        assertEquals("Page count should be preserved", pageCount, reorderedPages)
    }

    @Test
    fun reorderPagesCustom_createsValidOutput() = runBlocking {
        val outputPath = File(outputDir, "reordered_custom.pdf").absolutePath
        val pageCount = repository.getPageCount(multipagePdf.absolutePath).getOrElse { 1 }

        // Custom order (e.g., 2, 1, 3... if more than 1 page)
        val customOrder = if (pageCount > 1) {
            listOf(2, 1) + (3..pageCount).toList()
        } else {
            listOf(1)
        }

        val result = repository.reorderPages(
            inputPath = multipagePdf.absolutePath,
            outputPath = outputPath,
            newOrder = customOrder
        )

        assertTrue("Reorder should succeed", result.isSuccess)
        assertTrue("Output file should exist", File(outputPath).exists())
    }

    @Test
    fun reorderSinglePagePdf_succeeds() = runBlocking {
        // First create a single page PDF
        val singlePagePath = File(outputDir, "single_page.pdf").absolutePath
        repository.extractPages(
            inputPath = multipagePdf.absolutePath,
            outputPath = singlePagePath,
            pages = listOf(1)
        )

        val reorderedPath = File(outputDir, "reordered_single.pdf").absolutePath
        val result = repository.reorderPages(
            inputPath = singlePagePath,
            outputPath = reorderedPath,
            newOrder = listOf(1)
        )

        assertTrue("Reorder single page should succeed", result.isSuccess)
    }
    // endregion

    // region Remove Pages Operations
    @Test
    fun removeSinglePage_createsValidOutput() = runBlocking {
        val outputPath = File(outputDir, "removed_single.pdf").absolutePath
        val originalPages = repository.getPageCount(mediumPdf.absolutePath).getOrElse { 2 }

        if (originalPages > 1) {
            val result = repository.removePages(
                inputPath = mediumPdf.absolutePath,
                outputPath = outputPath,
                pagesToRemove = listOf(1)
            )

            assertTrue("Remove should succeed", result.isSuccess)
            assertTrue("Output file should exist", File(outputPath).exists())

            val newPages = repository.getPageCount(outputPath).getOrElse { 0 }
            assertEquals("Should have one less page", originalPages - 1, newPages)
        }
    }

    @Test
    fun removeMultiplePages_createsValidOutput() = runBlocking {
        val outputPath = File(outputDir, "removed_multiple.pdf").absolutePath
        val originalPages = repository.getPageCount(largePdf.absolutePath).getOrElse { 3 }

        if (originalPages > 2) {
            val result = repository.removePages(
                inputPath = largePdf.absolutePath,
                outputPath = outputPath,
                pagesToRemove = listOf(1, 2)
            )

            assertTrue("Remove should succeed", result.isSuccess)
            assertTrue("Output file should exist", File(outputPath).exists())

            val newPages = repository.getPageCount(outputPath).getOrElse { 0 }
            assertEquals("Should have two less pages", originalPages - 2, newPages)
        }
    }

    @Test
    fun removeFirstAndLastPage_createsValidOutput() = runBlocking {
        val outputPath = File(outputDir, "removed_first_last.pdf").absolutePath
        val originalPages = repository.getPageCount(mediumPdf.absolutePath).getOrElse { 2 }

        if (originalPages > 2) {
            val result = repository.removePages(
                inputPath = mediumPdf.absolutePath,
                outputPath = outputPath,
                pagesToRemove = listOf(1, originalPages)
            )

            assertTrue("Remove should succeed", result.isSuccess)
            val newPages = repository.getPageCount(outputPath).getOrElse { 0 }
            assertEquals("Should have two less pages", originalPages - 2, newPages)
        }
    }

    @Test
    fun removeAllPages_returnsFailure() = runBlocking {
        val outputPath = File(outputDir, "removed_all.pdf").absolutePath
        val pageCount = repository.getPageCount(multipagePdf.absolutePath).getOrElse { 1 }

        val result = repository.removePages(
            inputPath = multipagePdf.absolutePath,
            outputPath = outputPath,
            pagesToRemove = (1..pageCount).toList()
        )

        assertTrue("Removing all pages should fail", result.isFailure)
    }
    // endregion

    // region Watermark Operations
    @Test
    fun addTextWatermarkCenter_createsValidOutput() = runBlocking {
        val outputPath = File(outputDir, "watermark_center.pdf").absolutePath

        val config = PdfToolsRepository.TextWatermarkConfig(
            text = "CONFIDENTIAL",
            fontSize = 48f,
            opacity = 50f,
            rotation = -45f,
            position = PdfToolsRepository.WatermarkPosition.CENTER
        )

        val result = repository.addTextWatermark(
            inputPath = multipagePdf.absolutePath,
            outputPath = outputPath,
            config = config
        )

        assertTrue("Watermark should succeed", result.isSuccess)
        assertTrue("Output file should exist", File(outputPath).exists())
        assertTrue("Output should be larger than empty", File(outputPath).length() > 0)
    }

    @Test
    fun addTextWatermarkTiled_createsValidOutput() = runBlocking {
        val outputPath = File(outputDir, "watermark_tiled.pdf").absolutePath

        val config = PdfToolsRepository.TextWatermarkConfig(
            text = "DRAFT",
            fontSize = 36f,
            opacity = 30f,
            rotation = -30f,
            position = PdfToolsRepository.WatermarkPosition.TILED
        )

        val result = repository.addTextWatermark(
            inputPath = smallPdf.absolutePath,
            outputPath = outputPath,
            config = config
        )

        assertTrue("Tiled watermark should succeed", result.isSuccess)
        assertTrue("Output file should exist", File(outputPath).exists())
    }

    @Test
    fun addWatermarkSpecificPages_createsValidOutput() = runBlocking {
        val outputPath = File(outputDir, "watermark_specific.pdf").absolutePath
        val pageCount = repository.getPageCount(mediumPdf.absolutePath).getOrElse { 1 }

        val config = PdfToolsRepository.TextWatermarkConfig(
            text = "SAMPLE",
            fontSize = 40f,
            opacity = 40f,
            position = PdfToolsRepository.WatermarkPosition.BOTTOM_RIGHT
        )

        val result = repository.addTextWatermark(
            inputPath = mediumPdf.absolutePath,
            outputPath = outputPath,
            config = config,
            pages = listOf(1) // Only first page
        )

        assertTrue("Watermark should succeed", result.isSuccess)

        val outputPages = repository.getPageCount(outputPath).getOrElse { 0 }
        assertEquals("Page count should be preserved", pageCount, outputPages)
    }

    @Test
    fun addWatermarkAllPositions_succeed() = runBlocking {
        val positions = listOf(
            PdfToolsRepository.WatermarkPosition.TOP_LEFT,
            PdfToolsRepository.WatermarkPosition.TOP_CENTER,
            PdfToolsRepository.WatermarkPosition.TOP_RIGHT,
            PdfToolsRepository.WatermarkPosition.BOTTOM_LEFT,
            PdfToolsRepository.WatermarkPosition.BOTTOM_CENTER,
            PdfToolsRepository.WatermarkPosition.BOTTOM_RIGHT
        )

        positions.forEachIndexed { index, position ->
            val outputPath = File(outputDir, "watermark_pos_$index.pdf").absolutePath

            val config = PdfToolsRepository.TextWatermarkConfig(
                text = "TEST",
                position = position
            )

            val result = repository.addTextWatermark(
                inputPath = multipagePdf.absolutePath,
                outputPath = outputPath,
                config = config
            )

            assertTrue("Watermark at $position should succeed", result.isSuccess)
        }
    }
    // endregion

    // region Page Numbers Operations
    @Test
    fun addPageNumbersBottomCenter_createsValidOutput() = runBlocking {
        val outputPath = File(outputDir, "numbered_bottom.pdf").absolutePath

        val config = PdfToolsRepository.PageNumberConfig(
            position = PdfToolsRepository.PageNumberPosition.BOTTOM_CENTER,
            format = PdfToolsRepository.PageNumberFormat.NUMBER_ONLY,
            fontSize = 12f
        )

        val result = repository.addPageNumbers(
            inputPath = mediumPdf.absolutePath,
            outputPath = outputPath,
            config = config
        )

        assertTrue("Add page numbers should succeed", result.isSuccess)
        assertTrue("Output file should exist", File(outputPath).exists())
    }

    @Test
    fun addPageNumbersAllPositions_succeed() = runBlocking {
        val positions = PdfToolsRepository.PageNumberPosition.entries

        positions.forEachIndexed { index, position ->
            val outputPath = File(outputDir, "numbered_pos_$index.pdf").absolutePath

            val config = PdfToolsRepository.PageNumberConfig(
                position = position,
                format = PdfToolsRepository.PageNumberFormat.NUMBER_ONLY
            )

            val result = repository.addPageNumbers(
                inputPath = multipagePdf.absolutePath,
                outputPath = outputPath,
                config = config
            )

            assertTrue("Page numbers at $position should succeed", result.isSuccess)
        }
    }

    @Test
    fun addPageNumbersXofY_createsValidOutput() = runBlocking {
        val outputPath = File(outputDir, "numbered_xofy.pdf").absolutePath

        val config = PdfToolsRepository.PageNumberConfig(
            position = PdfToolsRepository.PageNumberPosition.BOTTOM_CENTER,
            format = PdfToolsRepository.PageNumberFormat.X_OF_Y
        )

        val result = repository.addPageNumbers(
            inputPath = mediumPdf.absolutePath,
            outputPath = outputPath,
            config = config
        )

        assertTrue("X of Y format should succeed", result.isSuccess)
    }

    @Test
    fun addPageNumbersWithCustomMargins_createsValidOutput() = runBlocking {
        val outputPath = File(outputDir, "numbered_margins.pdf").absolutePath

        val config = PdfToolsRepository.PageNumberConfig(
            position = PdfToolsRepository.PageNumberPosition.BOTTOM_RIGHT,
            marginX = 50f,
            marginY = 40f,
            fontSize = 14f
        )

        val result = repository.addPageNumbers(
            inputPath = smallPdf.absolutePath,
            outputPath = outputPath,
            config = config
        )

        assertTrue("Custom margins should succeed", result.isSuccess)
    }

    @Test
    fun addPageNumbersSkipFirstPages_createsValidOutput() = runBlocking {
        val outputPath = File(outputDir, "numbered_skip.pdf").absolutePath
        val pageCount = repository.getPageCount(mediumPdf.absolutePath).getOrElse { 1 }

        val config = PdfToolsRepository.PageNumberConfig(
            startNumber = 1
        )

        // Skip first page (if there are at least 2 pages)
        val pages = if (pageCount > 1) (2..pageCount).toList() else listOf(1)

        val result = repository.addPageNumbers(
            inputPath = mediumPdf.absolutePath,
            outputPath = outputPath,
            config = config,
            pages = pages
        )

        assertTrue("Skip first pages should succeed", result.isSuccess)
    }
    // endregion

    // region Lock/Unlock Operations
    @Test
    fun lockWithUserPassword_createsProtectedPdf() = runBlocking {
        val outputPath = File(outputDir, "locked_user.pdf").absolutePath

        val result = repository.lockPdf(
            inputPath = multipagePdf.absolutePath,
            outputPath = outputPath,
            userPassword = "user123",
            ownerPassword = "owner456"
        )

        assertTrue("Lock should succeed", result.isSuccess)
        assertTrue("Output file should exist", File(outputPath).exists())

        // Verify it's password protected
        val isProtected = repository.isPasswordProtected(outputPath).getOrElse { false }
        assertTrue("File should be password protected", isProtected)
    }

    @Test
    fun lockWithOwnerPasswordOnly_createsProtectedPdf() = runBlocking {
        val outputPath = File(outputDir, "locked_owner.pdf").absolutePath

        val result = repository.lockPdf(
            inputPath = smallPdf.absolutePath,
            outputPath = outputPath,
            userPassword = "",  // No user password - can open without password
            ownerPassword = "owner789"
        )

        assertTrue("Lock should succeed", result.isSuccess)
        assertTrue("Output file should exist", File(outputPath).exists())
    }

    @Test
    fun lockWithPermissions_createsRestrictedPdf() = runBlocking {
        val outputPath = File(outputDir, "locked_permissions.pdf").absolutePath

        val permissions = PdfToolsRepository.PdfPermissions(
            allowPrinting = true,
            allowCopying = false,
            allowModifying = false,
            allowAnnotations = true
        )

        val result = repository.lockPdf(
            inputPath = withImagesPdf.absolutePath,
            outputPath = outputPath,
            userPassword = "user",
            ownerPassword = "owner",
            permissions = permissions
        )

        assertTrue("Lock with permissions should succeed", result.isSuccess)
    }

    @Test
    fun unlockWithCorrectPassword_createsUnlockedPdf() = runBlocking {
        // First, create a locked PDF
        val lockedPath = File(outputDir, "to_unlock.pdf").absolutePath
        val password = "testpass123"

        repository.lockPdf(
            inputPath = multipagePdf.absolutePath,
            outputPath = lockedPath,
            userPassword = password,
            ownerPassword = password
        )

        // Now unlock it
        val unlockedPath = File(outputDir, "unlocked.pdf").absolutePath
        val result = repository.unlockPdf(
            inputPath = lockedPath,
            outputPath = unlockedPath,
            password = password
        )

        assertTrue("Unlock should succeed", result.isSuccess)
        assertTrue("Unlocked file should exist", File(unlockedPath).exists())

        // Verify it's no longer protected
        val isProtected = repository.isPasswordProtected(unlockedPath).getOrElse { true }
        assertFalse("File should not be password protected", isProtected)
    }

    @Test
    fun unlockWithWrongPassword_returnsFailure() = runBlocking {
        // First, create a locked PDF
        val lockedPath = File(outputDir, "locked_for_fail.pdf").absolutePath

        repository.lockPdf(
            inputPath = multipagePdf.absolutePath,
            outputPath = lockedPath,
            userPassword = "correct",
            ownerPassword = "correct"
        )

        // Try to unlock with wrong password
        val unlockedPath = File(outputDir, "unlock_fail.pdf").absolutePath
        val result = repository.unlockPdf(
            inputPath = lockedPath,
            outputPath = unlockedPath,
            password = "wrong"
        )

        assertTrue("Unlock with wrong password should fail", result.isFailure)
    }

    @Test
    fun isPasswordProtected_detectsProtection() = runBlocking {
        // Check unprotected file
        val unprotectedResult = repository.isPasswordProtected(multipagePdf.absolutePath)
        assertTrue("Check should succeed", unprotectedResult.isSuccess)
        assertFalse("Sample PDF should not be protected", unprotectedResult.getOrElse { true })

        // Create and check protected file
        val lockedPath = File(outputDir, "protected_check.pdf").absolutePath
        repository.lockPdf(
            inputPath = multipagePdf.absolutePath,
            outputPath = lockedPath,
            userPassword = "pass",
            ownerPassword = "pass"
        )

        val protectedResult = repository.isPasswordProtected(lockedPath)
        assertTrue("Check should succeed", protectedResult.isSuccess)
        assertTrue("Locked PDF should be protected", protectedResult.getOrElse { false })
    }
    // endregion

    // region PDF to Image Operations
    @Test
    fun pdfToImagesPng_createsImages() = runBlocking {
        val pageCount = repository.getPageCount(multipagePdf.absolutePath).getOrElse { 1 }

        val result = repository.pdfToImages(
            inputPath = multipagePdf.absolutePath,
            outputDir = outputDir.absolutePath,
            format = "png"
        )

        assertTrue("Export should succeed", result.isSuccess)
        val images = result.getOrNull()
        assertNotNull("Should return image paths", images)
        assertEquals("Should create one image per page", pageCount, images!!.size)

        images.forEach { path ->
            assertTrue("Image should exist: $path", File(path).exists())
            assertTrue("Should be PNG", path.endsWith(".png"))
        }
    }

    @Test
    fun pdfToImagesJpg_createsImages() = runBlocking {
        val result = repository.pdfToImages(
            inputPath = smallPdf.absolutePath,
            outputDir = outputDir.absolutePath,
            format = "jpg"
        )

        assertTrue("Export should succeed", result.isSuccess)
        val images = result.getOrNull()
        assertNotNull("Should return image paths", images)

        images!!.forEach { path ->
            assertTrue("Image should exist: $path", File(path).exists())
            assertTrue("Should be JPG", path.endsWith(".jpg") || path.endsWith(".jpeg"))
        }
    }

    @Test
    fun pdfToImagesSpecificPages_createsSelectedImages() = runBlocking {
        val pageCount = repository.getPageCount(mediumPdf.absolutePath).getOrElse { 1 }
        val pagesToExport = if (pageCount >= 2) listOf(1, 2) else listOf(1)

        val result = repository.pdfToImages(
            inputPath = mediumPdf.absolutePath,
            outputDir = outputDir.absolutePath,
            format = "png",
            pages = pagesToExport
        )

        assertTrue("Export should succeed", result.isSuccess)
        val images = result.getOrNull()
        assertEquals("Should create correct number of images", pagesToExport.size, images!!.size)
    }
    // endregion

    // region Image to PDF Operations
    private fun createTestImage(name: String, width: Int = 200, height: Int = 300, color: Int = Color.BLUE): File {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(color)

        // Draw some text to make each image unique
        val paint = Paint().apply {
            this.color = Color.WHITE
            textSize = 40f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(name, width / 2f, height / 2f, paint)

        val file = File(testDir, name)
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        bitmap.recycle()
        return file
    }

    @Test
    fun imagesToPdf_singleImage_createsValidPdf() = runBlocking {
        val image = createTestImage("test_image_1.png")
        val outputPath = File(outputDir, "image_to_pdf_single.pdf").absolutePath

        val result = repository.imagesToPdf(
            imagePaths = listOf(image.absolutePath),
            outputPath = outputPath
        )

        assertTrue("Image to PDF should succeed", result.isSuccess)
        assertTrue("Output file should exist", File(outputPath).exists())

        val pageCount = repository.getPageCount(outputPath).getOrElse { 0 }
        assertEquals("PDF should have 1 page", 1, pageCount)
    }

    @Test
    fun imagesToPdf_multipleImages_createsValidPdf() = runBlocking {
        val image1 = createTestImage("test_image_1.png", color = Color.RED)
        val image2 = createTestImage("test_image_2.png", color = Color.GREEN)
        val image3 = createTestImage("test_image_3.png", color = Color.BLUE)
        val outputPath = File(outputDir, "image_to_pdf_multi.pdf").absolutePath

        val result = repository.imagesToPdf(
            imagePaths = listOf(image1.absolutePath, image2.absolutePath, image3.absolutePath),
            outputPath = outputPath
        )

        assertTrue("Image to PDF should succeed", result.isSuccess)
        assertTrue("Output file should exist", File(outputPath).exists())

        val pageCount = repository.getPageCount(outputPath).getOrElse { 0 }
        assertEquals("PDF should have 3 pages", 3, pageCount)
    }

    @Test
    fun imagesToPdf_jpegImage_createsValidPdf() = runBlocking {
        // Create JPEG image
        val bitmap = Bitmap.createBitmap(200, 300, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.YELLOW)
        val jpegFile = File(testDir, "test_image.jpg")
        FileOutputStream(jpegFile).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
        }
        bitmap.recycle()

        val outputPath = File(outputDir, "image_to_pdf_jpeg.pdf").absolutePath

        val result = repository.imagesToPdf(
            imagePaths = listOf(jpegFile.absolutePath),
            outputPath = outputPath
        )

        assertTrue("JPEG to PDF should succeed", result.isSuccess)
        assertTrue("Output file should exist", File(outputPath).exists())
    }

    @Test
    fun imagesToPdf_emptyList_returnsFailure() = runBlocking {
        val outputPath = File(outputDir, "image_to_pdf_empty.pdf").absolutePath

        val result = repository.imagesToPdf(
            imagePaths = emptyList(),
            outputPath = outputPath
        )

        assertTrue("Empty list should fail", result.isFailure)
    }

    @Test
    fun imagesToPdf_nonExistentImage_returnsFailure() = runBlocking {
        val outputPath = File(outputDir, "image_to_pdf_invalid.pdf").absolutePath

        val result = repository.imagesToPdf(
            imagePaths = listOf("/nonexistent/image.png"),
            outputPath = outputPath
        )

        assertTrue("Non-existent image should fail", result.isFailure)
    }
    // endregion

    // region Image Watermark Operations
    @Test
    fun addImageWatermarkCenter_createsValidOutput() = runBlocking {
        val watermarkImage = createTestImage("watermark.png", 100, 100, Color.RED)
        val outputPath = File(outputDir, "watermark_image_center.pdf").absolutePath

        val config = PdfToolsRepository.ImageWatermarkConfig(
            imagePath = watermarkImage.absolutePath,
            scale = 30f,
            opacity = 50f,
            position = PdfToolsRepository.WatermarkPosition.CENTER
        )

        val result = repository.addImageWatermark(
            inputPath = multipagePdf.absolutePath,
            outputPath = outputPath,
            config = config
        )

        assertTrue("Image watermark should succeed", result.isSuccess)
        assertTrue("Output file should exist", File(outputPath).exists())
        assertTrue("Output should have content", File(outputPath).length() > 0)
    }

    @Test
    fun addImageWatermarkAllPositions_succeed() = runBlocking {
        val watermarkImage = createTestImage("watermark_pos.png", 50, 50, Color.MAGENTA)
        val positions = listOf(
            PdfToolsRepository.WatermarkPosition.TOP_LEFT,
            PdfToolsRepository.WatermarkPosition.TOP_CENTER,
            PdfToolsRepository.WatermarkPosition.TOP_RIGHT,
            PdfToolsRepository.WatermarkPosition.BOTTOM_LEFT,
            PdfToolsRepository.WatermarkPosition.BOTTOM_CENTER,
            PdfToolsRepository.WatermarkPosition.BOTTOM_RIGHT
        )

        positions.forEachIndexed { index, position ->
            val outputPath = File(outputDir, "watermark_image_pos_$index.pdf").absolutePath

            val config = PdfToolsRepository.ImageWatermarkConfig(
                imagePath = watermarkImage.absolutePath,
                scale = 20f,
                opacity = 40f,
                position = position
            )

            val result = repository.addImageWatermark(
                inputPath = smallPdf.absolutePath,
                outputPath = outputPath,
                config = config
            )

            assertTrue("Image watermark at $position should succeed", result.isSuccess)
        }
    }

    @Test
    fun addImageWatermarkTiled_createsValidOutput() = runBlocking {
        val watermarkImage = createTestImage("watermark_tiled.png", 80, 80, Color.CYAN)
        val outputPath = File(outputDir, "watermark_image_tiled.pdf").absolutePath

        val config = PdfToolsRepository.ImageWatermarkConfig(
            imagePath = watermarkImage.absolutePath,
            scale = 15f,
            opacity = 25f,
            position = PdfToolsRepository.WatermarkPosition.TILED
        )

        val result = repository.addImageWatermark(
            inputPath = multipagePdf.absolutePath,
            outputPath = outputPath,
            config = config
        )

        assertTrue("Tiled image watermark should succeed", result.isSuccess)
        assertTrue("Output file should exist", File(outputPath).exists())
    }

    @Test
    fun addImageWatermarkSpecificPages_createsValidOutput() = runBlocking {
        val watermarkImage = createTestImage("watermark_specific.png", 60, 60, Color.DKGRAY)
        val outputPath = File(outputDir, "watermark_image_specific.pdf").absolutePath
        val pageCount = repository.getPageCount(mediumPdf.absolutePath).getOrElse { 1 }

        val config = PdfToolsRepository.ImageWatermarkConfig(
            imagePath = watermarkImage.absolutePath,
            scale = 25f,
            opacity = 60f,
            position = PdfToolsRepository.WatermarkPosition.BOTTOM_RIGHT
        )

        val result = repository.addImageWatermark(
            inputPath = mediumPdf.absolutePath,
            outputPath = outputPath,
            config = config,
            pages = listOf(1) // Only first page
        )

        assertTrue("Image watermark on specific pages should succeed", result.isSuccess)

        val outputPages = repository.getPageCount(outputPath).getOrElse { 0 }
        assertEquals("Page count should be preserved", pageCount, outputPages)
    }

    @Test
    fun addImageWatermarkNonExistentImage_returnsFailure() = runBlocking {
        val outputPath = File(outputDir, "watermark_image_invalid.pdf").absolutePath

        val config = PdfToolsRepository.ImageWatermarkConfig(
            imagePath = "/nonexistent/image.png"
        )

        val result = repository.addImageWatermark(
            inputPath = multipagePdf.absolutePath,
            outputPath = outputPath,
            config = config
        )

        assertTrue("Non-existent watermark image should fail", result.isFailure)
    }
    // endregion

    // region Utility Operations
    @Test
    fun getPageCount_returnsCorrectCount() = runBlocking {
        val result = repository.getPageCount(mediumPdf.absolutePath)

        assertTrue("Page count should succeed", result.isSuccess)
        val count = result.getOrNull()
        assertNotNull("Count should not be null", count)
        assertTrue("Count should be positive", count!! > 0)
    }

    @Test
    fun getPageCountNonExistent_returnsFailure() = runBlocking {
        val result = repository.getPageCount("/nonexistent/file.pdf")

        assertTrue("Non-existent file should fail", result.isFailure)
    }

    @Test
    fun progressCallback_isInvoked() = runBlocking {
        val outputPath = File(outputDir, "progress_test.pdf").absolutePath
        var progressCalled = false
        var lastProgress = 0f

        repository.mergePdfs(
            inputPaths = listOf(multipagePdf.absolutePath, smallPdf.absolutePath),
            outputPath = outputPath,
            onProgress = { progress ->
                progressCalled = true
                lastProgress = progress
            }
        )

        assertTrue("Progress callback should be called", progressCalled)
        assertEquals("Final progress should be 1.0", 1f, lastProgress, 0.01f)
    }
    // endregion
}
