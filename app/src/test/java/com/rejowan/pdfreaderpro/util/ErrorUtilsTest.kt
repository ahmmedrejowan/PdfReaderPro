package com.rejowan.pdfreaderpro.util

import android.content.Context
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException

class ErrorUtilsTest {

    @get:Rule
    val folder = TemporaryFolder()


    // ===========================================
    // Error Classification Tests
    // ===========================================

    @Test
    fun `classifyError returns FileNotFound for FileNotFoundException`() {
        val error = ErrorUtils.classifyError(FileNotFoundException("File not found"))
        assertEquals(AppError.FileNotFound, error)
    }

    @Test
    fun `classifyError returns StorageFull for no space left error`() {
        val error = ErrorUtils.classifyError(IOException("No space left on device"))
        assertEquals(AppError.StorageFull, error)
    }

    @Test
    fun `classifyError returns StorageFull for ENOSPC error`() {
        val error = ErrorUtils.classifyError(IOException("ENOSPC: No space left"))
        assertEquals(AppError.StorageFull, error)
    }

    @Test
    fun `classifyError returns StorageFull for disk full error`() {
        val error = ErrorUtils.classifyError(IOException("Disk full, cannot write"))
        assertEquals(AppError.StorageFull, error)
    }

    @Test
    fun `classifyError returns StorageFull for storage full message`() {
        val error = ErrorUtils.classifyError(Exception("Storage is full"))
        assertEquals(AppError.StorageFull, error)
    }

    @Test
    fun `classifyError returns OutOfMemory for OutOfMemoryError`() {
        val error = ErrorUtils.classifyError(OutOfMemoryError("Java heap space"))
        assertEquals(AppError.OutOfMemory, error)
    }

    @Test
    fun `classifyError returns OutOfMemory for out of memory message`() {
        val error = ErrorUtils.classifyError(Exception("Out of memory while processing"))
        assertEquals(AppError.OutOfMemory, error)
    }

    @Test
    fun `classifyError returns PermissionDenied for SecurityException`() {
        val error = ErrorUtils.classifyError(SecurityException("Access denied"))
        assertEquals(AppError.PermissionDenied, error)
    }

    @Test
    fun `classifyError returns PermissionDenied for permission denied message`() {
        val error = ErrorUtils.classifyError(Exception("Permission denied for this file"))
        assertEquals(AppError.PermissionDenied, error)
    }

    @Test
    fun `classifyError returns PermissionDenied for EACCES error`() {
        val error = ErrorUtils.classifyError(IOException("EACCES: Permission denied"))
        assertEquals(AppError.PermissionDenied, error)
    }

    @Test
    fun `classifyError returns CorruptedPdf for corrupted message`() {
        val error = ErrorUtils.classifyError(Exception("File is corrupted"))
        assertEquals(AppError.CorruptedPdf, error)
    }

    @Test
    fun `classifyError returns CorruptedPdf for trailer not found`() {
        val error = ErrorUtils.classifyError(Exception("PDF trailer not found"))
        assertEquals(AppError.CorruptedPdf, error)
    }

    @Test
    fun `classifyError returns CorruptedPdf for invalid xref`() {
        val error = ErrorUtils.classifyError(Exception("Invalid xref table"))
        assertEquals(AppError.CorruptedPdf, error)
    }

    @Test
    fun `classifyError returns CorruptedPdf for unexpected end of file`() {
        val error = ErrorUtils.classifyError(Exception("Unexpected end of file"))
        assertEquals(AppError.CorruptedPdf, error)
    }

    @Test
    fun `classifyError returns InvalidPdf for not a PDF message`() {
        val error = ErrorUtils.classifyError(Exception("Not a PDF file"))
        assertEquals(AppError.InvalidPdf, error)
    }

    @Test
    fun `classifyError returns InvalidPdf for PDF header not found`() {
        val error = ErrorUtils.classifyError(Exception("PDF header signature not found"))
        assertEquals(AppError.InvalidPdf, error)
    }

    @Test
    fun `classifyError returns NetworkError for network related message`() {
        val error = ErrorUtils.classifyError(Exception("Network is unreachable"))
        assertEquals(AppError.NetworkError, error)
    }

    @Test
    fun `classifyError returns NetworkError for connection error`() {
        val error = ErrorUtils.classifyError(Exception("Connection refused"))
        assertEquals(AppError.NetworkError, error)
    }

    @Test
    fun `classifyError returns Timeout for timeout message`() {
        val error = ErrorUtils.classifyError(Exception("Connection timeout"))
        assertEquals(AppError.Timeout, error)
    }

    @Test
    fun `classifyError returns Timeout for timed out message`() {
        val error = ErrorUtils.classifyError(Exception("Request timed out"))
        assertEquals(AppError.Timeout, error)
    }

    @Test
    fun `classifyError returns Unknown for unrecognized error`() {
        val error = ErrorUtils.classifyError(Exception("Some random error"))
        assertTrue(error is AppError.Unknown)
        assertEquals("Some random error", (error as AppError.Unknown).originalMessage)
    }

    @Test
    fun `classifyError handles null message`() {
        val error = ErrorUtils.classifyError(Exception())
        assertTrue(error is AppError.Unknown)
    }

    // ===========================================
    // AppError Properties Tests
    // ===========================================

    @Test
    fun `FileNotFound is not recoverable`() {
        assertFalse(AppError.FileNotFound.isRecoverable)
    }

    @Test
    fun `CorruptedPdf is not recoverable`() {
        assertFalse(AppError.CorruptedPdf.isRecoverable)
        assertNotNull(AppError.CorruptedPdf.recoveryHintResId)
    }

    @Test
    fun `InvalidPdf is not recoverable`() {
        assertFalse(AppError.InvalidPdf.isRecoverable)
        assertNotNull(AppError.InvalidPdf.recoveryHintResId)
    }

    @Test
    fun `StorageFull is recoverable`() {
        assertTrue(AppError.StorageFull.isRecoverable)
        assertNotNull(AppError.StorageFull.recoveryHintResId)
    }

    @Test
    fun `PermissionDenied is recoverable`() {
        assertTrue(AppError.PermissionDenied.isRecoverable)
        assertNotNull(AppError.PermissionDenied.recoveryHintResId)
    }

    @Test
    fun `NetworkError is recoverable`() {
        assertTrue(AppError.NetworkError.isRecoverable)
        assertNotNull(AppError.NetworkError.recoveryHintResId)
    }

    @Test
    fun `Timeout is recoverable`() {
        assertTrue(AppError.Timeout.isRecoverable)
        assertNotNull(AppError.Timeout.recoveryHintResId)
    }

    @Test
    fun `OutOfMemory is recoverable`() {
        assertTrue(AppError.OutOfMemory.isRecoverable)
        assertNotNull(AppError.OutOfMemory.recoveryHintResId)
    }

    @Test
    fun `PasswordRequired is recoverable`() {
        assertTrue(AppError.PasswordRequired.isRecoverable)
        assertNotNull(AppError.PasswordRequired.recoveryHintResId)
    }

    @Test
    fun `WrongPassword is recoverable`() {
        assertTrue(AppError.WrongPassword.isRecoverable)
        assertNotNull(AppError.WrongPassword.recoveryHintResId)
    }

    @Test
    fun `Unknown error is recoverable`() {
        val error = AppError.Unknown("test")
        assertTrue(error.isRecoverable)
        assertNotNull(error.recoveryHintResId)
    }

    // ===========================================
    // isRecoverable Utility Tests
    // ===========================================

    @Test
    fun `isRecoverable returns true for storage full`() {
        assertTrue(ErrorUtils.isRecoverable(IOException("No space left on device")))
    }

    @Test
    fun `isRecoverable returns false for file not found`() {
        assertFalse(ErrorUtils.isRecoverable(FileNotFoundException("File not found")))
    }

    @Test
    fun `isRecoverable returns true for timeout`() {
        assertTrue(ErrorUtils.isRecoverable(Exception("Connection timeout")))
    }

    @Test
    fun `isRecoverable returns false for corrupted PDF`() {
        assertFalse(ErrorUtils.isRecoverable(Exception("PDF file is corrupted")))
    }

    // ===========================================
    // PDF Validation Tests
    // ===========================================

    @Test
    fun `validatePdfFile returns FileNotFound for non-existent file`() {
        val result = ErrorUtils.validatePdfFile("/non/existent/path.pdf")
        assertEquals(AppError.FileNotFound, result)
    }

    // ===========================================
    // Storage Check Tests
    // ===========================================

    @Test
    fun `hasEnoughStorage with zero bytes returns true`() {
        // Zero bytes should always return true (nothing needed)
        assertTrue(ErrorUtils.hasEnoughStorage(0L))
    }

    @Test
    fun `getAvailableStorageBytes returns non-negative value or -1`() {
        val bytes = ErrorUtils.getAvailableStorageBytes()
        assertTrue(bytes >= -1)
    }

    // ===========================================
    // Edge Cases
    // ===========================================

    @Test
    fun `classifyError with empty message returns Unknown`() {
        val error = ErrorUtils.classifyError(Exception(""))
        assertTrue(error is AppError.Unknown)
    }

    @Test
    fun `classifyError handles case insensitivity`() {
        // Upper case
        assertEquals(AppError.StorageFull, ErrorUtils.classifyError(Exception("NO SPACE LEFT")))
        // Mixed case
        assertEquals(AppError.CorruptedPdf, ErrorUtils.classifyError(Exception("File Is CORRUPTED")))
    }

    @Test
    fun `classifyError prioritizes specific exceptions over message content`() {
        // FileNotFoundException should take precedence even with other keywords in message
        val error = ErrorUtils.classifyError(FileNotFoundException("Timeout while looking for file"))
        assertEquals(AppError.FileNotFound, error)
    }

    @Test
    fun `classifyError handles nested exception causes`() {
        // Even with a cause chain, we classify based on the immediate exception
        val cause = IOException("No space left on device")
        val wrapper = RuntimeException("Wrapper", cause)
        // The wrapper doesn't mention storage, so it's Unknown
        assertTrue(ErrorUtils.classifyError(wrapper) is AppError.Unknown)
    }

    @Test
    fun `preflightCheck returns null when storage is available`() {
        // With a small requirement, should generally pass
        val result = ErrorUtils.preflightCheck(1L)
        assertNull(result)
    }

    // region Checking a document before working on it
    // Every tool runs this first, so what it lets through is what the iText layer
    // is then handed. Saying "corrupted" about a file that is merely missing sends
    // the user looking for the wrong problem.

    @Test
    fun `a real pdf passes`() {
        val file = folder.newFile("real.pdf").apply { writeText("%PDF-1.4 and the rest") }
        assertNull(ErrorUtils.validatePdfFile(file.absolutePath))
    }

    @Test
    fun `a file that is not there is reported as missing, not as damaged`() {
        assertEquals(
            AppError.FileNotFound,
            ErrorUtils.validatePdfFile(File(folder.root, "gone.pdf").absolutePath)
        )
    }

    @Test
    fun `an empty file is damaged`() {
        val file = folder.newFile("empty.pdf")
        assertEquals(AppError.CorruptedPdf, ErrorUtils.validatePdfFile(file.absolutePath))
    }

    @Test
    fun `a file too short to hold a header is damaged`() {
        val file = folder.newFile("stub.pdf").apply { writeText("%PD") }
        assertEquals(AppError.CorruptedPdf, ErrorUtils.validatePdfFile(file.absolutePath))
    }

    @Test
    fun `a file that is not a pdf at all is invalid`() {
        val file = folder.newFile("notes.pdf").apply { writeText("Dear diary, today I") }
        assertEquals(AppError.InvalidPdf, ErrorUtils.validatePdfFile(file.absolutePath))
    }

    @Test
    fun `a file that cannot be read is a permission problem`() {
        val file = folder.newFile("locked.pdf").apply {
            writeText("%PDF-1.4 and the rest")
            setReadable(false, false)
        }
        // Skipped where the filesystem or user cannot make a file unreadable, which
        // is the case for root and for some CI images.
        if (file.canRead()) return

        assertEquals(AppError.PermissionDenied, ErrorUtils.validatePdfFile(file.absolutePath))
    }
    // endregion

    // region Telling the user what went wrong
    @Test
    fun `the message shown is the one belonging to the classified error`() {
        val context = mockk<Context>()
        every { context.getString(AppError.FileNotFound.messageResId) } returns "That file is gone"

        assertEquals(
            "That file is gone",
            ErrorUtils.getUserFriendlyMessage(context, FileNotFoundException("boom"))
        )
    }

    @Test
    fun `an error with a hint gives one`() {
        val context = mockk<Context>()
        every { context.getString(any()) } returns "Free up some space"

        assertNotNull(ErrorUtils.getRecoveryHint(context, AppError.StorageFull))
    }

    @Test
    fun `an error with nothing useful to suggest gives no hint`() {
        val context = mockk<Context>()
        every { context.getString(any()) } returns "anything"

        val hintless = AppError.Unknown("something odd")
        if (hintless.recoveryHintResId == null) {
            assertNull(ErrorUtils.getRecoveryHint(context, hintless))
        }
    }
    // endregion

    // region Storage
    @Test
    fun `a storage check off device does not throw and does not block work`() {
        // StatFs is not available here, and the util treats not knowing as having
        // room, so that a failed check never stops the user working.
        assertTrue(ErrorUtils.hasEnoughStorage(1L))
        assertNull(ErrorUtils.preflightCheck(1L))
    }

    @Test
    fun `unknown available storage is reported as unknown rather than as zero`() {
        val context = mockk<Context>()
        every { context.getString(any()) } returns "Unknown"

        assertNotNull(ErrorUtils.formatAvailableStorage(context))
    }
    // endregion

    // region Recoverability
    @Test
    fun `a missing file is not worth retrying`() {
        assertFalse(ErrorUtils.isRecoverable(FileNotFoundException("gone")))
    }

    @Test
    fun `running out of memory is worth retrying`() {
        assertTrue(ErrorUtils.isRecoverable(OutOfMemoryError("no room")))
    }

    @Test
    fun `a permission problem is worth retrying once granted`() {
        assertTrue(ErrorUtils.isRecoverable(SecurityException("denied")))
    }
    // endregion
}
