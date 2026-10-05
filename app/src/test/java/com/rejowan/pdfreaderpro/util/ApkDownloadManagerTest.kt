package com.rejowan.pdfreaderpro.util

import android.app.DownloadManager
import android.content.Context
import android.database.Cursor
import android.net.Uri
import androidx.core.content.ContextCompat
import app.cash.turbine.test
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
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
@OptIn(ExperimentalCoroutinesApi::class)
class ApkDownloadManagerTest {

    @get:Rule
    val folder = TemporaryFolder()

    private lateinit var context: Context
    private lateinit var externalFiles: File
    private lateinit var manager: ApkDownloadManager
    private lateinit var downloads: DownloadManager

    @Before
    fun setUp() {
        externalFiles = folder.newFolder("external")
        context = mockk(relaxed = true)
        every { context.getExternalFilesDir(any()) } returns externalFiles
        // Looked up in the constructor; a relaxed mock hands back a plain object.
        downloads = mockk(relaxed = true)
        every { context.getSystemService(DownloadManager::class.java) } returns downloads
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

    // region Running a download
    /**
     * A download the system reports the given way.
     *
     * The whole flow is driven by polling a cursor, so the cursor is what decides
     * what the user sees. Column indices are the identity here, which is why each
     * one is answered by name rather than by position.
     */
    private fun systemReports(
        statuses: List<Int>,
        downloaded: Long = 512,
        total: Long = 1024,
        reason: Int = 0,
        localUri: String? = null
    ) {
        val cursor = mockk<Cursor>(relaxed = true)
        every { cursor.moveToFirst() } returns true
        every { cursor.getColumnIndex(DownloadManager.COLUMN_STATUS) } returns 0
        every { cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR) } returns 1
        every { cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES) } returns 2
        every { cursor.getColumnIndex(DownloadManager.COLUMN_REASON) } returns 3
        every { cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI) } returns 4
        every { cursor.getInt(0) } returnsMany statuses andThen statuses.last()
        every { cursor.getLong(1) } returns downloaded
        every { cursor.getLong(2) } returns total
        every { cursor.getInt(3) } returns reason
        every { cursor.getString(4) } returns localUri
        every { downloads.query(any()) } returns cursor
        every { downloads.enqueue(any()) } returns 42L

        mockkStatic(Uri::class)
        every { Uri.parse(any()) } returns mockk(relaxed = true)
        mockkStatic(ContextCompat::class)
        every { ContextCompat.registerReceiver(any(), any(), any(), any()) } returns null
    }

    @After
    fun releaseStatics() {
        unmockkStatic(Uri::class)
        unmockkStatic(ContextCompat::class)
    }

    private fun download() = manager.downloadApk(
        url = "https://example.invalid/app-2.5.0.apk",
        fileName = "app-2.5.0.apk",
        version = "2.5.0"
    )

    @Test
    fun `a download reports it is starting before anything else`() = runTest {
        systemReports(listOf(DownloadManager.STATUS_SUCCESSFUL))

        download().test {
            assertEquals(ApkDownloadManager.DownloadState.Starting, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `the version is recorded before the download starts`() = runTest {
        // The record is what identifies the download afterwards, so it has to
        // survive the app being killed mid-download.
        systemReports(listOf(DownloadManager.STATUS_SUCCESSFUL))

        download().test { cancelAndIgnoreRemainingEvents() }

        assertEquals("2.5.0", manager.getPendingApkVersion())
    }

    @Test
    fun `progress is reported as a percentage of the total`() = runTest {
        systemReports(listOf(DownloadManager.STATUS_RUNNING), downloaded = 512, total = 1024)

        download().test {
            assertEquals(ApkDownloadManager.DownloadState.Starting, awaitItem())
            val downloading = awaitItem() as ApkDownloadManager.DownloadState.Downloading
            assertEquals(50, downloading.progress)
            assertEquals(512L, downloading.downloadedBytes)
            assertEquals(1024L, downloading.totalBytes)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `a download of unknown size reports no progress rather than dividing by zero`() = runTest {
        systemReports(listOf(DownloadManager.STATUS_RUNNING), downloaded = 512, total = 0)

        download().test {
            awaitItem()
            assertEquals(0, (awaitItem() as ApkDownloadManager.DownloadState.Downloading).progress)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `a finished download reports the file it left behind`() = runTest {
        systemReports(listOf(DownloadManager.STATUS_SUCCESSFUL))

        download().test {
            assertEquals(ApkDownloadManager.DownloadState.Starting, awaitItem())
            val completed = awaitItem() as ApkDownloadManager.DownloadState.Completed
            assertEquals("app-2.5.0.apk", completed.file.name)
            awaitComplete()
        }
    }

    @Test
    fun `a download that runs then finishes reports both`() = runTest {
        systemReports(listOf(DownloadManager.STATUS_RUNNING, DownloadManager.STATUS_SUCCESSFUL))

        download().test {
            assertEquals(ApkDownloadManager.DownloadState.Starting, awaitItem())
            assertTrue(awaitItem() is ApkDownloadManager.DownloadState.Downloading)
            assertTrue(awaitItem() is ApkDownloadManager.DownloadState.Completed)
            awaitComplete()
        }
    }

    @Test
    fun `a failure is reported in words the user can act on`() = runTest {
        systemReports(
            listOf(DownloadManager.STATUS_FAILED),
            reason = DownloadManager.ERROR_INSUFFICIENT_SPACE
        )

        download().test {
            awaitItem()
            val failed = awaitItem() as ApkDownloadManager.DownloadState.Failed
            assertEquals("Insufficient storage space", failed.reason)
            awaitComplete()
        }
    }

    @Test
    fun `a failure nobody has a name for still says something`() = runTest {
        systemReports(listOf(DownloadManager.STATUS_FAILED), reason = 9999)

        download().test {
            awaitItem()
            val failed = awaitItem() as ApkDownloadManager.DownloadState.Failed
            assertTrue(failed.reason.contains("9999"))
            awaitComplete()
        }
    }

    @Test
    fun `a paused or pending download reports nothing until it moves`() = runTest {
        systemReports(
            listOf(
                DownloadManager.STATUS_PENDING,
                DownloadManager.STATUS_PAUSED,
                DownloadManager.STATUS_SUCCESSFUL
            )
        )

        download().test {
            assertEquals(ApkDownloadManager.DownloadState.Starting, awaitItem())
            assertTrue(awaitItem() is ApkDownloadManager.DownloadState.Completed)
            awaitComplete()
        }
    }

    @Test
    fun `with no download manager the download fails rather than hanging`() = runTest {
        every { context.getSystemService(DownloadManager::class.java) } returns null
        val without = ApkDownloadManager(context)

        without.downloadApk("https://example.invalid/app.apk", "app.apk").test {
            assertTrue(awaitItem() is ApkDownloadManager.DownloadState.Failed)
            awaitComplete()
        }
    }

    @Test
    fun `cancelling a download tells the system to drop it`() {
        manager.cancelDownload(42L)

        io.mockk.verify { downloads.remove(42L) }
    }
    // endregion
}
