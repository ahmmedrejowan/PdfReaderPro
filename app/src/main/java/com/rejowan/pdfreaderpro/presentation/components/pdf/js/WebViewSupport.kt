package com.bhuvaneshw.pdf.js

import android.content.Context
import androidx.webkit.WebViewCompat

/**
 * Provides information about the WebView support on the device.
 */
object WebViewSupport {

    /**
     * Checks the WebView version and returns the result.
     *
     * @param context The context to use for checking the WebView package.
     * @return A [CheckResult] indicating the status of the WebView.
     */
    fun check(context: Context): CheckResult {
        val version = getWebViewVersion(context)

        // https://github.com/mozilla/pdf.js/wiki/frequently-asked-questions#modern-build
        // https://docs.signageos.io/hc/en-us/articles/4405381554578-Browser-WebKit-and-Chromium-versions-by-each-Platform#h_01HABYXXZMDMS644M0BXH43GYD
        return when {
            version == null -> CheckResult.NO_WEBVIEW_FOUND
            version < 110 -> CheckResult.REQUIRES_UPDATE
            version < 115 -> CheckResult.UPDATE_RECOMMENDED
            else -> CheckResult.NO_ACTION_REQUIRED
        }
    }

    private fun getWebViewVersion(context: Context): Int? {
        return try {
            WebViewCompat.getCurrentWebViewPackage(context)
                ?.versionName
                ?.split(".")[0]
                ?.toIntOrNull()
        } catch (_: Exception) {
            null
        }
    }

    /**
     * The result of the WebView check.
     */
    enum class CheckResult {
        /**
         * The WebView version is sufficient.
         */
        NO_ACTION_REQUIRED,

        /**
         * The WebView version is too old and must be updated.
         */
        REQUIRES_UPDATE,

        /**
         * The WebView version is old and an update is recommended.
         */
        UPDATE_RECOMMENDED,

        /**
         * No WebView package was found on the device.
         */
        NO_WEBVIEW_FOUND
    }
}
