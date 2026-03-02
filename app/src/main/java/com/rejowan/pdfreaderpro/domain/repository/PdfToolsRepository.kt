package com.rejowan.pdfreaderpro.domain.repository

/**
 * Repository interface for PDF manipulation tools.
 * All operations return Result to handle success/failure gracefully.
 */
interface PdfToolsRepository {

    /**
     * Represents page selection for a PDF file.
     * @param path Path to the PDF file
     * @param pages List of pages to include (1-indexed), null means all pages
     */
    data class PdfPageSelection(
        val path: String,
        val pages: List<Int>? = null // null = all pages
    )

    /**
     * Merge multiple PDF files into a single PDF.
     * @param inputPaths List of paths to source PDFs (in order)
     * @param outputPath Path for the merged output PDF
     * @return Result with Unit on success, or exception on failure
     */
    suspend fun mergePdfs(
        inputPaths: List<String>,
        outputPath: String,
        onProgress: (Float) -> Unit = {}
    ): Result<Unit>

    /**
     * Merge multiple PDF files with page selections into a single PDF.
     * @param selections List of PdfPageSelection (path + selected pages)
     * @param outputPath Path for the merged output PDF
     * @return Result with Unit on success, or exception on failure
     */
    suspend fun mergePdfsWithSelection(
        selections: List<PdfPageSelection>,
        outputPath: String,
        onProgress: (Float) -> Unit = {}
    ): Result<Unit>

    /**
     * Split a PDF by page ranges.
     * @param inputPath Path to source PDF
     * @param outputDir Directory for output files
     * @param ranges List of page ranges (e.g., "1-5", "6-10")
     * @return Result with list of created file paths
     */
    suspend fun splitPdf(
        inputPath: String,
        outputDir: String,
        ranges: List<String>,
        onProgress: (Float) -> Unit = {}
    ): Result<List<String>>

    /**
     * Split PDF into individual pages.
     * @param inputPath Path to source PDF
     * @param outputDir Directory for output files
     * @return Result with list of created file paths
     */
    suspend fun splitIntoPages(
        inputPath: String,
        outputDir: String,
        onProgress: (Float) -> Unit = {}
    ): Result<List<String>>

    /**
     * Extract specific pages from a PDF.
     * @param inputPath Path to source PDF
     * @param outputPath Path for output PDF
     * @param pages List of page numbers to extract (1-indexed)
     * @return Result with Unit on success
     */
    suspend fun extractPages(
        inputPath: String,
        outputPath: String,
        pages: List<Int>,
        onProgress: (Float) -> Unit = {}
    ): Result<Unit>

    /**
     * Rotate pages in a PDF.
     * @param inputPath Path to source PDF
     * @param outputPath Path for output PDF
     * @param rotation Rotation angle (90, 180, 270)
     * @param pages Pages to rotate (null = all pages)
     * @return Result with Unit on success
     */
    suspend fun rotatePages(
        inputPath: String,
        outputPath: String,
        rotation: Int,
        pages: List<Int>? = null,
        onProgress: (Float) -> Unit = {}
    ): Result<Unit>

    /**
     * Reorder pages in a PDF.
     * @param inputPath Path to source PDF
     * @param outputPath Path for output PDF
     * @param newOrder New page order (1-indexed page numbers)
     * @return Result with Unit on success
     */
    suspend fun reorderPages(
        inputPath: String,
        outputPath: String,
        newOrder: List<Int>,
        onProgress: (Float) -> Unit = {}
    ): Result<Unit>

    /**
     * Compress a PDF to reduce file size.
     * @param inputPath Path to source PDF
     * @param outputPath Path for output PDF
     * @param quality Compression quality (0.0 = max compression, 1.0 = min compression)
     * @return Result with the new file size in bytes
     */
    suspend fun compressPdf(
        inputPath: String,
        outputPath: String,
        quality: Float = 0.5f,
        onProgress: (Float) -> Unit = {}
    ): Result<Long>

    /**
     * Convert images to PDF.
     * @param imagePaths List of image file paths
     * @param outputPath Path for output PDF
     * @return Result with Unit on success
     */
    suspend fun imagesToPdf(
        imagePaths: List<String>,
        outputPath: String,
        onProgress: (Float) -> Unit = {}
    ): Result<Unit>

    /**
     * Export PDF pages as images.
     * @param inputPath Path to source PDF
     * @param outputDir Directory for output images
     * @param format Image format ("png" or "jpg")
     * @param pages Pages to export (null = all pages)
     * @return Result with list of created image paths
     */
    suspend fun pdfToImages(
        inputPath: String,
        outputDir: String,
        format: String = "png",
        pages: List<Int>? = null,
        onProgress: (Float) -> Unit = {}
    ): Result<List<String>>

    /**
     * Get the number of pages in a PDF.
     */
    suspend fun getPageCount(inputPath: String): Result<Int>

    /**
     * Analyze PDF to estimate compression potential.
     * Returns estimated compression ratio (0.0 to 1.0, where 0.3 means 30% of original size).
     */
    suspend fun analyzeCompressionPotential(inputPath: String): Result<CompressionAnalysis>

    /**
     * Analysis result for compression estimation.
     */
    data class CompressionAnalysis(
        val bytesPerPage: Long,
        val hasImages: Boolean,
        val isAlreadyOptimized: Boolean,
        val estimatedRatioLow: Float,    // Conservative estimate (low compression)
        val estimatedRatioMedium: Float, // Balanced estimate
        val estimatedRatioHigh: Float    // Aggressive estimate
    )

    /**
     * Lock/encrypt a PDF with password protection.
     * @param inputPath Path to source PDF
     * @param outputPath Path for output PDF
     * @param userPassword Password required to open the PDF (can be empty for no open password)
     * @param ownerPassword Password for full access/permissions (required)
     * @param permissions Permissions to grant when opened with user password
     * @return Result with Unit on success
     */
    suspend fun lockPdf(
        inputPath: String,
        outputPath: String,
        userPassword: String,
        ownerPassword: String,
        permissions: PdfPermissions = PdfPermissions(),
        onProgress: (Float) -> Unit = {}
    ): Result<Unit>

    /**
     * PDF permissions when locked.
     */
    data class PdfPermissions(
        val allowPrinting: Boolean = false,
        val allowCopying: Boolean = false,
        val allowModifying: Boolean = false,
        val allowAnnotations: Boolean = false
    )

    /**
     * Unlock/decrypt a password-protected PDF.
     * @param inputPath Path to source PDF
     * @param outputPath Path for output PDF
     * @param password Password to unlock the PDF (user or owner password)
     * @return Result with Unit on success
     */
    suspend fun unlockPdf(
        inputPath: String,
        outputPath: String,
        password: String,
        onProgress: (Float) -> Unit = {}
    ): Result<Unit>

    /**
     * Check if a PDF is password protected.
     * @param inputPath Path to PDF file
     * @return Result with true if password protected, false otherwise
     */
    suspend fun isPasswordProtected(inputPath: String): Result<Boolean>

    /**
     * Remove specific pages from a PDF.
     * @param inputPath Path to source PDF
     * @param outputPath Path for output PDF
     * @param pagesToRemove List of page numbers to remove (1-indexed)
     * @return Result with Unit on success
     */
    suspend fun removePages(
        inputPath: String,
        outputPath: String,
        pagesToRemove: List<Int>,
        onProgress: (Float) -> Unit = {}
    ): Result<Unit>

    /**
     * Watermark configuration for text watermarks.
     */
    data class TextWatermarkConfig(
        val text: String,
        val fontSize: Float = 48f,
        val color: Int = 0x80808080.toInt(), // Gray with alpha
        val opacity: Float = 50f, // 1-100
        val rotation: Float = -45f, // Degrees
        val position: WatermarkPosition = WatermarkPosition.CENTER
    )

    /**
     * Watermark configuration for image watermarks.
     */
    data class ImageWatermarkConfig(
        val imagePath: String,
        val scale: Float = 30f, // 1-100 (percentage of page size)
        val opacity: Float = 50f, // 1-100
        val position: WatermarkPosition = WatermarkPosition.CENTER
    )

    /**
     * Watermark position options.
     */
    enum class WatermarkPosition {
        CENTER,
        TOP_LEFT,
        TOP_CENTER,
        TOP_RIGHT,
        BOTTOM_LEFT,
        BOTTOM_CENTER,
        BOTTOM_RIGHT,
        TILED
    }

    /**
     * Add a text watermark to PDF pages.
     * @param inputPath Path to source PDF
     * @param outputPath Path for output PDF
     * @param config Text watermark configuration
     * @param pages Pages to apply watermark (null = all pages)
     * @return Result with Unit on success
     */
    suspend fun addTextWatermark(
        inputPath: String,
        outputPath: String,
        config: TextWatermarkConfig,
        pages: List<Int>? = null,
        onProgress: (Float) -> Unit = {}
    ): Result<Unit>

    /**
     * Add an image watermark to PDF pages.
     * @param inputPath Path to source PDF
     * @param outputPath Path for output PDF
     * @param config Image watermark configuration
     * @param pages Pages to apply watermark (null = all pages)
     * @return Result with Unit on success
     */
    suspend fun addImageWatermark(
        inputPath: String,
        outputPath: String,
        config: ImageWatermarkConfig,
        pages: List<Int>? = null,
        onProgress: (Float) -> Unit = {}
    ): Result<Unit>
}
