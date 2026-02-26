package com.rejowan.pdfreaderpro.presentation.components.pdf.resource

import android.content.Context
import android.net.Uri
import android.util.Log
import android.webkit.WebResourceResponse
import androidx.webkit.WebViewAssetLoader

internal class PdfViewerResourceLoader(
    context: Context,
    onError: (Exception) -> Unit,
) : ResourceLoader {

    companion object {
        const val PATH = "/pdfviewer/"
    }

    private val assetLoader = WebViewAssetLoader.Builder()
        .setDomain(ResourceLoader.RESOURCE_DOMAIN)
        .addPathHandler(
            PATH,
            AssetsPathHandler(context, onError)
        )
        .build()

    override fun canHandle(uri: Uri) =
        uri.host == ResourceLoader.RESOURCE_DOMAIN && uri.path?.startsWith(PATH) == true

    override fun shouldInterceptRequest(uri: Uri): WebResourceResponse? {
        if (Uri.decode(uri.path) == "${PATH}com/rejowan/mozilla/pdfjs/sample.pdf") {
            Log.i("PdfViewer", "It seems like no source is provided!")
            return null
        }
        return assetLoader.shouldInterceptRequest(uri)
    }

    // Should not create sharable uri with PdfViewerResourceLoader
    override fun createSharableUri(context: Context, authority: String, source: String): Uri? = null

}

