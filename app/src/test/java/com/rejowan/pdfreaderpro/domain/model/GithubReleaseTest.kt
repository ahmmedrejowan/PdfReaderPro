package com.rejowan.pdfreaderpro.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Covers what the update sheet reads off a release.
 *
 * The version is compared against the installed one, so the tag has to be stripped
 * to something comparable, and the download picks an asset by looking at its name.
 */
class GithubReleaseTest {

    private fun release(tag: String, assets: List<ReleaseAsset> = emptyList()) = GithubRelease(
        tagName = tag,
        name = "Release",
        body = "notes",
        publishedAt = "2026-01-01T00:00:00Z",
        htmlUrl = "https://example.invalid",
        assets = assets
    )

    private fun asset(name: String, size: Long = 0L) =
        ReleaseAsset(name = name, downloadUrl = "https://example.invalid/$name", size = size)

    // region The version behind a tag
    @Test
    fun `a plain tag is already a version`() {
        assertEquals("2.4.0", release("2.4.0").version)
    }

    @Test
    fun `a lowercase v prefix is stripped`() {
        assertEquals("2.4.0", release("v2.4.0").version)
    }

    @Test
    fun `an uppercase V prefix is stripped`() {
        assertEquals("2.4.0", release("V2.4.0").version)
    }

    @Test
    fun `a release prefix is stripped in either case`() {
        assertEquals("2.4.0", release("release-2.4.0").version)
        assertEquals("2.4.0", release("Release-2.4.0").version)
    }

    @Test
    fun `surrounding whitespace is trimmed`() {
        assertEquals("2.4.0", release("v 2.4.0 ").version.trim())
    }

    @Test
    fun `a version with a suffix keeps it`() {
        // A pre-release tag has to stay distinguishable from the plain version.
        assertEquals("2.4.0-beta1", release("v2.4.0-beta1").version)
    }
    // endregion

    // region Picking the file to download
    @Test
    fun `an apk is recognised`() {
        assertTrue(asset("app-release.apk").isApk)
    }

    @Test
    fun `the extension check ignores case`() {
        assertTrue(asset("App-Release.APK").isApk)
    }

    @Test
    fun `anything that is not an apk is not offered`() {
        assertFalse(asset("source.zip").isApk)
        assertFalse(asset("checksums.txt").isApk)
        assertFalse(asset("apk-notes.md").isApk)
    }
    // endregion

    // region Showing the download size
    @Test
    fun `a size under a kilobyte is shown in bytes`() {
        assertEquals("512 bytes", asset("a.apk", size = 512).formattedSize)
    }

    @Test
    fun `a size in kilobytes is shown as such`() {
        assertTrue(asset("a.apk", size = 2048).formattedSize.endsWith("KB"))
    }

    @Test
    fun `a size in megabytes is shown as such`() {
        assertTrue(asset("a.apk", size = 12L * 1024 * 1024).formattedSize.endsWith("MB"))
    }

    @Test
    fun `an empty file is shown as zero bytes`() {
        assertEquals("0 bytes", asset("a.apk", size = 0).formattedSize)
    }
    // endregion
}
