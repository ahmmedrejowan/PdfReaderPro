package com.bhuvaneshw.pdf.resource

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import android.webkit.WebResourceResponse
import androidx.core.content.FileProvider
import androidx.webkit.WebViewAssetLoader
import java.io.File

internal class FileResourceLoader(
    onError: (Exception) -> Unit,
) : ResourceLoader {

    companion object {
        const val PATH = "/file/"
    }

    private val assetLoader = WebViewAssetLoader.Builder()
        .setDomain(ResourceLoader.RESOURCE_DOMAIN)
        .addPathHandler(
            PATH,
            FileUriPathHandler(onError)
        )
        .build()

    override fun canHandle(uri: Uri) =
        uri.host == ResourceLoader.RESOURCE_DOMAIN && uri.path?.startsWith(PATH) == true

    override fun shouldInterceptRequest(uri: Uri): WebResourceResponse? {
        return assetLoader.shouldInterceptRequest(uri)
    }

    override fun createSharableUri(context: Context, authority: String, source: String): Uri? {
        @SuppressLint("UseKtx")
        return FileProvider.getUriForFile(
            context,
            authority,
            File(Uri.parse(source).path?.substringAfter(PATH) ?: return null)
        )
    }

}

private class FileUriPathHandler(
    private val onError: (Exception) -> Unit,
) : WebViewAssetLoader.PathHandler {

    @SuppressLint("UseKtx")
    override fun handle(path: String): WebResourceResponse? {
        return try {
            val filePath = Uri.decode(path)
            val file = File(filePath)
            val mimeType = file.mimeType ?: "application/octet-stream"
            val inputStream = file.inputStream()
            WebResourceResponse(mimeType, "UTF-8", inputStream)
        } catch (e: Exception) {
            onError(e)
            null
        }
    }

}

private val File.mimeType: String?
    get() {
        val fileExtension = MimeTypeMap.getFileExtensionFromUrl(name)
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension?.lowercase())
    }
