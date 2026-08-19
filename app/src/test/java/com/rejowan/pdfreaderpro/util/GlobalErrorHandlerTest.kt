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

    @Before
    fun setUp() {
        originalHandler = Thread.getDefaultUncaughtExceptionHandler()
        previous = mockk(relaxed = true)
        Thread.setDefaultUncaughtExceptionHandler(previous)

        context = mockk(relaxed = true)
        every { context.applicationContext } returns context

        mockkObject(ErrorActivity)
        every { ErrorActivity.createIntent(any(), any(), any()) } returns mockk<Intent>(relaxed = true)
    }

    @After
    fun tearDown() {
        Thread.setDefaultUncaughtExceptionHandler(originalHandler)
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
    fun `the details carry the top of the stack, not the whole of it`() {
        val details = slot<String?>()
        every { ErrorActivity.createIntent(any(), any(), captureNullable(details)) } returns
            mockk<Intent>(relaxed = true)

        crash(RuntimeException("deep failure"))

        val captured = details.captured
        assertNotNull(captured)
        assertTrue(captured!!.contains("RuntimeException"))
        assertTrue(captured.lines().count { it.trim().startsWith("at ") } <= 5)
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
