package com.rejowan.pdfreaderpro.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.net.URI
import java.net.URLDecoder

/**
 * Covers the "Report on GitHub" link on the crash screen. GitHub fills the bug
 * form from query parameters named after the form's field ids, so the names
 * here have to match .github/ISSUE_TEMPLATE/bug_report.yml.
 */
class CrashIssueLinkTest {

    private fun link(message: String = "Index 5 out of bounds", report: String = "App: PDF Reader Pro\n\nboom") =
        CrashIssueLink.build(
            errorMessage = message,
            report = report,
            appVersion = "2.4.0 (9)",
            androidVersion = "Android 15 (API 35)",
            device = "Google Pixel 9"
        )

    private fun params(url: String): Map<String, String> =
        URI(url).rawQuery.split("&").associate { pair ->
            val (key, value) = pair.split("=", limit = 2)
            key to URLDecoder.decode(value, "UTF-8")
        }

    @Test
    fun `the link opens a new issue on the project with the bug form`() {
        val url = link()

        assertTrue(url.startsWith("https://github.com/ahmmedrejowan/PdfReaderPro/issues/new?"))
        assertEquals("bug_report.yml", params(url)["template"])
    }

    @Test
    fun `the form fields are filled from the crash`() {
        val fields = params(link())

        assertEquals("[Bug]: Crash: Index 5 out of bounds", fields["title"])
        assertEquals("2.4.0 (9)", fields["version"])
        assertEquals("Android 15 (API 35)", fields["android-version"])
        assertEquals("Google Pixel 9", fields["device"])
        assertTrue(fields.getValue("description").contains("Index 5 out of bounds"))
        assertEquals("App: PDF Reader Pro\n\nboom", fields["logs"])
    }

    @Test
    fun `characters that mean something in a URL survive the trip`() {
        val report = "at a.b(C.kt:1) & more # stuff ?x=1 100% \"quoted\""

        assertEquals(report, params(link(report = report))["logs"])
    }

    @Test
    fun `a long log is cut to fit the link and says where the rest is`() {
        val report = (1..2_000).joinToString("\n") { "\tat com.example.Deep.call$it(Deep.kt:$it)" }

        val url = link(report = report)
        val logs = params(url).getValue("logs")

        assertTrue(url.length <= CrashIssueLink.MAX_URL_LENGTH)
        assertTrue(logs.endsWith(CrashIssueLink.TRUNCATED_NOTE))
        // The top of the trace is the part that matters, so that is what is kept.
        assertTrue(logs.startsWith("\tat com.example.Deep.call1(Deep.kt:1)"))
    }

    @Test
    fun `a very long message does not push the link over the limit`() {
        val url = link(message = "x".repeat(20_000))

        assertTrue(url.length <= CrashIssueLink.MAX_URL_LENGTH)
        assertEquals("[Bug]: Crash: " + "x".repeat(80), params(url)["title"])
    }
}
