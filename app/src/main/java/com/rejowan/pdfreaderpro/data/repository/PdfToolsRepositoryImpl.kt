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
import com.itextpdf.kernel.colors.ColorConstants
import com.itextpdf.kernel.colors.DeviceRgb
import com.itextpdf.kernel.font.PdfFontFactory
import com.itextpdf.kernel.pdf.canvas.PdfCanvas
import com.itextpdf.kernel.pdf.extgstate.PdfExtGState
import com.itextpdf.kernel.utils.PdfMerger
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Image
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.properties.TextAlignment
import com.itextpdf.layout.properties.VerticalAlignment
import kotlin.math.cos
import kotlin.math.sin
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
            // Need to create PdfDocument to actually read the PDF and check encryption
            val reader = PdfReader(inputPath)
            val pdfDoc = PdfDocument(reader)
            val isEncrypted = reader.isEncrypted
            pdfDoc.close()
            Result.success(isEncrypted)
        } catch (e: com.itextpdf.kernel.exceptions.BadPasswordException) {
            // If we get a BadPasswordException, the PDF is password protected
            Result.success(true)
        } catch (e: Exception) {
            Timber.e(e, "Failed to check PDF encryption")
            Result.failure(e)
        }
    }

    override suspend fun removePages(
        inputPath: String,
        outputPath: String,
        pagesToRemove: List<Int>,
        onProgress: (Float) -> Unit
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            require(pagesToRemove.isNotEmpty()) { "At least one page must be selected for removal" }

            onProgress(0.1f)

            val sourceDoc = PdfDocument(PdfReader(inputPath))
            val totalPages = sourceDoc.numberOfPages

            // Validate pages to remove
            val validPagesToRemove = pagesToRemove.filter { it in 1..totalPages }.toSet()
            require(validPagesToRemove.size < totalPages) { "Cannot remove all pages from PDF" }

            onProgress(0.2f)

            // Calculate pages to keep
            val pagesToKeep = (1..totalPages).filter { it !in validPagesToRemove }

            onProgress(0.3f)

            // Create new PDF with only the pages to keep
            val destDoc = PdfDocument(PdfWriter(outputPath))

            pagesToKeep.forEachIndexed { index, pageNum ->
                sourceDoc.copyPagesTo(pageNum, pageNum, destDoc)
                onProgress(0.3f + (0.6f * (index + 1) / pagesToKeep.size))
            }

            sourceDoc.close()
            destDoc.close()

            onProgress(1f)

            Timber.d("Removed ${validPagesToRemove.size} pages from PDF: $outputPath")
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to remove pages from PDF")
            Result.failure(e)
        }
    }

    override suspend fun addTextWatermark(
        inputPath: String,
        outputPath: String,
        config: PdfToolsRepository.TextWatermarkConfig,
        pages: List<Int>?,
        onProgress: (Float) -> Unit
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            onProgress(0.1f)

            val sourceDoc = PdfDocument(PdfReader(inputPath))
            val destDoc = PdfDocument(PdfWriter(outputPath))
            val totalPages = sourceDoc.numberOfPages

            // Copy all pages first
            sourceDoc.copyPagesTo(1, totalPages, destDoc)
            sourceDoc.close()

            onProgress(0.3f)

            // Determine which pages to watermark
            val pagesToWatermark = pages?.filter { it in 1..totalPages } ?: (1..totalPages).toList()

            // Extract color components from the Int color
            val alpha = ((config.color shr 24) and 0xFF) / 255f
            val red = ((config.color shr 16) and 0xFF) / 255f
            val green = ((config.color shr 8) and 0xFF) / 255f
            val blue = (config.color and 0xFF) / 255f

            val color = DeviceRgb(red, green, blue)
            val font = PdfFontFactory.createFont()

            // Calculate effective opacity (config opacity * color alpha)
            val effectiveOpacity = (config.opacity / 100f) * alpha

            pagesToWatermark.forEachIndexed { index, pageNum ->
                val page = destDoc.getPage(pageNum)
                val pageSize = page.pageSize
                val canvas = PdfCanvas(page)

                // Set transparency
                val gs = PdfExtGState()
                gs.setFillOpacity(effectiveOpacity)
                canvas.setExtGState(gs)

                canvas.saveState()

                when (config.position) {
                    PdfToolsRepository.WatermarkPosition.TILED -> {
                        // Draw tiled watermarks
                        val textWidth = font.getWidth(config.text, config.fontSize)
                        val textHeight = config.fontSize
                        val spacingX = textWidth + 100
                        val spacingY = textHeight + 100

                        var y = 0f
                        while (y < pageSize.height + spacingY) {
                            var x = 0f
                            while (x < pageSize.width + spacingX) {
                                canvas.saveState()
                                canvas.concatMatrix(
                                    cos(Math.toRadians(config.rotation.toDouble())),
                                    sin(Math.toRadians(config.rotation.toDouble())),
                                    -sin(Math.toRadians(config.rotation.toDouble())),
                                    cos(Math.toRadians(config.rotation.toDouble())),
                                    x.toDouble(),
                                    y.toDouble()
                                )
                                canvas.beginText()
                                    .setFontAndSize(font, config.fontSize)
                                    .setColor(color, true)
                                    .moveText(0.0, 0.0)
                                    .showText(config.text)
                                    .endText()
                                canvas.restoreState()
                                x += spacingX
                            }
                            y += spacingY
                        }
                    }
                    else -> {
                        // Calculate position
                        val (x, y) = calculateTextPosition(
                            config.position,
                            pageSize.width,
                            pageSize.height,
                            font.getWidth(config.text, config.fontSize),
                            config.fontSize
                        )

                        // Apply rotation around the text center
                        canvas.concatMatrix(
                            cos(Math.toRadians(config.rotation.toDouble())),
                            sin(Math.toRadians(config.rotation.toDouble())),
                            -sin(Math.toRadians(config.rotation.toDouble())),
                            cos(Math.toRadians(config.rotation.toDouble())),
                            x.toDouble(),
                            y.toDouble()
                        )

                        canvas.beginText()
                            .setFontAndSize(font, config.fontSize)
                            .setColor(color, true)
                            .moveText(0.0, 0.0)
                            .showText(config.text)
                            .endText()
                    }
                }

                canvas.restoreState()
                onProgress(0.3f + (0.6f * (index + 1) / pagesToWatermark.size))
            }

            destDoc.close()
            onProgress(1f)

            Timber.d("Text watermark added successfully: $outputPath")
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to add text watermark")
            Result.failure(e)
        }
    }

    override suspend fun addImageWatermark(
        inputPath: String,
        outputPath: String,
        config: PdfToolsRepository.ImageWatermarkConfig,
        pages: List<Int>?,
        onProgress: (Float) -> Unit
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            onProgress(0.1f)

            val sourceDoc = PdfDocument(PdfReader(inputPath))
            val destDoc = PdfDocument(PdfWriter(outputPath))
            val totalPages = sourceDoc.numberOfPages

            // Copy all pages first
            sourceDoc.copyPagesTo(1, totalPages, destDoc)
            sourceDoc.close()

            onProgress(0.3f)

            // Load the watermark image
            val imageData = ImageDataFactory.create(config.imagePath)

            // Determine which pages to watermark
            val pagesToWatermark = pages?.filter { it in 1..totalPages } ?: (1..totalPages).toList()

            pagesToWatermark.forEachIndexed { index, pageNum ->
                val page = destDoc.getPage(pageNum)
                val pageSize = page.pageSize
                val canvas = PdfCanvas(page)

                // Set transparency
                val gs = PdfExtGState()
                gs.setFillOpacity(config.opacity / 100f)
                canvas.setExtGState(gs)

                // Calculate image size based on scale
                val maxDimension = minOf(pageSize.width, pageSize.height) * (config.scale / 100f)
                val aspectRatio = imageData.width / imageData.height
                val imageWidth: Float
                val imageHeight: Float
                if (aspectRatio > 1) {
                    imageWidth = maxDimension
                    imageHeight = maxDimension / aspectRatio
                } else {
                    imageHeight = maxDimension
                    imageWidth = maxDimension * aspectRatio
                }

                when (config.position) {
                    PdfToolsRepository.WatermarkPosition.TILED -> {
                        // Draw tiled watermarks
                        val spacingX = imageWidth + 50
                        val spacingY = imageHeight + 50

                        var y = 0f
                        while (y < pageSize.height) {
                            var x = 0f
                            while (x < pageSize.width) {
                                canvas.addImageWithTransformationMatrix(
                                    imageData,
                                    imageWidth,
                                    0f,
                                    0f,
                                    imageHeight,
                                    x,
                                    y
                                )
                                x += spacingX
                            }
                            y += spacingY
                        }
                    }
                    else -> {
                        // Calculate position
                        val (x, y) = calculateImagePosition(
                            config.position,
                            pageSize.width,
                            pageSize.height,
                            imageWidth,
                            imageHeight
                        )

                        canvas.addImageWithTransformationMatrix(
                            imageData,
                            imageWidth,
                            0f,
                            0f,
                            imageHeight,
                            x,
                            y
                        )
                    }
                }

                onProgress(0.3f + (0.6f * (index + 1) / pagesToWatermark.size))
            }

            destDoc.close()
            onProgress(1f)

            Timber.d("Image watermark added successfully: $outputPath")
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to add image watermark")
            Result.failure(e)
        }
    }

    private fun calculateTextPosition(
        position: PdfToolsRepository.WatermarkPosition,
        pageWidth: Float,
        pageHeight: Float,
        textWidth: Float,
        textHeight: Float
    ): Pair<Float, Float> {
        val margin = 50f
        return when (position) {
            PdfToolsRepository.WatermarkPosition.CENTER ->
                Pair(pageWidth / 2 - textWidth / 2, pageHeight / 2)
            PdfToolsRepository.WatermarkPosition.TOP_LEFT ->
                Pair(margin, pageHeight - margin - textHeight)
            PdfToolsRepository.WatermarkPosition.TOP_CENTER ->
                Pair(pageWidth / 2 - textWidth / 2, pageHeight - margin - textHeight)
            PdfToolsRepository.WatermarkPosition.TOP_RIGHT ->
                Pair(pageWidth - margin - textWidth, pageHeight - margin - textHeight)
            PdfToolsRepository.WatermarkPosition.BOTTOM_LEFT ->
                Pair(margin, margin)
            PdfToolsRepository.WatermarkPosition.BOTTOM_CENTER ->
                Pair(pageWidth / 2 - textWidth / 2, margin)
            PdfToolsRepository.WatermarkPosition.BOTTOM_RIGHT ->
                Pair(pageWidth - margin - textWidth, margin)
            PdfToolsRepository.WatermarkPosition.TILED ->
                Pair(0f, 0f) // Handled separately
        }
    }

    override suspend fun addPageNumbers(
        inputPath: String,
        outputPath: String,
        config: PdfToolsRepository.PageNumberConfig,
        pages: List<Int>?,
        onProgress: (Float) -> Unit
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            onProgress(0.1f)

            val sourceDoc = PdfDocument(PdfReader(inputPath))
            val destDoc = PdfDocument(PdfWriter(outputPath))
            val totalPages = sourceDoc.numberOfPages

            // Copy all pages first
            sourceDoc.copyPagesTo(1, totalPages, destDoc)
            sourceDoc.close()

            onProgress(0.3f)

            // Determine which pages to number
            val pagesToNumber = pages?.filter { it in 1..totalPages } ?: (1..totalPages).toList()

            // Extract color components
            val red = ((config.color shr 16) and 0xFF) / 255f
            val green = ((config.color shr 8) and 0xFF) / 255f
            val blue = (config.color and 0xFF) / 255f

            val color = DeviceRgb(red, green, blue)
            val font = PdfFontFactory.createFont()

            pagesToNumber.forEachIndexed { index, pageNum ->
                val page = destDoc.getPage(pageNum)
                val pageSize = page.pageSize
                val canvas = PdfCanvas(page)

                // Calculate the display number (respecting startNumber)
                val displayNumber = config.startNumber + (pageNum - 1)

                // Format the page number text
                val numberText = formatPageNumber(
                    format = config.format,
                    currentPage = displayNumber,
                    totalPages = totalPages + config.startNumber - 1,
                    prefix = config.prefix,
                    suffix = config.suffix
                )

                // Calculate text width for centering
                val textWidth = font.getWidth(numberText, config.fontSize)

                // Calculate position
                val (x, y) = calculatePageNumberPosition(
                    position = config.position,
                    pageWidth = pageSize.width,
                    pageHeight = pageSize.height,
                    textWidth = textWidth,
                    marginX = config.marginX,
                    marginY = config.marginY
                )

                canvas.beginText()
                    .setFontAndSize(font, config.fontSize)
                    .setColor(color, true)
                    .moveText(x.toDouble(), y.toDouble())
                    .showText(numberText)
                    .endText()

                onProgress(0.3f + (0.6f * (index + 1) / pagesToNumber.size))
            }

            destDoc.close()
            onProgress(1f)

            Timber.d("Page numbers added successfully: $outputPath")
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to add page numbers")
            Result.failure(e)
        }
    }

    private fun formatPageNumber(
        format: PdfToolsRepository.PageNumberFormat,
        currentPage: Int,
        totalPages: Int,
        prefix: String,
        suffix: String
    ): String {
        return when (format) {
            PdfToolsRepository.PageNumberFormat.NUMBER_ONLY -> "$currentPage"
            PdfToolsRepository.PageNumberFormat.PAGE_X -> "Page $currentPage"
            PdfToolsRepository.PageNumberFormat.X_OF_Y -> "$currentPage of $totalPages"
            PdfToolsRepository.PageNumberFormat.DASH_X_DASH -> "- $currentPage -"
            PdfToolsRepository.PageNumberFormat.CUSTOM -> "$prefix$currentPage$suffix"
        }
    }

    private fun calculatePageNumberPosition(
        position: PdfToolsRepository.PageNumberPosition,
        pageWidth: Float,
        pageHeight: Float,
        textWidth: Float,
        marginX: Float,
        marginY: Float
    ): Pair<Float, Float> {
        return when (position) {
            PdfToolsRepository.PageNumberPosition.TOP_LEFT ->
                Pair(marginX, pageHeight - marginY)
            PdfToolsRepository.PageNumberPosition.TOP_CENTER ->
                Pair(pageWidth / 2 - textWidth / 2, pageHeight - marginY)
            PdfToolsRepository.PageNumberPosition.TOP_RIGHT ->
                Pair(pageWidth - marginX - textWidth, pageHeight - marginY)
            PdfToolsRepository.PageNumberPosition.BOTTOM_LEFT ->
                Pair(marginX, marginY)
            PdfToolsRepository.PageNumberPosition.BOTTOM_CENTER ->
                Pair(pageWidth / 2 - textWidth / 2, marginY)
            PdfToolsRepository.PageNumberPosition.BOTTOM_RIGHT ->
                Pair(pageWidth - marginX - textWidth, marginY)
        }
    }

    private fun calculateImagePosition(
        position: PdfToolsRepository.WatermarkPosition,
        pageWidth: Float,
        pageHeight: Float,
        imageWidth: Float,
        imageHeight: Float
    ): Pair<Float, Float> {
        val margin = 50f
        return when (position) {
            PdfToolsRepository.WatermarkPosition.CENTER ->
                Pair(pageWidth / 2 - imageWidth / 2, pageHeight / 2 - imageHeight / 2)
            PdfToolsRepository.WatermarkPosition.TOP_LEFT ->
                Pair(margin, pageHeight - margin - imageHeight)
            PdfToolsRepository.WatermarkPosition.TOP_CENTER ->
                Pair(pageWidth / 2 - imageWidth / 2, pageHeight - margin - imageHeight)
            PdfToolsRepository.WatermarkPosition.TOP_RIGHT ->
                Pair(pageWidth - margin - imageWidth, pageHeight - margin - imageHeight)
            PdfToolsRepository.WatermarkPosition.BOTTOM_LEFT ->
                Pair(margin, margin)
            PdfToolsRepository.WatermarkPosition.BOTTOM_CENTER ->
                Pair(pageWidth / 2 - imageWidth / 2, margin)
            PdfToolsRepository.WatermarkPosition.BOTTOM_RIGHT ->
                Pair(pageWidth - margin - imageWidth, margin)
            PdfToolsRepository.WatermarkPosition.TILED ->
                Pair(0f, 0f) // Handled separately
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
