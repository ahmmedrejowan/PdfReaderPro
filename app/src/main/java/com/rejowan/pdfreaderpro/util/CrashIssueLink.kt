package com.rejowan.pdfreaderpro.util

import java.net.URLEncoder

/**
 * Builds a link that opens a new GitHub issue from the crash screen, with the
 * bug report form already filled in. GitHub fills issue form fields from query
 * parameters named after the field ids in .github/ISSUE_TEMPLATE/bug_report.yml.
 *
 * Browsers and GitHub reject very long URLs, so the crash log is cut to fit and
 * the reporter is pointed to Copy details for the rest.
 */
object CrashIssueLink {

    private const val NEW_ISSUE_URL =
        "https://github.com/ahmmedrejowan/PdfReaderPro/issues/new"

    /** Stays well under the ~8,000 characters GitHub accepts in a URL. */
    internal const val MAX_URL_LENGTH = 7_000

    internal const val TRUNCATED_NOTE =
        "\n… (log cut to fit the link; tap Copy details on the crash screen and paste the full log here)"

    fun build(
        errorMessage: String,
        report: String,
        appVersion: String,
        androidVersion: String,
        device: String
    ): String {
        val title = "[Bug]: Crash: ${errorMessage.lineSequence().first().take(80)}"
        val fields = linkedMapOf(
            "template" to "bug_report.yml",
            "title" to title,
            "description" to "The app crashed and showed the error screen.\n\nError: ${errorMessage.take(500)}",
            "actual" to "The app crashed.",
            "version" to appVersion,
            "android-version" to androidVersion,
            "device" to device
        )
        val withoutLogs = buildUrl(fields)

        // Fit as much of the log as the URL allows, cutting whole lines.
        var logs = report
        var url = withoutLogs + "&logs=" + encode(logs)
        if (url.length <= MAX_URL_LENGTH) return url

        val lines = report.lines()
        var keep = lines.size
        while (keep > 0) {
            keep = (keep * 3) / 4
            logs = lines.take(keep).joinToString("\n") + TRUNCATED_NOTE
            url = withoutLogs + "&logs=" + encode(logs)
            if (url.length <= MAX_URL_LENGTH) return url
        }
        return withoutLogs + "&logs=" + encode(TRUNCATED_NOTE.trim())
    }

    private fun buildUrl(fields: Map<String, String>): String =
        NEW_ISSUE_URL + "?" + fields.entries.joinToString("&") { (key, value) ->
            "$key=${encode(value)}"
        }

    private fun encode(value: String): String = URLEncoder.encode(value, "UTF-8")
}
