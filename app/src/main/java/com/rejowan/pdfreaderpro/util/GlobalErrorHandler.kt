package com.rejowan.pdfreaderpro.util

import android.content.Context
import kotlinx.coroutines.CoroutineExceptionHandler
import timber.log.Timber

/**
 * Global error handler for uncaught exceptions.
 * Logs crashes and allows for crash reporting integration.
 */
object GlobalErrorHandler {

    fun setup(context: Context) {
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()

        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Timber.e(throwable, "Uncaught exception in thread: ${thread.name}")

            // Log crash details for debugging
            logCrashDetails(throwable)

            // Call default handler to show crash dialog / restart app
            defaultHandler?.uncaughtException(thread, throwable)
        }

        Timber.d("Global error handler initialized")
    }

    private fun logCrashDetails(throwable: Throwable) {
        Timber.e("=== CRASH REPORT ===")
        Timber.e("Exception: ${throwable.javaClass.simpleName}")
        Timber.e("Message: ${throwable.message}")
        Timber.e("Stack trace:")
        throwable.stackTrace.take(10).forEach { element ->
            Timber.e("  at $element")
        }
        throwable.cause?.let { cause ->
            Timber.e("Caused by: ${cause.javaClass.simpleName}: ${cause.message}")
        }
        Timber.e("=== END CRASH REPORT ===")
    }
}

/**
 * CoroutineExceptionHandler for structured concurrency.
 * Use this in CoroutineScope to catch and log exceptions.
 */
val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
    Timber.e(throwable, "Coroutine exception")
}
