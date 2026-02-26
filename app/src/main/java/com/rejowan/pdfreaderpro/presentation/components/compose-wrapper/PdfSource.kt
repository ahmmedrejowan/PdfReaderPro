package com.bhuvaneshw.pdf.compose

import android.net.Uri
import androidx.core.net.toUri

/**
 * Represents the source of a PDF document to be loaded into the [PdfViewer].
 *
 * This is a sealed class with different implementations for various sources.
 */
sealed class PdfSource {

    /**
     * Represents a PDF document from the application's assets.
     *
     * @param assetPath The path to the PDF file in the assets folder.
     */
    data class Asset(val assetPath: String) : PdfSource()

    /**
     * Represents a PDF document from a content URI.
     *
     * @param contentUri The content URI of the PDF document.
     */
    data class ContentUri(val contentUri: Uri) : PdfSource() {
        /**
         * @param contentUri The string representation of the content URI.
         */
        constructor(contentUri: String) : this(contentUri.toUri())
    }

    /**
     * Represents a PDF document from a file.
     *
     * @param file The file object representing the PDF document.
     */
    data class File(val file: java.io.File) : PdfSource() {
        /**
         * @param filePath The path to the PDF file.
         */
        constructor(filePath: String) : this(java.io.File(filePath))
    }

    /**
     * Represents a PDF document from a generic source.
     *
     * This can be used to provide any kind of accepted sources.
     *
     * @param source The string representation of the source.
     */
    data class Plain(val source: String) : PdfSource() {
        /**
         * @param source The URI representation of the source.
         */
        constructor(source: Uri) : this(source.toString())
    }

    /**
     * Represents a PDF document from a URL.
     *
     * @param url The URL of the PDF document.
     */
    data class Url(val url: String) : PdfSource() {
        /**
         * @param url The URI of the PDF document.
         */
        constructor(url: Uri) : this(url.toString())
    }
}
