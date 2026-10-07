package com.rejowan.pdfreaderpro.util

import android.content.Context
import android.os.Build
import android.os.Process
import com.rejowan.pdfreaderpro.BuildConfig
import com.rejowan.pdfreaderpro.presentation.ErrorActivity
import kotlinx.coroutines.CoroutineExceptionHandler
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.system.exitProcess

/**
 * Global error handler for uncaught exceptions.
 * Shows a user-friendly error screen instead of crashing.
 */
object GlobalErrorHandler {

    private var applicationContext: Context? = null

    /** Set by the error screen, which runs in a process of its own. */
    @Volatile
    internal var isCrashScreenProcess = false

    /** Whatever handler was installed before us, usually the platform's. */
    private var previousHandler: Thread.UncaughtExceptionHandler? = null

    fun setup(context: Context) {
        applicationContext = context.applicationContext
        previousHandler = Thread.getDefaultUncaughtExceptionHandler()

        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Timber.e(throwable, "Uncaught exception in thread: ${thread.name}")

            // Log crash details for debugging
            logCrashDetails(throwable)

            // Show error activity instead of crashing
            try {
                // A crash on the error screen itself goes to the platform; showing
                // the error screen again would loop.
                check(!isCrashScreenProcess) { "Crash in the error screen" }
                showErrorActivity(thread, throwable)
            } catch (e: Exception) {
                Timber.e(e, "Failed to show error activity")
                // Hand back to the handler we replaced so the platform still gets
                // to report and tear down the crash, rather than exiting silently.
                val handler = previousHandler
                if (handler != null) handler.uncaughtException(thread, throwable) else exitProcess(1)
            }
        }

        Timber.d("Global error handler initialized")
    }

    private fun showErrorActivity(thread: Thread, throwable: Throwable) {
        val context = applicationContext ?: return

        val errorMessage = when {
            throwable.message?.isNotBlank() == true -> requireNotNull(throwable.message)
            else -> "An unexpected error occurred: ${throwable.javaClass.simpleName}"
        }

        val intent = ErrorActivity.createIntent(context, errorMessage, buildCrashReport(thread, throwable))
        context.startActivity(intent)

        // The error screen runs in its own process (":crash" in the manifest). This
        // one has lost its main thread, so leaving it alive only freezes the app
        // into an "app not responding" dialog.
        terminateProcess()
    }

    /** Ends the crashed process. Replaced in tests, where it would end the test run. */
    internal var terminateProcess: () -> Unit = {
        Process.killProcess(Process.myPid())
        exitProcess(10)
    }

    /**
     * What the error screen shows and copies: the app and device it happened on,
     * then the full stack trace with every cause. Kept under [MAX_REPORT_CHARS] so
     * it fits in the intent that carries it to the error screen.
     */
    internal fun buildCrashReport(
        thread: Thread,
        throwable: Throwable,
        timeMillis: Long = System.currentTimeMillis()
    ): String {
        val report = buildString {
            appendLine("App: PDF Reader Pro ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE}, ${BuildConfig.BUILD_TYPE})")
            appendLine("Android: ${Build.VERSION.RELEASE.orEmpty()} (API ${Build.VERSION.SDK_INT})")
            appendLine("Device: ${Build.MANUFACTURER.orEmpty()} ${Build.MODEL.orEmpty()}".trimEnd())
            appendLine("ABIs: ${Build.SUPPORTED_ABIS?.joinToString().orEmpty()}")
            appendLine("Thread: ${thread.name}")
            appendLine("Time: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z", Locale.US).format(Date(timeMillis))}")
            appendLine()
            append(throwable.stackTraceToString())
        }
        return if (report.length <= MAX_REPORT_CHARS) {
            report
        } else {
            report.take(MAX_REPORT_CHARS) + "\n… (truncated)"
        }
    }

    /**
     * Intent extras travel as UTF-16 through a binder buffer of about 1 MB that
     * the whole process shares, so the report stays near 80 KB.
     */
    internal const val MAX_REPORT_CHARS = 40_000

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
