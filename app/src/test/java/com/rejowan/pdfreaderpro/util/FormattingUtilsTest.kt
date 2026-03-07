package com.rejowan.pdfreaderpro.util

import org.junit.Assert.*
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FormattingUtilsTest {

    // region formattedFileSize Tests
    @Test
    fun `formattedFileSize formats bytes correctly`() {
        assertEquals("0 B", FormattingUtils.formattedFileSize(0))
        assertEquals("1 B", FormattingUtils.formattedFileSize(1))
        assertEquals("512 B", FormattingUtils.formattedFileSize(512))
        assertEquals("1023 B", FormattingUtils.formattedFileSize(1023))
    }

    @Test
    fun `formattedFileSize formats kilobytes correctly`() {
        assertEquals("1.0 KB", FormattingUtils.formattedFileSize(1024))
        assertEquals("1.5 KB", FormattingUtils.formattedFileSize(1536))
        assertEquals("10.0 KB", FormattingUtils.formattedFileSize(10240))
        assertEquals("100.0 KB", FormattingUtils.formattedFileSize(102400))
        assertEquals("1023.0 KB", FormattingUtils.formattedFileSize(1024 * 1023))
    }

    @Test
    fun `formattedFileSize formats megabytes correctly`() {
        assertEquals("1.0 MB", FormattingUtils.formattedFileSize(1024 * 1024))
        assertEquals("1.5 MB", FormattingUtils.formattedFileSize((1.5 * 1024 * 1024).toLong()))
        assertEquals("10.0 MB", FormattingUtils.formattedFileSize(10 * 1024 * 1024))
        assertEquals("100.0 MB", FormattingUtils.formattedFileSize(100L * 1024 * 1024))
        assertEquals("500.0 MB", FormattingUtils.formattedFileSize(500L * 1024 * 1024))
    }

    @Test
    fun `formattedFileSize formats gigabytes correctly`() {
        assertEquals("1.0 GB", FormattingUtils.formattedFileSize(1024L * 1024 * 1024))
        assertEquals("1.5 GB", FormattingUtils.formattedFileSize((1.5 * 1024 * 1024 * 1024).toLong()))
        assertEquals("10.0 GB", FormattingUtils.formattedFileSize(10L * 1024 * 1024 * 1024))
        assertEquals("100.0 GB", FormattingUtils.formattedFileSize(100L * 1024 * 1024 * 1024))
    }

    @Test
    fun `formattedFileSize handles boundary values`() {
        // Just below KB
        assertEquals("1023 B", FormattingUtils.formattedFileSize(1023))
        // Exactly KB
        assertEquals("1.0 KB", FormattingUtils.formattedFileSize(1024))
        // Just below MB
        assertEquals("1024.0 KB", FormattingUtils.formattedFileSize(1024 * 1024 - 1))
        // Exactly MB
        assertEquals("1.0 MB", FormattingUtils.formattedFileSize(1024 * 1024))
        // Just below GB
        assertEquals("1024.0 MB", FormattingUtils.formattedFileSize(1024L * 1024 * 1024 - 1024))
        // Exactly GB
        assertEquals("1.0 GB", FormattingUtils.formattedFileSize(1024L * 1024 * 1024))
    }

    @Test
    fun `formattedFileSize handles large values`() {
        // 1 TB
        val oneTB = 1024L * 1024 * 1024 * 1024
        assertEquals("1024.0 GB", FormattingUtils.formattedFileSize(oneTB))
    }

    @Test
    fun `formattedFileSize uses US locale for decimal point`() {
        val result = FormattingUtils.formattedFileSize(1536)
        assertTrue(result.contains("."))
        assertFalse(result.contains(","))
    }
    // endregion

    // region truncateName Tests
    @Test
    fun `truncateName returns original name when shorter than max`() {
        val name = "short.pdf"
        assertEquals(name, FormattingUtils.truncateName(name))
    }

    @Test
    fun `truncateName returns original name when equals max`() {
        val name = "a".repeat(32)
        assertEquals(name, FormattingUtils.truncateName(name))
    }

    @Test
    fun `truncateName truncates name longer than max`() {
        val name = "a".repeat(50)
        val result = FormattingUtils.truncateName(name)

        assertTrue(result.length < name.length)
        assertTrue(result.contains("..."))
        // First 18 + "..." + last 10 = 31 characters
        assertEquals(31, result.length)
    }

    @Test
    fun `truncateName preserves first 18 characters`() {
        val name = "FirstEighteenChars_MiddlePart_LastTenChars.pdf"
        val result = FormattingUtils.truncateName(name)

        assertTrue(result.startsWith("FirstEighteenChars"))
    }

    @Test
    fun `truncateName preserves last 10 characters`() {
        val name = "FirstPart_MiddlePart_EndingPDF.pdf"
        val result = FormattingUtils.truncateName(name)

        assertTrue(result.endsWith("ngPDF.pdf"))
    }

    @Test
    fun `truncateName with custom maxLength`() {
        val name = "a".repeat(50)

        // With default maxLength (32)
        val result1 = FormattingUtils.truncateName(name)
        assertTrue(result1.contains("..."))

        // With larger maxLength
        val result2 = FormattingUtils.truncateName(name, 60)
        assertEquals(name, result2)
    }

    @Test
    fun `truncateName handles special characters`() {
        val name = "文件名_测试文档_很长的名字_需要截断.pdf"
        val result = FormattingUtils.truncateName(name, 20)

        assertTrue(result.contains("..."))
    }
    // endregion

    // region formattedDate Tests
    @Test
    fun `formattedDate formats timestamp correctly`() {
        // Create a known date: January 15, 2024
        val calendar = java.util.Calendar.getInstance().apply {
            set(2024, 0, 15, 0, 0, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }
        val timestamp = calendar.timeInMillis

        val result = FormattingUtils.formattedDate(timestamp)

        assertEquals("15/01/2024", result)
    }

    @Test
    fun `formattedDate handles different dates`() {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.US)

        // Test various dates
        val dates = listOf(
            "01/01/2020",
            "31/12/2023",
            "29/02/2024", // Leap year
            "15/06/2025"
        )

        for (dateStr in dates) {
            val date = sdf.parse(dateStr)!!
            val result = FormattingUtils.formattedDate(date.time)
            assertEquals(dateStr, result)
        }
    }

    @Test
    fun `formattedDate handles epoch timestamp`() {
        val result = FormattingUtils.formattedDate(0)
        // January 1, 1970 00:00:00 UTC
        assertNotNull(result)
        assertTrue(result.contains("/"))
    }

    @Test
    fun `formattedDate handles current timestamp`() {
        val now = System.currentTimeMillis()
        val result = FormattingUtils.formattedDate(now)

        // Should be today's date
        val expected = SimpleDateFormat("dd/MM/yyyy", Locale.US).format(Date(now))
        assertEquals(expected, result)
    }
    // endregion

    // region formattedDateTime Tests
    @Test
    fun `formattedDateTime includes time component`() {
        val calendar = java.util.Calendar.getInstance().apply {
            set(2024, 5, 15, 14, 30, 0) // June 15, 2024, 14:30
            set(java.util.Calendar.MILLISECOND, 0)
        }
        val timestamp = calendar.timeInMillis

        val result = FormattingUtils.formattedDateTime(timestamp)

        assertEquals("15/06/2024 14:30", result)
    }

    @Test
    fun `formattedDateTime handles midnight`() {
        val calendar = java.util.Calendar.getInstance().apply {
            set(2024, 0, 1, 0, 0, 0) // January 1, 2024, 00:00
            set(java.util.Calendar.MILLISECOND, 0)
        }
        val timestamp = calendar.timeInMillis

        val result = FormattingUtils.formattedDateTime(timestamp)

        assertEquals("01/01/2024 00:00", result)
    }

    @Test
    fun `formattedDateTime handles 23-59`() {
        val calendar = java.util.Calendar.getInstance().apply {
            set(2024, 11, 31, 23, 59, 0) // December 31, 2024, 23:59
            set(java.util.Calendar.MILLISECOND, 0)
        }
        val timestamp = calendar.timeInMillis

        val result = FormattingUtils.formattedDateTime(timestamp)

        assertEquals("31/12/2024 23:59", result)
    }

    @Test
    fun `formattedDateTime handles current timestamp`() {
        val now = System.currentTimeMillis()
        val result = FormattingUtils.formattedDateTime(now)

        // Should contain current date and time
        val expected = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.US).format(Date(now))
        assertEquals(expected, result)
    }
    // endregion

    // region extractParentFolders Tests
    @Test
    fun `extractParentFolders extracts folder path`() {
        val fullPath = "/storage/emulated/0/Documents/PDFs/work/file.pdf"
        val result = FormattingUtils.extractParentFolders(fullPath)

        assertEquals("Documents > PDFs > work", result)
    }

    @Test
    fun `extractParentFolders returns Storage for short paths`() {
        // Less than 4 parts after splitting
        assertEquals("Storage", FormattingUtils.extractParentFolders("/storage/test.pdf"))
        assertEquals("Storage", FormattingUtils.extractParentFolders("/a/b/c.pdf"))
    }

    @Test
    fun `extractParentFolders handles root path`() {
        assertEquals("Storage", FormattingUtils.extractParentFolders("/file.pdf"))
    }

    @Test
    fun `extractParentFolders removes leading slash`() {
        val fullPath = "/storage/emulated/0/Documents/file.pdf"
        val result = FormattingUtils.extractParentFolders(fullPath)

        assertFalse(result.startsWith("/"))
    }

    @Test
    fun `extractParentFolders handles typical Android paths`() {
        // Internal storage path: /storage/emulated/0/Download/Books/file.pdf
        // After removing prefix and splitting: ["storage", "emulated", "0", "Download", "Books", "file.pdf"]
        // Drop first 3, drop last 1: ["Download", "Books"] -> "Download > Books"
        val internal = "/storage/emulated/0/Download/Books/file.pdf"
        assertEquals("Download > Books", FormattingUtils.extractParentFolders(internal))

        // SD card path: /storage/XXXX-XXXX/Documents/Work/file.pdf
        // After removing prefix and splitting: ["storage", "XXXX-XXXX", "Documents", "Work", "file.pdf"]
        // Drop first 3, drop last 1: ["Work"] -> "Work"
        // Note: SD card paths have 2 prefix segments, not 3, so one folder name gets dropped
        val sdCard = "/storage/XXXX-XXXX/Documents/Work/file.pdf"
        assertEquals("Work", FormattingUtils.extractParentFolders(sdCard))
    }

    @Test
    fun `extractParentFolders handles deep nesting`() {
        val deepPath = "/storage/emulated/0/a/b/c/d/e/f/file.pdf"
        val result = FormattingUtils.extractParentFolders(deepPath)

        assertEquals("a > b > c > d > e > f", result)
    }

    @Test
    fun `extractParentFolders handles special characters in folder names`() {
        val specialPath = "/storage/emulated/0/测试/文档/file.pdf"
        val result = FormattingUtils.extractParentFolders(specialPath)

        assertEquals("测试 > 文档", result)
    }

    @Test
    fun `extractParentFolders handles spaces in folder names`() {
        val spacePath = "/storage/emulated/0/My Documents/Work Files/file.pdf"
        val result = FormattingUtils.extractParentFolders(spacePath)

        assertEquals("My Documents > Work Files", result)
    }
    // endregion

    // region Edge Cases
    @Test
    fun `formattedFileSize handles negative values`() {
        // Negative file sizes shouldn't happen but function should handle gracefully
        val result = FormattingUtils.formattedFileSize(-1)
        // Could be "-1 B" or handled differently
        assertNotNull(result)
    }

    @Test
    fun `truncateName handles empty string`() {
        val result = FormattingUtils.truncateName("")
        assertEquals("", result)
    }

    @Test
    fun `truncateName handles very short string`() {
        val result = FormattingUtils.truncateName("a")
        assertEquals("a", result)
    }

    @Test
    fun `extractParentFolders handles empty path`() {
        val result = FormattingUtils.extractParentFolders("")
        assertEquals("Storage", result)
    }

    @Test
    fun `extractParentFolders handles path without leading slash`() {
        val result = FormattingUtils.extractParentFolders("storage/emulated/0/Documents/file.pdf")
        assertEquals("Documents", result)
    }
    // endregion
}
