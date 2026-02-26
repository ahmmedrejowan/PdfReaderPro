package com.bhuvaneshw.pdf.compose

import com.bhuvaneshw.pdf.PdfViewer

/**
 * A callback to be invoked when the PDF viewer is ready.
 * @see com.bhuvaneshw.pdf.PdfViewer
 */
sealed interface OnReadyCallback {
    /**
     * Called when the PDF viewer is ready.
     *
     * @param pdfViewer The [PdfViewer] instance.
     * @param loadSource A function that loads the PDF source.
     */
    fun onReady(pdfViewer: PdfViewer, loadSource: () -> Unit)
}

/**
 * A default implementation of [OnReadyCallback].
 *
 * This callback loads the PDF source and then invokes an optional callback.
 *
 * @param callback An optional callback to be invoked after the PDF source is loaded.
 * @see com.bhuvaneshw.pdf.PdfViewer
 */
data class DefaultOnReadyCallback(
    private val callback: (PdfViewer.() -> Unit)? = null
) : OnReadyCallback {
    override fun onReady(pdfViewer: PdfViewer, loadSource: () -> Unit) {
        loadSource()
        callback?.invoke(pdfViewer)
    }
}

/**
 * A custom implementation of [OnReadyCallback].
 *
 * This callback allows for custom logic to be executed when the PDF viewer is ready.
 * The implementer is responsible for calling `loadSource()` to load the PDF.
 *
 * @param callback A callback that will be invoked with the [PdfViewer] instance and a `loadSource` lambda.
 * @see com.bhuvaneshw.pdf.PdfViewer
 */
data class CustomOnReadyCallback(
    private val callback: PdfViewer.(loadSource: () -> Unit) -> Unit
) : OnReadyCallback {
    override fun onReady(pdfViewer: PdfViewer, loadSource: () -> Unit) {
        callback(pdfViewer, loadSource)
    }
}
