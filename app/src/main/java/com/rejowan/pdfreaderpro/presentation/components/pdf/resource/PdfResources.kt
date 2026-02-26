package com.bhuvaneshw.pdf.resource

import android.content.Context

/**
 * Provides utility functions to check for the availability of optional PDF features.
 */
object PdfResources {

    /**
     * Checks if the color profile module is available.
     *
     * @param context The application context.
     * @return `true` if the color profile module is available, `false` otherwise.
     */
    fun isColorProfileAvailable(context: Context): Boolean {
        return context.call(
            className = "com.rejowan.pdf.icc.PdfColorProfile",
            methodName = "isPresent"
        )
    }

    /**
     * Checks if the JPEG2000 image decoder module is available.
     *
     * @param context The application context.
     * @return `true` if the JPEG2000 module is available, `false` otherwise.
     */
    fun isJPEG2000Available(context: Context): Boolean {
        return context.call(
            className = "com.rejowan.pdf.jp2.JPEG2000",
            methodName = "isPresent"
        )
    }

    private fun Context.call(className: String, methodName: String): Boolean {
        return try {
            val clazz = Class.forName(className)
            val instance = clazz.getDeclaredConstructor().newInstance()
            val method = clazz.getDeclaredMethod(methodName, Context::class.java)

            method.isAccessible = true
            method.invoke(instance, this) as Boolean
        } catch (_: Exception) {
            false
        }
    }

}
