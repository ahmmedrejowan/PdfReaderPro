package com.rejowan.pdfreaderpro.util

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Environment
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.content.getSystemService
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.isActive
import timber.log.Timber
import java.io.File

/**
 * Manages APK downloads with progress tracking and installation.
 */
class ApkDownloadManager(private val context: Context) {

    companion object {
        private const val TAG = "ApkDownloader"
        private const val UPDATES_DIR = "updates"
        private const val VERSION_FILE = "pending_version.txt"
        private const val PROGRESS_POLL_INTERVAL = 500L
    }

    private val downloadManager: DownloadManager? = context.getSystemService()

    /**
     * Represents the current state of an APK download.
     */
    sealed class DownloadState {
        data object Idle : DownloadState()
        data object Starting : DownloadState()
        data class Downloading(
            val progress: Int,
            val downloadedBytes: Long,
            val totalBytes: Long
        ) : DownloadState()
        data class Completed(val file: File) : DownloadState()
        data class Failed(val reason: String) : DownloadState()
        data object Cancelled : DownloadState()
    }

    /**
     * Downloads an APK from the given URL and emits progress updates.
     *
     * @param url The download URL for the APK
     * @param fileName The name to save the file as
     * @param version The version string to save for later reference
     * @return A Flow emitting DownloadState updates
     */
    fun downloadApk(url: String, fileName: String, version: String? = null): Flow<DownloadState> = callbackFlow {
        // Save version for later reference
        version?.let { savePendingVersion(it) }
        Timber.tag(TAG).d("=== DOWNLOAD START ===")
        Timber.tag(TAG).d("URL: $url")
        Timber.tag(TAG).d("File: $fileName")

        if (downloadManager == null) {
            Timber.tag(TAG).e("DownloadManager not available")
            trySend(DownloadState.Failed("Download manager not available"))
            close()
            return@callbackFlow
        }

        trySend(DownloadState.Starting)

        // Prepare download directory
        val updatesDir = File(context.getExternalFilesDir(null), UPDATES_DIR)
        if (!updatesDir.exists()) {
            updatesDir.mkdirs()
        }

        // Delete old APK if exists
        val targetFile = File(updatesDir, fileName)
        if (targetFile.exists()) {
            targetFile.delete()
            Timber.tag(TAG).d("Deleted existing file: ${targetFile.absolutePath}")
        }

        // Create download request
        val request = DownloadManager.Request(Uri.parse(url)).apply {
            setTitle("Downloading Update")
            setDescription("PDF Reader Pro v${VersionUtils.extractVersionFromFileName(fileName)}")
            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
            setDestinationInExternalFilesDir(context, null, "$UPDATES_DIR/$fileName")
            setAllowedOverMetered(true)
            setAllowedOverRoaming(true)
        }

        // Start download
        val downloadId = downloadManager.enqueue(request)
        Timber.tag(TAG).d("Download started with ID: $downloadId")

        // Register completion receiver
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                if (id == downloadId) {
                    Timber.tag(TAG).d("Download broadcast received for ID: $id")
                    // Query final status
                    val query = DownloadManager.Query().setFilterById(downloadId)
                    downloadManager.query(query)?.use { cursor ->
                        if (cursor.moveToFirst()) {
                            val statusIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
                            val status = cursor.getInt(statusIndex)

                            when (status) {
                                DownloadManager.STATUS_SUCCESSFUL -> {
                                    // Get actual file path from DownloadManager
                                    val localUriIndex = cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI)
                                    val localUri = cursor.getString(localUriIndex)
                                    val actualFile = localUri?.let { uri ->
                                        Uri.parse(uri).path?.let { File(it) }
                                    } ?: targetFile

                                    Timber.tag(TAG).d("=== DOWNLOAD COMPLETE ===")
                                    Timber.tag(TAG).d("File: ${actualFile.absolutePath}")
                                    Timber.tag(TAG).d("Exists: ${actualFile.exists()}")
                                    Timber.tag(TAG).d("Size: ${actualFile.length()} bytes")
                                    trySend(DownloadState.Completed(actualFile))
                                }
                                DownloadManager.STATUS_FAILED -> {
                                    val reasonIndex = cursor.getColumnIndex(DownloadManager.COLUMN_REASON)
                                    val reason = cursor.getInt(reasonIndex)
                                    val errorMsg = getFailureReason(reason)
                                    Timber.tag(TAG).e("Download failed: $errorMsg")
                                    trySend(DownloadState.Failed(errorMsg))
                                }
                            }
                        }
                    }
                    close()
                }
            }
        }

        ContextCompat.registerReceiver(
            context,
            receiver,
            IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )

        // Poll for progress
        while (isActive) {
            val query = DownloadManager.Query().setFilterById(downloadId)
            downloadManager.query(query)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val statusIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
                    val bytesDownloadedIndex = cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)
                    val bytesTotalIndex = cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)

                    val status = cursor.getInt(statusIndex)
                    val bytesDownloaded = cursor.getLong(bytesDownloadedIndex)
                    val bytesTotal = cursor.getLong(bytesTotalIndex)

                    when (status) {
                        DownloadManager.STATUS_RUNNING -> {
                            val progress = if (bytesTotal > 0) {
                                ((bytesDownloaded * 100) / bytesTotal).toInt()
                            } else 0
                            Timber.tag(TAG).v("Progress: $progress% ($bytesDownloaded / $bytesTotal)")
                            trySend(DownloadState.Downloading(progress, bytesDownloaded, bytesTotal))
                        }
                        DownloadManager.STATUS_SUCCESSFUL -> {
                            // Get actual file path from DownloadManager
                            val localUriIndex = cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI)
                            val localUri = cursor.getString(localUriIndex)
                            val actualFile = localUri?.let { uri ->
                                Uri.parse(uri).path?.let { File(it) }
                            } ?: targetFile

                            Timber.tag(TAG).d("=== DOWNLOAD COMPLETE (polled) ===")
                            Timber.tag(TAG).d("File: ${actualFile.absolutePath}")
                            Timber.tag(TAG).d("Exists: ${actualFile.exists()}")
                            Timber.tag(TAG).d("Size: ${actualFile.length()} bytes")
                            trySend(DownloadState.Completed(actualFile))
                            close()
                            return@use
                        }
                        DownloadManager.STATUS_FAILED -> {
                            val reasonIndex = cursor.getColumnIndex(DownloadManager.COLUMN_REASON)
                            val reason = cursor.getInt(reasonIndex)
                            val errorMsg = getFailureReason(reason)
                            Timber.tag(TAG).e("Download failed (polled): $errorMsg")
                            trySend(DownloadState.Failed(errorMsg))
                            close()
                            return@use
                        }
                        DownloadManager.STATUS_PAUSED -> {
                            Timber.tag(TAG).d("Download paused")
                        }
                        DownloadManager.STATUS_PENDING -> {
                            Timber.tag(TAG).d("Download pending")
                        }
                    }
                }
            }
            delay(PROGRESS_POLL_INTERVAL)
        }

        awaitClose {
            Timber.tag(TAG).d("Download flow closed")
            try {
                context.unregisterReceiver(receiver)
            } catch (e: Exception) {
                // Receiver may already be unregistered
            }
        }
    }

    /**
     * Cancels an ongoing download.
     */
    fun cancelDownload(downloadId: Long) {
        downloadManager?.remove(downloadId)
        Timber.tag(TAG).d("Download cancelled: $downloadId")
    }

    /**
     * Prompts the user to install the downloaded APK.
     */
    fun installApk(file: File): Boolean {
        Timber.tag(TAG).d("=== INSTALL APK ===")
        Timber.tag(TAG).d("File: ${file.absolutePath}")
        Timber.tag(TAG).d("Exists: ${file.exists()}")
        Timber.tag(TAG).d("Size: ${file.length()} bytes")

        if (!file.exists()) {
            Timber.tag(TAG).e("APK file does not exist")
            return false
        }

        return try {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                file
            )
            Timber.tag(TAG).d("URI: $uri")

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/vnd.android.package-archive")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
            }

            context.startActivity(intent)
            Timber.tag(TAG).d("Install intent launched")
            true
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Failed to launch install intent")
            false
        }
    }

    /**
     * Checks if the app has permission to install APKs from unknown sources.
     */
    fun canInstallApks(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.packageManager.canRequestPackageInstalls()
        } else {
            true
        }
    }

    /**
     * Returns an intent to open the install unknown apps settings.
     */
    fun getInstallPermissionIntent(): Intent? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Intent(
                android.provider.Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                Uri.parse("package:${context.packageName}")
            ).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
        } else {
            null
        }
    }

    /**
     * Cleans up old downloaded APKs and version file.
     */
    fun cleanupOldDownloads() {
        val updatesDir = File(context.getExternalFilesDir(null), UPDATES_DIR)
        if (updatesDir.exists()) {
            updatesDir.listFiles()?.forEach { file ->
                if (file.name.endsWith(".apk")) {
                    file.delete()
                    Timber.tag(TAG).d("Cleaned up: ${file.name}")
                }
            }
        }
        clearPendingVersion()
    }

    /**
     * Gets the pending APK file if one exists and is valid.
     * Returns null if no APK is ready for install.
     */
    fun getPendingApk(): File? {
        val updatesDir = File(context.getExternalFilesDir(null), UPDATES_DIR)
        if (!updatesDir.exists()) return null

        val apkFiles = updatesDir.listFiles()?.filter {
            it.name.endsWith(".apk") && it.exists() && it.length() > 0
        }

        return apkFiles?.maxByOrNull { it.lastModified() }?.also {
            Timber.tag(TAG).d("Found pending APK: ${it.name} (${it.length()} bytes)")
        }
    }

    /**
     * Checks if there's a pending APK ready to install.
     * Returns false if the pending APK version is <= current app version.
     */
    fun hasPendingApk(currentAppVersion: String): Boolean {
        val pendingVersion = getPendingApkVersion() ?: return false

        // If pending version <= current version, it's already installed
        if (!VersionUtils.isNewerVersion(pendingVersion, currentAppVersion)) {
            Timber.tag(TAG).d("Pending APK v$pendingVersion is not newer than current v$currentAppVersion - cleaning up")
            cleanupOldDownloads()
            return false
        }
        return true
    }

    /**
     * Gets version of the pending APK (from saved version file).
     */
    fun getPendingApkVersion(): String? {
        val updatesDir = File(context.getExternalFilesDir(null), UPDATES_DIR)
        val versionFile = File(updatesDir, VERSION_FILE)
        return if (versionFile.exists()) {
            versionFile.readText().trim().also {
                Timber.tag(TAG).d("Read pending version: $it")
            }
        } else {
            // Fallback to extracting from filename
            getPendingApk()?.name?.let { VersionUtils.extractVersionFromFileName(it) }
        }
    }

    /**
     * Saves the pending version to a file.
     */
    private fun savePendingVersion(version: String) {
        val updatesDir = File(context.getExternalFilesDir(null), UPDATES_DIR)
        if (!updatesDir.exists()) updatesDir.mkdirs()
        val versionFile = File(updatesDir, VERSION_FILE)
        versionFile.writeText(version)
        Timber.tag(TAG).d("Saved pending version: $version")
    }

    /**
     * Clears the saved pending version.
     */
    private fun clearPendingVersion() {
        val updatesDir = File(context.getExternalFilesDir(null), UPDATES_DIR)
        val versionFile = File(updatesDir, VERSION_FILE)
        if (versionFile.exists()) {
            versionFile.delete()
            Timber.tag(TAG).d("Cleared pending version file")
        }
    }

    private fun getFailureReason(reason: Int): String {
        return when (reason) {
            DownloadManager.ERROR_CANNOT_RESUME -> "Cannot resume download"
            DownloadManager.ERROR_DEVICE_NOT_FOUND -> "Storage not found"
            DownloadManager.ERROR_FILE_ALREADY_EXISTS -> "File already exists"
            DownloadManager.ERROR_FILE_ERROR -> "Storage error"
            DownloadManager.ERROR_HTTP_DATA_ERROR -> "Network data error"
            DownloadManager.ERROR_INSUFFICIENT_SPACE -> "Insufficient storage space"
            DownloadManager.ERROR_TOO_MANY_REDIRECTS -> "Too many redirects"
            DownloadManager.ERROR_UNHANDLED_HTTP_CODE -> "HTTP error"
            DownloadManager.ERROR_UNKNOWN -> "Unknown error"
            else -> "Download failed (code: $reason)"
        }
    }
}
