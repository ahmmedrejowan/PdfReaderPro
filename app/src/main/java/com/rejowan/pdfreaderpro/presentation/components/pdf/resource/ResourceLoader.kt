package com.rejowan.pdfreaderpro.presentation.components.pdf.resource

import android.content.Context
import android.net.Uri
import android.webkit.WebResourceResponse

internal interface ResourceLoader {

    fun canHandle(uri: Uri): Boolean
    fun shouldInterceptRequest(uri: Uri): WebResourceResponse?
    fun createSharableUri(context: Context, authority: String, source: String): Uri?

    companion object {
        internal const val RESOURCE_DOMAIN = "pdfviewer-assets.rejowan.app"
    }
}
