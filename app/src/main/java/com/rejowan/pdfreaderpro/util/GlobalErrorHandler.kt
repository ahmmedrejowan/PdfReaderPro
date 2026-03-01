package com.rejowan.pdfreaderpro.util

import android.content.Context
import com.rejowan.pdfreaderpro.presentation.ErrorActivity
import kotlinx.coroutines.CoroutineExceptionHandler
import timber.log.Timber
import kotlin.system.exitProcess

/**
 * Global error handler for uncaught exceptions.
 * Shows a user-friendly error screen instead of crashing.
 */
object GlobalErrorHandler {

    private var applicationContext: Context? = null

    fun setup(context: Context) {
        applicationContext = context.applicationContext

        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Timber.e(throwable, "Uncaught exception in thread: ${thread.name}")

            // Log crash details for debugging
            logCrashDetails(throwable)

            // Show error activity instead of crashing
            try {
                showErrorActivity(throwable)
            } catch (e: Exception) {
                Timber.e(e, "Failed to show error activity")
                // If we can't show error activity, exit gracefully
                exitProcess(1)
            }
        }

        Timber.d("Global error handler initialized")
    }

    private fun showErrorActivity(throwable: Throwable) {
        val context = applicationContext ?: return

        val errorMessage = when {
            throwable.message?.isNotBlank() == true -> throwable.message!!
            else -> "An unexpected error occurred: ${throwable.javaClass.simpleName}"
        }

        val errorDetails = buildString {
            appendLine("Exception: ${throwable.javaClass.simpleName}")
            throwable.stackTrace.take(5).forEach { element ->
                appendLine("  at $element")
            }
        }

        val intent = ErrorActivity.createIntent(context, errorMessage, errorDetails)
        context.startActivity(intent)

        // Kill the current process to prevent further issues
        android.os.Process.killProcess(android.os.Process.myPid())
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
