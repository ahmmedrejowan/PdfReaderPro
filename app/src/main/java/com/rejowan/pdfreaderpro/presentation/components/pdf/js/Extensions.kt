package com.bhuvaneshw.pdf.js

import android.util.Base64
import android.webkit.WebView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

internal infix fun WebView.with(jsObject: JsObject): Pair<WebView, JsObject> {
    return this to jsObject
}

internal operator fun String.invoke(
    vararg args: Any,
    callback: ((String?) -> Unit)? = null
): Triple<String, String, ((String?) -> Unit)?> {
    return Triple(this, args.joinToString(separator = ", ") { "$it" }, callback)
}

internal infix fun WebView.callDirectly(function: Triple<String, String, ((String?) -> Unit)?>) {
    evaluateJavascript("${function.first}(${function.second});", function.third)
}

internal infix fun WebView.call(function: Triple<String, String, ((String?) -> Unit)?>) {
    execute(jsCode = "${function.first}(${function.second});", callback = function.third)
}

internal infix fun WebView.set(property: Triple<String, String, ((String?) -> Unit)?>) {
    execute(jsCode = "${property.first} = ${property.second};", callback = property.third)
}

internal infix fun WebView.setDirectly(property: Triple<String, String, ((String?) -> Unit)?>) {
    evaluateJavascript("${property.first} = ${property.second};", property.third)
}

internal infix fun Pair<WebView, JsObject>.call(function: Triple<String, String, ((String?) -> Unit)?>) {
    first.execute(
        jsCode = "${function.first}(${function.second});",
        jsObject = second,
        callback = function.third
    )
}

internal infix fun Pair<WebView, JsObject>.set(property: Triple<String, String, ((String?) -> Unit)?>) {
    first.execute(
        jsCode = "${property.first} = ${property.second};",
        jsObject = second,
        callback = property.third
    )
}

internal fun WebView.execute(
    jsCode: String,
    jsObject: JsObject = PdfViewerApplication,
    callback: ((String?) -> Unit)?,
) {
    evaluateJavascript("${jsObject.objectName}.$jsCode", callback)
}

internal fun String.toJsString() = "`$this`"

internal fun Int.toJsRgba(): String {
    val alpha = (this shr 24) and 0xFF
    val red = (this shr 16) and 0xFF
    val green = (this shr 8) and 0xFF
    val blue = this and 0xFF
    return "rgba($red, $green, $blue, ${alpha / 255f})"
}

internal fun Int.toJsHex(includeAlpha: Boolean = true): String {
    val alpha = (this shr 24) and 0xFF
    val red = (this shr 16) and 0xFF
    val green = (this shr 8) and 0xFF
    val blue = this and 0xFF

    return if (includeAlpha) String.format("#%02X%02X%02X%02X", red, green, blue, alpha)
    else String.format("#%02X%02X%02X", red, green, blue)
}

internal fun JSONObject.getBoolean(name: String, default: Boolean): Boolean {
    return try {
        getBoolean(name)
    } catch (_: JSONException) {
        default
    }
}

internal suspend infix fun WebView.evaluate(jsCode: String): String? {
    return withContext(Dispatchers.Main) {
        suspendCoroutine { continuation ->
            evaluateJavascript(jsCode, continuation::resume)
        }
    }
}

internal fun String.decode(): String = String(Base64.decode(this, Base64.DEFAULT))
internal fun String.encode():String = "btoa(unescape(encodeURIComponent($this)))"
