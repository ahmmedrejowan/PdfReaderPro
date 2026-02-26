package com.bhuvaneshw.pdf.print

import android.print.PrintDocumentAdapter
import android.webkit.ValueCallback
import android.webkit.WebView

/**
 * An internal class that serves as a bridge between the Android printing framework and a PdfViewer
 * for printing PDF documents. It extends [PrintDocumentAdapter] and provides a base for
 * custom print implementations.
 */
abstract class PdfPrintBridge : PrintDocumentAdapter() {
    internal lateinit var webView: WebView

    internal fun evaluateJavascript(script: String, resultCallback: ValueCallback<String>?) =
        webView.evaluateJavascript(script, resultCallback)

    internal abstract fun onMessage(message: String?, type: String?, pageNum: Int?)
}
