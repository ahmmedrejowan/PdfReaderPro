package com.bhuvaneshw.pdf

/**
 * Represents the metadata and properties of a PDF document.
 *
 * @property title The title of the document.
 * @property subject The subject of the document.
 * @property author The name of the person who created the document.
 * @property creator The name of the application that created the original document.
 * @property producer The name of the application that converted the original document to PDF.
 * @property creationDate The date and time the document was created, as a string.
 * @property modifiedDate The date and time the document was most recently modified, as a string.
 * @property keywords A list of keywords associated with the document, typically comma-separated.
 * @property language The primary language of the document, represented by a language code (e.g., "en-US").
 * @property pdfFormatVersion The PDF specification version the document conforms to (e.g., "1.7").
 * @property fileSize The total size of the PDF file in bytes.
 * @property isLinearized A flag indicating if the PDF is "linearized" (optimized for viewing).
 * @property encryptFilterName The name of the security handler used for encryption, if any.
 * @property isAcroFormPresent A flag indicating if the document contains an AcroForm (interactive forms).
 * @property isCollectionPresent A flag indicating if the document is a PDF Portfolio (a collection of files).
 * @property isSignaturesPresent A flag indicating if the document contains digital signatures.
 * @property isXFAPresent A flag indicating if the document contains XFA (XML Forms Architecture) forms.
 * @property customJson A JSON string containing any custom or non-standard properties found in the document.
 */
data class PdfDocumentProperties(
    val title: String,
    val subject: String,
    val author: String,
    val creator: String,
    val producer: String,
    val creationDate: String,
    val modifiedDate: String,
    val keywords: String,
    val language: String,
    val pdfFormatVersion: String,
    val fileSize: Long,
    val isLinearized: Boolean,
    val encryptFilterName: String,
    val isAcroFormPresent: Boolean,
    val isCollectionPresent: Boolean,
    val isSignaturesPresent: Boolean,
    val isXFAPresent: Boolean,
    val customJson: String,
)