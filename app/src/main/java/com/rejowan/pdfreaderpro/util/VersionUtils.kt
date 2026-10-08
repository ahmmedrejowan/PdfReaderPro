package com.rejowan.pdfreaderpro.util

/**
 * Utility functions for version string parsing and comparison.
 */
object VersionUtils {

    /**
     * Compares two semantic version strings.
     * @return true if [newVersion] is greater than [currentVersion]
     */
    fun isNewerVersion(newVersion: String, currentVersion: String): Boolean {
        return try {
            compareVersions(newVersion, currentVersion) > 0
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Parses a version string into a list of integers.
     * Handles "v" prefix and pre-release suffixes (e.g., "2.0.0-beta.1").
     */
    fun parseVersion(version: String): List<Int> {
        return version
            .removePrefix("v")
            .removePrefix("V")
            .substringBefore("+")
            .split("-")[0]
            .split(".")
            .mapNotNull { it.toIntOrNull() }
    }

    /**
     * Extracts version from APK filename.
     * Example: "app-release-2.0.0.apk" -> "2.0.0"
     */
    fun extractVersionFromFileName(fileName: String): String {
        val name = fileName.removeSuffix(".apk")
        // "PdfReaderPro-v2.5.0-beta.1" keeps its pre-release part.
        FILE_VERSION.find(name)?.let { return it.groupValues[1] }
        return name.substringAfterLast("-").ifEmpty { "unknown" }
    }

    private val FILE_VERSION = Regex("""(v?\d+(?:\.\d+)+(?:-[0-9A-Za-z.]+)?)$""")

    /**
     * Compares two versions and returns:
     * - positive if v1 > v2
     * - negative if v1 < v2
     * - zero if v1 == v2
     */
    fun compareVersions(v1: String, v2: String): Int {
        val parts1 = parseVersion(v1)
        val parts2 = parseVersion(v2)

        for (i in 0 until maxOf(parts1.size, parts2.size)) {
            val p1 = parts1.getOrElse(i) { 0 }
            val p2 = parts2.getOrElse(i) { 0 }

            when {
                p1 > p2 -> return 1
                p1 < p2 -> return -1
            }
        }
        // Only real versions have a pre-release part; "also-invalid" is not one.
        if (parts1.isEmpty() || parts2.isEmpty()) return 0
        return comparePreRelease(preReleaseOf(v1), preReleaseOf(v2))
    }

    /** The part after "-" in "2.5.0-beta.1", without any "+build" metadata; null for a full release. */
    private fun preReleaseOf(version: String): String? =
        version.substringBefore('+').substringAfter('-', missingDelimiterValue = "").ifEmpty { null }

    /**
     * Semantic versioning's order for the same numbers: a pre-release comes before
     * the full release, so someone on 2.5.0-beta.1 is offered 2.5.0. Two
     * pre-releases compare part by part, numbers numerically: beta.2 > beta.1,
     * rc > beta.
     */
    private fun comparePreRelease(a: String?, b: String?): Int {
        if (a == null || b == null) {
            return when {
                a == null && b == null -> 0
                a == null -> 1
                else -> -1
            }
        }
        val partsA = a.split('.')
        val partsB = b.split('.')
        for (i in 0 until minOf(partsA.size, partsB.size)) {
            val x = partsA[i]
            val y = partsB[i]
            val nx = x.toIntOrNull()
            val ny = y.toIntOrNull()
            val result = when {
                nx != null && ny != null -> nx.compareTo(ny)
                nx != null -> -1
                ny != null -> 1
                else -> x.compareTo(y)
            }
            if (result != 0) return result.coerceIn(-1, 1)
        }
        return partsA.size.compareTo(partsB.size).coerceIn(-1, 1)
    }
}
