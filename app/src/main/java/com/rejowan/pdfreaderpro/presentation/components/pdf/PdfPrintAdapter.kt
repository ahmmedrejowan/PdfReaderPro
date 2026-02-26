package com.bhuvaneshw.pdf

import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentInfo
import android.util.Base64
import android.util.Log
import com.bhuvaneshw.pdf.print.PdfPrintBridge
import org.json.JSONArray
import java.io.FileOutputStream

// TODO: Remove in future version
@PdfUnstablePrintApi
@Deprecated(
    message = "Deprecated and will be removed. Use DefaultPdfPrintAdapter instead.",
    replaceWith = ReplaceWith(
        "DefaultPdfPrintAdapter",
        "com.rejowan.pdf.print.DefaultPdfPrintAdapter"
    ),
    level = DeprecationLevel.ERROR
)
class SimplePdfPrintAdapter : PdfPrintBridge() {
    override fun onLayout(
        oldAttributes: PrintAttributes?,
        newAttributes: PrintAttributes?,
        cancellationSignal: android.os.CancellationSignal?,
        callback: LayoutResultCallback?,
        metadata: android.os.Bundle?
    ) {
        evaluateJavascript("PDFViewerApplication.pdfViewer.pagesCount;") { result ->
            if (cancellationSignal?.isCanceled == true) {
                callback?.onLayoutCancelled()
                return@evaluateJavascript
            }

            try {
                val pageCount = result.toIntOrNull() ?: 0

                val builder = PrintDocumentInfo.Builder("PDFDocument.pdf")
                builder.setContentType(PrintDocumentInfo.CONTENT_TYPE_PHOTO)
                    .setPageCount(pageCount)

                callback?.onLayoutFinished(builder.build(), true)
            } catch (e: Exception) {
                Log.e("error", "${e.message}, $e")
                callback?.onLayoutFailed("Failed to retrieve layout information")
            }
        }
    }

    private var destination: ParcelFileDescriptor? = null
    private var cancellationSignal: android.os.CancellationSignal? = null
    private var callback: WriteResultCallback? = null
    override fun onWrite(
        pages: Array<PageRange>,
        destination: ParcelFileDescriptor,
        cancellationSignal: android.os.CancellationSignal,
        callback: WriteResultCallback
    ) {
        try {
            this.destination = destination
            this.cancellationSignal = cancellationSignal
            this.callback = callback
            evaluateJavascript(
                """PDFViewerApplication.pdfDocument
                    |.saveDocument()
                    |.then(data => Array.from(new Uint8Array(data)))
                    |.then(array => JWI.conveyMessage(JSON.stringify(array)));
                    |""".trimMargin(),
                null
            )
        } catch (e: Exception) {
            Log.e("PrintError - PrintAdapter", "${e.message}, $e")
        }
    }

    override fun onMessage(message: String?, type: String?, pageNum: Int?) {
        if (cancellationSignal?.isCanceled == true) {
            callback?.onWriteCancelled()
            return
        }

        try {
            val pdfData = decodeBase64OrByteArray(message!!)

            FileOutputStream(destination?.fileDescriptor).use { outputStream ->
                outputStream.write(pdfData)
                outputStream.flush()
            }

            callback?.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
        } catch (e: Exception) {
            Log.e("PrintError - PrintAdapter", "${e.message}, $e")
            callback?.onWriteFailed(e.message ?: "Failed to write PDF data")
        }
    }

    private fun decodeBase64OrByteArray(result: String): ByteArray {
        return try {
            val base64 = result.trim('"')
            Base64.decode(base64, Base64.DEFAULT)
        } catch (e: Exception) {
            JSONArray(result).let { jsonArray ->
                ByteArray(jsonArray.length()).apply {
                    for (i in 0 until jsonArray.length()) {
                        this[i] = jsonArray.getInt(i).toByte()
                    }
                }
            }
        }
    }

}
