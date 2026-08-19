package com.rejowan.pdfreaderpro.util

import android.app.DownloadManager
import android.content.Context
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

/**
 * Covers what the app knows about an update it already downloaded.
 *
 * The download itself runs through the platform DownloadManager and a broadcast
 * receiver, neither of which exists here, so this covers the part either side of
 * it: deciding whether a downloaded APK is still worth installing, and clearing
 * one out once it is not.
 *
 * Getting that wrong is user-visible in an annoying way. A stale APK left behind
 * after the update is installed would keep offering to install the version the
 * user is already running.
 */
class ApkDownloadManagerTest {

    @get:Rule
    val folder = TemporaryFolder()

    private lateinit var context: Context
    private lateinit var externalFiles: File
    private lateinit var manager: ApkDownloadManager

    @Before
    fun setUp() {
        externalFiles = folder.newFolder("external")
        context = mockk(relaxed = true)
        every { context.getExternalFilesDir(any()) } returns externalFiles
        // Looked up in the constructor; a relaxed mock hands back a plain object.
        every {
            context.getSystemService(DownloadManager::class.java)
        } returns mockk<DownloadManager>(relaxed = true)
        manager = ApkDownloadManager(context)
    }

    private fun updatesDir() = File(externalFiles, "updates").apply { mkdirs() }

    private fun downloadedApk(name: String = "app-2.5.0.apk", content: String = "apk bytes") =
        File(updatesDir(), name).apply { writeText(content) }

    private fun recordedVersion(version: String) =
        File(updatesDir(), "pending_version.txt").apply { writeText(version) }

    // region Finding a downloaded update
    @Test
    fun `nothing downloaded means nothing pending`() {
        assertNull(manager.getPendingApk())
        assertNull(manager.getPendingApkVersion())
    }

    @Test
    fun `a downloaded apk is found`() {
        val apk = downloadedApk()

        assertEquals(apk.absolutePath, manager.getPendingApk()?.absolutePath)
    }

    @Test
    fun `an empty file is not offered as an update`() {
        // A download that failed part way through leaves a file behind, and
        // installing it would fail in front of the user.
        File(updatesDir(), "app-2.5.0.apk").createNewFile()

        assertNull(manager.getPendingApk())
    }

    @Test
    fun `files that are not apks are ignored`() {
        File(updatesDir(), "notes.txt").writeText("not an update")

        assertNull(manager.getPendingApk())
    }

    @Test
    fun `the newest download is the one offered`() {
        val older = downloadedApk("app-2.4.0.apk").apply { setLastModified(1_000L) }
        val newer = downloadedApk("app-2.5.0.apk").apply { setLastModified(9_000L) }

        assertEquals(newer.absolutePath, manager.getPendingApk()?.absolutePath)
        assertTrue(older.exists())
    }
    // endregion

    // region Which version is waiting
    @Test
    fun `the recorded version is used when there is one`() {
        downloadedApk("app-whatever.apk")
        recordedVersion("2.5.0")

        assertEquals("2.5.0", manager.getPendingApkVersion())
    }

    @Test
    fun `surrounding whitespace in the record is trimmed`() {
        recordedVersion(" 2.5.0\n")

        assertEquals("2.5.0", manager.getPendingApkVersion())
    }

    @Test
    fun `with no record, the version comes from the file name`() {
        // Downloads from an earlier version of the app have no record alongside
        // them, and would otherwise be invisible.
        downloadedApk("PdfReaderPro-2.5.0.apk")

        assertEquals("2.5.0", manager.getPendingApkVersion())
    }
    // endregion

    // region Deciding whether to offer it
    @Test
    fun `an update newer than the running app is offered`() {
        downloadedApk()
        recordedVersion("2.5.0")

        assertTrue(manager.hasPendingApk(currentAppVersion = "2.4.0"))
    }

    @Test
    fun `an update matching the running app is not offered, and is cleared out`() {
        val apk = downloadedApk()
        recordedVersion("2.4.0")

        assertFalse(manager.hasPendingApk(currentAppVersion = "2.4.0"))
        assertFalse(apk.exists())
        assertNull(manager.getPendingApkVersion())
    }

    @Test
    fun `an update older than the running app is not offered`() {
        downloadedApk()
        recordedVersion("2.3.0")

        assertFalse(manager.hasPendingApk(currentAppVersion = "2.4.0"))
    }

    @Test
    fun `with nothing downloaded there is nothing to offer`() {
        assertFalse(manager.hasPendingApk(currentAppVersion = "2.4.0"))
    }
    // endregion

    // region Clearing out
    @Test
    fun `clearing removes the download and its record`() {
        val apk = downloadedApk()
        val record = recordedVersion("2.5.0")

        manager.cleanupOldDownloads()

        assertFalse(apk.exists())
        assertFalse(record.exists())
    }

    @Test
    fun `clearing leaves anything that is not an update alone`() {
        val other = File(updatesDir(), "notes.txt").apply { writeText("keep me") }

        manager.cleanupOldDownloads()

        assertTrue(other.exists())
    }

    @Test
    fun `clearing with nothing downloaded does not throw`() {
        manager.cleanupOldDownloads()
    }
    // endregion
}
