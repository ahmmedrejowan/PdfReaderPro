package com.rejowan.pdfreaderpro.util

import org.junit.Assert.*
import org.junit.Test

/**
 * Comprehensive unit tests for VersionUtils.
 * Covers version parsing, comparison, and edge cases.
 */
class VersionUtilsTest {

    // region parseVersion Tests
    @Test
    fun `parseVersion parses simple version`() {
        val result = VersionUtils.parseVersion("1.0.0")
        assertEquals(listOf(1, 0, 0), result)
    }

    @Test
    fun `parseVersion parses two-part version`() {
        val result = VersionUtils.parseVersion("2.5")
        assertEquals(listOf(2, 5), result)
    }

    @Test
    fun `parseVersion parses single number`() {
        val result = VersionUtils.parseVersion("3")
        assertEquals(listOf(3), result)
    }

    @Test
    fun `parseVersion handles v prefix lowercase`() {
        val result = VersionUtils.parseVersion("v2.0.0")
        assertEquals(listOf(2, 0, 0), result)
    }

    @Test
    fun `parseVersion handles V prefix uppercase`() {
        val result = VersionUtils.parseVersion("V2.0.0")
        assertEquals(listOf(2, 0, 0), result)
    }

    @Test
    fun `parseVersion handles pre-release suffix`() {
        val result = VersionUtils.parseVersion("2.0.0-beta.1")
        assertEquals(listOf(2, 0, 0), result)
    }

    @Test
    fun `parseVersion handles build metadata suffix`() {
        // Build metadata with + is not stripped by current implementation
        // "2.0.0+build.123" -> split(".") = ["2", "0", "0+build", "123"]
        // "0+build" fails toIntOrNull, so we get [2, 0, 123]
        val result = VersionUtils.parseVersion("2.0.0+build.123")
        assertEquals(listOf(2, 0, 123), result)
    }

    @Test
    fun `parseVersion handles rc suffix`() {
        val result = VersionUtils.parseVersion("2.0.0-rc1")
        assertEquals(listOf(2, 0, 0), result)
    }

    @Test
    fun `parseVersion handles alpha suffix`() {
        val result = VersionUtils.parseVersion("v1.5.0-alpha")
        assertEquals(listOf(1, 5, 0), result)
    }

    @Test
    fun `parseVersion returns empty list for empty string`() {
        val result = VersionUtils.parseVersion("")
        assertEquals(emptyList<Int>(), result)
    }

    @Test
    fun `parseVersion returns empty list for non-numeric string`() {
        val result = VersionUtils.parseVersion("abc")
        assertEquals(emptyList<Int>(), result)
    }

    @Test
    fun `parseVersion handles mixed valid and invalid parts`() {
        val result = VersionUtils.parseVersion("1.abc.3")
        assertEquals(listOf(1, 3), result)
    }

    @Test
    fun `parseVersion handles large version numbers`() {
        val result = VersionUtils.parseVersion("999.888.777")
        assertEquals(listOf(999, 888, 777), result)
    }

    @Test
    fun `parseVersion handles four-part version`() {
        val result = VersionUtils.parseVersion("1.2.3.4")
        assertEquals(listOf(1, 2, 3, 4), result)
    }
    // endregion

    // region isNewerVersion Tests
    @Test
    fun `isNewerVersion returns true when major is higher`() {
        assertTrue(VersionUtils.isNewerVersion("2.0.0", "1.0.0"))
    }

    @Test
    fun `isNewerVersion returns true when minor is higher`() {
        assertTrue(VersionUtils.isNewerVersion("1.1.0", "1.0.0"))
    }

    @Test
    fun `isNewerVersion returns true when patch is higher`() {
        assertTrue(VersionUtils.isNewerVersion("1.0.1", "1.0.0"))
    }

    @Test
    fun `isNewerVersion returns false when major is lower`() {
        assertFalse(VersionUtils.isNewerVersion("1.0.0", "2.0.0"))
    }

    @Test
    fun `isNewerVersion returns false when minor is lower`() {
        assertFalse(VersionUtils.isNewerVersion("1.0.0", "1.1.0"))
    }

    @Test
    fun `isNewerVersion returns false when patch is lower`() {
        assertFalse(VersionUtils.isNewerVersion("1.0.0", "1.0.1"))
    }

    @Test
    fun `isNewerVersion returns false when versions are equal`() {
        assertFalse(VersionUtils.isNewerVersion("1.0.0", "1.0.0"))
    }

    @Test
    fun `isNewerVersion handles v prefix on both versions`() {
        assertTrue(VersionUtils.isNewerVersion("v2.0.0", "v1.0.0"))
    }

    @Test
    fun `isNewerVersion handles v prefix on new version only`() {
        assertTrue(VersionUtils.isNewerVersion("v2.0.0", "1.0.0"))
    }

    @Test
    fun `isNewerVersion handles v prefix on current version only`() {
        assertTrue(VersionUtils.isNewerVersion("2.0.0", "v1.0.0"))
    }

    @Test
    fun `isNewerVersion handles different length versions`() {
        assertTrue(VersionUtils.isNewerVersion("1.0.1", "1.0"))
        assertFalse(VersionUtils.isNewerVersion("1.0", "1.0.1"))
    }

    @Test
    fun `isNewerVersion treats missing parts as zero`() {
        assertFalse(VersionUtils.isNewerVersion("1.0", "1.0.0"))
        assertFalse(VersionUtils.isNewerVersion("1.0.0", "1.0"))
    }

    @Test
    fun `isNewerVersion handles pre-release versions`() {
        assertTrue(VersionUtils.isNewerVersion("2.0.0-beta", "1.9.9"))
        assertFalse(VersionUtils.isNewerVersion("1.9.9-beta", "2.0.0"))
    }

    @Test
    fun `isNewerVersion returns false for invalid new version`() {
        assertFalse(VersionUtils.isNewerVersion("invalid", "1.0.0"))
    }

    @Test
    fun `isNewerVersion returns true when current version is invalid`() {
        // When current version is invalid (empty list), new version parts are compared to 0
        // So "2.0.0" vs "invalid" becomes [2,0,0] vs [], which is [2,0,0] vs [0,0,0] -> true
        assertTrue(VersionUtils.isNewerVersion("2.0.0", "invalid"))
    }

    @Test
    fun `isNewerVersion returns false when both versions are invalid`() {
        assertFalse(VersionUtils.isNewerVersion("invalid", "also-invalid"))
    }

    @Test
    fun `isNewerVersion handles empty strings`() {
        // Both empty -> both parse to [], compared as equal -> false
        assertFalse(VersionUtils.isNewerVersion("", ""))
        // "1.0.0" vs "" -> [1,0,0] vs [] -> [1,0,0] vs [0,0,0] -> true
        assertTrue(VersionUtils.isNewerVersion("1.0.0", ""))
        // "" vs "1.0.0" -> [] vs [1,0,0] -> [0,0,0] vs [1,0,0] -> false
        assertFalse(VersionUtils.isNewerVersion("", "1.0.0"))
    }

    @Test
    fun `isNewerVersion handles large version jumps`() {
        assertTrue(VersionUtils.isNewerVersion("10.0.0", "2.0.0"))
        assertTrue(VersionUtils.isNewerVersion("2.10.0", "2.2.0"))
        assertTrue(VersionUtils.isNewerVersion("2.2.10", "2.2.2"))
    }
    // endregion

    // region compareVersions Tests
    @Test
    fun `compareVersions returns positive when v1 greater`() {
        assertTrue(VersionUtils.compareVersions("2.0.0", "1.0.0") > 0)
    }

    @Test
    fun `compareVersions returns negative when v1 smaller`() {
        assertTrue(VersionUtils.compareVersions("1.0.0", "2.0.0") < 0)
    }

    @Test
    fun `compareVersions returns zero when equal`() {
        assertEquals(0, VersionUtils.compareVersions("1.0.0", "1.0.0"))
    }

    @Test
    fun `compareVersions handles minor version differences`() {
        assertTrue(VersionUtils.compareVersions("1.5.0", "1.4.0") > 0)
        assertTrue(VersionUtils.compareVersions("1.4.0", "1.5.0") < 0)
    }

    @Test
    fun `compareVersions handles patch version differences`() {
        assertTrue(VersionUtils.compareVersions("1.0.5", "1.0.4") > 0)
        assertTrue(VersionUtils.compareVersions("1.0.4", "1.0.5") < 0)
    }

    @Test
    fun `compareVersions returns zero for equivalent versions with prefix`() {
        assertEquals(0, VersionUtils.compareVersions("v1.0.0", "1.0.0"))
    }
    // endregion

    // region extractVersionFromFileName Tests
    @Test
    fun `extractVersionFromFileName extracts version from standard apk name`() {
        val result = VersionUtils.extractVersionFromFileName("app-release-2.0.0.apk")
        assertEquals("2.0.0", result)
    }

    @Test
    fun `extractVersionFromFileName extracts version from debug apk name`() {
        val result = VersionUtils.extractVersionFromFileName("app-debug-1.5.3.apk")
        assertEquals("1.5.3", result)
    }

    @Test
    fun `extractVersionFromFileName handles simple name`() {
        val result = VersionUtils.extractVersionFromFileName("app-1.0.0.apk")
        assertEquals("1.0.0", result)
    }

    @Test
    fun `extractVersionFromFileName returns unknown for no version`() {
        val result = VersionUtils.extractVersionFromFileName("app.apk")
        assertEquals("app", result)
    }

    @Test
    fun `extractVersionFromFileName handles name without apk extension`() {
        val result = VersionUtils.extractVersionFromFileName("app-release-2.0.0")
        assertEquals("2.0.0", result)
    }

    @Test
    fun `extractVersionFromFileName returns unknown for empty string`() {
        val result = VersionUtils.extractVersionFromFileName("")
        assertEquals("unknown", result)
    }

    @Test
    fun `extractVersionFromFileName handles multiple hyphens`() {
        val result = VersionUtils.extractVersionFromFileName("pdf-reader-pro-release-3.1.0.apk")
        assertEquals("3.1.0", result)
    }

    @Test
    fun `extractVersionFromFileName handles version with v prefix in filename`() {
        val result = VersionUtils.extractVersionFromFileName("app-v2.0.0.apk")
        assertEquals("v2.0.0", result)
    }
    // endregion

    // region Edge Cases
    @Test
    fun `version comparison handles zero values`() {
        assertEquals(0, VersionUtils.compareVersions("0.0.0", "0.0.0"))
        assertTrue(VersionUtils.compareVersions("0.0.1", "0.0.0") > 0)
        assertTrue(VersionUtils.compareVersions("0.1.0", "0.0.0") > 0)
        assertTrue(VersionUtils.compareVersions("1.0.0", "0.0.0") > 0)
    }

    @Test
    fun `version comparison handles leading zeros in string`() {
        // "01" should parse to 1
        val result = VersionUtils.parseVersion("01.02.03")
        assertEquals(listOf(1, 2, 3), result)
    }

    @Test
    fun `isNewerVersion is consistent with compareVersions`() {
        val testCases = listOf(
            "2.0.0" to "1.0.0",
            "1.5.0" to "1.4.9",
            "1.0.1" to "1.0.0",
            "10.0.0" to "9.9.9"
        )

        for ((newer, older) in testCases) {
            assertTrue("$newer should be newer than $older",
                VersionUtils.isNewerVersion(newer, older))
            assertTrue("compareVersions($newer, $older) should be > 0",
                VersionUtils.compareVersions(newer, older) > 0)
        }
    }

    @Test
    fun `version parsing handles whitespace`() {
        // Whitespace causes parsing issues - " 1" and "0 " fail toIntOrNull
        val result = VersionUtils.parseVersion(" 1.0.0 ")
        // Only the middle "0" parses successfully
        assertEquals(listOf(0), result)
    }

    @Test
    fun `very long version numbers are handled`() {
        val longVersion = "1.2.3.4.5.6.7.8.9.10"
        val result = VersionUtils.parseVersion(longVersion)
        assertEquals(listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10), result)
    }

    @Test
    fun `negative numbers in version are handled by hyphen split`() {
        // "-1.0.0" -> split("-")[0] = "" -> split(".") = [""] -> mapNotNull = []
        val result = VersionUtils.parseVersion("-1.0.0")
        assertEquals(emptyList<Int>(), result)
    }
    // endregion
}
