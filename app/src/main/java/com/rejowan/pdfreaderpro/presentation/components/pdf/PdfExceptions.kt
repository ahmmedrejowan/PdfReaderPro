package com.bhuvaneshw.pdf

/**
 * Base class for all PDF-related exceptions.
 * @param message The detail message for this exception.
 */
open class PdfException(message: String) : RuntimeException(message)

/**
 * Thrown when an operation is aborted.
 * @param message The detail message for this exception.
 */
class AbortException(message: String) : PdfException(message)

/**
 * Thrown when the provided PDF file is invalid or cannot be parsed.
 * @param message The detail message for this exception.
 */
class InvalidPDFException(message: String) : PdfException(message)

/**
 * Thrown when the rendering of a PDF page is cancelled.
 * @param message The detail message for this exception.
 */
class RenderingCancelledException(message: String) : PdfException(message)

/**
 * Thrown to indicate an error in the response while fetching a PDF document.
 * @param message The detail message for this exception.
 */
class ResponseException(message: String) : PdfException(message)

/**
 * Thrown when an operation is attempted on the viewer before it has been initialized.
 */
class PdfViewerNotInitializedException : PdfException("Pdf Viewer not yet initialized!")

@Suppress("NOTHING_TO_INLINE")
internal inline fun exceptionFrom(message: String, type: String): PdfException {
    return when (type) {
        "AbortException" -> AbortException(message)
        "InvalidPDFException" -> InvalidPDFException(message)
        "RenderingCancelledException" -> RenderingCancelledException(message)
        "ResponseException" -> ResponseException(message)
        else -> PdfException(message)
    }
}
