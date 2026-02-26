package com.bhuvaneshw.pdf.resource

import android.content.Context
import android.net.Uri
import android.webkit.WebResourceResponse
import androidx.webkit.WebViewAssetLoader
import androidx.webkit.WebViewAssetLoader.PathHandler
import com.bhuvaneshw.pdf.PdfException

internal class AssetResourceLoader(
    context: Context,
    onError: (Exception) -> Unit,
) : ResourceLoader {

    companion object {
        const val PATH = "/assets/"
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
        if (uri.host == ResourceLoader.RESOURCE_DOMAIN && uri.path?.startsWith("${PATH}com/rejowan/mozilla") == true) {
            throw IllegalAccessException("Not allowed to load PdfViewer's internal files.")
        }

        return assetLoader.shouldInterceptRequest(uri)
    }

    override fun createSharableUri(context: Context, authority: String, source: String): Uri? = null

}

internal class AssetsPathHandler(
    private val context: Context,
    private val onError: (Exception) -> Unit,
    private val actualAssetLoader: PathHandler = WebViewAssetLoader.AssetsPathHandler(context)
) : PathHandler by actualAssetLoader {

    override fun handle(path: String): WebResourceResponse? {
        val error = assetExists(context, path)
        if (error != null) {
            when ("$error") {
                "java.io.FileNotFoundException: com/rejowan/mozilla/web/wasm/openjpeg.wasm",
                "java.io.FileNotFoundException: com/rejowan/mozilla/web/wasm/openjpeg_nowasm_fallback.js"
                    -> onError(PdfException("JPEG2000 not found. Please include jp2 module!"))

                "java.io.FileNotFoundException: com/rejowan/mozilla/web/wasm/qcms_bg.wasm"
                    -> onError(PdfException("Color profile not found. Please include icc module!"))

                else -> onError(error)
            }
            return null
        }

        return actualAssetLoader.handle(path)
    }
}

private fun assetExists(context: Context, path: String): Exception? {
    return try {
        context.assets.open(path).use {
            null
        }
    } catch (e: Exception) {
        e
    }
}
