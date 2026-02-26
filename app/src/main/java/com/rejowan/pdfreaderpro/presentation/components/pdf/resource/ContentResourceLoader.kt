package com.bhuvaneshw.pdf.resource

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.webkit.WebResourceResponse
import androidx.webkit.WebViewAssetLoader

internal class ContentResourceLoader(
    context: Context,
    onError: (Exception) -> Unit,
) : ResourceLoader {

    companion object {
        const val PATH = "/content/"
    }

    private val assetLoader = WebViewAssetLoader.Builder()
        .setDomain(ResourceLoader.RESOURCE_DOMAIN)
        .addPathHandler(
            PATH,
            ContentUriPathHandler(context, onError)
        )
        .build()

    override fun canHandle(uri: Uri) =
        uri.host == ResourceLoader.RESOURCE_DOMAIN && uri.path?.startsWith(PATH) == true

    override fun shouldInterceptRequest(uri: Uri): WebResourceResponse? {
        return assetLoader.shouldInterceptRequest(uri)
    }

    override fun createSharableUri(context: Context, authority: String, source: String): Uri? {
        @SuppressLint("UseKtx")
        return Uri.parse(Uri.parse(source).path?.substringAfter(PATH))
    }

}

private class ContentUriPathHandler(
    private val context: Context,
    private val onError: (Exception) -> Unit,
) : WebViewAssetLoader.PathHandler {

    @SuppressLint("UseKtx")
    override fun handle(path: String): WebResourceResponse? {
        return try {
            val uri = Uri.parse(path)
            val mimeType = context.contentResolver.getType(uri) ?: "application/octet-stream"
            val inputStream = context.contentResolver.openInputStream(uri)
            WebResourceResponse(mimeType, "UTF-8", inputStream)
        } catch (e: Exception) {
            onError(e)
            null
        }
    }

}
