package com.bhuvaneshw.pdf.resource

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.webkit.WebResourceResponse
import androidx.webkit.WebViewAssetLoader

internal class NetworkResourceLoader(
    onError: (Exception) -> Unit,
) : ResourceLoader {

    companion object {
        const val PATH = "/network/"
    }

    internal var handler: NetworkResourceHandler = DefaultNetworkResourceHandler()
    private val assetLoader = WebViewAssetLoader.Builder()
        .setDomain(ResourceLoader.RESOURCE_DOMAIN)
        .addPathHandler(
            PATH,
            NetworkUriPathHandler(loader = this, onError = onError)
        )
        .build()

    override fun canHandle(uri: Uri) =
        uri.host == ResourceLoader.RESOURCE_DOMAIN && uri.path?.startsWith(PATH) == true

    override fun shouldInterceptRequest(uri: Uri): WebResourceResponse? {
        return assetLoader.shouldInterceptRequest(uri)
    }

    override fun createSharableUri(context: Context, authority: String, source: String): Uri? = null

}

private class NetworkUriPathHandler(
    private val loader: NetworkResourceLoader,
    private val onError: (Exception) -> Unit,
) : WebViewAssetLoader.PathHandler {

    @SuppressLint("UseKtx")
    override fun handle(path: String): WebResourceResponse? {
        return try {
            val url = Uri.decode(path)

            val (mimeType, encoding, inputStream) = loader.handler.open(url)

            WebResourceResponse(mimeType, encoding, inputStream)
        } catch (e: Exception) {
            onError(e)
            null
        }
    }

}
