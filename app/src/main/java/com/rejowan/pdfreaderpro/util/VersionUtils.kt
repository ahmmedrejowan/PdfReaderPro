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
            val newParts = parseVersion(newVersion)
            val currentParts = parseVersion(currentVersion)

            for (i in 0 until maxOf(newParts.size, currentParts.size)) {
                val newPart = newParts.getOrElse(i) { 0 }
                val currentPart = currentParts.getOrElse(i) { 0 }

                when {
                    newPart > currentPart -> return true
                    newPart < currentPart -> return false
                }
            }
            false
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
            .split("-")[0]
            .split(".")
            .mapNotNull { it.toIntOrNull() }
    }

    /**
     * Extracts version from APK filename.
     * Example: "app-release-2.0.0.apk" -> "2.0.0"
     */
    fun extractVersionFromFileName(fileName: String): String {
        return fileName
            .removeSuffix(".apk")
            .substringAfterLast("-")
            .ifEmpty { "unknown" }
    }

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
        return 0
    }
}
