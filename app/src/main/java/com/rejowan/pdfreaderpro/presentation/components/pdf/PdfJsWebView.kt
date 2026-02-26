@file:SuppressLint("UseKtx")

package com.bhuvaneshw.pdf

import android.annotation.SuppressLint
import android.graphics.Color
import android.net.Uri
import android.webkit.ConsoleMessage
import android.webkit.RenderProcessGoneDetail
import android.webkit.URLUtil
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import com.bhuvaneshw.pdf.js.callDirectly
import com.bhuvaneshw.pdf.js.invoke

@SuppressLint("SetJavaScriptEnabled")
@Suppress("FunctionName")
internal fun PdfViewer.PdfJsWebView() = WebView(context).apply {
    setBackgroundColor(Color.TRANSPARENT)

    if (isInEditMode) return@apply

    settings.run {
        javaScriptEnabled = true

        allowFileAccess = false
        allowContentAccess = false
        @Suppress("DEPRECATION")
        allowFileAccessFromFileURLs = false
        @Suppress("DEPRECATION")
        allowUniversalAccessFromFileURLs = false
    }

    webChromeClient = object : WebChromeClient() {
        override fun onShowFileChooser(
            webView: WebView?,
            filePathCallback: ValueCallback<Array<out Uri?>?>?,
            fileChooserParams: FileChooserParams?
        ): Boolean {
            return listeners.any { it.onShowFileChooser(filePathCallback, fileChooserParams) }
        }

        override fun onConsoleMessage(consoleMessage: ConsoleMessage?) =
            PdfViewer.preventWebViewConsoleLog
    }

    webViewClient = object : WebViewClient() {
//            override fun shouldOverrideUrlLoading(
//                view: WebView?,
//                request: WebResourceRequest?
//            ): Boolean {
//                val url = request?.url.toString()
//
//                if (url.startsWith("file:///android_asset/"))
//                    return super.shouldOverrideUrlLoading(view, request)
//
//                if (URLUtil.isValidUrl(url))
//                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
//
//                return true
//            }

        override fun onReceivedError(
            view: WebView?,
            request: WebResourceRequest?,
            error: WebResourceError?
        ) {
            listeners.forEach {
                it.onReceivedError(
                    WebViewError(
                        errorCode = error?.errorCode,
                        description = error?.description?.toString(),
                        failingUrl = request?.url?.toString(),
                        isForMainFrame = request?.isForMainFrame
                    )
                )
            }
        }

        @Suppress("OVERRIDE_DEPRECATION")
        override fun onReceivedError(
            view: WebView?,
            errorCode: Int,
            description: String?,
            failingUrl: String?
        ) {
            listeners.forEach {
                it.onReceivedError(
                    WebViewError(
                        errorCode = errorCode,
                        description = description,
                        failingUrl = failingUrl,
                    )
                )
            }
        }

        override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)
            if (view == null) return

            if (!isInitialized) {
                view callDirectly "setupHelper" {
                    post {
                        isInitialized = true
                        tempBackgroundColor?.let { setContainerBackgroundColor(it) }
                        onReadyListeners.forEach { it(this@PdfJsWebView) }
                    }
                }
            }
        }

        override fun shouldInterceptRequest(
            view: WebView?,
            request: WebResourceRequest
        ): WebResourceResponse? {
            val uri = request.url

            return resourceLoaders
                .firstOrNull { it.canHandle(uri) }
                ?.shouldInterceptRequest(uri)
        }

        @Suppress("OVERRIDE_DEPRECATION")
        override fun shouldInterceptRequest(
            view: WebView?,
            url: String?
        ): WebResourceResponse? {
            val uri = Uri.parse(url)

            return resourceLoaders
                .firstOrNull { it.canHandle(uri) }
                ?.shouldInterceptRequest(uri)
        }

        override fun onRenderProcessGone(
            view: WebView?,
            detail: RenderProcessGoneDetail?
        ): Boolean {
            var handled = false
            listeners.forEach {
                handled = it.onRenderProcessGone(detail) || handled
            }
            return handled
        }
    }

    setDownloadListener { url, _, contentDisposition, mimetype, _ ->
        val fileName = URLUtil.guessFileName(url, contentDisposition, mimetype)
        webInterface.getBase64StringFromBlobUrl(url, fileName, mimetype)
            ?.let { evaluateJavascript(it, null) }
    }
}
