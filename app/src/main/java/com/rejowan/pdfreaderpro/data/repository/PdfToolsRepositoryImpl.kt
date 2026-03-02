package com.rejowan.pdfreaderpro.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import com.itextpdf.io.image.ImageDataFactory
import com.itextpdf.kernel.geom.PageSize
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfReader
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.kernel.pdf.ReaderProperties
import com.itextpdf.kernel.pdf.WriterProperties
import com.itextpdf.kernel.pdf.EncryptionConstants
import com.itextpdf.kernel.utils.PdfMerger
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Image
import com.rejowan.pdfreaderpro.domain.repository.PdfToolsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream

class PdfToolsRepositoryImpl(
    private val context: Context
) : PdfToolsRepository {

    override suspend fun mergePdfs(
        inputPaths: List<String>,
        outputPath: String,
        onProgress: (Float) -> Unit
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            require(inputPaths.size >= 2) { "At least 2 PDFs required for merging" }

            val pdfDoc = PdfDocument(PdfWriter(outputPath))
            val merger = PdfMerger(pdfDoc)
            val totalFiles = inputPaths.size

            inputPaths.forEachIndexed { index, path ->
                val sourceDoc = PdfDocument(PdfReader(path))
                merger.merge(sourceDoc, 1, sourceDoc.numberOfPages)
                sourceDoc.close()
                onProgress((index + 1).toFloat() / totalFiles)
            }

            pdfDoc.close()
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to merge PDFs")
            Result.failure(e)
        }
    }

    override suspend fun mergePdfsWithSelection(
        selections: List<PdfToolsRepository.PdfPageSelection>,
        outputPath: String,
        onProgress: (Float) -> Unit
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            require(selections.isNotEmpty()) { "At least 1 PDF required for merging" }

            val pdfDoc = PdfDocument(PdfWriter(outputPath))
            val merger = PdfMerger(pdfDoc)
            val totalSelections = selections.size

            selections.forEachIndexed { index, selection ->
                val sourceDoc = PdfDocument(PdfReader(selection.path))
                val totalPages = sourceDoc.numberOfPages

                if (selection.pages == null) {
                    // All pages
                    merger.merge(sourceDoc, 1, totalPages)
                } else {
                    // Selected pages only
                    val validPages = selection.pages.filter { it in 1..totalPages }
                    if (validPages.isNotEmpty()) {
                        // PdfMerger.merge() requires a contiguous range, so we need to copy pages individually
                        validPages.forEach { pageNum ->
                            sourceDoc.copyPagesTo(pageNum, pageNum, pdfDoc)
                        }
                    }
                }

                sourceDoc.close()
                onProgress((index + 1).toFloat() / totalSelections)
            }

            pdfDoc.close()
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to merge PDFs with selection")
            Result.failure(e)
        }
    }

    override suspend fun splitPdf(
        inputPath: String,
        outputDir: String,
        ranges: List<String>,
        onProgress: (Float) -> Unit
    ): Result<List<String>> = withContext(Dispatchers.IO) {
        try {
            val sourceDoc = PdfDocument(PdfReader(inputPath))
            val totalPages = sourceDoc.numberOfPages
            val createdFiles = mutableListOf<String>()
            val baseName = File(inputPath).nameWithoutExtension

            ranges.forEachIndexed { index, range ->
                val (start, end) = parseRange(range, totalPages)
                val outputPath = "$outputDir/${baseName}_part${index + 1}.pdf"

                val destDoc = PdfDocument(PdfWriter(outputPath))
                sourceDoc.copyPagesTo(start, end, destDoc)
                destDoc.close()

                createdFiles.add(outputPath)
                onProgress((index + 1).toFloat() / ranges.size)
            }

            sourceDoc.close()
            Result.success(createdFiles)
        } catch (e: Exception) {
            Timber.e(e, "Failed to split PDF")
            Result.failure(e)
        }
    }

    override suspend fun splitIntoPages(
        inputPath: String,
        outputDir: String,
        onProgress: (Float) -> Unit
    ): Result<List<String>> = withContext(Dispatchers.IO) {
        try {
            val sourceDoc = PdfDocument(PdfReader(inputPath))
            val totalPages = sourceDoc.numberOfPages
            val createdFiles = mutableListOf<String>()
            val baseName = File(inputPath).nameWithoutExtension

            for (pageNum in 1..totalPages) {
                val outputPath = "$outputDir/${baseName}_page$pageNum.pdf"
                val destDoc = PdfDocument(PdfWriter(outputPath))
                sourceDoc.copyPagesTo(pageNum, pageNum, destDoc)
                destDoc.close()
                createdFiles.add(outputPath)
                onProgress(pageNum.toFloat() / totalPages)
            }

            sourceDoc.close()
            Result.success(createdFiles)
        } catch (e: Exception) {
            Timber.e(e, "Failed to split PDF into pages")
            Result.failure(e)
        }
    }

    override suspend fun extractPages(
        inputPath: String,
        outputPath: String,
        pages: List<Int>,
        onProgress: (Float) -> Unit
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            require(pages.isNotEmpty()) { "At least one page must be selected" }

            val sourceDoc = PdfDocument(PdfReader(inputPath))
            val destDoc = PdfDocument(PdfWriter(outputPath))

            pages.forEachIndexed { index, pageNum ->
                sourceDoc.copyPagesTo(pageNum, pageNum, destDoc)
                onProgress((index + 1).toFloat() / pages.size)
            }

            sourceDoc.close()
            destDoc.close()
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to extract pages")
            Result.failure(e)
        }
    }

    override suspend fun rotatePages(
        inputPath: String,
        outputPath: String,
        rotation: Int,
        pages: List<Int>?,
        onProgress: (Float) -> Unit
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            require(rotation in listOf(90, 180, 270)) { "Rotation must be 90, 180, or 270" }

            val sourceDoc = PdfDocument(PdfReader(inputPath))
            val destDoc = PdfDocument(PdfWriter(outputPath))
            val totalPages = sourceDoc.numberOfPages

            sourceDoc.copyPagesTo(1, totalPages, destDoc)

            val pagesToRotate = pages ?: (1..totalPages).toList()
            pagesToRotate.forEachIndexed { index, pageNum ->
                if (pageNum in 1..totalPages) {
                    val page = destDoc.getPage(pageNum)
                    val currentRotation = page.rotation
                    page.setRotation((currentRotation + rotation) % 360)
                }
                onProgress((index + 1).toFloat() / pagesToRotate.size)
            }

            sourceDoc.close()
            destDoc.close()
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to rotate pages")
            Result.failure(e)
        }
    }

    override suspend fun reorderPages(
        inputPath: String,
        outputPath: String,
        newOrder: List<Int>,
        onProgress: (Float) -> Unit
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val sourceDoc = PdfDocument(PdfReader(inputPath))
            val destDoc = PdfDocument(PdfWriter(outputPath))

            newOrder.forEachIndexed { index, pageNum ->
                sourceDoc.copyPagesTo(pageNum, pageNum, destDoc)
                onProgress((index + 1).toFloat() / newOrder.size)
            }

            sourceDoc.close()
            destDoc.close()
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to reorder pages")
            Result.failure(e)
        }
    }

    override suspend fun compressPdf(
        inputPath: String,
        outputPath: String,
        quality: Float,
        onProgress: (Float) -> Unit
    ): Result<Long> = withContext(Dispatchers.IO) {
        try {
            val writerProperties = WriterProperties()
                .setCompressionLevel(9) // Max compression for streams
                .setFullCompressionMode(true)

            val sourceDoc = PdfDocument(PdfReader(inputPath))
            val destDoc = PdfDocument(PdfWriter(outputPath, writerProperties))
            val totalPages = sourceDoc.numberOfPages

            sourceDoc.copyPagesTo(1, totalPages, destDoc)

            for (pageNum in 1..totalPages) {
                onProgress(pageNum.toFloat() / totalPages)
            }

            sourceDoc.close()
            destDoc.close()

            val outputFile = File(outputPath)
            Result.success(outputFile.length())
        } catch (e: Exception) {
            Timber.e(e, "Failed to compress PDF")
            Result.failure(e)
        }
    }

    override suspend fun imagesToPdf(
        imagePaths: List<String>,
        outputPath: String,
        onProgress: (Float) -> Unit
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            require(imagePaths.isNotEmpty()) { "At least one image required" }

            val pdfDoc = PdfDocument(PdfWriter(outputPath))
            val document = Document(pdfDoc)

            imagePaths.forEachIndexed { index, imagePath ->
                val imageData = ImageDataFactory.create(imagePath)
                val image = Image(imageData)

                // Scale image to fit page while maintaining aspect ratio
                val pageSize = PageSize.A4
                val availableWidth = pageSize.width - 72 // 36pt margins on each side
                val availableHeight = pageSize.height - 72

                val imageWidth = image.imageWidth
                val imageHeight = image.imageHeight
                val widthRatio = availableWidth / imageWidth
                val heightRatio = availableHeight / imageHeight
                val scaleFactor = minOf(widthRatio, heightRatio)

                image.scaleToFit(imageWidth * scaleFactor, imageHeight * scaleFactor)

                if (index > 0) {
                    document.add(com.itextpdf.layout.element.AreaBreak())
                }
                document.add(image)

                onProgress((index + 1).toFloat() / imagePaths.size)
            }

            document.close()
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to convert images to PDF")
            Result.failure(e)
        }
    }

    override suspend fun pdfToImages(
        inputPath: String,
        outputDir: String,
        format: String,
        pages: List<Int>?,
        onProgress: (Float) -> Unit
    ): Result<List<String>> = withContext(Dispatchers.IO) {
        try {
            val file = File(inputPath)
            val fd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
            val renderer = PdfRenderer(fd)
            val totalPages = renderer.pageCount
            val pagesToExport = pages ?: (1..totalPages).toList()
            val createdFiles = mutableListOf<String>()
            val baseName = file.nameWithoutExtension

            pagesToExport.forEachIndexed { index, pageNum ->
                val pageIndex = pageNum - 1 // PdfRenderer uses 0-based index
                if (pageIndex in 0 until totalPages) {
                    val page = renderer.openPage(pageIndex)

                    // Create bitmap at 2x scale for better quality
                    val scale = 2
                    val bitmap = Bitmap.createBitmap(
                        page.width * scale,
                        page.height * scale,
                        Bitmap.Config.ARGB_8888
                    )

                    page.render(
                        bitmap,
                        null,
                        null,
                        PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY
                    )
                    page.close()

                    val extension = if (format.lowercase() == "jpg") "jpg" else "png"
                    val outputPath = "$outputDir/${baseName}_page$pageNum.$extension"

                    FileOutputStream(outputPath).use { out ->
                        val compressFormat = if (format.lowercase() == "jpg") {
                            Bitmap.CompressFormat.JPEG
                        } else {
                            Bitmap.CompressFormat.PNG
                        }
                        bitmap.compress(compressFormat, 90, out)
                    }

                    bitmap.recycle()
                    createdFiles.add(outputPath)
                }
                onProgress((index + 1).toFloat() / pagesToExport.size)
            }

            renderer.close()
            fd.close()
            Result.success(createdFiles)
        } catch (e: Exception) {
            Timber.e(e, "Failed to export PDF to images")
            Result.failure(e)
        }
    }

    override suspend fun getPageCount(inputPath: String): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val pdfDoc = PdfDocument(PdfReader(inputPath))
            val count = pdfDoc.numberOfPages
            pdfDoc.close()
            Result.success(count)
        } catch (e: Exception) {
            Timber.e(e, "Failed to get page count")
            Result.failure(e)
        }
    }

    override suspend fun analyzeCompressionPotential(
        inputPath: String
    ): Result<PdfToolsRepository.CompressionAnalysis> = withContext(Dispatchers.IO) {
        try {
            val file = File(inputPath)
            val fileSize = file.length()

            val pdfDoc = PdfDocument(PdfReader(inputPath))
            val pageCount = pdfDoc.numberOfPages

            // Calculate bytes per page
            val bytesPerPage = if (pageCount > 0) fileSize / pageCount else fileSize

            // Check for images by scanning PDF objects
            var hasImages = false
            var imageCount = 0
            var totalImageBytes = 0L

            for (i in 1..pdfDoc.numberOfPages) {
                val page = pdfDoc.getPage(i)
                val resources = page.resources

                // Check for XObject (images are stored as XObjects)
                val xObjects = resources?.getResource(com.itextpdf.kernel.pdf.PdfName.XObject) as? com.itextpdf.kernel.pdf.PdfDictionary
                if (xObjects != null) {
                    for (key in xObjects.keySet()) {
                        val xObject = xObjects.getAsStream(key)
                        if (xObject != null) {
                            val subtype = xObject.getAsName(com.itextpdf.kernel.pdf.PdfName.Subtype)
                            if (subtype == com.itextpdf.kernel.pdf.PdfName.Image) {
                                hasImages = true
                                imageCount++
                                // Estimate image size from stream length
                                xObject.getAsNumber(com.itextpdf.kernel.pdf.PdfName.Length)?.let {
                                    totalImageBytes += it.longValue()
                                }
                            }
                        }
                    }
                }
            }

            pdfDoc.close()

            // Determine if already optimized based on bytes per page
            // Well-optimized PDFs typically have lower bytes per page
            val isAlreadyOptimized = bytesPerPage < 30_000 && !hasImages

            // Calculate estimated compression ratios based on content analysis
            val (ratioLow, ratioMedium, ratioHigh) = when {
                // Scanned/image-heavy PDFs (>500KB per page)
                bytesPerPage > 500_000 -> Triple(0.70f, 0.50f, 0.35f)

                // Image-heavy PDFs (200-500KB per page)
                bytesPerPage > 200_000 -> Triple(0.80f, 0.60f, 0.45f)

                // Mixed content (100-200KB per page)
                bytesPerPage > 100_000 -> Triple(0.85f, 0.70f, 0.55f)

                // Light images or formatted text (50-100KB per page)
                bytesPerPage > 50_000 -> Triple(0.90f, 0.80f, 0.70f)

                // Mostly text (30-50KB per page)
                bytesPerPage > 30_000 -> Triple(0.95f, 0.88f, 0.80f)

                // Already compact/optimized (<30KB per page)
                else -> Triple(0.98f, 0.95f, 0.90f)
            }

            // Adjust if has images (more potential for compression)
            val adjustedRatios = if (hasImages && imageCount > pageCount / 2) {
                // Many images - more compression potential
                Triple(
                    (ratioLow - 0.05f).coerceAtLeast(0.5f),
                    (ratioMedium - 0.10f).coerceAtLeast(0.4f),
                    (ratioHigh - 0.15f).coerceAtLeast(0.3f)
                )
            } else {
                Triple(ratioLow, ratioMedium, ratioHigh)
            }

            Timber.d("PDF Analysis: ${file.name} - ${bytesPerPage / 1024}KB/page, images=$hasImages ($imageCount), optimized=$isAlreadyOptimized")
            Timber.d("Estimated ratios: low=${adjustedRatios.first}, medium=${adjustedRatios.second}, high=${adjustedRatios.third}")

            Result.success(
                PdfToolsRepository.CompressionAnalysis(
                    bytesPerPage = bytesPerPage,
                    hasImages = hasImages,
                    isAlreadyOptimized = isAlreadyOptimized,
                    estimatedRatioLow = adjustedRatios.first,
                    estimatedRatioMedium = adjustedRatios.second,
                    estimatedRatioHigh = adjustedRatios.third
                )
            )
        } catch (e: Exception) {
            Timber.e(e, "Failed to analyze PDF")
            Result.failure(e)
        }
    }

    override suspend fun lockPdf(
        inputPath: String,
        outputPath: String,
        userPassword: String,
        ownerPassword: String,
        permissions: PdfToolsRepository.PdfPermissions,
        onProgress: (Float) -> Unit
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            require(ownerPassword.isNotEmpty()) { "Owner password is required" }

            onProgress(0.1f)

            // Calculate permissions bitmask
            var permissionFlags = 0
            if (permissions.allowPrinting) {
                permissionFlags = permissionFlags or EncryptionConstants.ALLOW_PRINTING
            }
            if (permissions.allowCopying) {
                permissionFlags = permissionFlags or EncryptionConstants.ALLOW_COPY
            }
            if (permissions.allowModifying) {
                permissionFlags = permissionFlags or EncryptionConstants.ALLOW_MODIFY_CONTENTS
            }
            if (permissions.allowAnnotations) {
                permissionFlags = permissionFlags or EncryptionConstants.ALLOW_MODIFY_ANNOTATIONS
            }

            onProgress(0.2f)

            // Set up writer with encryption
            val writerProperties = WriterProperties()
                .setStandardEncryption(
                    userPassword.toByteArray(),
                    ownerPassword.toByteArray(),
                    permissionFlags,
                    EncryptionConstants.ENCRYPTION_AES_256
                )

            onProgress(0.3f)

            // Read source and write encrypted PDF
            val reader = PdfReader(inputPath)
            val writer = PdfWriter(outputPath, writerProperties)
            val pdfDoc = PdfDocument(reader, writer)

            val totalPages = pdfDoc.numberOfPages
            onProgress(0.5f)

            // Copy all pages (encryption is applied automatically)
            for (i in 1..totalPages) {
                onProgress(0.5f + (0.4f * i / totalPages))
            }

            pdfDoc.close()
            onProgress(1f)

            Timber.d("PDF locked successfully: $outputPath")
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to lock PDF")
            Result.failure(e)
        }
    }

    override suspend fun unlockPdf(
        inputPath: String,
        outputPath: String,
        password: String,
        onProgress: (Float) -> Unit
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            onProgress(0.1f)

            // Set up reader with password
            val readerProperties = ReaderProperties()
                .setPassword(password.toByteArray())

            onProgress(0.2f)

            // Open encrypted PDF with password
            val reader = PdfReader(inputPath, readerProperties)
            reader.setUnethicalReading(true) // Allow reading even with restrictions

            onProgress(0.3f)

            // Create new PDF without encryption
            val writer = PdfWriter(outputPath)
            val pdfDoc = PdfDocument(reader, writer)

            val totalPages = pdfDoc.numberOfPages
            onProgress(0.5f)

            // Pages are automatically copied
            for (i in 1..totalPages) {
                onProgress(0.5f + (0.4f * i / totalPages))
            }

            pdfDoc.close()
            onProgress(1f)

            Timber.d("PDF unlocked successfully: $outputPath")
            Result.success(Unit)
        } catch (e: com.itextpdf.kernel.exceptions.BadPasswordException) {
            Timber.e(e, "Wrong password for PDF")
            Result.failure(Exception("Incorrect password"))
        } catch (e: Exception) {
            Timber.e(e, "Failed to unlock PDF")
            Result.failure(e)
        }
    }

    override suspend fun isPasswordProtected(inputPath: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val reader = PdfReader(inputPath)
            val isEncrypted = reader.isEncrypted
            reader.close()
            Result.success(isEncrypted)
        } catch (e: com.itextpdf.kernel.exceptions.BadPasswordException) {
            // If we get a BadPasswordException, the PDF is password protected
            Result.success(true)
        } catch (e: Exception) {
            Timber.e(e, "Failed to check PDF encryption")
            Result.failure(e)
        }
    }

    private fun parseRange(range: String, maxPages: Int): Pair<Int, Int> {
        val parts = range.trim().split("-")
        return when (parts.size) {
            1 -> {
                val page = parts[0].toInt().coerceIn(1, maxPages)
                page to page
            }
            2 -> {
                val start = parts[0].toInt().coerceIn(1, maxPages)
                val end = parts[1].toInt().coerceIn(start, maxPages)
                start to end
            }
            else -> throw IllegalArgumentException("Invalid range format: $range")
        }
    }
}
