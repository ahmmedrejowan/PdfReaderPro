package com.bhuvaneshw.pdf

/**
 * Represents an error that occurred while loading a web page, analogous to `WebResourceError`.
 *
 * @see android.webkit.WebResourceError
 */
data class WebViewError(
    val errorCode: Int?,
    val description: String?,
    val failingUrl: String?,
    val isForMainFrame: Boolean? = null,
)
