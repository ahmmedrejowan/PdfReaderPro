package com.rejowan.pdfreaderpro.util

import android.content.Context
import android.content.Intent
import com.rejowan.pdfreaderpro.presentation.ErrorActivity
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.slot
import io.mockk.unmockkObject
import io.mockk.verify
import kotlinx.coroutines.CoroutineExceptionHandler
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Covers what happens after an uncaught exception.
 *
 * This is the last thing that runs before the user would otherwise see the app
 * disappear, so the two branches both matter: normally an error screen comes up,
 * and when even that fails the crash is handed back to the platform rather than
 * swallowed. A handler that quietly exits would lose the report.
 *
 * The default handler is global state, so it is put back afterwards.
 */
class GlobalErrorHandlerTest {

    private lateinit var context: Context
    private var originalHandler: Thread.UncaughtExceptionHandler? = null
    private lateinit var previous: Thread.UncaughtExceptionHandler
    private var terminated = false
    private lateinit var originalTerminate: () -> Unit

    @Before
    fun setUp() {
        originalHandler = Thread.getDefaultUncaughtExceptionHandler()
        previous = mockk(relaxed = true)
        Thread.setDefaultUncaughtExceptionHandler(previous)

        context = mockk(relaxed = true)
        every { context.applicationContext } returns context

        terminated = false
        originalTerminate = GlobalErrorHandler.terminateProcess
        GlobalErrorHandler.terminateProcess = { terminated = true }

        mockkObject(ErrorActivity)
        every { ErrorActivity.createIntent(any(), any(), any()) } returns mockk<Intent>(relaxed = true)
    }

    @After
    fun tearDown() {
        Thread.setDefaultUncaughtExceptionHandler(originalHandler)
        GlobalErrorHandler.terminateProcess = originalTerminate
        GlobalErrorHandler.isCrashScreenProcess = false
        unmockkObject(ErrorActivity)
    }

    private fun crash(throwable: Throwable) {
        GlobalErrorHandler.setup(context)
        Thread.getDefaultUncaughtExceptionHandler()!!
            .uncaughtException(Thread.currentThread(), throwable)
    }

    @Test
    fun `setting up replaces the default handler`() {
        GlobalErrorHandler.setup(context)

        assertNotEquals(previous, Thread.getDefaultUncaughtExceptionHandler())
    }

    @Test
    fun `a crash brings up the error screen instead of disappearing`() {
        crash(IllegalStateException("something went wrong"))

        verify { context.startActivity(any()) }
    }

    @Test
    fun `the message shown is the one the exception carried`() {
        val message = slot<String>()
        every { ErrorActivity.createIntent(any(), capture(message), any()) } returns
            mockk<Intent>(relaxed = true)

        crash(IllegalStateException("the document could not be opened"))

        assertEquals("the document could not be opened", message.captured)
    }

    @Test
    fun `an exception with no message is still named`() {
        // "null" on the error screen tells the user nothing, so the class name
        // stands in when there is no message.
        val message = slot<String>()
        every { ErrorActivity.createIntent(any(), capture(message), any()) } returns
            mockk<Intent>(relaxed = true)

        crash(NullPointerException())

        assertTrue(message.captured.contains("NullPointerException"))
    }

    @Test
    fun `a blank message is treated as no message`() {
        val message = slot<String>()
        every { ErrorActivity.createIntent(any(), capture(message), any()) } returns
            mockk<Intent>(relaxed = true)

        crash(IllegalArgumentException("   "))

        assertTrue(message.captured.contains("IllegalArgumentException"))
    }

    @Test
    fun `the details carry the whole stack, causes included`() {
        // The cause is often the real failure, so a report that stops at the top
        // frames leaves out the part a bug report needs.
        val details = slot<String?>()
        every { ErrorActivity.createIntent(any(), any(), captureNullable(details)) } returns
            mockk<Intent>(relaxed = true)
        val cause = IllegalArgumentException("bad page index")
        val throwable = RuntimeException("deep failure", cause)

        crash(throwable)

        val captured = details.captured
        assertNotNull(captured)
        assertTrue(captured!!.contains("RuntimeException: deep failure"))
        assertTrue(captured.contains("Caused by: java.lang.IllegalArgumentException: bad page index"))
        val frames = captured.lines().count { it.trim().startsWith("at ") }
        assertTrue(frames >= throwable.stackTrace.size)
    }

    @Test
    fun `the report says which app version and thread crashed`() {
        val report = GlobalErrorHandler.buildCrashReport(
            Thread.currentThread(),
            IllegalStateException("boom")
        )

        assertTrue(report.contains("PDF Reader Pro ${com.rejowan.pdfreaderpro.BuildConfig.VERSION_NAME}"))
        assertTrue(report.contains("Thread: ${Thread.currentThread().name}"))
        assertTrue(report.contains("IllegalStateException: boom"))
    }

    @Test
    fun `a very deep stack is cut short so it still reaches the error screen`() {
        // The report travels in an intent, which has a size limit.
        val deep = RuntimeException("deep")
        deep.stackTrace = Array(20_000) { StackTraceElement("com.example.Deep", "call$it", "Deep.kt", it) }

        val report = GlobalErrorHandler.buildCrashReport(Thread.currentThread(), deep)

        assertTrue(report.length <= 100_000 + 20)
        assertTrue(report.endsWith("(truncated)"))
    }

    @Test
    fun `the crashed process ends once the error screen is on its way`() {
        // The error screen runs in its own process; this one has lost its main
        // thread, and left alive it freezes into an "app not responding" dialog.
        crash(IllegalStateException("main thread failure"))

        verify { context.startActivity(any()) }
        assertTrue(terminated)
    }

    @Test
    fun `a crash on a background thread also brings up the error screen`() {
        // Work done off the main thread (loading, rendering, saving) fails there,
        // not on the main thread, and still has to be caught.
        GlobalErrorHandler.setup(context)
        val details = slot<String?>()
        every { ErrorActivity.createIntent(any(), any(), captureNullable(details)) } returns
            mockk<Intent>(relaxed = true)

        val worker = Thread({ throw IllegalStateException("render failed") }, "pdf-render")
        worker.start()
        worker.join()

        verify { context.startActivity(any()) }
        assertTrue(details.captured!!.contains("Thread: pdf-render"))
        assertTrue(terminated)
    }

    @Test
    fun `a crash on the error screen itself goes to the platform instead of looping`() {
        GlobalErrorHandler.isCrashScreenProcess = true
        val throwable = IllegalStateException("error screen failure")

        crash(throwable)

        verify(exactly = 0) { context.startActivity(any()) }
        verify { previous.uncaughtException(any(), throwable) }
    }

    @Test
    fun `a crash the error screen cannot handle is handed back to the platform`() {
        // Otherwise the process would go down with nothing reported anywhere.
        every { context.startActivity(any()) } throws IllegalStateException("no activity")
        val throwable = IllegalStateException("original failure")

        crash(throwable)

        verify { previous.uncaughtException(any(), throwable) }
    }

    @Test
    fun `the coroutine handler swallows failures rather than taking the app down`() {
        val handler = coroutineExceptionHandler

        handler.handleException(handler, RuntimeException("background work failed"))
    }

    @Test
    fun `the coroutine handler is installed as a coroutine exception handler`() {
        assertNotNull(coroutineExceptionHandler[CoroutineExceptionHandler])
    }
}
